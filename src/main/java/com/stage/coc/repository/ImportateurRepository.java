package com.stage.coc.repository;

import com.stage.coc.entity.Importateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImportateurRepository extends JpaRepository<Importateur, Long> {
    Optional<Importateur> findByUserId(Long userId);
    Optional<Importateur> findByIce(String ice);

    Optional<Importateur> findByRaisonSocialeAndIce(String importateurNom, String importateurIce);

}