package dev.voxelbrew.api.exporter;

import static org.assertj.core.api.Assertions.*;

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
        assertThat(registry).isInstanceOf(ServiceLoaderExporterProviderRegistry.class);
        assertThat(registry.descriptors().stream().map(ExporterDescriptor::id).toList())
                .containsExactly("first", "second");
        assertThat(registry.require("first")).isInstanceOf(FirstProvider.class);
        assertThat(registry.require("first").descriptor().version()).isEqualTo("1.0.0");
        assertThat(registry.require("second")).isInstanceOf(SecondProvider.class);
    }

    @Test
    void reusesLoadedProvidersAcrossLookups() {
        var registry =
                ServiceLoader.load(ExporterProviderRegistry.class).findFirst().orElseThrow();
        assertThat(registry.require("first")).isSameAs(registry.require("first"));
    }

    @Test
    void configuresTheServiceLoadedRegistry() throws Exception {
        try (var registry = ServiceLoader.load(ExporterProviderRegistry.class)
                .findFirst()
                .orElseThrow()
                .withDirectory(plugins)) {
            assertThat(registry.require("first").descriptor().name()).isEqualTo("First");
            assertThat(registry.descriptors().stream()
                            .map(ExporterDescriptor::id)
                            .toList())
                    .containsExactly("first", "second");
        }
    }

    @Test
    void suppliedProviderWinsDuplicateIdAndDescriptorsAreReadOnly() throws Exception {
        var supplied = new FirstProvider();
        try (var registry = new ServiceLoaderExporterProviderRegistry(plugins, List.of(supplied))) {
            assertThat(registry.require("first")).isSameAs(supplied);
            assertThat(registry.descriptors().stream()
                            .map(ExporterDescriptor::id)
                            .toList())
                    .containsExactly("first", "second");
            assertThatThrownBy(() -> registry.descriptors().clear()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> registry.require("missing"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("first");
        }
    }

    @Test
    void rejectsNonDirectoryPluginPathAndCannotReconfigureAfterClose() throws Exception {
        var file = Files.writeString(plugins.resolve("not-a-directory"), "contents");
        assertThatThrownBy(() -> new ServiceLoaderExporterProviderRegistry(file))
                .isInstanceOf(IOException.class);

        var registry = new ServiceLoaderExporterProviderRegistry();
        registry.close();
        assertThatCode(registry::close).doesNotThrowAnyException();
        assertThatThrownBy(() -> registry.withDirectory(plugins)).isInstanceOf(IllegalStateException.class);
    }
}
