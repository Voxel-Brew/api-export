package dev.voxelbrew.api.exporter.component;

import static java.util.Objects.requireNonNull;

import java.util.List;

/** A single selection from visible choices. */
public record RadioButtonGroup(String id, String label, List<String> choices, String selected)
        implements ExportDialogInput {
    public RadioButtonGroup {
        requireNonNull(id);
        requireNonNull(label);
        choices = List.copyOf(choices);
        if (choices.isEmpty() || !choices.contains(selected))
            throw new IllegalArgumentException("Selected value must be one of the choices");
    }
}
