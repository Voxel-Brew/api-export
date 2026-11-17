package dev.voxelbrew.api.exporter.component;

import lombok.NonNull;

/** A boolean option. */
public record CheckBox(@NonNull String id, @NonNull String label, boolean selected) implements ExportDialogInput {}
