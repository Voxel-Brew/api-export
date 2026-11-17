package dev.voxelbrew.api.exporter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExporterDescriptorTest {
    @Test
    void requiresAnExporterVersion() {
        assertThrows(NullPointerException.class, () -> new ExporterDescriptor("example", "Example", null));
        assertThrows(IllegalArgumentException.class, () -> new ExporterDescriptor("example", "Example", " "));
        assertEquals("1.2.3", new ExporterDescriptor("example", "Example", "1.2.3").version());
    }

    @Test
    void rejectsInvalidDiscoveryMetadataWithoutChangingValidValues() {
        for (String id : java.util.List.of("", "Bad", "has space", "_hidden"))
            assertThrows(IllegalArgumentException.class, () -> new ExporterDescriptor(id, "Example", "1.0"));
        assertThrows(IllegalArgumentException.class, () -> new ExporterDescriptor("example", " ", "1.0"));
        assertThrows(NullPointerException.class, () -> new ExporterDescriptor("example", null, "1.0"));
        var descriptor = new ExporterDescriptor("example.format", "  Example  ", "v1");
        assertEquals("example.format", descriptor.id());
        assertEquals("  Example  ", descriptor.name());
    }
}
