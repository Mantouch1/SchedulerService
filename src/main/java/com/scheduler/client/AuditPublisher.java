package com.scheduler.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Sends business events to the Reporting/Audit service. Best effort: a failure never breaks the caller. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditPublisher {

	private final AuditClient auditClient;
	private final ServiceTokenProvider tokenProvider;

	@Value("${spring.application.name}")
	private String source;
	@Value("${app.audit.enabled:true}")
	private boolean enabled;

	public void publish(String actor, String eventType, String entityType, String entityId, String details) {
		if (!enabled) {
			return;
		}
		try {
			auditClient.publish(tokenProvider.bearer(), AuditEventRequest.builder()
					.source(source).eventType(eventType).actor(actor)
					.entityType(entityType).entityId(entityId).details(details).build());
		} catch (Exception e) {
			log.warn("Audit event {} not published: {}", eventType, e.getMessage());
		}
	}
}
