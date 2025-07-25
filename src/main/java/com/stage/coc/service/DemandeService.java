package com.stage.coc.service;

import com.stage.coc.dto.request.DemandeRequest;
import com.stage.coc.dto.request.MarchandiseRequest;
import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.dto.response.MarchandiseResponse;
import com.stage.coc.entity.*;
import com.stage.coc.enums.StatusDemande;
import com.stage.coc.enums.TypeUser;
import com.stage.coc.exception.ResourceNotFoundException;
import com.stage.coc.repository.*;
import com.stage.coc.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final ImportateurRepository importateurRepository;
    private final ExportateurRepository exportateurRepository;
    private final MarchandiseRepository marchandiseRepository;
    private final UserRepository userRepository;
    private final AffectationService affectationService;
    private final AvisMarchandiseRepository avisMarchandiseRepository;

    /**
     * Créer une nouvelle demande - Compatible Importateur ET Exportateur
     */
    public DemandeResponse creerDemande(DemandeRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        System.out.println("🔍 Création demande par utilisateur: " + user.getEmail() + " (Type: " + user.getTypeUser() + ")");

        Importateur importateur;
        Exportateur exportateur;

        if (user.getTypeUser() == TypeUser.IMPORTATEUR) {
            // L'utilisateur connecté est un importateur
            importateur = getOrCreateImportateurFromUser(user);
            exportateur = creerOuRecupererExportateur(request);
        } else if (user.getTypeUser() == TypeUser.EXPORTATEUR) {
            // L'utilisateur connecté est un exportateur
            exportateur = getOrCreateExportateurFromUser(user);
            importateur = creerOuRecupererImportateur(request);
        } else {
            throw new RuntimeException("Seuls les importateurs et exportateurs peuvent créer des demandes");
        }

        // Créer la demande
        Demande demande = new Demande();
        demande.setImportateur(importateur);
        demande.setExportateur(exportateur);
        demande.setStatus(StatusDemande.DEPOSE);

        // Sauvegarder la demande d'abord pour avoir un ID
        demande = demandeRepository.save(demande);
        System.out.println("✅ Demande créée avec ID: " + demande.getId() + " et numéro: " + demande.getNumeroDemande());

        // Ajouter les marchandises
        for (MarchandiseRequest marchandiseReq : request.getMarchandises()) {
            Marchandise marchandise = new Marchandise();
            marchandise.setCategorie(marchandiseReq.getCategorie());
            marchandise.setQuantite(marchandiseReq.getQuantite());
            marchandise.setUniteQuantite(marchandiseReq.getUniteQuantite());
            marchandise.setValeurDh(marchandiseReq.getValeurDh());
            marchandise.setNomProduit(marchandiseReq.getNomProduit());
            marchandise.setFabricant(marchandiseReq.getFabricant());
            marchandise.setAdresseFabricant(marchandiseReq.getAdresseFabricant());
            marchandise.setPaysOrigine(marchandiseReq.getPaysOrigine());
            marchandise.setDemande(demande);
            marchandiseRepository.save(marchandise);
        }

        // Recharger la demande avec toutes les relations
        demande = demandeRepository.findById(demande.getId()).orElse(demande);

        // Affecter automatiquement à un bureau de contrôle et un agent
        affectationService.affecterDemande(demande);

        // Recharger une dernière fois après affectation
        demande = demandeRepository.findById(demande.getId()).orElse(demande);

        System.out.println("✅ Demande finalisée avec bureau: " +
                (demande.getBureauControle() != null ? demande.getBureauControle().getNom() : "AUCUN") +
                " et agent: " +
                (demande.getAgent() != null && demande.getAgent().getUser() != null ? demande.getAgent().getUser().getNom() : "AUCUN"));

        return convertToResponse(demande);
    }

    /**
     * Récupérer les demandes de l'utilisateur connecté (Importateur OU Exportateur)
     */
    public List<DemandeResponse> getMesDemandesUtilisateur() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        System.out.println("🔍 Récupération demandes pour utilisateur: " + user.getEmail() + " (Type: " + user.getTypeUser() + ")");

        List<Demande> demandes;

        switch (user.getTypeUser()) {
            case IMPORTATEUR:
                Importateur importateur = importateurRepository.findByUserId(currentUser.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Profil importateur non trouvé"));
                demandes = demandeRepository.findByImportateurId(importateur.getId());
                System.out.println("✅ " + demandes.size() + " demandes trouvées pour l'importateur ID: " + importateur.getId());
                break;

            case EXPORTATEUR:
                Exportateur exportateur = exportateurRepository.findByUserId(currentUser.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Profil exportateur non trouvé"));
                demandes = demandeRepository.findByExportateurId(exportateur.getId());
                System.out.println("✅ " + demandes.size() + " demandes trouvées pour l'exportateur ID: " + exportateur.getId());
                break;

            default:
                throw new RuntimeException("Type d'utilisateur non autorisé pour cette action");
        }

        return demandes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les demandes affectées à l'agent connecté
     */
    public List<DemandeResponse> getDemandesAgent() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent agent = user.getAgent();
        if (agent == null) {
            System.out.println("⚠️ Aucun profil agent trouvé pour l'utilisateur: " + user.getEmail());
            throw new ResourceNotFoundException("Profil agent non trouvé");
        }

        List<Demande> demandes = demandeRepository.findByAgentId(agent.getId());
        System.out.println("✅ " + demandes.size() + " demandes trouvées pour l'agent ID: " + agent.getId());

        return demandes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Prendre en charge une demande (pour les agents)
     */
    public DemandeResponse prendreEnCharge(Long demandeId) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent agent = user.getAgent();
        if (agent == null || !demande.getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à traiter cette demande");
        }

        if (demande.getStatus() != StatusDemande.DEPOSE) {
            throw new RuntimeException("Cette demande ne peut plus être prise en charge");
        }

        demande.setStatus(StatusDemande.EN_COURS_DE_TRAITEMENT);
        demande.setDateTraitement(LocalDateTime.now());
        demande = demandeRepository.save(demande);

        System.out.println("✅ Demande " + demande.getNumeroDemande() + " prise en charge par l'agent: " + agent.getUser().getNom());

        return convertToResponse(demande);
    }

    /**
     * Méthodes utilitaires privées
     */
    private Importateur getOrCreateImportateurFromUser(User user) {
        return importateurRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil importateur non trouvé pour cet utilisateur"));
    }

    private Exportateur getOrCreateExportateurFromUser(User user) {
        return exportateurRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profil exportateur non trouvé pour cet utilisateur"));
    }

    private Exportateur creerOuRecupererExportateur(DemandeRequest request) {
        return exportateurRepository
                .findByRaisonSocialeAndPays(request.getExportateurNom(), request.getExportateurPays())
                .orElseGet(() -> {
                    System.out.println("📝 Création d'un nouvel exportateur: " + request.getExportateurNom());
                    Exportateur newExportateur = new Exportateur();
                    newExportateur.setRaisonSociale(request.getExportateurNom());
                    newExportateur.setTelephone(request.getExportateurTelephone());
                    newExportateur.setEmail(request.getExportateurEmail());
                    newExportateur.setAdresse(request.getExportateurAdresse());
                    newExportateur.setPays(request.getExportateurPays());
                    newExportateur.setIfu(request.getExportateurIfu());
                    return exportateurRepository.save(newExportateur);
                });
    }

    private Importateur creerOuRecupererImportateur(DemandeRequest request) {
        return importateurRepository
                .findByRaisonSocialeAndIce(request.getImportateurNom(), request.getImportateurIce())
                .orElseGet(() -> {
                    System.out.println("📝 Création d'un nouvel importateur: " + request.getImportateurNom());
                    Importateur newImportateur = new Importateur();
                    newImportateur.setRaisonSociale(request.getImportateurNom());
                    newImportateur.setAdresse(request.getImportateurAdresse());
                    newImportateur.setCodeDouane(request.getImportateurCodeDouane());
                    newImportateur.setIce(request.getImportateurIce());
                    return importateurRepository.save(newImportateur);
                });
    }

    /**
     * Convertir une entité Demande en DTO Response
     */
    private DemandeResponse convertToResponse(Demande demande) {
        DemandeResponse response = new DemandeResponse();
        response.setId(demande.getId());
        response.setNumeroDemande(demande.getNumeroDemande());
        response.setStatus(demande.getStatus());
        response.setDateCreation(demande.getDateCreation());
        response.setDateTraitement(demande.getDateTraitement());
        response.setDateCloture(demande.getDateCloture());
        response.setDecisionGlobale(demande.getDecisionGlobale());
        response.setDelaiEstime(calculerDelaiEstime(demande));
        response.setDateAffectation(demande.getDateCreation()); // Date d'affectation = date de création pour l'instant

        if (demande.getImportateur() != null) {
            response.setImportateurNom(demande.getImportateur().getRaisonSociale());
        }

        if (demande.getExportateur() != null) {
            response.setExportateurNom(demande.getExportateur().getRaisonSociale());
        }

        if (demande.getBureauControle() != null) {
            response.setBureauControleNom(demande.getBureauControle().getNom());
        }

        if (demande.getAgent() != null && demande.getAgent().getUser() != null) {
            response.setAgentNom(demande.getAgent().getUser().getNom());
        }

        // Convertir les marchandises avec leurs avis
        if (demande.getMarchandises() != null) {
            List<MarchandiseResponse> marchandises = demande.getMarchandises().stream()
                    .map(this::convertMarchandiseToResponse)
                    .collect(Collectors.toList());
            response.setMarchandises(marchandises);
        }

        return response;
    }

    /**
     * Convertir une entité Marchandise en DTO Response
     */
    private MarchandiseResponse convertMarchandiseToResponse(Marchandise marchandise) {
        MarchandiseResponse response = new MarchandiseResponse();
        response.setId(marchandise.getId());
        response.setCategorie(marchandise.getCategorie());
        response.setQuantite(marchandise.getQuantite());
        response.setUniteQuantite(marchandise.getUniteQuantite());
        response.setValeurDh(marchandise.getValeurDh());
        response.setNomProduit(marchandise.getNomProduit());
        response.setFabricant(marchandise.getFabricant());
        response.setAdresseFabricant(marchandise.getAdresseFabricant());
        response.setPaysOrigine(marchandise.getPaysOrigine());

        // Ajouter l'avis si disponible
        avisMarchandiseRepository.findByMarchandiseId(marchandise.getId())
                .ifPresent(avis -> {
                    response.setAvis(avis.getAvis().toString());
                    response.setCommentaire(avis.getCommentaire());
                });

        return response;
    }

    private String calculerDelaiEstime(Demande demande) {
        if (demande.getMarchandises() != null) {
            int nombreMarchandises = demande.getMarchandises().size();
            int delai = Math.max(1, nombreMarchandises / 2 + 1);
            return delai + " jour(s)";
        }
        return "2 jour(s)";
    }
}