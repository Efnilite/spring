package dev.efnilite.spring;

import dev.efnilite.spring.config.Config;
import dev.efnilite.spring.resource.OutdatedPlugin;
import dev.efnilite.spring.resource.Resource;
import dev.efnilite.vilib.ViPlugin;
import dev.efnilite.vilib.util.Logging;
import dev.efnilite.vilib.util.Task;
import dev.efnilite.vilib.util.elevator.GitElevator;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Main Spring class.
 */
public class Spring extends ViPlugin {

    private static Spring spring;
    private static Logging logging;
    private static ResourceFactory factory;

    public static ResourceFactory getFactory() {
        return factory;
    }

    /**
     * @param child The file name.
     * @return A file from within the plugin folder.
     */
    public static File getInFolder(String child) {
        return new File(spring.getDataFolder(), child);
    }

    /**
     * @return The logger.
     */
    public static Logging logging() {
        return logging;
    }

    /**
     * @return The plugin instance.
     */
    public static Spring getPlugin() {
        return spring;
    }

    @Override
    public void enable() {
        spring = this;
        logging = new Logging(this);

        Config.reload();

        registerCommand("spring", new SpringCommand());

        Task.create(this)
                .async()
                .execute(() -> {
                    logging.info("Getting existing resources...");
                    factory = ResourceFactory.read();
                })
                .run();

        Task.create(this)
                .async()
                .delay(5 * 20)
                .repeat(Config.CONFIG.getInt("check_interval") * 20)
                .execute(() -> {
                    logging.info("Checking for updates...");
                    factory.getResources().forEach(Resource::checkForUpdates);
                })
                .run();
    }

    @Override
    public void disable() {
        factory.write();

        for (OutdatedPlugin plugin : Spring.getFactory().getOutdated()) {
            if (plugin.plugin() != null) {
                getServer().getPluginManager().disablePlugin(plugin.plugin());
            }

            try {
                Files.delete(plugin.outdatedFile());
            } catch (IOException ex) { // todo make ignored
                ex.printStackTrace();
            }
        }
    }

    @Override
    public @Nullable GitElevator getElevator() {
        return null;
    }
}