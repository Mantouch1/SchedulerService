package com.scheduler.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditEventRequest {
	private String source;
	private String eventType;
	private String actor;
	private String entityType;
	private String entityId;
	private String details;
}
