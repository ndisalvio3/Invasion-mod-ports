# Invasion

So you think your base is tough, do you?

This repository contains the source for the Invasion mod ported to modern tooling.

## Quick Start
Use Java 21 and the Gradle wrapper:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build
```

## Work Tracking & Documentation
- Jira: https://nicholas.atlassian.net (project `DEV`).
- Confluence: space list unavailable via API (404); confirm access if needed.
- Track all work in Jira; do not recreate TODO files.
- Capture decisions, designs, and implementation notes in Confluence.
- Keep tickets current with professional status updates and clear acceptance criteria.

## Repository Guidelines
- Keep commits focused and avoid modifying `AGENTS.md` except to update instructions.
- Prefer small, focused changes and follow existing naming/style conventions.
- Document behavior changes in Jira and Confluence.
- If the build fails due to missing dependencies or network restrictions, note it in your PR summary.

## Testing
- `./gradlew build` compiles the code and runs any tests.

## Validation
- Playtest full cycle (night spawns, wave timing, spawn points, difficulty scaling).
- Verify damage values, drops, mob behaviors, burning in daylight.
- Confirm client/server compatibility, networking stability.
- Run full client + dedicated server test on NeoForge 1.21.5.
