package dev.voxelbrew.api.exporter.component;

import static java.util.Objects.requireNonNull;

/** A single-line editable value. */
public record TextField(String id, String label, String value) implements ExportDialogInput {
    public TextField {
        requireNonNull(id);
        requireNonNull(label);
        requireNonNull(value);
    }
}
