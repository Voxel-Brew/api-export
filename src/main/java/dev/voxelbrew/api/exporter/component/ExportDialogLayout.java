package dev.voxelbrew.api.exporter.component;

/** A component that arranges child components in a dialog. */
public sealed interface ExportDialogLayout extends ExportDialogComponent permits HorizontalLayout, VerticalLayout {}
