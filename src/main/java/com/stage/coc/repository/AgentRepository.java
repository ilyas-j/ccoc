package com.stage.coc.repository;

import com.stage.coc.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByUserId(Long userId);
    List<Agent> findByBureauControleId(Long bureauControleId);
    List<Agent> findByBureauControleIdAndSuperviseur(Long bureauControleId, boolean superviseur);

    @Query("SELECT a FROM Agent a WHERE a.bureauControle.id = ?1 AND a.disponible = true AND a.enConge = false ORDER BY a.chargeTravail ASC")
    List<Agent> findAvailableAgentsByBureauControleOrderByWorkload(Long bureauControleId);
}