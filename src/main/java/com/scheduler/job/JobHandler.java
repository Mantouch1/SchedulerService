package com.scheduler.job;

/** A job the scheduler can run. Add a new @Component implementing this interface to add a new job. */
public interface JobHandler {

	String name();

	String description();

	/** Does the work and returns a one-line summary (stored in the run history). */
	String run() throws Exception;
}
