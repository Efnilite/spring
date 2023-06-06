package dev.efnilite.spring.resource.io.file;

import java.io.InputStream;

/**
 * Container for download data.
 *
 * @param type The file type.
 * @param file The file stream.
 */
public record Downloadable(FileType type, InputStream file) {

    public Downloader getDownloader() {
        return switch (type) {
            case JAR -> new JarDownloader(this);
            case ZIP -> new ZipDownloader(this);
            case SK -> new SkDownloader(this);
        };
    }
}