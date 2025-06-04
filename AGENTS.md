# Repository Guidelines
This project uses Gradle to build a Minecraft mod. Ensure you are using **Java 8** and run

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew build
```

This will compile the code and run any tests.

If the build fails due to missing dependencies or network restrictions, note this in your PR summary.

Please ensure the repository remains tidy. Keep commits focused and avoid modifying AGENTS files except to update instructions. When submitting a PR, mention if the build fails due to missing dependencies or network issues.
