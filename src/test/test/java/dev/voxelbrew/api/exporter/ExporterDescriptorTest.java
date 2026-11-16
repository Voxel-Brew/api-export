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
}
