package com.tss.aml.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tss.aml.entity.Customer;
import com.tss.aml.entity.Enums.KycStatus;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
    long countByKycStatus(KycStatus kycStatus);
}
