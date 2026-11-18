package com.scheduler.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scheduler.entity.JobDefinition;

public interface JobDefinitionRepository extends JpaRepository<JobDefinition, String> {
}
