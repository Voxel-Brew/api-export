package dev.voxelbrew.api.exporter.component;

/** Component whose value can be supplied by a frontend. */
public sealed interface ExportDialogInput extends ExportDialogComponent
        permits TextField, TextArea, CheckBox, ComboBox, Slider, FileDestination, RadioButtonGroup {
    String id();
}
