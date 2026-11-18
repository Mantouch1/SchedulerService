package com.scheduler.job;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.scheduler.client.NotificationClient;
import com.scheduler.client.NotificationRequest;
import com.scheduler.client.ServiceTokenProvider;
import com.scheduler.client.TenderClient;
import com.scheduler.client.TenderDTO;
import com.scheduler.client.TenderListResponse;
import com.scheduler.entity.ReminderSent;
import com.scheduler.repository.ReminderSentRepository;

class DeadlineReminderJobTest {

	private final ServiceTokenProvider tokens = mock(ServiceTokenProvider.class);
	private final TenderClient tenderClient = mock(TenderClient.class);
	private final NotificationClient notificationClient = mock(NotificationClient.class);
	private final ReminderSentRepository reminders = mock(ReminderSentRepository.class);
	private final LocalDate today = LocalDate.now(ZoneId.of("Europe/Berlin"));
	private DeadlineReminderJob job;

	@BeforeEach
	void setUp() {
		job = new DeadlineReminderJob(tokens, tenderClient, notificationClient, reminders, 3, "OPEN", "Europe/Berlin");
		when(tokens.bearer()).thenReturn("Bearer t");
	}

	private TenderDTO tender(long id, String status, int daysToDeadline, String createdBy) {
		TenderDTO t = new TenderDTO();
		t.setId(id);
		t.setTitle("Tender " + id);
		t.setReference("REF-" + id);
		t.setStatusLabel(status);
		t.setDeadlineDate(today.plusDays(daysToDeadline));
		t.setCreatedBy(createdBy);
		return t;
	}

	private void tendersAre(TenderDTO... tenders) {
		TenderListResponse r = new TenderListResponse();
		r.setPayload(List.of(tenders));
		when(tenderClient.listTenders("Bearer t")).thenReturn(r);
	}

	@Test
	void remindsOnlyOpenTendersInTheWindowNotYetNotifiedWithAnEmailAuthor() {
		tendersAre(
				tender(1, "OPEN", 2, "a@x.de"),      // -> reminder
				tender(2, "OPEN", 10, "b@x.de"),     // deadline too far
				tender(3, "CLOSED", 1, "c@x.de"),    // not open
				tender(4, "open", 1, "d@x.de"),      // already notified
				tender(5, "OPEN", 1, "admin"));      // author is not an e-mail
		when(reminders.existsByTenderIdAndDeadlineDate(eq(4L), any())).thenReturn(true);

		String result = job.run();

		verify(notificationClient, times(1)).send(eq("Bearer t"), any(NotificationRequest.class));
		verify(reminders, times(1)).save(any(ReminderSent.class));
		assertTrue(result.contains("sent=1"), result);
		assertTrue(result.contains("alreadyNotified=1"), result);
		assertTrue(result.contains("noRecipient=1"), result);
	}

	@Test
	void aFailedNotificationIsCountedAndNotRecorded() {
		tendersAre(tender(1, "OPEN", 1, "a@x.de"));
		doThrow(new RuntimeException("boom")).when(notificationClient).send(any(), any());

		String result = job.run();

		verify(reminders, never()).save(any());
		assertTrue(result.contains("failed=1"), result);
	}

	@Test
	void emptyTenderListIsFine() {
		when(tenderClient.listTenders("Bearer t")).thenReturn(new TenderListResponse());
		assertTrue(job.run().contains("tenders=0"));
	}
}
