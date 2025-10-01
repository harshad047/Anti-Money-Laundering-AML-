package com.tss.aml.repository;

import com.tss.aml.entity.CountryRisk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRiskRepository extends JpaRepository<CountryRisk, Long> {
    CountryRisk findByCountryCode(String code);
}