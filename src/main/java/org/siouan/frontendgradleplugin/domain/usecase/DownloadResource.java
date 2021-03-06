package org.siouan.frontendgradleplugin.domain.usecase;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import javax.annotation.Nonnull;

import org.siouan.frontendgradleplugin.domain.model.DownloadSettings;
import org.siouan.frontendgradleplugin.domain.model.Logger;
import org.siouan.frontendgradleplugin.domain.provider.ChannelProvider;
import org.siouan.frontendgradleplugin.domain.provider.FileManager;

/**
 * Downloads a resource with efficient behavior and low impact on memory. This downloader uses a temporary directory to
 * store data being downloaded.
 */
public class DownloadResource {

    public static final String TMP_EXTENSION = ".tmp";

    private final FileManager fileManager;

    private final ChannelProvider channelProvider;

    private final Logger logger;

    public DownloadResource(final FileManager fileManager, final ChannelProvider channelProvider, final Logger logger) {
        this.fileManager = fileManager;
        this.channelProvider = channelProvider;
        this.logger = logger;
    }

    /**
     * Downloads a resource at a given URL in a destination file, by using a temporary directory. It is up to the caller
     * to ensure the temporary directory and the directory receiving the destination file exist and are writable.
     *
     * @param downloadSettings Download parameters.
     * @throws IOException If the resource could not be downloaded, or could not be written in the temporary directory,
     * or moved to the destination file. In this case, any file created in the temporary directory is removed.
     */
    public void execute(@Nonnull final DownloadSettings downloadSettings) throws IOException {
        final URL resourceUrl = downloadSettings.getResourceUrl();
        final String resourceName = downloadSettings.getDestinationFilePath().getFileName().toString();
        final Path downloadedFilePath = downloadSettings
            .getTemporaryDirectoryPath()
            .resolve(resourceName + TMP_EXTENSION);
        logger.debug("Downloading resource at '{}' (proxy: {})", downloadSettings.getResourceUrl(),
            downloadSettings.getProxy());
        try (final ReadableByteChannel resourceInputChannel = channelProvider.getReadableByteChannel(resourceUrl,
            downloadSettings.getProxy());
             final FileChannel resourceOutputChannel = channelProvider.getWritableFileChannelForNewFile(
                 downloadedFilePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                 StandardOpenOption.TRUNCATE_EXISTING)) {
            resourceOutputChannel.transferFrom(resourceInputChannel, 0, Long.MAX_VALUE);
        } catch (final IOException e) {
            fileManager.deleteIfExists(downloadedFilePath);
            throw e;
        }

        fileManager.move(downloadedFilePath, downloadSettings.getDestinationFilePath(),
            StandardCopyOption.REPLACE_EXISTING);
    }
}
