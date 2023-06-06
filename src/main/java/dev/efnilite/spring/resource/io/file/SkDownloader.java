package dev.efnilite.spring.resource.io.file;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

// todo
public record SkDownloader(Downloadable downloadable) implements Downloader {

    @Override
    public @NotNull List<Path> save() throws IOException {
        Path path = getRandomPath("sk");
        Files.copy(downloadable.file(), path, StandardCopyOption.REPLACE_EXISTING);
        downloadable.file().close();

        return Collections.singletonList(path);
    }
}
