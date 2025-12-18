# Repository Guidelines

## Project Structure & Module Organization

- `src/main/java/` — Java sources (desktop Swing UI under `es/jklabs/gui/`, S3/Firebase logic under
  `es/jklabs/utilidades/`).
- `src/main/resources/` — assets and configuration:
    - `i18n/` (`*.properties`) for user-facing strings.
    - `img/` icons used by the UI.
    - `json/` bundled JSON (treat as sensitive; see Security).
- Gradle build files: `build.gradle`, `settings.gradle`, wrapper in `gradle/` + `gradlew`.

## Build, Test, and Development Commands

- `./gradlew clean build` — compiles and produces the application distribution.
- `./gradlew run` — runs the app locally using `application.mainClass` (`es.jklabs.BeyondDeploy`).
- `./gradlew test` — runs unit tests (JUnit 4). Add tests under `src/test/java/` as the project grows.
- `./gradlew distZip` — builds a distributable ZIP (see `build/distributions/`).

Note: the wrapper targets Gradle 9; use JDK 17+ to run Gradle tasks reliably.

## Coding Style & Naming Conventions

- Java: 4-space indentation, braces on the same line, keep methods small and focused.
- Packages follow `es.jklabs.*`; keep new code in the closest existing package (UI vs. utilities vs. models).
- Resources: add new i18n keys to `src/main/resources/i18n/mensajes.properties` (and errors to `errores.properties`).

## Testing Guidelines

- Framework: JUnit 4 (`*Test` naming, placed in `src/test/java/` mirroring package structure).
- Prefer fast unit tests for utilities/models; avoid tests that require real AWS/Firebase credentials.

## Commit & Pull Request Guidelines

- Commit messages in history are short, imperative, and often Spanish (e.g., “Actualización de dependencias”); follow
  that style and mention the main area touched (`gui`, `utilidades`, `resources`).
- PRs should include: what/why, how to test (`./gradlew build`), and screenshots for UI changes.

## Security & Configuration Tips

- Do not commit credentials. If you must change `src/main/resources/json/*.json`, coordinate key rotation and document
  the impact in the PR.
