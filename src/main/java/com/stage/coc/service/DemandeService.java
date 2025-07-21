package com.stage.coc.service;

import com.stage.coc.dto.request.DemandeRequest;
import com.stage.coc.dto.request.MarchandiseRequest;
import com.stage.coc.dto.response.DemandeResponse;
import com.stage.coc.dto.response.MarchandiseResponse;
import com.stage.coc.entity.*;
import com.stage.coc.enums.StatusDemande;
<<<<<<< HEAD
import com.stage.coc.enums.TypeUser;
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
import com.stage.coc.exception.ResourceNotFoundException;
import com.stage.coc.repository.*;
import com.stage.coc.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.ArrayList;
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
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

<<<<<<< HEAD
    /**
     * Créer une nouvelle demande - Compatible Importateur ET Exportateur
     */
    public DemandeResponse creerDemande(DemandeRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Importateur importateur;
        Exportateur exportateur;

        // Gestion selon le type d'utilisateur connecté
        if (user.getTypeUser() == TypeUser.IMPORTATEUR) {
            // L'utilisateur connecté est un importateur
            importateur = getOrCreateImportateurFromUser(user);
            // Créer ou récupérer l'exportateur depuis les données du formulaire
            exportateur = creerOuRecupererExportateur(request);

        } else if (user.getTypeUser() == TypeUser.EXPORTATEUR) {
            // L'utilisateur connecté est un exportateur
            exportateur = getOrCreateExportateurFromUser(user);
            // Créer ou récupérer l'importateur depuis les données du formulaire
            importateur = creerOuRecupererImportateur(request);

        } else {
            throw new RuntimeException("Seuls les importateurs et exportateurs peuvent créer des demandes");
        }
=======
    public DemandeResponse creerDemande(DemandeRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Récupérer ou créer l'importateur
        Importateur importateur = importateurRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Importateur non trouvé"));

        // Récupérer ou créer l'exportateur
        Exportateur exportateur = exportateurRepository
                .findByRaisonSocialeAndPays(request.getExportateurNom(), request.getExportateurPays())
                .orElseGet(() -> {
                    Exportateur newExportateur = new Exportateur();
                    newExportateur.setRaisonSociale(request.getExportateurNom());
                    newExportateur.setTelephone(request.getExportateurTelephone());
                    newExportateur.setEmail(request.getExportateurEmail());
                    newExportateur.setAdresse(request.getExportateurAdresse());
                    newExportateur.setPays(request.getExportateurPays());
                    newExportateur.setIfu(request.getExportateurIfu());
                    return exportateurRepository.save(newExportateur);
                });
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8

        // Créer la demande
        Demande demande = new Demande();
        demande.setImportateur(importateur);
        demande.setExportateur(exportateur);
        demande.setStatus(StatusDemande.DEPOSE);

<<<<<<< HEAD
        // Sauvegarder la demande d'abord pour avoir un ID
=======
        // Sauvegarder la demande d'abord
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
        demande = demandeRepository.save(demande);

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

        // Affecter automatiquement à un bureau de contrôle et un agent
        affectationService.affecterDemande(demande);

        return convertToResponse(demande);
    }

<<<<<<< HEAD
    /**
     * Récupérer les demandes de l'utilisateur connecté (Importateur OU Exportateur)
     */


    /**
     * Récupérer les demandes affectées à l'agent connecté
     */
=======
    public List<DemandeResponse> getMesDemandesImportateur() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Importateur importateur = importateurRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Importateur non trouvé"));

        List<Demande> demandes = demandeRepository.findByImportateurId(importateur.getId());
        return demandes.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
    public List<DemandeResponse> getDemandesAgent() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent agent = user.getAgent();
        if (agent == null) {
            throw new ResourceNotFoundException("Agent non trouvé");
        }

        List<Demande> demandes = demandeRepository.findByAgentId(agent.getId());
        return demandes.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

<<<<<<< HEAD
    /**
     * Prendre en charge une demande (pour les agents)
     */
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
    public DemandeResponse prendreEnCharge(Long demandeId) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Agent agent = user.getAgent();
        if (agent == null || !demande.getAgent().getId().equals(agent.getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à traiter cette demande");
        }

        demande.setStatus(StatusDemande.EN_COURS_DE_TRAITEMENT);
        demande.setDateTraitement(LocalDateTime.now());
        demande = demandeRepository.save(demande);

        return convertToResponse(demande);
    }

<<<<<<< HEAD
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
                    Exportateur newExportateur = new Exportateur();
                    newExportateur.setRaisonSociale(request.getExportateurNom());
                    newExportateur.setTelephone(request.getExportateurTelephone());
                    newExportateur.setEmail(request.getExportateurEmail());
                    newExportateur.setAdresse(request.getExportateurAdresse());
                    newExportateur.setPays(request.getExportateurPays());
                    newExportateur.setIfu(request.getExportateurIfu());
                    // Note: On ne crée pas d'utilisateur associé pour un exportateur "externe"
                    return exportateurRepository.save(newExportateur);
                });
    }

    private Importateur creerOuRecupererImportateur(DemandeRequest request) {
        return importateurRepository
                .findByRaisonSocialeAndIce(request.getImportateurNom(), request.getImportateurIce())
                .orElseGet(() -> {
                    Importateur newImportateur = new Importateur();
                    newImportateur.setRaisonSociale(request.getImportateurNom());
                    newImportateur.setAdresse(request.getImportateurAdresse());
                    newImportateur.setCodeDouane(request.getImportateurCodeDouane());
                    newImportateur.setIce(request.getImportateurIce());
                    // Note: On ne crée pas d'utilisateur associé pour un importateur "externe"
                    return importateurRepository.save(newImportateur);
                });
    }
    /**
     * Convertir une entité Demande en DTO Response
     */
    public DemandeResponse convertToResponse(Demande demande) {
=======
    private DemandeResponse convertToResponse(Demande demande) {
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
        DemandeResponse response = new DemandeResponse();
        response.setId(demande.getId());
        response.setNumeroDemande(demande.getNumeroDemande());
        response.setStatus(demande.getStatus());
        response.setDateCreation(demande.getDateCreation());
        response.setDateTraitement(demande.getDateTraitement());
        response.setDateCloture(demande.getDateCloture());
        response.setDecisionGlobale(demande.getDecisionGlobale());

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

<<<<<<< HEAD
        response.setDelaiEstime(calculerDelaiEstime(demande));
        response.setDateAffectation(demande.getDateCreation());

        // Convertir les marchandises - CORRECTION ICI
=======
        // Convertir les marchandises
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
        if (demande.getMarchandises() != null) {
            List<MarchandiseResponse> marchandises = demande.getMarchandises().stream()
                    .map(this::convertMarchandiseToResponse)
                    .collect(Collectors.toList());
            response.setMarchandises(marchandises);
        }

        return response;
    }

<<<<<<< HEAD
    /**
     * Convertir une entité Marchandise en DTO Response - CORRECTION ICI AUSSI
     */
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
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

        if (marchandise.getAvisMarchandise() != null) {
            response.setAvis(marchandise.getAvisMarchandise().getAvis().toString());
            response.setCommentaire(marchandise.getAvisMarchandise().getCommentaire());
        }

        return response;
    }
<<<<<<< HEAD

    private String calculerDelaiEstime(Demande demande) {
        if (demande.getMarchandises() != null) {
            int nombreMarchandises = demande.getMarchandises().size();
            int delai = Math.max(1, nombreMarchandises / 2 + 1);
            return delai + " jour(s)";
        }
        return "2 jour(s)";
    }

    /**
     * Convertir une entité Marchandise en DTO Response
     */

    public List<DemandeResponse> getMesDemandesUtilisateur() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        switch (user.getTypeUser()) {
            case IMPORTATEUR:
                Importateur importateur = importateurRepository.findByUserId(currentUser.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Importateur non trouvé"));
                demandeRepository.findByImportateurId(importateur.getId());
                break;

            case EXPORTATEUR:
                List<Demande> byExportateurUserId = demandeRepository.findByExportateurUserId(currentUser.getId());
                break;

            default:
                throw new RuntimeException("Type d'utilisateur non autorisé pour cette action");
        }
        return List.of();
    }
}

=======
}
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
