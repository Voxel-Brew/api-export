package dev.voxelbrew.api.exporter.component;

import static java.util.Objects.requireNonNull;

/** A multi-line editable value. */
public record TextArea(String id, String label, String value) implements ExportDialogInput {
    public TextArea {
        requireNonNull(id);
        requireNonNull(label);
        requireNonNull(value);
    }
}
