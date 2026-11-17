package dev.voxelbrew.api.exporter.component;

import lombok.NonNull;

/** Read-only text. */
public record Label(@NonNull String text) implements ExportDialogComponent {}
