package dev.voxelbrew.api.exporter.component;

import java.util.List;

/** Arranges child components in a row. */
public record HorizontalLayout(List<ExportDialogComponent> children) implements ExportDialogLayout {
    public HorizontalLayout(ExportDialogComponent... children) {
        this(List.of(children));
    }

    public HorizontalLayout {
        children = List.copyOf(children);
    }
}
