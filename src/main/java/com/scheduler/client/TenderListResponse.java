package com.scheduler.client;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/** TenderFlow wraps its answers in {status, message, payload}. */
@Data
@NoArgsConstructor
public class TenderListResponse {

	private String status;
	private String message;
	private List<TenderDTO> payload;
}
