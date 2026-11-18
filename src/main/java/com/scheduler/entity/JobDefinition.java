package com.scheduler.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDefinition {

	@Id
	private String name;

	private String description;

	@Builder.Default
	@Column(nullable = false)
	private boolean enabled = true;

	private LocalDateTime lastRunAt;

	@Enumerated(EnumType.STRING)
	private JobStatus lastStatus;
}
