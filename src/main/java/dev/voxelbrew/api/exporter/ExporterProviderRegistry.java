package dev.voxelbrew.api.exporter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Discovers classpath and installed exporter providers for a host workflow. */
public interface ExporterProviderRegistry extends AutoCloseable {
    /** Configures plugin discovery on the service-loaded instance. */
    ExporterProviderRegistry withDirectory(Path directory) throws IOException;

    List<ExporterDescriptor> descriptors();

    ExporterProvider<?> require(String id);

    @Override
    void close() throws IOException;
}
