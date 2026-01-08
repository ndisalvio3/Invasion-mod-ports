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

## Workflow (follow for every session, including when the user says "begin")
- Check Jira immediately and start work on the highest-priority open issue (In Progress first, then To Do) without asking for direction.
- Move the issue to In Progress as soon as work begins.
- Implement changes following existing naming/style conventions; keep commits focused; avoid modifying `AGENTS.md` except to update instructions.
- Run `./gradlew build` after changes; if it fails, fix it before proceeding.
- Before finishing: comment on the Jira issue, commit and push the changes with a detailed, in-scope message, and transition the issue to Done.
- Only reply `done` to the user after the build passes, the issue is updated, and changes are pushed.
