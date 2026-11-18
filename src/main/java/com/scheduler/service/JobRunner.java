package com.scheduler.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.scheduler.client.AuditPublisher;
import com.scheduler.dto.JobRunResponse;
import com.scheduler.entity.JobDefinition;
import com.scheduler.entity.JobRun;
import com.scheduler.entity.JobStatus;
import com.scheduler.exception.ConflictException;
import com.scheduler.exception.ResourceNotFoundException;
import com.scheduler.job.JobHandler;
import com.scheduler.repository.JobDefinitionRepository;
import com.scheduler.repository.JobRunRepository;

/** Runs a job, records the run (history) and publishes an audit event. A failing job never throws to the caller. */
@Service
public class JobRunner {

	public static final String SCHEDULE = "SCHEDULE";
	public static final String MANUAL = "MANUAL";

	private final Map<String, JobHandler> handlers;
	private final JobDefinitionRepository definitions;
	private final JobRunRepository runs;
	private final AuditPublisher audit;
	private final Set<String> running = ConcurrentHashMap.newKeySet();

	public JobRunner(List<JobHandler> handlerList, JobDefinitionRepository definitions, JobRunRepository runs, AuditPublisher audit) {
		this.handlers = handlerList.stream().collect(Collectors.toMap(JobHandler::name, Function.identity()));
		this.definitions = definitions;
		this.runs = runs;
		this.audit = audit;
	}

	public List<JobHandler> handlers() {
		return List.copyOf(handlers.values());
	}

	/** Called by the cron triggers: skipped (empty) when the job is disabled or already running. */
	public Optional<JobRunResponse> runScheduled(String jobName) {
		JobDefinition def = definitions.findById(jobName).orElse(null);
		if (def == null || !def.isEnabled() || !handlers.containsKey(jobName)) {
			return Optional.empty();
		}
		if (!running.add(jobName)) {
			return Optional.empty();
		}
		return Optional.of(execute(def, SCHEDULE, "scheduler"));
	}

	/** Called by POST /api/jobs/{name}/run: works even if the job is disabled; 409 if it is already running. */
	public JobRunResponse runManual(String jobName, String triggeredBy) {
		JobDefinition def = definitions.findById(jobName)
				.orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobName));
		if (!handlers.containsKey(jobName)) {
			throw new ResourceNotFoundException("Job not found: " + jobName);
		}
		if (!running.add(jobName)) {
			throw new ConflictException("Job is already running: " + jobName);
		}
		return execute(def, MANUAL, triggeredBy);
	}

	private JobRunResponse execute(JobDefinition def, String triggerType, String triggeredBy) {
		JobRun run = runs.save(JobRun.builder().jobName(def.getName()).triggerType(triggerType).triggeredBy(triggeredBy)
				.startedAt(LocalDateTime.now()).status(JobStatus.RUNNING).build());
		try {
			run.setMessage(handlers.get(def.getName()).run());
			run.setStatus(JobStatus.SUCCESS);
		} catch (Exception e) {
			String msg = e.getClass().getSimpleName() + ": " + e.getMessage();
			run.setMessage(msg.length() > 2000 ? msg.substring(0, 2000) : msg);
			run.setStatus(JobStatus.FAILED);
		} finally {
			running.remove(def.getName());
		}
		run.setFinishedAt(LocalDateTime.now());
		runs.save(run);
		def.setLastRunAt(run.getFinishedAt());
		def.setLastStatus(run.getStatus());
		definitions.save(def);
		audit.publish(triggeredBy, "JOB_" + run.getStatus(), "JOB", def.getName(), run.getMessage());
		return toResponse(run);
	}

	public static JobRunResponse toResponse(JobRun r) {
		return JobRunResponse.builder().id(r.getId()).jobName(r.getJobName()).triggerType(r.getTriggerType())
				.triggeredBy(r.getTriggeredBy()).startedAt(r.getStartedAt()).finishedAt(r.getFinishedAt())
				.status(r.getStatus().name()).message(r.getMessage()).build();
	}
}
