package dev.voxelbrew.api.exporter;

import dev.voxelbrew.api.i18n.TranslationService;
import dev.voxelbrew.api.model.vui.CheckBox;
import dev.voxelbrew.api.model.vui.FileDestination;
import dev.voxelbrew.api.model.vui.Layout;
import dev.voxelbrew.api.model.vui.VerticalLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Standard request for a local output path and explicit overwrite consent.
 *
 * <p>Use this request when a destination is the only setting needed. Exporters needing additional
 * settings may define another {@link ExportDialog} type and arrange matching host handlers. This
 * request neither writes files nor reserves a destination: the exporter must validate the path,
 * honor overwrite consent even if another process creates a file later, and publish safely.
 */
@Slf4j
@lombok.experimental.Accessors(fluent = true)
public final class FileExportDialog implements ExportDialog<FileExportDialog.Destination> {
    private final TranslationService translations;

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

    @Getter
    private final FileDestination destinationInput;

    @Getter
    private final CheckBox overwriteInput;

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
     * Creates a file request; no filesystem access or dialog interaction occurs here.
     *
     * @param title     title for the host's file interaction
     * @param extension suggested extension without a leading dot, such as {@code vxb} or {@code gcode.3mf}
     * @throws NullPointerException     if the title is null
     * @throws IllegalArgumentException if the extension is null, empty or contains invalid segments
     */
    public FileExportDialog(@lombok.NonNull String title, String extension) {
        this(
                title,
                extension,
                ServiceLoader.load(TranslationService.class, TranslationService.class.getClassLoader())
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("A TranslationService provider is required")));
    }

    /** Creates a file request using the host's translation service. */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "The dialog shares the host service to observe its current translations.")
    public FileExportDialog(
            @lombok.NonNull String title, String extension, @lombok.NonNull TranslationService translations) {
        this.translations = translations;
        this.title = title;
        if (extension == null || !extension.matches("[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*"))
            throw new IllegalArgumentException("Invalid extension");
        this.extension = extension;
        this.destinationInput = new FileDestination("destination", extension);
        this.overwriteInput = new CheckBox("overwrite", message("api.export.overwrite"), false);
    }

    private String message(String key, Object... arguments) {
        return translations.translate(key, arguments).orElseGet(() -> {
            log.warn("Missing translation for key {}", key);
            return key;
        });
    }

    @Override
    public Layout layout() {
        return new VerticalLayout(destinationInput, overwriteInput);
    }

    @Override
    public Map<String, String> validate(Map<String, String> values) {
        var errors = new LinkedHashMap<String, String>();

        var overwrite = values.getOrDefault(overwriteInput.id(), Boolean.toString(overwriteInput.selected()));
        var path = values.get(destinationInput.id());

        if (path == null || path.isBlank()) {
            errors.put(destinationInput.id(), message("api.export.destination.choose"));
        } else {
            try {
                var destination = Path.of(path);
                var parent = destination.toAbsolutePath().normalize().getParent();
                if (Files.isDirectory(destination)) {
                    errors.put(destinationInput.id(), message("api.export.destination.file"));
                } else if (parent == null || !Files.exists(parent)) {
                    errors.put(destinationInput.id(), message("api.export.destination.parent.missing"));
                } else if (!Files.isDirectory(parent)) {
                    errors.put(destinationInput.id(), message("api.export.destination.parent.directory"));
                } else if (!Files.isWritable(parent)) {
                    errors.put(destinationInput.id(), message("api.export.destination.parent.writable"));
                } else if ("false".equals(overwrite) && Files.exists(destination)) {
                    errors.put(destinationInput.id(), message("api.export.destination.exists"));
                }
            } catch (java.nio.file.InvalidPathException failure) {
                errors.put(destinationInput.id(), message("api.export.destination.invalid", failure.getReason()));
            }
        }
        if (!"true".equals(overwrite) && !"false".equals(overwrite)) {
            errors.put(overwriteInput.id(), message("api.export.overwrite.boolean"));
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
}
