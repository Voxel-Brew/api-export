package dev.voxelbrew.api.exporter.component;

/** UI-neutral component in an exporter dialog. */
public sealed interface ExportDialogComponent permits ExportDialogLayout, ExportDialogInput, Label, Separator {}
