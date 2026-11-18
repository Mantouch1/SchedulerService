package com.scheduler.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRun {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String jobName;

	/** SCHEDULE (cron) or MANUAL (POST /api/jobs/{name}/run). */
	@Column(name = "trigger_type", nullable = false)
	private String triggerType;

	private String triggeredBy;

	@Column(nullable = false)
	private LocalDateTime startedAt;

	private LocalDateTime finishedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private JobStatus status;

	@Column(columnDefinition = "text")
	private String message;
}
