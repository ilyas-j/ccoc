package com.stage.coc.repository;

import com.stage.coc.entity.Demande;
import com.stage.coc.enums.StatusDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {
    List<Demande> findByExportateurId(Long exportateurId);
    List<Demande> findByImportateurId(Long importateurId);
    List<Demande> findByAgentId(Long agentId);
    List<Demande> findByBureauControleId(Long bureauControleId);
    List<Demande> findByStatus(StatusDemande status);

    @Query("SELECT d FROM Demande d WHERE d.bureauControle.id = ?1 AND d.status = ?2")
    List<Demande> findByBureauControleIdAndStatus(Long bureauControleId, StatusDemande status);

    @Query("SELECT d FROM Demande d JOIN d.exportateur e WHERE e.email = :email")
    List<Demande> findByExportateurEmail(@Param("email") String email);

    // Method for finding demands by exportateur's user ID
    @Query("SELECT d FROM Demande d WHERE d.exportateur.user.id = :userId")
    List<Demande> findByExportateurUserId(@Param("userId") Long userId);

    // ✅ AJOUTER CETTE MÉTHODE pour éviter les demandes dupliquées
    List<Demande> findByImportateurIdAndExportateurId(Long importateurId, Long exportateurId);

    @Query("SELECT d FROM Demande d WHERE d.agent.user.id = :userId")
    List<Demande> findByAgentUserId(@Param("userId") Long userId);

    @Query("SELECT d FROM Demande d WHERE d.agent.id = :agentId AND d.status = :status")
    List<Demande> findByAgentIdAndStatus(@Param("agentId") Long agentId, @Param("status") StatusDemande status);}