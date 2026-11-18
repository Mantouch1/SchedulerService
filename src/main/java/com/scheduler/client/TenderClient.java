package com.scheduler.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tender-client", url = "${tenderflow.service.url}")
public interface TenderClient {

	@GetMapping("/api/tenders/tenders")
	TenderListResponse listTenders(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);
}
