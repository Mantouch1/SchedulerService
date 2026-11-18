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
public class JobDefinitionResponse {

	private String name;
	private String description;
	private boolean enabled;
	private LocalDateTime lastRunAt;
	private String lastStatus;
}
