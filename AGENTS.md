# Repository Guidelines

## Project Structure & Module Organization

This repository contains the standalone VoxelBrew Export API. Public Java contracts live in
`src/main/java/dev/voxelbrew/exporter/api`; shared export values are in the parent
`dev.voxelbrew.exporter` package. Version metadata belongs in `src/main/resources`. Unit tests use
the nonstandard source root `src/test/test/java`; reserve `src/test/integrationTest` and
`src/test/e2eTest` for their corresponding Gradle suites. Build logic is in `build.gradle.kts` and
`gradle/*.gradle.kts`; quality configuration is under `config/quality`.

Keep this module API-only. It may depend on `dev.voxelbrew.api:model`, but must not expose JavaFX,
CLI, native engine, Protobuf, or dependency-injection implementation types. Exporter
implementations belong in the main VoxelBrew application repository.

## Build, Test, and Development Commands

Java 21 and the committed Gradle wrapper are required.

- `./gradlew test` runs the JUnit unit suite.
- `./gradlew check` runs unit and integration tests, Spotless checks, SpotBugs, dependency-boundary
  validation, and release-script tests.
- `./gradlew e2eTest` runs the separate end-to-end suite when one is present.
- `./gradlew spotlessApply` formats Java, Kotlin Gradle scripts, Markdown, YAML, and text files.
- `./gradlew publishToMavenLocal -PuseMavenLocal=true` publishes a local artifact for consumer
  testing after the BOM and model API are available locally.

On Windows, replace `./gradlew` with `gradlew.bat`.

## Coding Style & Naming Conventions

Spotless enforces Palantir Java Format and ktfmt; do not hand-align code against those tools. Use
UTF-8, four-space Java indentation, `UpperCamelCase` types, `lowerCamelCase` members, and lowercase
package names. Compilation uses `-Xlint:all -Werror`, so resolve every warning. Preserve public API
compatibility deliberately and document behavioral contracts with concise Javadoc.

## Testing Guidelines

Tests use JUnit 5. Name test classes `*Test` and methods after observable behavior, for example
`unsupportedDialogsAndDuplicateHandlersFailExplicitly`. Add unit coverage for contract behavior and
integration coverage for service discovery or external consumption. No numeric coverage threshold
is configured; test success, cancellation, invalid input, and failure propagation.

## Commit & Pull Request Guidelines

Recent commits use short, imperative subjects, optionally prefixed by scope (`docs:`, `build:`,
`ci:`). Keep commits focused. Pull requests should explain API and compatibility impact, link the
relevant issue, list verification commands, and update README examples when contracts change.
Screenshots are only needed for documentation or rendered-output changes. Never commit generated
`build/` output, credentials, signing material, or local plugin artifacts.
