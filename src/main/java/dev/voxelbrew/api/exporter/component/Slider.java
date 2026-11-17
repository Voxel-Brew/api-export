package dev.voxelbrew.api.exporter.component;

import lombok.NonNull;

/** A bounded numeric value. */
public record Slider(@NonNull String id, @NonNull String label, double minimum, double maximum, double value)
        implements ExportDialogInput {
    public Slider {
        if (!Double.isFinite(minimum)
                || !Double.isFinite(maximum)
                || !Double.isFinite(value)
                || minimum >= maximum
                || value < minimum
                || value > maximum) throw new IllegalArgumentException("Invalid slider range or value");
    }
}
