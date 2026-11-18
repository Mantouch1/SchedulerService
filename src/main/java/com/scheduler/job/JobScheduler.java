package com.scheduler.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.scheduler.service.JobRunner;

import lombok.RequiredArgsConstructor;

/** The cron triggers. Each method only asks the runner to start a job (which checks enabled / already running). */
@Component
@RequiredArgsConstructor
public class JobScheduler {

	private final JobRunner runner;

	@Scheduled(cron = "${app.jobs.deadline-reminder.cron:0 0 8 * * *}", zone = "${app.jobs.timezone:Europe/Berlin}")
	public void deadlineReminder() {
		runner.runScheduled(DeadlineReminderJob.NAME);
	}
}
