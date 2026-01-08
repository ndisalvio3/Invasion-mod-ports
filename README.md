# Invasion

So you think your base is tough, do you?

This repository contains the source for the Invasion mod, ported to modern tooling.

## Build & Test
Use Java 21 and the Gradle wrapper:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build
```

- `./gradlew build` compiles the code and runs any tests.
- Always run `./gradlew build` after code changes before reporting results.
- Build must pass before committing or moving a Jira issue from In Progress to Completed.
- Scope guard: if the build fails for any reason, troubleshoot and fix it before committing or completing the issue; do not defer or leave failures unresolved.

## Work Tracking & Documentation
- Jira: https://nicholas.atlassian.net (project `DEV`).
- Do not use Confluence.
- At the start of a new chat, check Jira immediately and begin work.
- Prioritize issues in In Progress over To Do.
- If there are any issues in To Do or In Progress, work on them first without asking for direction.
- Only ask for guidance when there are no open issues to work on.
- Move the issue to In Progress as soon as work begins.
- Track all work in Jira; do not recreate TODO files.
- Use sub-tasks as needed.
- Track all work on a specific issue via sub-tasks.
- Use issue comments as needed for clarifications and updates.
- Keep tickets current with professional status updates and clear acceptance criteria.

## Repository Guidelines
- Keep commits focused and avoid modifying `AGENTS.md` except to update instructions.
- Prefer small, focused changes and follow existing naming/style conventions.
- Document behavior changes in Jira.
- If the build fails, fix it before proceeding; do not defer or leave it unresolved.
- Each commit must be fully working and include a detailed commit message that stays within the scope of the issue worked on.
- Whenever a Markdown file is modified, beautify it before continuing.

## Validation
- Playtest full cycle (night spawns, wave timing, spawn points, difficulty scaling).
- Verify damage values, drops, mob behaviors, burning in daylight.
- Confirm client/server compatibility, networking stability.
- Run full client + dedicated server test on NeoForge 1.21.5.

## Completion Checklist
- Run Build & Test and Validation before moving an issue to Completed.
- Upon commit, move the issue from In Progress to Completed.
- Commit and push each issue's changes before starting the next task.
