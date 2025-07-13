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

    public DemandeResponse creerDemande(DemandeRequest request) {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Importateur importateur;
        Exportateur exportateur;

        if (user.getTypeUser() == TypeUser.IMPORTATEUR) {
            // L'importateur fait la demande
            importateur = importateurRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Importateur non trouvé"));

            // Récupérer ou créer l'exportateur
            exportateur = exportateurRepository
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
        } else if (user.getTypeUser() == TypeUser.EXPORTATEUR) {
            // L'exportateur fait la demande
            // Récupérer ou créer l'importateur
            importateur = importateurRepository.findByIce(request.getImportateurIce())
                    .orElseGet(() -> {
                        // Créer un nouveau user pour l'importateur s'il n'existe pas
                        User newImportateurUser = new User();
                        newImportateurUser.setEmail(request.getImportateurEmail());
                        newImportateurUser.setNom(request.getImportateurNom());
                        newImportateurUser.setTelephone(request.getImportateurTelephone());
                        newImportateurUser.setTypeUser(TypeUser.IMPORTATEUR);
                        newImportateurUser.setPassword(""); // Pas de mot de passe, créé par exportateur
                        newImportateurUser.setEnabled(false); // Compte non activé
                        newImportateurUser = userRepository.save(newImportateurUser);

                        Importateur newImportateur = new Importateur();
                        newImportateur.setUser(newImportateurUser);
                        newImportateur.setRaisonSociale(request.getImportateurNom());
                        newImportateur.setAdresse(request.getImportateurAdresse());
                        newImportateur.setCodeDouane(request.getImportateurCodeDouane());
                        newImportateur.setIce(request.getImportateurIce());
                        return importateurRepository.save(newImportateur);
                    });

            // L'exportateur est l'utilisateur actuel
            exportateur = exportateurRepository.findByRaisonSocialeAndPays(user.getNom(), "France") // Adapter selon les données
                    .orElseGet(() -> {
                        Exportateur newExportateur = new Exportateur();
                        newExportateur.setRaisonSociale(user.getNom());
                        newExportateur.setEmail(user.getEmail());
                        newExportateur.setTelephone(user.getTelephone());
                        // Autres informations à récupérer du profil utilisateur
                        return exportateurRepository.save(newExportateur);
                    });
        } else {
            throw new RuntimeException("Seuls les importateurs et exportateurs peuvent créer des demandes");
        }

        // Créer la demande
        Demande demande = new Demande();
        demande.setImportateur(importateur);
        demande.setExportateur(exportateur);
        demande.setStatus(StatusDemande.DEPOSE);

        // Sauvegarder la demande d'abord
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

    public List<DemandeResponse> getMesDemandesImportateur() {
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        List<Demande> demandes;

        if (user.getTypeUser() == TypeUser.IMPORTATEUR) {
            Importateur importateur = importateurRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Importateur non trouvé"));
            demandes = demandeRepository.findByImportateurId(importateur.getId());
        } else if (user.getTypeUser() == TypeUser.EXPORTATEUR) {
            // Pour un exportateur, trouver les demandes où il est l'exportateur
            // Pour un exportateur, trouver les demandes où il est l'exportateur
            Exportateur exportateur = exportateurRepository.findByEmail(user.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Exportateur non trouvé"));
            demandes = demandeRepository.findByExportateurId(exportateur.getId());
        } else {
            throw new RuntimeException("Seuls les importateurs et exportateurs peuvent consulter leurs demandes");
        }

        return demandes.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

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

    private DemandeResponse convertToResponse(Demande demande) {
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

        // Convertir les marchandises
        if (demande.getMarchandises() != null) {
            List<MarchandiseResponse> marchandises = demande.getMarchandises().stream()
                    .map(this::convertMarchandiseToResponse)
                    .collect(Collectors.toList());
            response.setMarchandises(marchandises);
        }

        return response;
    }

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
}