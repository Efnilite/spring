package dev.efnilite.spring.resource.io.file;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public record ZipDownloader(Downloadable downloadable) implements Downloader {

    @Override
    public @NotNull List<Path> save() throws IOException {
        Path zipPath = getRandomPath("zip");

        Files.copy(downloadable.file(), zipPath, StandardCopyOption.REPLACE_EXISTING);
        downloadable.file().close();

        Path folderPath = getRandomPath();
        unzip(folderPath, zipPath);

        Files.delete(zipPath);

        try (Stream<Path> stream = Files.list(folderPath)) {
            return stream.filter(file -> file.getFileName().endsWith(".jar") || file.getFileName().endsWith(".sk")).toList();
        }
    }

    private void unzip(Path folderPath, Path sourceFile) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(sourceFile.toFile())))) {
            Files.createDirectory(folderPath);

            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                try (FileOutputStream fos = new FileOutputStream(folderPath.resolve(entry.getName()).toFile())) {
                    zip.transferTo(fos);
                }
                zip.closeEntry();
            }
        }
    }
}
