# Repository Guidelines

## Project Structure & Module Organization

- `src/main/java/` — Java sources (desktop Swing UI under `es/jklabs/gui/`, S3/GitHub Releases logic under
  `es/jklabs/utilidades/`).
- `src/main/resources/` — assets and configuration:
    - `i18n/` (`*.properties`) for user-facing strings.
    - `img/` icons used by the UI.
  - `packaging/linux/` resources for `jpackage` Linux installers (`.desktop`, `metainfo`, maintainer scripts, icon).
    - `json/` bundled JSON (treat as sensitive; see Security).
- Runtime logs (outside repo): Linux `~/.local/share/BeyondDeploy/logs/`, macOS
  `~/Library/Application Support/BeyondDeploy/logs/`, Windows `%LOCALAPPDATA%\\BeyondDeploy\\logs\\`.
- Gradle build files: `build.gradle`, `settings.gradle`, wrapper in `gradle/` + `gradlew`.

## Build, Test, and Development Commands

- `./gradlew clean build` — compiles and produces the application distribution.
- `./gradlew run` — runs the app locally using `application.mainClass` (`es.jklabs.BeyondDeploy`).
- `./gradlew test` — runs unit tests (JUnit 4). Add tests under `src/test/java/` as the project grows.
- `./gradlew distZip` — builds a distributable ZIP (see `build/distributions/`).
- `./gradlew -PinstallerType=deb -PinstallerIcon=src/main/resources/packaging/linux/beyonddeploy.png jpackage` — builds
  Linux `.deb` installer in `build/jpackage/`.
- `./gradlew -PinstallerType=msi jpackage` — builds Windows `.msi` installer (run on Windows runner/host).
- `./gradlew -PinstallerType=dmg jpackage` — builds macOS `.dmg` installer (run on macOS runner/host).

Note: the wrapper targets Gradle 9; use JDK 21+ to run Gradle tasks reliably.

## Coding Style & Naming Conventions

- Java: 4-space indentation, braces on the same line, keep methods small and focused.
- Packages follow `es.jklabs.*`; keep new code in the closest existing package (UI vs. utilities vs. models).
- Resources: add new i18n keys to `src/main/resources/i18n/mensajes.properties` (and errors to `errores.properties`).
- S3 integration uses AWS SDK for Java 2.x (`software.amazon.awssdk.*`) and `S3TransferManager`; do not add new AWS SDK
  v1 (`com.amazonaws.*`) usage.
- Desktop notifications are centralized in `es.jklabs.gui.utilidades.Growls` via `two-slices`; do not call
  `SystemTray`, `TrayIcon`, or `notify-send` directly.

## Testing Guidelines

- Framework: JUnit 4 (`*Test` naming, placed in `src/test/java/` mirroring package structure).
- Prefer fast unit tests for utilities/models; avoid tests that require real AWS/GitHub credentials or network access.

## Commit & Pull Request Guidelines

- Commit messages in history are short, imperative, and often Spanish (e.g., “Actualización de dependencias”); follow
  that style and mention the main area touched (`gui`, `utilidades`, `resources`).
- PRs should include: what/why, how to test (`./gradlew build`), and screenshots for UI changes.

## Security & Configuration Tips

- Do not commit credentials. If you must change `src/main/resources/json/*.json`, coordinate key rotation and document
  the impact in the PR.
- For automated `.deb` publication to APT, configure `APT_REPO_DISPATCH_TOKEN` secret (repo dispatch permission on apt
  repo)
  and optional vars `APT_REPO_OWNER` / `APT_REPO_NAME`.
