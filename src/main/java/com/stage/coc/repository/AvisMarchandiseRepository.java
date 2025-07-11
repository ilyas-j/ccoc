package com.stage.coc.repository;

import com.stage.coc.entity.AvisMarchandise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvisMarchandiseRepository extends JpaRepository<AvisMarchandise, Long> {
    Optional<AvisMarchandise> findByMarchandiseId(Long marchandiseId);
}