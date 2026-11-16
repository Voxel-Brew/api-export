package dev.voxelbrew.api.exporter.component;

import java.util.List;

/** Arranges child components in a column. */
public record VerticalLayout(List<ExportDialogComponent> children) implements ExportDialogLayout {
    public VerticalLayout(ExportDialogComponent... children) {
        this(List.of(children));
    }

    public VerticalLayout {
        children = List.copyOf(children);
    }
}
