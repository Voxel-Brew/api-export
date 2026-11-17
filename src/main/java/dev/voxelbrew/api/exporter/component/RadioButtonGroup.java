package dev.voxelbrew.api.exporter.component;

import java.util.List;
import lombok.NonNull;

/** A single selection from visible choices. */
public record RadioButtonGroup(@NonNull String id, @NonNull String label, List<String> choices, String selected)
        implements ExportDialogInput {
    public RadioButtonGroup {
        choices = List.copyOf(choices);
        if (choices.isEmpty() || !choices.contains(selected))
            throw new IllegalArgumentException("Selected value must be one of the choices");
    }
}
