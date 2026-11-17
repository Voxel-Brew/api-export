package dev.voxelbrew.api.exporter;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ServiceLoaderExporterProviderRegistryIntegrationTest {
    @TempDir
    Path directory;

    @Test
    void discoversInstalledProviderAndReleasesItsJarOnClose() throws Exception {
        Path source = directory.resolve("src/plugin/InstalledProvider.java");
        Files.createDirectories(source.getParent());
        Files.writeString(source, """
                package plugin;
                public final class InstalledProvider implements dev.voxelbrew.api.exporter.ExporterProvider<String> {
                    public dev.voxelbrew.api.exporter.ExporterDescriptor descriptor() {
                        return new dev.voxelbrew.api.exporter.ExporterDescriptor("installed", "Installed", "1.2");
                    }
                    public dev.voxelbrew.api.exporter.Exporter<String> create() {
                        return (document, window) -> java.util.Optional.of("done");
                    }
                }
                """);
        Path classes = Files.createDirectories(directory.resolve("classes"));
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("Tests require a JDK").isNotNull();
        assertThat(compiler.run(
                        null,
                        null,
                        null,
                        "-classpath",
                        System.getProperty("java.class.path"),
                        "-d",
                        classes.toString(),
                        source.toString()))
                .isZero();

        Path plugins = Files.createDirectories(directory.resolve("plugins"));
        Path jar = plugins.resolve("installed.jar");
        try (var output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("plugin/InstalledProvider.class"));
            Files.copy(classes.resolve("plugin/InstalledProvider.class"), output);
            output.closeEntry();
            output.putNextEntry(new JarEntry("META-INF/services/dev.voxelbrew.api.exporter.ExporterProvider"));
            output.write("plugin.InstalledProvider\n".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }

        try (var registry = new ServiceLoaderExporterProviderRegistry(plugins)) {
            var provider = registry.require("installed");
            assertThat(provider.descriptor().name()).isEqualTo("Installed");
            assertThat(provider.descriptor().version()).isEqualTo("1.2");
            assertThat(provider.getClass().getClassLoader())
                    .isNotSameAs(getClass().getClassLoader());
            assertThat(provider.create()).isNotNull();
        }
        Files.delete(jar);
        assertThat(jar).doesNotExist();
    }
}
