package dev.voxelbrew.api.exporter;

import java.util.Objects;

/**
 * Immutable discovery metadata shared by file, model and remote-service exporters.
 *
 * <p>The ID is used for programmatic selection and must remain stable across releases. Choose an ID
 * distinct from built-ins and other plugins; duplicate IDs do not create separate menu entries.
 * File extensions, settings and result types belong to the implementation, not this descriptor.
 *
 * @param id nonempty identifier matching {@code [a-z0-9][a-z0-9.-]*}, for example {@code cloud-model}
 * @param name nonblank display label used by the host, for example {@code Cloud model upload}
 * @param version nonblank version of the exporter implementation shown by the host
 */
public record ExporterDescriptor(String id, String name, String version) {
    /**
     * Creates and validates discovery metadata without normalizing either value.
     *
     * @param id stable exporter identifier
     * @param name human-readable display label
     * @param version exporter implementation version
     * @throws IllegalArgumentException if the ID is null or invalid, or the name or version is blank
     * @throws NullPointerException if the name or version is null
     */
    public ExporterDescriptor {
        if (id == null || !id.matches("[a-z0-9][a-z0-9.-]*")) throw new IllegalArgumentException("Invalid exporter ID");
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) throw new IllegalArgumentException("Missing exporter name");
        Objects.requireNonNull(version, "version");
        if (version.isBlank()) throw new IllegalArgumentException("Missing exporter version");
    }
}
