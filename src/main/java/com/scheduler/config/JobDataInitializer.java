package com.scheduler.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.scheduler.entity.JobDefinition;
import com.scheduler.job.JobHandler;
import com.scheduler.repository.JobDefinitionRepository;

import java.util.List;

import lombok.RequiredArgsConstructor;

/** Creates the job_definitions rows of the known jobs (enabled by default); never touches existing ones. */
@Component
@RequiredArgsConstructor
public class JobDataInitializer implements ApplicationRunner {

	private final List<JobHandler> handlers;
	private final JobDefinitionRepository definitions;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		for (JobHandler h : handlers) {
			if (!definitions.existsById(h.name())) {
				definitions.save(JobDefinition.builder().name(h.name()).description(h.description()).enabled(true).build());
			}
		}
	}
}
