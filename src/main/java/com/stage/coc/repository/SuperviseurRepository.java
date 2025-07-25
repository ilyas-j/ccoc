package com.stage.coc.repository;

import com.stage.coc.entity.Superviseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuperviseurRepository extends JpaRepository<Superviseur, Long> {
    Optional<Superviseur> findByUserId(Long userId);
    List<Superviseur> findByBureauControleId(Long bureauControleId);
}