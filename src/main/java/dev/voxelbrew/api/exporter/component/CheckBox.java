package dev.voxelbrew.api.exporter.component;

import static java.util.Objects.requireNonNull;

/** A boolean option. */
public record CheckBox(String id, String label, boolean selected) implements ExportDialogInput {
    public CheckBox {
        requireNonNull(id);
        requireNonNull(label);
    }
}
