package dev.efnilite.spring.resource.io.file;

import dev.efnilite.spring.Spring;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Reads a .jar {@link Path} and retrieves the plugin name and api-version from its plugin.yml.
 */
public class PluginInfo {

    public final Path path;
    private Map<String, String> info;

    public PluginInfo(Path path) {
        this.path = path;

        try {
            Path temp = unzip();

            if (temp == null) {
                return;
            }

            read(temp);

            Files.delete(temp);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nullable
    private Path unzip() throws IOException {
        Path file = Spring.getInFolder("%s.yml".formatted(UUID.randomUUID())).toPath();

        try (ZipInputStream stream = new ZipInputStream(new BufferedInputStream(new FileInputStream(path.toFile())))) {
            ZipEntry entry;

            while ((entry = stream.getNextEntry()) != null) {
                if (entry.getName().equalsIgnoreCase("plugin.yml")) {
                    FileOutputStream out = new FileOutputStream(file.toFile());
                    stream.transferTo(out);
                    out.close();
                    stream.closeEntry();
                    stream.close();
                    return file;
                }

                stream.closeEntry();
            }
        }
        return null;
    }

    private void read(Path path) throws IOException {
        info = Files.readAllLines(path).stream()
                .filter(line -> line.toLowerCase().contains("version:") || line.toLowerCase().contains("name:") || line.toLowerCase().contains("depend:"))
                .collect(Collectors.toMap(k -> k.split(":")[0].toLowerCase(), v -> v.split(":")[1]));
    }

    public String getVersion() {
        return info.getOrDefault("version", "")
                .replaceAll("['\"]", "")
                .trim();
    }

    /**
     * @return The plugin's name.
     */
    public String getName() {
        return info.getOrDefault("name", "")
                .replaceAll("['\"]", "")
                .trim();
    }

    /**
     * @return A list of the plugin's dependencies.
     */
    public List<String> getDepend() {
        return Arrays.stream(info.getOrDefault("name", "")
                        .replaceAll("['\"\\[]]", "")
                        .trim()
                        .split(","))
                .map(String::trim)
                .toList();
    }
}