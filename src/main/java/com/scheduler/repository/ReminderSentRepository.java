package com.scheduler.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scheduler.entity.ReminderSent;

public interface ReminderSentRepository extends JpaRepository<ReminderSent, Long> {

	boolean existsByTenderIdAndDeadlineDate(Long tenderId, LocalDate deadlineDate);
}
