package org.siouan.frontendgradleplugin.infrastructure.archiver;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.siouan.frontendgradleplugin.domain.model.ArchiverContext;
import org.siouan.frontendgradleplugin.domain.model.ExplodeSettings;

/**
 * Context used to extract entries in a TAR archive.
 *
 * @since 1.1.3
 */
class TarArchiverContext implements ArchiverContext {

    /**
     * Explode settings.
     */
    private final ExplodeSettings settings;

    /**
     * TAR archive input stream.
     */
    private final TarArchiveInputStream inputStream;

    /**
     * Builds a context providing explode settings, and archive input stream.
     *
     * @param settings Explode settings.
     * @param inputStream Archive input stream.
     */
    public TarArchiverContext(final ExplodeSettings settings, final TarArchiveInputStream inputStream) {
        this.settings = settings;
        this.inputStream = inputStream;
    }

    @Nonnull
    @Override
    public ExplodeSettings getSettings() {
        return settings;
    }

    /**
     * Gets the archive input stream.
     *
     * @return Input stream.
     */
    public TarArchiveInputStream getInputStream() {
        return inputStream;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
