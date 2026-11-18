# SchedulerService

Runs the platform's scheduled jobs and keeps their history in its own PostgreSQL database (`scheduler_db`). Port **8087**.

## Job: DEADLINE_REMINDER
Every day at 08:00 (Europe/Berlin, `REMINDER_CRON`): reads the tenders from TenderFlow (Feign), and for every **open** tender
whose deadline is within the next 3 days (`REMINDER_DAYS_BEFORE`) asks NotificationService (Feign) to e-mail its author (`createdBy`,
must be an e-mail). One reminder per tender and deadline (table `reminders_sent`). Each run is stored in `job_runs`
and published as an audit event (`JOB_SUCCESS` / `JOB_FAILED`) to ReportingService.
`OPEN_STATUS_NAME` must match the name of the open status in TenderFlow.

## Endpoints (JWT, permission SCHEDULER_MANAGE)
| Method | URL | |
|---|---|---|
| GET | /api/jobs | list jobs (enabled, last run) |
| PATCH | /api/jobs/{name}/enabled?value=true\|false | enable / disable the cron trigger |
| POST | /api/jobs/{name}/run | run now (works even if disabled, 409 if already running) |
| GET | /api/jobs/runs?name=&page=&size= | run history |

Add a job: create a `@Component` implementing `JobHandler` and a `@Scheduled` method in `JobScheduler`.
Limitation: with several replicas every instance would run the job; the "already notified" table prevents duplicate e-mails,
but a distributed lock (ShedLock) is the proper fix.

## Run
Env vars: `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET` (same as IdentityService), `SERVICE_ACCOUNT_EMAIL`, `SERVICE_ACCOUNT_PASSWORD`
(the technical account created by IdentityService), plus the services must be running (IdentityService, TenderFlow, NotificationService).
Docker: `docker network create platform-net` (once), `copy .env.example .env`, `docker compose up --build`.
