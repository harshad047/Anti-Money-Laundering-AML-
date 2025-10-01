package com.tss.aml.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.tss.aml.entity.*;

import com.tss.aml.entity.Enums.KycStatus;
import com.tss.aml.entity.Enums.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@Entity
@Table(name = "customers",
       uniqueConstraints = {
         @UniqueConstraint(columnNames = "email")
       })
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name split
    @NotBlank @Size(max=100)
    @Column(name="first_name", nullable=false)
    private String firstName;

    @Size(max=100)
    @Column(name="middle_name")
    private String middleName;

    @NotBlank @Size(max=100)
    @Column(name="last_name", nullable=false)
    private String lastName;

    // Contact
    @NotBlank @Email @Size(max=150)
    @Column(nullable=false)
    private String email;

    @Size(max=20)
    private String phone; // add pattern if needed

    // Authentication fields
    @NotBlank
    @Size(min=8, max=100)
    @Column(name="password", nullable=false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable=false)
    private Role role = Role.CUSTOMER;


    // Address as embeddable
    @Embedded
    private Address address;

    
    // Other metadata
    @Enumerated(EnumType.STRING)
    @Column(name="kyc_status", nullable=false)
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy="customer", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Document> documents = new ArrayList<>();

    public void addDocument(Document doc) {
        documents.add(doc);
        doc.setCustomer(this);
    }

    public void removeDocument(Document doc) {
        documents.remove(doc);
        doc.setCustomer(null);

}
}
