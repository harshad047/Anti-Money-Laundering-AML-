package com.tss.aml.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tss.aml.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> { }
