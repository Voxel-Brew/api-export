package dev.voxelbrew.api.exporter;

import static org.assertj.core.api.Assertions.*;

import dev.voxelbrew.api.model.vui.ComboBox;
import dev.voxelbrew.api.model.vui.FileDestination;
import dev.voxelbrew.api.model.vui.RadioButtonGroup;
import dev.voxelbrew.api.model.vui.Slider;
import dev.voxelbrew.api.model.vui.VerticalLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportDialogTest {
    private java.util.Locale previousDisplayLocale;

    @Test
    void standaloneApiLocalizesValidationInPolish() {
        java.util.Locale.setDefault(java.util.Locale.Category.DISPLAY, java.util.Locale.forLanguageTag("pl"));
        var dialog = new FileExportDialog("Export", "vxb");
        assertThat(dialog.validate(Map.of())).containsEntry("destination", "Wybierz miejsce docelowe");
        assertThatThrownBy(() -> dialog.buildSettings(Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Choose a destination");
        assertThatThrownBy(() -> new ExporterDescriptor("invalid id", "Export", "1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid exporter ID");
    }

    @org.junit.jupiter.api.BeforeEach
    void selectEnglishForExpectedMessages() {
        previousDisplayLocale = java.util.Locale.getDefault(java.util.Locale.Category.DISPLAY);
        java.util.Locale.setDefault(java.util.Locale.Category.DISPLAY, java.util.Locale.ENGLISH);
    }

    @org.junit.jupiter.api.AfterEach
    void restoreDisplayLocale() {
        java.util.Locale.setDefault(java.util.Locale.Category.DISPLAY, previousDisplayLocale);
    }

    @Test
    void dialogDescribesItsInputs() {
        var layout = new VerticalLayout(
                new RadioButtonGroup("project", "Project", List.of("team", "personal"), "team"),
                new FileDestination("destination", "txt"));
        assertThat(layout.children()).hasSize(2);
        assertThat(((RadioButtonGroup) layout.children().getFirst()).id()).isEqualTo("project");
        assertThat(((FileDestination) layout.children().get(1)).defaultPath()).isNull();
        assertThat(new FileDestination("destination", "txt", Path.of("report.txt")).defaultPath())
                .isEqualTo(Path.of("report.txt"));
    }

    @Test
    void componentValuesAreValidatedAndCopied() {
        var choices = new java.util.ArrayList<>(List.of("draft", "final"));
        var combo = new ComboBox("quality", "Quality", choices, "draft");
        choices.add("other");
        assertThat(combo.choices()).containsExactly("draft", "final");
        assertThatThrownBy(() -> new ComboBox("quality", "Quality", List.of("draft"), "missing"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Slider("scale", "Scale", 0, 1, 2)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fileDialogReportsAllInvalidFields() {
        var dialog = new FileExportDialog("Report", "txt");
        var errors = dialog.validate(Map.of("destination", "", "overwrite", "maybe"));

        assertThat(errors)
                .containsEntry("destination", "Choose a destination")
                .containsEntry("overwrite", "Overwrite must be true or false")
                .hasSize(2);
        assertThatThrownBy(() -> dialog.buildSettings(Map.of("destination", "")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fileDialogRequiresOverwriteConsentForExistingDestination(@TempDir Path directory) throws Exception {
        var path = Files.writeString(directory.resolve("report.txt"), "existing");
        var dialog = new FileExportDialog("Report", "txt");
        var withoutConsent = Map.of("destination", path.toString(), "overwrite", "false");

        assertThat(dialog.validate(withoutConsent))
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of("destination", "Destination already exists; enable Overwrite"));
        assertThat(dialog.validate(Map.of("destination", path.toString())))
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of("destination", "Destination already exists; enable Overwrite"));
        assertThatThrownBy(() -> dialog.buildSettings(withoutConsent)).isInstanceOf(IllegalArgumentException.class);
        assertThat(dialog.validate(Map.of("destination", path.toString(), "overwrite", "true")))
                .isEmpty();
        assertThat(dialog.buildSettings(Map.of("destination", path.toString(), "overwrite", "true")))
                .isEqualTo(new FileExportDialog.Destination(path, true));
        assertThat(dialog.validate(Map.of("destination", "bad\u0000path", "overwrite", "true")))
                .containsKey("destination");
    }

    @Test
    void fileDialogRejectsInvalidConfiguration() {
        assertThatThrownBy(() -> new FileExportDialog(null, "txt")).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new FileExportDialog("Report", "bad extension"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new FileExportDialog("Report", "gcode.3mf").extension()).isEqualTo("gcode.3mf");
        assertThatThrownBy(() -> new FileExportDialog.Destination(null, false))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void fileDialogReusesInputIdsAndDefaultOverwrite(@TempDir Path directory) {
        var dialog = new FileExportDialog("Report", "txt");
        var layout = (VerticalLayout) dialog.layout();
        var destinationInput = (FileDestination) layout.children().get(0);
        var overwriteInput =
                (dev.voxelbrew.api.model.vui.CheckBox) layout.children().get(1);
        var path = directory.resolve("new.txt");

        assertThat(((VerticalLayout) dialog.layout()).children().get(0)).isSameAs(destinationInput);
        assertThat(((VerticalLayout) dialog.layout()).children().get(1)).isSameAs(overwriteInput);
        assertThat(dialog.destinationInput()).isSameAs(destinationInput);
        assertThat(dialog.overwriteInput()).isSameAs(overwriteInput);
        assertThat(destinationInput.id()).isEqualTo("destination");
        assertThat(overwriteInput.id()).isEqualTo("overwrite");
        assertThat(overwriteInput.selected()).isFalse();
        assertThat(dialog.buildSettings(Map.of(destinationInput.id(), path.toString())))
                .isEqualTo(new FileExportDialog.Destination(path, false));
    }

    @Test
    void fileDialogValidatesDirectoryAndParent(@TempDir Path directory) throws Exception {
        var dialog = new FileExportDialog("Report", "txt");

        assertThat(dialog.validate(Map.of("destination", directory.toString())))
                .containsEntry("destination", "Destination must be a file, not a directory");
        assertThat(dialog.validate(Map.of(
                        "destination", directory.resolve("missing/report.txt").toString())))
                .containsEntry("destination", "Destination parent directory does not exist");
        var parentFile = directory.resolve("parent.txt");
        Files.writeString(parentFile, "contents");
        assertThat(dialog.validate(
                        Map.of("destination", parentFile.resolve("report.txt").toString())))
                .containsEntry("destination", "Destination parent must be a directory");
        var relative = Path.of("file-export-" + UUID.randomUUID() + ".txt");
        assertThat(dialog.validate(Map.of("destination", relative.toString()))).isEmpty();
    }
}
