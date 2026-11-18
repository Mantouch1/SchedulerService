package com.scheduler.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.scheduler.entity.JobRun;

public interface JobRunRepository extends JpaRepository<JobRun, Long> {

	Page<JobRun> findByJobName(String jobName, Pageable pageable);
}
