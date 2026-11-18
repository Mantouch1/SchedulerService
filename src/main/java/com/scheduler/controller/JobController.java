package com.scheduler.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scheduler.dto.JobDefinitionResponse;
import com.scheduler.dto.JobRunResponse;
import com.scheduler.dto.PageResponse;
import com.scheduler.service.JobAdminService;
import com.scheduler.service.JobRunner;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCHEDULER_MANAGE')")
public class JobController {

	private final JobAdminService admin;
	private final JobRunner runner;

	@GetMapping
	public List<JobDefinitionResponse> list() {
		return admin.list();
	}

	@PatchMapping("/{name}/enabled")
	public JobDefinitionResponse setEnabled(@PathVariable String name, @RequestParam boolean value) {
		return admin.setEnabled(name, value);
	}

	/** Runs the job now and waits for the result. */
	@PostMapping("/{name}/run")
	public JobRunResponse run(@PathVariable String name, Authentication auth) {
		return runner.runManual(name, auth.getName());
	}

	@GetMapping("/runs")
	public PageResponse<JobRunResponse> history(@RequestParam(required = false) String name,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
		return admin.history(name, page, size);
	}
}
