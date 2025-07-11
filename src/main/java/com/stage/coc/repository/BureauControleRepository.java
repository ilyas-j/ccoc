package com.stage.coc.repository;

import com.stage.coc.entity.BureauControle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BureauControleRepository extends JpaRepository<BureauControle, Long> {
    Optional<BureauControle> findByNom(String nom);
}
