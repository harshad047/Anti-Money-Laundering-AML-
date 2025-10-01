package com.tss.aml.repository;

import com.tss.aml.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findByIsActiveTrueOrderByPriorityAsc();
}