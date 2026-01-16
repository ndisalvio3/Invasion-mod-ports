# Invasion

So you think your base is tough, do you?

This repository contains the source for the Invasion mod, ported to modern tooling.

Use this Repository for reference including the specific branch: https://github.com/UnstoppableN/Invasion-mod/tree/1.7.2

## Build & Test
Use Java 21 and the Gradle wrapper:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build
```

## Workflow (follow for every session, including when the user says "begin")
- Check `TODO.md` immediately and start work on the highest-priority open item (In Progress first, then To Do) without asking for direction.
- Move the item to In Progress in `TODO.md` as soon as work begins.
- Implement changes following existing naming/style conventions; keep commits focused; avoid modifying `AGENTS.md` except to update instructions.
- Run `./gradlew build` after changes; if it fails, fix it before proceeding.
- Before finishing: commit and push the changes with a detailed, in-scope message, and transition the item to Done in `TODO.md`.
- Only reply `done` to the user after the build passes, `TODO.md` is updated, and changes are pushed.
