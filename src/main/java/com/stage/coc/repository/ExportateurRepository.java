package com.stage.coc.repository;

import com.stage.coc.entity.Exportateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExportateurRepository extends JpaRepository<Exportateur, Long> {
    Optional<Exportateur> findByRaisonSocialeAndPays(String raisonSociale, String pays);
}