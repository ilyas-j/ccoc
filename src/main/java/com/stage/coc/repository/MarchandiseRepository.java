package com.stage.coc.repository;

import com.stage.coc.entity.Marchandise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarchandiseRepository extends JpaRepository<Marchandise, Long> {
    List<Marchandise> findByDemandeId(Long demandeId);
}
