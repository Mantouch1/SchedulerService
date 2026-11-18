package com.scheduler.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One row per (tender, deadline): guarantees that a reminder is sent only once. */
@Entity
@Table(name = "reminders_sent", uniqueConstraints = @UniqueConstraint(columnNames = { "tender_id", "deadline_date" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderSent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tender_id", nullable = false)
	private Long tenderId;

	@Column(name = "deadline_date", nullable = false)
	private LocalDate deadlineDate;

	private String recipientEmail;

	@Column(nullable = false, updatable = false)
	private LocalDateTime sentAt;

	@PrePersist
	private void onCreate() {
		this.sentAt = LocalDateTime.now();
	}
}
