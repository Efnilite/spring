package dev.efnilite.spring.menu;

import dev.efnilite.spring.Spring;
import dev.efnilite.spring.SpringCommand;
import dev.efnilite.spring.resource.Resource;
import dev.efnilite.spring.util.ChatAnswer;
import dev.efnilite.vilib.inventory.Menu;
import dev.efnilite.vilib.inventory.animation.WaveEastAnimation;
import dev.efnilite.vilib.inventory.item.Item;
import dev.efnilite.vilib.util.Task;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ResourceMenu {

    public void open(Player player, Resource resource) {
        Menu menu = getMenu(resource.getPluginName())
            .item(9, new Item(Material.WEEPING_VINES, "<#CC0000><bold>Remove")
                .lore("<gray>Remove this resource from Spring.", "<gray>It will no longer be automatically updated.")
                .click(event -> {
                    Spring.getFactory().removeResource(resource);

                    SpringCommand.sendPrefixed(player, "'%s' has been removed.".formatted(resource.getPluginName()));

                    player.closeInventory();
                    new ResourcesMenu().open(player);
                }))
            .item(10, new Item(Material.SPYGLASS, "<#B800CC><bold>Check for updates")
                .lore(() -> List.of("<gray>Check for updates.", "",
                    "<dark_gray>Last checked: %s ago".formatted(ResourcesMenu.getFormattedTime(resource.getLastChecked()))))
                .click(event -> {
                    SpringCommand.sendPrefixed(player, "Checking '%s' for updates...".formatted(resource.getPluginName()));

                    Task.create(Spring.getPlugin())
                        .async()
                        .execute(() -> {
                            resource.checkForUpdates();

                            SpringCommand.sendPrefixed(player, resource.isOutdated() ?
                                "'%s' is outdated. It will be updated on server restart/shutdown.".formatted(resource.getPluginName()) :
                                "'%s' is up-to-date.".formatted(resource.getPluginName()));
                        })
                        .run();
                }))
            .item(11, new Item(Material.WRITABLE_BOOK, "<#0400CC><bold>Change URL")
                    .lore("<gray>Change this resource's download URL.")
                    .click(event -> ChatAnswer.from(player)
                            .onCancel(() -> SpringCommand.sendPrefixed(player, "Changing this resource's URL has been cancelled."))
                            .onAnswer(resource::setPlatform)));

        menu.open(player);

        // subscribe to keep last checked up-to-date
        menu.update(10);
    }

    public void open(Player player, Plugin plugin) {
        Menu menu = getMenu(plugin.getName())
            .item(9, new Item(Material.SUGAR_CANE, "<#00CC00><bold>Add")
                .lore("<gray>This will add this plugin to Spring.", "<gray>You will have to provide a download URL.")
                .click(event -> ChatAnswer.from(player)
                    .onCancel(() -> SpringCommand.sendPrefixed(player, "Adding this resource has been cancelled."))
                    .onAnswer(url -> Spring.getFactory().addPlugin(player, url, plugin))));

        menu.open(player);

        // subscribe to keep last checked up-to-date
        menu.update(10);
    }

    private Menu getMenu(String name) {
        return new Menu(3, "<white>%s".formatted(name))
            .item(22, new Item(Material.ARROW, "<red><bold>Close").click(event -> event.getPlayer().closeInventory()))
            .fillBackground(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
            .animation(new WaveEastAnimation())
            .distributeRowsEvenly();
    }

}
