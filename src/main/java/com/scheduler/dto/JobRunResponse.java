package com.scheduler.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobRunResponse {

	private Long id;
	private String jobName;
	private String triggerType;
	private String triggeredBy;
	private LocalDateTime startedAt;
	private LocalDateTime finishedAt;
	private String status;
	private String message;
}
