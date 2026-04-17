# Selenium Automation Framework (Java + TestNG)

Production-grade Selenium framework with:
- Java (LTS), Maven, TestNG
- Page Object Model + PageFactory
- WebDriverManager (Chrome, Edge)
- Explicit waits only (`WebDriverWait`) — no `Thread.sleep`
- ExtentReports + screenshots on failures
- Log4j2 logging
- Retry analyzer + parallel execution via `testng.xml`
- Environment support (`QA`, `DEV`) via `config.properties`

## Prerequisites
- JDK 21+ installed and on `PATH`
- Maven installed and on `PATH`
- Chrome or Edge installed

## Run
`mvn clean test`

## Codespaces / Linux one-command run
`bash scripts/run-ci.sh`

This runner defaults to CI-safe settings:
- `headless=true`
- `ci=true`
- `video.recording.enabled=false`
- uses `BROWSER_BINARY_PATH` when provided

## GitHub Actions schedule
- Daily at `6:00 PM IST`
- Workflow cron: `30 12 * * *` (GitHub Actions cron is UTC, so `12:30 UTC = 6:00 PM IST`)
- After each run, the workflow updates:
  - this repo: `reports/extent-report.html`
  - report repo: `dailyreports-/solwerDailyReports/extent-report.html`

## GitHub Actions secret
- Add repository secret `DAILY_REPORTS_PAT` in `solwerbetawebdailyrun`
- Use a GitHub PAT that has write access to `ArunvLB/dailyreports-`

## Key paths
- Config: `src/test/resources/config.properties`
- Suite: `src/test/resources/testng.xml`
- Extent report: `reports/extent-report.html`
- Logs: `reports/framework.log`
- Screenshots: `screenshots/`
