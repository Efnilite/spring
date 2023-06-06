package dev.efnilite.spring.menu;

import dev.efnilite.spring.Spring;
import dev.efnilite.spring.resource.Resource;
import dev.efnilite.vilib.inventory.PagedMenu;
import dev.efnilite.vilib.inventory.animation.RandomAnimation;
import dev.efnilite.vilib.inventory.item.Item;
import dev.efnilite.vilib.inventory.item.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ResourcesMenu {

    public static String getFormattedTime(long lastChecked) {
        String string = "";

        Duration duration = Duration.between(Instant.ofEpochMilli(lastChecked), Instant.now());

        string = addPart(duration.toHoursPart(), string, "hour", "hours");
        string = addPart(duration.toMinutesPart(), string, "minute", "minutes");
        string = addPart(duration.toSecondsPart(), string, "second", "seconds");

        return string.trim();
    }

    private static String addPart(int time, String existing, String one, String two) {
        return existing + (time > 0 ? "%s %s ".formatted(time, (time == 1 ? one : two)) : "");
    }

    public void open(Player player) {
        PagedMenu menu = new PagedMenu(3, "<white>Resources");

        List<MenuItem> items = new ArrayList<>();

        List<Resource> running = Spring.getFactory().getResources().stream()
                .filter(Resource::isRunning)
                .toList();

        // active, registered and updated resources
        running.stream()
            .filter(resource -> !resource.isOutdated())
            .sorted(Comparator.comparing(Resource::getPluginName))
            .forEachOrdered(resource -> items.add(new Item(Material.LIME_STAINED_GLASS_PANE, "<#00CC00><bold>%s".formatted(resource.getPluginName()))
                .lore(() -> List.of("<gray>This plugin is up-to-date.",
                    "",
                    "<dark_gray>• Last checked: %s ago".formatted(getFormattedTime(resource.getLastChecked())),
                    "",
                    "<dark_gray>Click to manage."))
                .click(event -> new ResourceMenu().open(player, resource))));

        // active, registered but outdated resources
        running.stream()
            .filter(Resource::isOutdated)
            .sorted(Comparator.comparing(Resource::getPluginName))
            .forEachOrdered(resource -> items.add(new Item(Material.ORANGE_STAINED_GLASS_PANE, "<#FF9900><bold>%s".formatted(resource.getPluginName()))
                .lore(() -> List.of("<gray>This plugin is outdated.",
                    "<gray>It will be updated on server shutdown/restart.",
                    "",
                    "<dark_gray>• Last checked: %s ago".formatted(getFormattedTime(resource.getLastChecked())),
                    "",
                    "<dark_gray>Click to manage."))
                .click(event -> new ResourceMenu().open(player, resource))));

        List<String> managed = Spring.getFactory().getResources().stream()
            .map(Resource::getPluginName)
            .toList();

        // unregistered plugins
        Arrays.stream(Bukkit.getPluginManager().getPlugins())
            .filter(plugin -> !managed.contains(plugin.getName()))
            .sorted(Comparator.comparing(Plugin::getName))
            .forEachOrdered(plugin -> items.add(new Item(Material.RED_STAINED_GLASS_PANE, "<#940000>%s".formatted(plugin.getName()))
                .lore("<gray>This plugin is not managed by Spring.", "", "<dark_gray>Click to manage.")
                .click(event -> new ResourceMenu().open(player, plugin))));

        // inactive resources
        Spring.getFactory().getResources().stream()
                .filter(resource -> !running.contains(resource))
                .sorted(Comparator.comparing(Resource::getPluginName))
                .forEachOrdered(resource -> items.add(new Item(Material.BLUE_STAINED_GLASS_PANE, "<#006ECC><bold>%s".formatted(resource.getPluginName()))
                    .lore(() -> List.of("<gray>This plugin is not active.",
                        "",
                        "<dark_gray>• Last checked: %s ago".formatted(getFormattedTime(resource.getLastChecked())),
                        "",
                        "<dark_gray>Click to manage."))
                    .click(event -> new ResourceMenu().open(player, resource))));

        menu.displayRows(0, 1)
            .addToDisplay(items)
            .prevPage(19, new Item(Material.RED_DYE, "<#DE1F1F><bold>«").click(event -> menu.page(-1)))
            .nextPage(25, new Item(Material.LIME_DYE, "<#0DCB07><bold>»").click(event -> menu.page(1)))
            .item(21, new Item(Material.REPEATER, "<#6F0000><bold>Update all").lore("<gray>This will attempt to restart the server.")
                    .click(event -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart")))
            .item(23, new Item(Material.ARROW, "<red><bold>Close").click(event -> event.getPlayer().closeInventory()))
            .fillBackground(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            .animation(new RandomAnimation())
            .open(player);

        // subscribe to keep last checked up-to-date
        menu.update(10);
    }
}