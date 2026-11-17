package dev.voxelbrew.api.exporter;

import static org.junit.jupiter.api.Assertions.*;

import dev.voxelbrew.api.exporter.component.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExportDialogComponentsTest {
    @Test
    void nestedLayoutsKeepAnImmutableSnapshotOfChildren() {
        var children = new ArrayList<ExportDialogComponent>();
        children.add(new TextField("name", "Name", "draft"));
        var row = new HorizontalLayout(children);
        var column = new VerticalLayout(row, new Separator(), new Label("Done"));
        children.clear();

        assertEquals(1, row.children().size());
        assertEquals(3, column.children().size());
        assertSame(row, column.children().getFirst());
        assertThrows(UnsupportedOperationException.class, () -> row.children().clear());
        assertThrows(NullPointerException.class, () -> new VerticalLayout(new Label("ok"), null));
    }

    @Test
    void selectionInputsRequireAValidDefaultAndCopyChoices() {
        var choices = new ArrayList<>(List.of("draft", "final"));
        var radio = new RadioButtonGroup("stage", "Stage", choices, "draft");
        choices.add("archived");
        assertEquals(List.of("draft", "final"), radio.choices());
        assertThrows(UnsupportedOperationException.class, () -> radio.choices().clear());
        assertThrows(
                IllegalArgumentException.class,
                () -> new RadioButtonGroup("stage", "Stage", List.of("draft"), "final"));
        assertThrows(IllegalArgumentException.class, () -> new ComboBox("stage", "Stage", List.of(), "draft"));
    }

    @Test
    void sliderRejectsNonFiniteOrOutOfRangeValues() {
        assertThrows(IllegalArgumentException.class, () -> new Slider("scale", "Scale", 0, 1, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new Slider("scale", "Scale", 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Slider("scale", "Scale", 0, 1, -0.1));
        assertEquals(0.5, new Slider("scale", "Scale", 0, 1, 0.5).value());
    }
}
