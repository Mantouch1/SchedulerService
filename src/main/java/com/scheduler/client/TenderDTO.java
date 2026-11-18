package com.scheduler.client;

import java.time.LocalDate;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Subset of TenderFlow's TenderResponseDTO. */
@Data
@NoArgsConstructor
public class TenderDTO {

	private Long id;
	private String reference;
	private String title;
	private String statusLabel;
	private LocalDate deadlineDate;
	private String createdBy;
}
