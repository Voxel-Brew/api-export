package dev.voxelbrew.api.exporter.component;

import static java.util.Objects.requireNonNull;

/** A bounded numeric value. */
public record Slider(String id, String label, double minimum, double maximum, double value)
        implements ExportDialogInput {
    public Slider {
        requireNonNull(id);
        requireNonNull(label);
        if (!Double.isFinite(minimum)
                || !Double.isFinite(maximum)
                || !Double.isFinite(value)
                || minimum >= maximum
                || value < minimum
                || value > maximum) throw new IllegalArgumentException("Invalid slider range or value");
    }
}
