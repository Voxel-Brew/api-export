package dev.voxelbrew.api.exporter;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNotNull(compiler, "Tests require a JDK");
        assertEquals(
                0,
                compiler.run(
                        null,
                        null,
                        null,
                        "-classpath",
                        System.getProperty("java.class.path"),
                        "-d",
                        classes.toString(),
                        source.toString()));

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
            assertEquals("Installed", provider.descriptor().name());
            assertEquals("1.2", provider.descriptor().version());
            assertNotSame(getClass().getClassLoader(), provider.getClass().getClassLoader());
            assertNotNull(provider.create());
        }
        Files.delete(jar);
        assertFalse(Files.exists(jar));
    }
}
