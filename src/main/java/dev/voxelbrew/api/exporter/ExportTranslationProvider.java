package dev.voxelbrew.api.exporter;

import dev.voxelbrew.api.i18n.PropertiesTranslationProvider;
import java.util.List;
import java.util.Locale;

public final class ExportTranslationProvider extends PropertiesTranslationProvider {
    public ExportTranslationProvider() {
        super("dev.voxelbrew.api.exporter.messages");
    }

    @Override
    protected List<Locale> getLocales() {
        return List.of("en", "pl", "fr", "it", "es", "pt", "de", "sv", "nb", "zh", "ja").stream()
                .map(Locale::forLanguageTag)
                .toList();
    }
}
