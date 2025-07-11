package com.stage.coc.service;

import com.stage.coc.entity.Demande;
import com.stage.coc.entity.Document;
import com.stage.coc.enums.TypeDocument;
import com.stage.coc.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    private final DocumentRepository documentRepository;

    public Document stockerDocument(MultipartFile file, Demande demande, TypeDocument typeDocument) {
        try {
            // Créer le répertoire s'il n'existe pas
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Générer un nom de fichier unique
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

            // Copier le fichier
            Path targetLocation = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Sauvegarder en base
            Document document = new Document();
            document.setNomFichier(fileName);
            document.setCheminFichier(targetLocation.toString());
            document.setTypeDocument(typeDocument);
            document.setTailleFichier(file.getSize());
            document.setDemande(demande);

            return documentRepository.save(document);

        } catch (IOException ex) {
            throw new RuntimeException("Impossible de stocker le fichier " + file.getOriginalFilename(), ex);
        }
    }
}