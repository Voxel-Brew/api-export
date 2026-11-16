package dev.voxelbrew.api.exporter;

import dev.voxelbrew.api.exporter.component.CheckBox;
import dev.voxelbrew.api.exporter.component.ExportDialogLayout;
import dev.voxelbrew.api.exporter.component.FileDestination;
import dev.voxelbrew.api.exporter.component.VerticalLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;

/**
 * Standard request for a local output path and explicit overwrite consent.
 *
 * <p>Use this request when a destination is the only setting needed. Exporters needing additional
 * settings may define another {@link ExportDialog} type and arrange matching host handlers. This
 * request neither writes files nor reserves a destination: the exporter must validate the path,
 * honor overwrite consent even if another process creates a file later, and publish safely.
 */
@lombok.experimental.Accessors(fluent = true)
public final class FileExportDialog implements ExportDialog<FileExportDialog.Destination> {
    /**
     * Title shown by a file chooser or prompt.
     *
     * @return human-readable interaction title
     */
    @Getter
    private final String title;
    /**
     * Suggested filename extension and host filter, without a leading dot.
     *
     * @return nonempty ASCII alphanumeric extension, optionally compound; original case is preserved
     */
    @Getter
    private final String extension;

    protected final FileDestination destinationInput;
    protected final CheckBox overwriteInput;

    /**
     * Creates a file request; no filesystem access or dialog interaction occurs here.
     *
     * @param title     title for the host's file interaction
     * @param extension suggested extension without a leading dot, such as {@code vxb} or {@code gcode.3mf}
     * @throws NullPointerException     if the title is null
     * @throws IllegalArgumentException if the extension is null, empty or contains invalid segments
     */
    public FileExportDialog(String title, String extension) {
        this.title = Objects.requireNonNull(title, "title");
        if (extension == null || !extension.matches("[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*"))
            throw new IllegalArgumentException("Invalid extension");
        this.extension = extension;
        this.destinationInput = new FileDestination("destination", extension);
        this.overwriteInput = new CheckBox("overwrite", "Overwrite", false);
    }

    @Override
    public ExportDialogLayout layout() {
        return new VerticalLayout(destinationInput, overwriteInput);
    }

    @Override
    public Map<String, String> validate(Map<String, String> values) {
        var errors = new LinkedHashMap<String, String>();

        var overwrite = values.getOrDefault(overwriteInput.id(), Boolean.toString(overwriteInput.selected()));
        var path = values.get(destinationInput.id());

        if (path == null || path.isBlank()) {
            errors.put(destinationInput.id(), "Choose a destination");
        } else {
            try {
                var destination = Path.of(path);
                var parent = destination.toAbsolutePath().normalize().getParent();
                if (Files.isDirectory(destination)) {
                    errors.put(destinationInput.id(), "Destination must be a file, not a directory");
                } else if (parent == null || !Files.exists(parent)) {
                    errors.put(destinationInput.id(), "Destination parent directory does not exist");
                } else if (!Files.isWritable(parent)) {
                    errors.put(destinationInput.id(), "Destination parent directory is not writable");
                } else if ("false".equals(overwrite) && Files.exists(destination)) {
                    errors.put(destinationInput.id(), "Destination already exists; enable Overwrite");
                }
            } catch (java.nio.file.InvalidPathException failure) {
                errors.put(destinationInput.id(), "Invalid destination path: " + failure.getReason());
            }
        }
        if (!"true".equals(overwrite) && !"false".equals(overwrite)) {
            errors.put(overwriteInput.id(), "Overwrite must be true or false");
        }

        return Map.copyOf(errors);
    }

    @Override
    public Destination buildSettings(Map<String, String> values) {
        var errors = validate(values);
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors.values()));
        var path = values.get(destinationInput.id());
        var overwrite = values.getOrDefault(overwriteInput.id(), Boolean.toString(overwriteInput.selected()));
        return new Destination(Path.of(path), Boolean.parseBoolean(overwrite));
    }

    /**
     * Accepted file destination and replacement policy.
     *
     * <p>The path is retained as supplied, without normalization, extension insertion or existence
     * checks. Consent to overwrite permits replacement; it does not require the file to exist.
     * Exporters remain responsible for safe publication and preserving existing data on failure.
     *
     * @param path      output path, which may be relative
     * @param overwrite whether replacing an existing output is authorized
     */
    public record Destination(Path path, boolean overwrite) {
        /**
         * Creates destination settings without accessing the filesystem.
         *
         * @param path      output path, retained unchanged
         * @param overwrite whether replacement is authorized
         * @throws NullPointerException if the path is null
         */
        public Destination {
            Objects.requireNonNull(path, "path");
        }
    }

    /**
     * Creates a headless host that always confirms this destination for file requests.
     *
     * <p>Useful for parsed CLI arguments and tests. The host supports only {@link FileExportDialog};
     * other request types fail explicitly. It does not show a prompt or append an extension.
     * Validation rejects an existing destination without overwrite consent.
     *
     * @param path      destination to return for every file request
     * @param overwrite explicit replacement policy
     * @return host that records and returns the supplied destination on confirmation
     * @throws NullPointerException if the path is null
     */
    public static ExportWindow destination(Path path, boolean overwrite) {
        var selected = new Destination(path, overwrite);
        return new ExportWindow() {
            @Override
            public <R> Optional<R> show(ExportDialog<R> dialog) {
                if (!(dialog instanceof FileExportDialog fileDialog))
                    throw new UnsupportedOperationException(
                            "Unsupported export dialog: " + dialog.getClass().getName());
                var values = Map.of(
                        fileDialog.destinationInput.id(), selected.path().toString(),
                        fileDialog.overwriteInput.id(), Boolean.toString(selected.overwrite()));
                var errors = dialog.validate(values);
                if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors.values()));
                return Optional.of(dialog.buildSettings(values));
            }
        };
    }
}
