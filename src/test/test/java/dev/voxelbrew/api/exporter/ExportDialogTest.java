package dev.voxelbrew.api.exporter;

import static org.junit.jupiter.api.Assertions.*;

import dev.voxelbrew.api.exporter.component.ComboBox;
import dev.voxelbrew.api.exporter.component.FileDestination;
import dev.voxelbrew.api.exporter.component.RadioButtonGroup;
import dev.voxelbrew.api.exporter.component.Slider;
import dev.voxelbrew.api.exporter.component.VerticalLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportDialogTest {
    @Test
    void dialogDescribesItsInputs() {
        var layout = new VerticalLayout(
                new RadioButtonGroup("project", "Project", List.of("team", "personal"), "team"),
                new FileDestination("destination", "txt"));
        assertEquals(2, layout.children().size());
        assertEquals("project", ((RadioButtonGroup) layout.children().getFirst()).id());
        assertNull(((FileDestination) layout.children().get(1)).defaultPath());
        assertEquals(
                Path.of("report.txt"), new FileDestination("destination", "txt", Path.of("report.txt")).defaultPath());
    }

    @Test
    void componentValuesAreValidatedAndCopied() {
        var choices = new java.util.ArrayList<>(List.of("draft", "final"));
        var combo = new ComboBox("quality", "Quality", choices, "draft");
        choices.add("other");
        assertEquals(List.of("draft", "final"), combo.choices());
        assertThrows(
                IllegalArgumentException.class, () -> new ComboBox("quality", "Quality", List.of("draft"), "missing"));
        assertThrows(IllegalArgumentException.class, () -> new Slider("scale", "Scale", 0, 1, 2));
    }

    @Test
    void fileDialogReportsAllInvalidFields() {
        var dialog = new FileExportDialog("Report", "txt");
        var errors = dialog.validate(Map.of("destination", "", "overwrite", "maybe"));

        assertEquals("Choose a destination", errors.get("destination"));
        assertEquals("Overwrite must be true or false", errors.get("overwrite"));
        assertEquals(2, errors.size());
        assertThrows(IllegalArgumentException.class, () -> dialog.buildSettings(Map.of("destination", "")));
    }

    @Test
    void fileDialogRequiresOverwriteConsentForExistingDestination(@TempDir Path directory) throws Exception {
        var path = Files.writeString(directory.resolve("report.txt"), "existing");
        var dialog = new FileExportDialog("Report", "txt");
        var withoutConsent = Map.of("destination", path.toString(), "overwrite", "false");

        assertEquals(
                Map.of("destination", "Destination already exists; enable Overwrite"), dialog.validate(withoutConsent));
        assertEquals(
                Map.of("destination", "Destination already exists; enable Overwrite"),
                dialog.validate(Map.of("destination", path.toString())));
        assertThrows(IllegalArgumentException.class, () -> dialog.buildSettings(withoutConsent));
        assertEquals(Map.of(), dialog.validate(Map.of("destination", path.toString(), "overwrite", "true")));
        assertTrue(dialog.validate(Map.of("destination", "bad\u0000path", "overwrite", "true"))
                .containsKey("destination"));
    }

    @Test
    void fileDialogReusesInputIdsAndDefaultOverwrite(@TempDir Path directory) {
        var dialog = new FileExportDialog("Report", "txt");
        var layout = (VerticalLayout) dialog.layout();
        var destinationInput = (FileDestination) layout.children().get(0);
        var overwriteInput = (dev.voxelbrew.api.exporter.component.CheckBox)
                layout.children().get(1);
        var path = directory.resolve("new.txt");

        assertSame(
                destinationInput, ((VerticalLayout) dialog.layout()).children().get(0));
        assertSame(overwriteInput, ((VerticalLayout) dialog.layout()).children().get(1));
        assertEquals("destination", destinationInput.id());
        assertEquals("overwrite", overwriteInput.id());
        assertFalse(overwriteInput.selected());
        assertEquals(
                new FileExportDialog.Destination(path, false),
                dialog.buildSettings(Map.of(destinationInput.id(), path.toString())));
    }

    @Test
    void fileDialogValidatesDirectoryAndParent(@TempDir Path directory) {
        var dialog = new FileExportDialog("Report", "txt");

        assertEquals(
                "Destination must be a file, not a directory",
                dialog.validate(Map.of("destination", directory.toString())).get("destination"));
        assertEquals(
                "Destination parent directory does not exist",
                dialog.validate(Map.of(
                                "destination",
                                directory.resolve("missing/report.txt").toString()))
                        .get("destination"));
        var relative = Path.of("file-export-" + UUID.randomUUID() + ".txt");
        assertEquals(Map.of(), dialog.validate(Map.of("destination", relative.toString())));
    }

    @Test
    void destinationHostUsesDialogSettings() {
        var path = Path.of("report.txt");
        var window = FileExportDialog.destination(path, true);
        assertEquals(
                new FileExportDialog.Destination(path, true),
                window.show(new FileExportDialog("Report", "txt")).orElseThrow());
        assertThrows(
                UnsupportedOperationException.class,
                () -> window.show(new ExportDialog<Map<String, String>>() {
                    @Override
                    public String title() {
                        return "Other";
                    }

                    @Override
                    public dev.voxelbrew.api.exporter.component.ExportDialogLayout layout() {
                        return new VerticalLayout();
                    }

                    @Override
                    public Map<String, String> buildSettings(Map<String, String> values) {
                        return values;
                    }

                    @Override
                    public Map<String, String> validate(Map<String, String> values) {
                        return Map.of();
                    }
                }));
    }
}
