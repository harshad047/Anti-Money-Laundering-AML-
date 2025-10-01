// com.tss.aml.repository.TransactionRepository.java

package com.tss.aml.repository;

import com.tss.aml.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCustomerId(String customerId);
    List<Transaction> findByStatus(String status);
}