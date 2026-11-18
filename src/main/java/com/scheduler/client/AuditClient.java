package com.scheduler.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "audit-client", url = "${report.service.url}")
public interface AuditClient {

	@PostMapping("/api/events")
	void publish(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody AuditEventRequest event);
}
