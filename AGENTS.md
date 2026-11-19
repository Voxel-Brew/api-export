# Repository Guidelines

## Project Structure & Module Organization

This repository contains the standalone VoxelBrew Export API. Public Java contracts live in
`src/main/java/dev/voxelbrew/api/exporter`. Version metadata belongs in `src/main/resources`. Unit tests use
the nonstandard source root `src/test/test/java`; reserve `src/test/integrationTest` and
`src/test/e2eTest` for their corresponding Gradle suites. Build logic is in `build.gradle.kts` and
`gradle/*.gradle.kts`; SpotBugs configuration comes from the BOM checkout.

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

Tests use JUnit 5 for execution and AssertJ for assertions. Prefer `assertThat` and
`assertThatThrownBy` over JUnit assertion methods, and assert observable behavior rather than
implementation details. Name test classes `*Test` and methods after observable behavior, for example
`unsupportedDialogsAndDuplicateHandlersFailExplicitly`. Add unit coverage for contract behavior and
integration coverage for service discovery or external consumption. No numeric coverage threshold
is configured; test success, cancellation, invalid input, and failure propagation.

## Pre-commit SpotBugs

Before committing, run `./gradlew spotbugsMain` from this API repository or
`./gradlew :export:spotbugsMain` from the composite workspace.
Use `.\gradlew.bat` in place of `./gradlew` on Windows.

## Commit & Pull Request Guidelines

Recent commits use short, imperative subjects, optionally prefixed by scope (`docs:`, `build:`,
`ci:`). Keep commits focused. Pull requests should explain API and compatibility impact, link the
relevant issue, list verification commands, and update README examples when contracts change.
Screenshots are only needed for documentation or rendered-output changes. Never commit generated
`build/` output, credentials, signing material, or local plugin artifacts.

## Lombok in Java code

Use Lombok where it removes routine Java boilerplate without obscuring a public contract. Apply these annotations in Java-bearing repositories and modules; the website has no Java production code.

- `@Getter` / `@Setter`: Generate straightforward accessors for fields that should be exposed or mutable. Prefer field-level annotations when only some fields qualify; retain custom accessors that enforce invariants or make defensive copies.
- `@RequiredArgsConstructor` / `@NoArgsConstructor`: Generate constructors for required final or `@NonNull` fields, or a no-argument constructor when a framework or serializer needs one. Keep explicit constructors when they validate, transform, or document special initialization behavior.
- `@NonNull`: Mark required fields, parameters, and other supported elements to express and enforce non-null inputs. Replace equivalent `Objects.requireNonNull` checks in those places; keep explicit checks when they provide distinct behavior or messages.
- `@ToString`: Generate useful diagnostic string representations for value or state classes. Exclude secrets, large graphs, and recursive relationships.
- `@Data`: Use for mutable data holders that genuinely need getters, setters, equality, hashing, and string representation together. Prefer narrower annotations when only some behavior is appropriate.
- `@Value`: Use for immutable value types when its generated constructor, accessors, equality, hashing, and string representation fit the contract. Preserve defensive copies for mutable components.
- `@EqualsAndHashCode`: Generate value equality for classes whose identity is defined by selected fields. Choose included fields and superclass handling deliberately; avoid mutable fields that would break hash collections.
- `@Slf4j`: Add a logger to classes that emit operational diagnostics. Use parameterized logging and keep credentials and other sensitive data out of logs.
- `@Builder`: Use for objects with many optional or named construction inputs when it improves call-site clarity. Keep required-field validation and invariants explicit.
- `@UtilityClass`: Use for stateless collections of static helpers that must not be instantiated. Do not use it for classes that may need instances, inheritance, or dependency injection.

## AssertJ in Java tests

Use AssertJ (org.assertj:assertj-core) for assertions in Java unit, integration, and end-to-end tests. Keep JUnit Jupiter for test lifecycle, parameterization, and execution. Declare AssertJ without a version in test dependencies; the BOM manages its version. Prefer ssertThat, ssertThatThrownBy, and ssertThatCode with descriptive failure messages and appropriate numeric tolerances. Do not add new JUnit Assertions calls.
