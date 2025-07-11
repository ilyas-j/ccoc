package com.stage.coc.dto.request;

import com.stage.coc.enums.AvisMarchandise;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AvisMarchandiseRequest {
    @NotNull(message = "Marchandise ID est obligatoire")
    private Long marchandiseId;

    @NotNull(message = "Avis est obligatoire")
    private AvisMarchandise avis;

    private String commentaire;
}
