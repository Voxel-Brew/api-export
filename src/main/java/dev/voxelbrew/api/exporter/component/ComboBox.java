package dev.voxelbrew.api.exporter.component;

import static java.util.Objects.requireNonNull;

import java.util.List;

/** A single selection from a drop-down list. */
public record ComboBox(String id, String label, List<String> choices, String selected) implements ExportDialogInput {
    public ComboBox {
        requireNonNull(id);
        requireNonNull(label);
        choices = List.copyOf(choices);
        if (choices.isEmpty() || !choices.contains(selected))
            throw new IllegalArgumentException("Selected value must be one of the choices");
    }
}
