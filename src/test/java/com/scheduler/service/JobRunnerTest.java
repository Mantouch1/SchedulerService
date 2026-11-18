package com.scheduler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.scheduler.client.AuditPublisher;
import com.scheduler.dto.JobRunResponse;
import com.scheduler.entity.JobDefinition;
import com.scheduler.entity.JobRun;
import com.scheduler.exception.ResourceNotFoundException;
import com.scheduler.job.JobHandler;
import com.scheduler.repository.JobDefinitionRepository;
import com.scheduler.repository.JobRunRepository;

import java.util.List;

class JobRunnerTest {

	private final JobDefinitionRepository definitions = mock(JobDefinitionRepository.class);
	private final JobRunRepository runs = mock(JobRunRepository.class);
	private final AuditPublisher audit = mock(AuditPublisher.class);
	private final AtomicInteger executions = new AtomicInteger();
	private boolean fail = false;
	private JobRunner runner;

	@BeforeEach
	void setUp() {
		JobHandler handler = new JobHandler() {
			public String name() { return "J"; }
			public String description() { return "test job"; }
			public String run() throws Exception {
				executions.incrementAndGet();
				if (fail) {
					throw new IllegalStateException("boom");
				}
				return "done";
			}
		};
		runner = new JobRunner(List.of(handler), definitions, runs, audit);
		when(runs.save(any(JobRun.class))).thenAnswer(i -> i.getArgument(0));
	}

	private void jobIs(boolean enabled) {
		when(definitions.findById("J")).thenReturn(Optional.of(JobDefinition.builder().name("J").enabled(enabled).build()));
	}

	@Test
	void manualRunRecordsSuccessAndPublishesAnAuditEvent() {
		jobIs(true);
		JobRunResponse r = runner.runManual("J", "admin@x.de");
		assertEquals("SUCCESS", r.getStatus());
		assertEquals("done", r.getMessage());
		assertEquals("MANUAL", r.getTriggerType());
		verify(audit).publish("admin@x.de", "JOB_SUCCESS", "JOB", "J", "done");
	}

	@Test
	void aFailingJobIsRecordedAsFailedAndNeverThrows() {
		jobIs(true);
		fail = true;
		JobRunResponse r = runner.runManual("J", "admin@x.de");
		assertEquals("FAILED", r.getStatus());
		assertTrue(r.getMessage().contains("boom"));
	}

	@Test
	void scheduledRunIsSkippedWhenTheJobIsDisabled() {
		jobIs(false);
		assertTrue(runner.runScheduled("J").isEmpty());
		assertEquals(0, executions.get());
	}

	@Test
	void manualRunWorksEvenWhenDisabled() {
		jobIs(false);
		assertEquals("SUCCESS", runner.runManual("J", "a").getStatus());
		assertEquals(1, executions.get());
	}

	@Test
	void unknownJobIsNotFound() {
		when(definitions.findById("X")).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> runner.runManual("X", "a"));
	}
}
