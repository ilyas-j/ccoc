package com.stage.coc.entity;

import com.stage.coc.enums.TypeDocument;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_fichier")
    private String nomFichier;

    @Column(name = "chemin_fichier")
    private String cheminFichier;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_document")
    private TypeDocument typeDocument;

    @Column(name = "taille_fichier")
    private Long tailleFichier;

    @Column(name = "date_upload")
    private LocalDateTime dateUpload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id")
    private Demande demande;

    @PrePersist
    protected void onCreate() {
        dateUpload = LocalDateTime.now();
    }
}