package com.tss.aml.entity;

import java.time.Instant;

import com.tss.aml.entity.Enums.DocumentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
@Entity
@Table(name="customer_documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="customer_id", nullable=false)
    private Customer customer;

    @NotBlank
    @Column(name="doc_type", nullable=false)
    private String docType; 

    @NotBlank
    @Column(name="storage_path", nullable=false, length=1000)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @Column(name="uploaded_at", nullable=false)
    private Instant uploadedAt = Instant.now();

}
