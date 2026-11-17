package dev.voxelbrew.api.exporter.component;

import lombok.NonNull;

/** A single-line editable value. */
public record TextField(
        @NonNull String id, @NonNull String label, @NonNull String value) implements ExportDialogInput {}
