package com.stage.coc.controller;

import com.stage.coc.entity.Demande;
import com.stage.coc.entity.Document;
import com.stage.coc.enums.TypeDocument;
import com.stage.coc.exception.ResourceNotFoundException;
import com.stage.coc.repository.DemandeRepository;
import com.stage.coc.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentController {

    private final FileStorageService fileStorageService;
    private final DemandeRepository demandeRepository;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('IMPORTATEUR') or hasRole('EXPORTATEUR')")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            @RequestParam("demandeId") Long demandeId) {

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        TypeDocument typeDocument = TypeDocument.valueOf(type.toUpperCase());
        Document document = fileStorageService.stockerDocument(file, demande, typeDocument);

        Map<String, Object> response = new HashMap<>();
        response.put("nom", document.getNomFichier());
        response.put("type", type);
        response.put("taille", document.getTailleFichier() + " bytes");
        response.put("url", "/documents/" + document.getNomFichier());
        response.put("dateUpload", document.getDateUpload());
        response.put("message", "Document uploadé avec succès");

        return ResponseEntity.ok(response);
    }
}
