package com.stage.coc.repository;

import com.stage.coc.entity.Demande;
import com.stage.coc.enums.StatusDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {
    List<Demande> findByImportateurId(Long importateurId);
    List<Demande> findByExportateurId(Long exportateurId);
    List<Demande> findByAgentId(Long agentId);
    List<Demande> findByBureauControleId(Long bureauControleId);
    List<Demande> findByStatus(StatusDemande status);

    @Query("SELECT d FROM Demande d WHERE d.bureauControle.id = ?1 AND d.status = ?2")
    List<Demande> findByBureauControleIdAndStatus(Long bureauControleId, StatusDemande status);
}