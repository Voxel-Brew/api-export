package dev.voxelbrew.api.exporter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExporterProviderRegistryTest {
    @TempDir
    Path plugins;

    public static final class FirstProvider implements ExporterProvider<String> {
        @Override
        public ExporterDescriptor descriptor() {
            return new ExporterDescriptor("first", "First", "1.0.0");
        }

        @Override
        public Exporter<String> create() {
            return (document, window) -> Optional.of("first");
        }
    }

    public static final class SecondProvider implements ExporterProvider<String> {
        @Override
        public ExporterDescriptor descriptor() {
            return new ExporterDescriptor("second", "Second", "2.0.0");
        }

        @Override
        public Exporter<String> create() {
            return (document, window) -> Optional.of("second");
        }
    }

    @Test
    void discoversEveryRegisteredClasspathService() throws Exception {
        var registry =
                ServiceLoader.load(ExporterProviderRegistry.class).findFirst().orElseThrow();
        assertInstanceOf(ServiceLoaderExporterProviderRegistry.class, registry);
        assertEquals(
                List.of("first", "second"),
                registry.descriptors().stream().map(ExporterDescriptor::id).toList());
        assertInstanceOf(FirstProvider.class, registry.require("first"));
        assertEquals("1.0.0", registry.require("first").descriptor().version());
        assertInstanceOf(SecondProvider.class, registry.require("second"));
    }

    @Test
    void reusesLoadedProvidersAcrossLookups() {
        var registry =
                ServiceLoader.load(ExporterProviderRegistry.class).findFirst().orElseThrow();
        assertSame(registry.require("first"), registry.require("first"));
    }

    @Test
    void configuresTheServiceLoadedRegistry() throws Exception {
        try (var registry = ServiceLoader.load(ExporterProviderRegistry.class)
                .findFirst()
                .orElseThrow()
                .withDirectory(plugins)) {
            assertEquals("First", registry.require("first").descriptor().name());
            assertEquals(
                    List.of("first", "second"),
                    registry.descriptors().stream().map(ExporterDescriptor::id).toList());
        }
    }

    @Test
    void suppliedProviderWinsDuplicateIdAndDescriptorsAreReadOnly() throws Exception {
        var supplied = new FirstProvider();
        try (var registry = new ServiceLoaderExporterProviderRegistry(plugins, List.of(supplied))) {
            assertSame(supplied, registry.require("first"));
            assertEquals(
                    List.of("first", "second"),
                    registry.descriptors().stream().map(ExporterDescriptor::id).toList());
            assertThrows(
                    UnsupportedOperationException.class,
                    () -> registry.descriptors().clear());
            assertTrue(assertThrows(IllegalArgumentException.class, () -> registry.require("missing"))
                    .getMessage()
                    .contains("first"));
        }
    }

    @Test
    void rejectsNonDirectoryPluginPathAndCannotReconfigureAfterClose() throws Exception {
        var file = Files.writeString(plugins.resolve("not-a-directory"), "contents");
        assertThrows(IOException.class, () -> new ServiceLoaderExporterProviderRegistry(file));

        var registry = new ServiceLoaderExporterProviderRegistry();
        registry.close();
        assertDoesNotThrow(registry::close);
        assertThrows(IllegalStateException.class, () -> registry.withDirectory(plugins));
    }
}
