package dev.voxelbrew.api.exporter;

import dev.voxelbrew.api.model.CadDocument;
import java.util.Optional;

/**
 * Exports a CAD document to an implementation-defined destination, such as a local file or a cloud
 * service. Implementations own settings collection, model conversion, validation and publication;
 * the host does not supply an output stream or assume that the result is a file.
 *
 * <p>The exporter shows its dialog through the supplied window, then runs publication after
 * confirmation. Native evaluation is not provided by this interface: an exporter needing evaluated
 * geometry must supply its own evaluation dependencies. Ordinary exports must not mutate the input
 * document or its save metadata. Geometry exporters must reject incomplete or failed evaluations
 * and define their selection, units, coordinate space and treatment of supports explicitly.
 *
 * @param <R> accepted dialog settings type
 * @see ExporterProvider
 */
@FunctionalInterface
public interface Exporter<R> {
    /**
     * Shows the interaction and completes an export after confirmation.
     *
     * <p>The host chooses the calling thread and arranges for
     * {@link ExportWindow#show(ExportDialog)} to run on its interaction thread. Return an empty
     * {@link Optional} without starting publication when the dialog is dismissed.
     * Propagate other failures to the caller.
     *
     * @param document document to export; must not be {@code null}
     * @param window host for exporter-defined interactions
     * @return accepted settings after successful publication, or empty when cancelled
     * @throws Exception if conversion or publication fails
     */
    Optional<R> export(CadDocument document, ExportWindow window) throws Exception;
}
