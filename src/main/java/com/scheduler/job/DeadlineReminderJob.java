package com.scheduler.job;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.scheduler.client.NotificationClient;
import com.scheduler.client.NotificationRequest;
import com.scheduler.client.ServiceTokenProvider;
import com.scheduler.client.TenderClient;
import com.scheduler.client.TenderDTO;
import com.scheduler.client.TenderListResponse;
import com.scheduler.entity.ReminderSent;
import com.scheduler.repository.ReminderSentRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Reads the tenders from TenderFlow (Feign, technical account) and, for every OPEN tender whose deadline is within
 * the next N days, asks NotificationService (Feign) to e-mail its author. One reminder per tender and deadline.
 */
@Slf4j
@Component
public class DeadlineReminderJob implements JobHandler {

	public static final String NAME = "DEADLINE_REMINDER";

	private final ServiceTokenProvider tokenProvider;
	private final TenderClient tenderClient;
	private final NotificationClient notificationClient;
	private final ReminderSentRepository reminderRepository;
	private final int daysBefore;
	private final String openStatusName;
	private final String timezone;

	public DeadlineReminderJob(ServiceTokenProvider tokenProvider, TenderClient tenderClient,
			NotificationClient notificationClient, ReminderSentRepository reminderRepository,
			@Value("${app.jobs.deadline-reminder.days-before:3}") int daysBefore,
			@Value("${app.jobs.open-status-name:OPEN}") String openStatusName,
			@Value("${app.jobs.timezone:Europe/Berlin}") String timezone) {
		this.tokenProvider = tokenProvider;
		this.tenderClient = tenderClient;
		this.notificationClient = notificationClient;
		this.reminderRepository = reminderRepository;
		this.daysBefore = daysBefore;
		this.openStatusName = openStatusName;
		this.timezone = timezone;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public String description() {
		return "E-mails the author of every open tender whose deadline is within the next " + daysBefore + " days";
	}

	@Override
	public String run() {
		String bearer = tokenProvider.bearer();
		TenderListResponse response = tenderClient.listTenders(bearer);
		List<TenderDTO> tenders = response == null || response.getPayload() == null ? List.of() : response.getPayload();

		LocalDate today = LocalDate.now(ZoneId.of(timezone));
		LocalDate limit = today.plusDays(daysBefore);
		int due = 0, sent = 0, already = 0, noRecipient = 0, failed = 0;

		for (TenderDTO t : tenders) {
			LocalDate deadline = t.getDeadlineDate();
			if (deadline == null || deadline.isBefore(today) || deadline.isAfter(limit)) {
				continue;
			}
			if (t.getStatusLabel() == null || !openStatusName.equalsIgnoreCase(t.getStatusLabel())) {
				continue;
			}
			due++;
			String recipient = t.getCreatedBy();
			if (recipient == null || !recipient.contains("@")) {
				noRecipient++;
				continue;
			}
			if (reminderRepository.existsByTenderIdAndDeadlineDate(t.getId(), deadline)) {
				already++;
				continue;
			}
			try {
				notificationClient.send(bearer, new NotificationRequest(recipient,
						"Deadline approaching: " + t.getTitle(),
						"The tender \"" + t.getTitle() + "\" (" + t.getReference() + ") closes on " + deadline + ".",
						"DEADLINE_REMINDER"));
				reminderRepository.save(ReminderSent.builder().tenderId(t.getId()).deadlineDate(deadline).recipientEmail(recipient).build());
				sent++;
			} catch (Exception e) {
				failed++;
				log.warn("Reminder for tender {} failed: {}", t.getId(), e.getMessage());
			}
		}
		return "tenders=" + tenders.size() + ", due=" + due + ", sent=" + sent + ", alreadyNotified=" + already
				+ ", noRecipient=" + noRecipient + ", failed=" + failed;
	}
}
