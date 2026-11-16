package dev.voxelbrew.api.exporter.component;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

/**
 * File chooser destination with a suggested extension and optional initial path.
 *
 * @param id input ID used by the host
 * @param extension suggested file extension
 * @param defaultPath initial path, or {@code null} to let the host choose or prompt
 */
public record FileDestination(String id, String extension, Path defaultPath) implements ExportDialogInput {
    /** Creates a destination input whose initial path is chosen by the host. */
    public FileDestination(String id, String extension) {
        this(id, extension, null);
    }

    public FileDestination {
        requireNonNull(id);
        requireNonNull(extension);
    }
}
