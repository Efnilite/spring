package dev.efnilite.spring.resource;

import dev.efnilite.spring.Spring;
import dev.efnilite.spring.resource.io.file.PluginInfo;
import dev.efnilite.spring.resource.io.web.Requester;
import dev.efnilite.spring.resource.platform.Platform;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Represents a resource.
 */
public final class Resource {

    /**
     * @param pluginName The plugin name.
     * @param version    The version.
     * @return The preferred path for Spring jars.
     */
    public static Path getPreferredPath(String pluginName, String version) {
        return Path.of("plugins/%s-%s.jar".formatted(pluginName, version));
    }

    /**
     * Deserializes a {@link Resource}.
     *
     * @param map The serialized {@link Resource}.
     * @return The {@link Resource} instance based on the map data.
     */
    public static Resource deserialize(Map<String, Object> map) {
        String pluginName = (String) map.get("pluginName");
        String currentVersion = (String) map.get("currentVersion");
        Path pathToFile = Path.of((String) map.get("pathToFile"));
        Platform platform = Platform.of((String) map.get("platform"));

        return new Resource(pluginName, currentVersion, pathToFile, platform);
    }

    private long lastChecked;
    private Path pathToFile;
    private Boolean isOutdated;

    private final String pluginName;
    private final String currentVersion;
    private Platform platform;

    /**
     * @param pluginName     The plugin name.
     * @param currentVersion The plugin's current version.
     * @param pathToFile     The path to the plugin's file.
     * @param platform       The platform which is used to download.
     */
    public Resource(String pluginName, String currentVersion, Path pathToFile, Platform platform) {
        this.pluginName = pluginName;
        this.currentVersion = currentVersion;
        this.pathToFile = pathToFile;
        this.platform = platform;
    }

    /**
     * Updates this resource.
     *
     * @throws IOException If downloading the files, moving the files,
     *                     or deleting the parent folder (when using zip) fails.
     */
    public void update() throws IOException {
        List<Path> paths = getRequester().getDownload().getDownloader().save();

        for (Path path : paths) {
            PluginInfo info = new PluginInfo(path);

            Path preferredPath = getPreferredPath(info.getName(), info.getVersion());

            Files.move(path, preferredPath); // create new file

            if (pluginName.equals(info.getName())) {
                Spring.getFactory().addOutdated(new OutdatedPlugin(Bukkit.getPluginManager().getPlugin(pluginName), pathToFile)); // delete previous file
                pathToFile = preferredPath; // update current path
            }
        }

        // delete folder if zip is used
        if (paths.size() > 1) {
            Files.deleteIfExists(paths.get(0).getParent());
        }
    }

    /**
     * Sends an HTTP request to get the latest available version and compares it to the current.
     * Should be run async.
     */
    public void checkForUpdates() {
        lastChecked = System.currentTimeMillis();

        String latestVersion = getRequester().getLatestVersion();

        if (latestVersion == null) {
            isOutdated = false;
            return;
        }

        ModuleDescriptor.Version latest = ModuleDescriptor.Version.parse(latestVersion);
        ModuleDescriptor.Version current = ModuleDescriptor.Version.parse(currentVersion);

        isOutdated = latest.compareTo(current) < 0;
    }

    /**
     * Sets whether this resource is outdated.
     *
     * @param isOutdated True when this resource is outdated, false if not.
     */
    public void setOutdated(boolean isOutdated) {
        lastChecked = System.currentTimeMillis();

        this.isOutdated = isOutdated;
    }

    /**
     * Sets this {@link Resource}'s URL platform.
     *
     * @param url The url.
     */
    public void setPlatform(String url) {
        Platform newPlatform = Platform.of(url);

        platform = newPlatform != null ? newPlatform : platform;
    }

    /**
     * Serializes this {@link Resource} instance.
     *
     * @return A map with all components serialized.
     */
    public Map<String, Object> serialize() {
        return Map.of("pluginName", pluginName,
                "currentVersion", currentVersion,
                "pathToFile", pathToFile.toString(),
                "platform", platform.toString());
    }

    /**
     * Should be run async.
     *
     * @return True when an update is available, false if the check failed or there is no update.
     */
    public boolean isOutdated() {
        if (isOutdated == null) {
            checkForUpdates();
        }

        return isOutdated;
    }

    /**
     * @return True when the associated plugin is enabled, false if not.
     */
    public boolean isRunning() {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    /**
     * @return The {@link Requester} associated with this resource's {@link Platform}.
     */
    public Requester getRequester() {
        return platform.getRequester();
    }

    public String getPluginName() {
        return pluginName;
    }

    public long getLastChecked() {
        return lastChecked;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public Path getPathToFile() {
        return pathToFile;
    }

    public Platform getPlatform() {
        return platform;
    }
}