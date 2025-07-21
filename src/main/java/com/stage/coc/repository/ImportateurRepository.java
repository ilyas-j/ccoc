package com.stage.coc.repository;

import com.stage.coc.entity.Importateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImportateurRepository extends JpaRepository<Importateur, Long> {
    Optional<Importateur> findByUserId(Long userId);
    Optional<Importateur> findByIce(String ice);
<<<<<<< HEAD

    Optional<Importateur> findByRaisonSocialeAndIce(String importateurNom, String importateurIce);
=======
>>>>>>> f59e6dfdfa7c5770947b5d62e0df2f48aee08cc8
}