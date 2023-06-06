package dev.efnilite.spring.resource.io.file;

import dev.efnilite.spring.Spring;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Downloader subclasses convert the provided {@link InputStream} to files.
 */
// todo
// - hangar, other hosts
// - restart stuff
public interface Downloader {

    @NotNull List<Path> save() throws IOException;

    /**
     * @return A random file path with no extension.
     */
    default Path getRandomPath() {
        return getRandomPath("");
    }

    /**
     * @param extension The extension. Dots emitted. Example: 'zip', 'jar'.
     * @return A random file path.
     */
    default Path getRandomPath(@NotNull String extension) {
        return Spring.getInFolder("%s%s".formatted(UUID.randomUUID(), !extension.isEmpty() ? ".%s".formatted(extension) : "")).toPath();
    }
}
