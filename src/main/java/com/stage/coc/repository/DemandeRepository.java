package com.stage.coc.repository;

import com.stage.coc.entity.Demande;
import com.stage.coc.enums.StatusDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
<<<<<<< HEAD
import org.springframework.data.repository.query.Param;
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {
    List<Demande> findByImportateurId(Long importateurId);
    List<Demande> findByAgentId(Long agentId);
    List<Demande> findByBureauControleId(Long bureauControleId);
    List<Demande> findByStatus(StatusDemande status);

    @Query("SELECT d FROM Demande d WHERE d.bureauControle.id = ?1 AND d.status = ?2")
    List<Demande> findByBureauControleIdAndStatus(Long bureauControleId, StatusDemande status);
<<<<<<< HEAD
    @Query("SELECT d FROM Demande d JOIN d.exportateur e WHERE e.email = :email")
    List<Demande> findByExportateurEmail(@Param("email") String email);

    // Alternative si les exportateurs ont un userId (à adapter selon votre modèle)
    @Query("SELECT d FROM Demande d WHERE d.exportateur.id = :exportateurId")
    default List<Demande> findByExportateurId(@Param("exportateurId") Long exportateurId) {
        return null;
    }

    List<Demande> findByExportateurUserId(Long exportateurUserId);
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
}
