package com.tss.aml.service;

import com.tss.aml.entity.Customer;
import com.tss.aml.entity.Document;
import com.tss.aml.repository.CustomerRepository;
import com.tss.aml.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Transactional
    public Document uploadAndSaveDocument(String userEmail, MultipartFile file, String docType) throws IOException {
        // 1. Find the customer associated with the logged-in user
        Customer customer = customerRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found for email: " + userEmail));

        // 2. Upload the file to Cloudinary in a user-specific folder
        String folderName = "customer_" + customer.getId();
        String fileUrl = cloudinaryService.uploadFile(file, folderName);

        // 3. Create and save the document entity
        Document document = new Document();
        document.setCustomer(customer);
        document.setDocType(docType.toUpperCase()); 
        document.setStoragePath(fileUrl);
        
        return documentRepository.save(document);
    }
}