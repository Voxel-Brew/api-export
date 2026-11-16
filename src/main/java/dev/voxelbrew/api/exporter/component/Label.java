package dev.voxelbrew.api.exporter.component;

import static java.util.Objects.requireNonNull;

/** Read-only text. */
public record Label(String text) implements ExportDialogComponent {
    public Label {
        requireNonNull(text);
    }
}
