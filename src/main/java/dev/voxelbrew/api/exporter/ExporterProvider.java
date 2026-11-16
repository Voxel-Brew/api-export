package dev.voxelbrew.api.exporter;

/**
 * Discovery and construction entry point for an exporter implementation.
 *
 * <p>External JARs register a public implementation with a public no-argument constructor in the
 * UTF-8 resource {@code META-INF/services/dev.voxelbrew.api.exporter.ExporterProvider}. Each line
 * names a provider class, not an exporter class. The host discovers providers with
 * {@link java.util.ServiceLoader}; no dependency-injection annotations are required. Compile against
 * the host API but do not bundle its classes into the plugin JAR.
 *
 * <p>Keep construction and metadata lookup lightweight. The host chooses the thread on which an
 * export runs. Provider-owned resources must remain valid until exports using them have completed.
 * Discovery alone does not register frontend handlers for
 * custom {@link ExportDialog} types; those require separate host integration.
 *
 * @param <T> settings type accepted by the exporter dialog created by this provider
 */
public interface ExporterProvider<T> {
    /**
     * Describes the option shown in export menus and selected by identifier in headless workflows.
     *
     * @return non-null metadata with a stable, unique ID, display name, and version
     */
    ExporterDescriptor descriptor();

    /**
     * Creates an exporter.
     *
     * <p>Keep invocation-specific settings local to each export or create separate instances so that
     * simultaneous exports cannot overwrite each other's state.
     *
     * @return non-null exporter implementing this provider's advertised option
     */
    Exporter<T> create();
}
