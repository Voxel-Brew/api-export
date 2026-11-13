# Export API instructions

- Own only public exporter contracts, read-only export views, descriptors, typed options and writer ports.
- Depend only on api/model for CAD/manufacturing values; never depend on AGPL application modules. Keep STL/3MF formats, registries, slicer execution and file publication in VoxelBrew/exporter.
- Keep native handles, generated Protobuf, JavaFX, CLI parsing and DI annotations out of contracts.
- Version the SPI explicitly; verify external consumption through the independent provider fixture.
- Do not bundle API classes inside plugin JARs. Document changes in the application repository docs/file-io.md.

## Standalone repository

- Use Java 21 and the committed Gradle wrapper. Preserve the MIT license and public Java packages.
- Run `./gradlew spotlessApply` (Windows: `.\gradlew.bat spotlessApply`) before every commit, then review changes.
- Run `check` and validate Maven POM dependencies when changing publication configuration.
- Never add application-module dependencies or commit generated output or credentials.
