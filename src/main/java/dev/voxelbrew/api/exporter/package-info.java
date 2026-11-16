/**
 * Public, frontend-neutral contracts for implementing and discovering VoxelBrew exporters.
 *
 * <p>Start with an {@link dev.voxelbrew.api.exporter.ExporterProvider}: expose stable metadata and
 * create an {@link dev.voxelbrew.api.exporter.Exporter}. Register
 * the provider's class name in
 * {@code META-INF/services/dev.voxelbrew.api.exporter.ExporterProvider} in an independently built
 * plugin JAR. The host supplies API classes at runtime; do not package copies into that JAR.
 *
 * <p>An export receives a document and a host window. The host chooses the calling thread and
 * ensures dialog display runs on its interaction thread. The exporter performs conversion and
 * publication before returning. For example:
 *
 * <pre>{@code
 * var destination = window.show(new FileExportDialog("Export report", "txt"));
 * if (destination.isEmpty()) return java.util.Optional.empty();
 * publish(document, destination.get());
 * return destination;
 * }</pre>
 *
 * <p>Here {@code publish} is an implementation-owned operation that validates the output, honors
 * overwrite consent and returns only after safe publication. Checked failures propagate to the caller.
 * A cloud exporter can instead define an {@link dev.voxelbrew.api.exporter.ExportDialog}
 * for account/project settings and return those settings after upload. Every frontend using that request
 * needs a corresponding handler; provider discovery does not generate user interfaces.
 *
 * <p>The API exposes shared document values, not native geometry handles or an evaluation service.
 * Exporters requiring evaluated solids or meshes must obtain those through their implementation
 * dependencies. Keep frontend types and native pointers out of custom public interaction contracts.
 * The module README contains a complete provider, packaging instructions and validation guidance.
 */
package dev.voxelbrew.api.exporter;
