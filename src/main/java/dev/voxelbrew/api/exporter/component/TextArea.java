package dev.voxelbrew.api.exporter.component;

import lombok.NonNull;

/** A multi-line editable value. */
public record TextArea(
        @NonNull String id, @NonNull String label, @NonNull String value) implements ExportDialogInput {}
