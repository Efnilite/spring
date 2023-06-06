package dev.efnilite.spring;

import dev.efnilite.spring.resource.OutdatedPlugin;
import dev.efnilite.spring.resource.Resource;
import dev.efnilite.spring.resource.io.file.PluginInfo;
import dev.efnilite.spring.resource.platform.Platform;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.efnilite.spring.SpringCommand.sendPrefixed;

public class ResourceFactory {

    private final List<Resource> resources = new ArrayList<>();
    private final List<OutdatedPlugin> outdated = new ArrayList<>();

    private ResourceFactory() {
    }

    /**
     * @return A new {@link ResourceFactory} instance based on previous saved data.
     */
    public static ResourceFactory read() {
        if (!getFile().exists()) {
            return new ResourceFactory();
        }

        try (ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(getFile())))) {
            List<Map<String, Object>> resources = (List<Map<String, Object>>) stream.readObject();

            ResourceFactory factory = new ResourceFactory();
            factory.resources.addAll(resources.stream()
                    .map(Resource::deserialize)
                    .toList());
            return factory;
        } catch (IOException | ClassNotFoundException ex) {
            Spring.logging().stack("Error while trying to read resources file %s".formatted(getFile()), ex);
            return new ResourceFactory();
        }
    }

    private static File getFile() {
        return Spring.getInFolder("resources");
    }

    public void write() {
        try (ObjectOutputStream stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getFile())))) {
            stream.writeObject(resources.stream()
                    .map(Resource::serialize)
                    .toList());
            stream.flush();
        } catch (IOException ex) {
            Spring.logging().stack("Error while trying to save resources file %s".formatted(getFile()), ex);
        }
    }

    public void addFromUrl(@NotNull CommandSender sender, @NotNull String url) {
        Platform platform = Platform.of(url);

        if (platform == null) {
            sendPrefixed(sender, "<red>That url uses an unsupported platform.");
            return;
        }

        List<Path> newPaths;
        try {
            newPaths = platform.getRequester()
                    .getDownload()
                    .getDownloader()
                    .save();
        } catch (IOException ex) {
            sendPrefixed(sender, "<red>Error while trying to download this resource. Check the console.");
            Spring.logging().stack("Error while trying to download resource at url '%s'".formatted(url), ex);
            return;
        }

        for (Path newPath : newPaths) {
            if (!newPath.toFile().exists()) {
                sendPrefixed(sender, "<red>There was an error while downloading. Check the console.");
                return;
            }

            PluginInfo newInfo = new PluginInfo(newPath);
            String pluginName = newInfo.getName();
            String pluginVersion = newInfo.getVersion();

            sendPrefixed(sender, "Plugin has been identified as '%s'.".formatted(pluginName));

            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin != null) {
                sendPrefixed(sender, "'%s' has been detected as an existing plugin on this server.".formatted(pluginName));

                addPlugin(sender, url, plugin);

                try {
                    Files.delete(newPath);
                } catch (IOException ex) {
                    Spring.logging().stack("Error while trying to delete outdated path", ex);
                }
                return;
            }

            Path preferredPath = Resource.getPreferredPath(pluginName, pluginVersion);
            try {
                Files.move(newPath, preferredPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                try {
                    Files.delete(newPath);
                } catch (IOException ignored) {

                }

                sendPrefixed(sender, "There was an error while trying to rename the file. Check the console.");
                Spring.logging().stack("Error while trying to rename file at '%s' of plugin '%s'".formatted(newPath, pluginName), ex);
            }

            Resource resource = new Resource(pluginName, pluginVersion, preferredPath, platform);
            resource.setOutdated(false);
            resources.add(resource);

            sendPrefixed(sender, "'%s' has been successfully installed and added to Spring.".formatted(pluginName));
        }
    }

    public void addPlugin(@NotNull CommandSender sender, @NotNull String url, @NotNull Plugin plugin) {
        String pluginName = plugin.getName();
        String pluginVersion = plugin.getDescription().getVersion();
        Platform platform = Platform.of(url);

        if (platform == null) {
            sendPrefixed(sender, "<red>That url uses an unsupported platform.");
            return;
        }

        File jar;
        try {
            jar = getJar(plugin);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
            sendPrefixed(sender, "<red>Error while trying to get the file of plugin '%s'.".formatted(pluginName));
            Spring.logging().stack("Error while trying to get file of plugin '%s'".formatted(pluginName), ex);
            return;
        }

        Resource resource = new Resource(pluginName, pluginVersion, jar.toPath(), platform);

        if (resource.isOutdated()) {
            try {
                resource.update();
            } catch (IOException ex) {
                Spring.logging().stack("Error while trying to update resource '%s'".formatted(pluginName), ex);
            }
        } else {
            // can't move newly downloaded jar to preferred path if plugin is already present at that path.
            if (!jar.getName().equals(Resource.getPreferredPath(pluginName, pluginVersion).getFileName().toString())) {
                // on plugin shutdown change jar name to preferred format
                outdated.add(new OutdatedPlugin(plugin, jar.toPath()));
            }
        }

        sendPrefixed(sender, "'%s' has been successfully added to Spring.".formatted(pluginName));
    }

    // moves provided path to the preferred jar path
    private File getJar(Plugin plugin) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = JavaPlugin.class.getDeclaredMethod("getFile");
        method.setAccessible(true);
        return (File) method.invoke(plugin);
    }

    /**
     * Adds an outdated plugin. Will be deleted on plugin disable.
     *
     * @param plugin The plugin.
     */
    public void addOutdated(OutdatedPlugin plugin) {
        outdated.add(plugin);
    }

    public void removeResource(Resource resource) {
        resources.remove(resource);
    }

    public List<Resource> getResources() {
        return resources;
    }

    public List<OutdatedPlugin> getOutdated() {
        return outdated;
    }
}
