package com.scheduler.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.scheduler.dto.JobDefinitionResponse;
import com.scheduler.dto.JobRunResponse;
import com.scheduler.dto.PageResponse;
import com.scheduler.entity.JobDefinition;
import com.scheduler.entity.JobRun;
import com.scheduler.exception.ResourceNotFoundException;
import com.scheduler.repository.JobDefinitionRepository;
import com.scheduler.repository.JobRunRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobAdminService {

	private final JobDefinitionRepository definitions;
	private final JobRunRepository runs;

	public List<JobDefinitionResponse> list() {
		return definitions.findAll(Sort.by("name")).stream().map(this::toResponse).toList();
	}

	public JobDefinitionResponse setEnabled(String name, boolean enabled) {
		JobDefinition def = definitions.findById(name).orElseThrow(() -> new ResourceNotFoundException("Job not found: " + name));
		def.setEnabled(enabled);
		return toResponse(definitions.save(def));
	}

	public PageResponse<JobRunResponse> history(String name, int page, int size) {
		PageRequest pr = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "startedAt"));
		Page<JobRun> result = (name == null || name.isBlank()) ? runs.findAll(pr) : runs.findByJobName(name.trim(), pr);
		return PageResponse.of(result.map(JobRunner::toResponse));
	}

	private JobDefinitionResponse toResponse(JobDefinition d) {
		return JobDefinitionResponse.builder().name(d.getName()).description(d.getDescription()).enabled(d.isEnabled())
				.lastRunAt(d.getLastRunAt()).lastStatus(d.getLastStatus() == null ? null : d.getLastStatus().name()).build();
	}
}
