package dev.voxelbrew.api.exporter;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExporterDescriptorTest {
    @Test
    void requiresAnExporterVersion() {
        assertThatThrownBy(() -> new ExporterDescriptor("example", "Example", null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ExporterDescriptor("example", "Example", " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new ExporterDescriptor("example", "Example", "1.2.3").version())
                .isEqualTo("1.2.3");
    }

    @Test
    void rejectsInvalidDiscoveryMetadataWithoutChangingValidValues() {
        for (String id : java.util.List.of("", "Bad", "has space", "_hidden")) {
            assertThatThrownBy(() -> new ExporterDescriptor(id, "Example", "1.0"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> new ExporterDescriptor("example", " ", "1.0"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ExporterDescriptor("example", null, "1.0"))
                .isInstanceOf(NullPointerException.class);
        var descriptor = new ExporterDescriptor("example.format", "  Example  ", "v1");
        assertThat(descriptor.id()).isEqualTo("example.format");
        assertThat(descriptor.name()).isEqualTo("  Example  ");
    }
}
