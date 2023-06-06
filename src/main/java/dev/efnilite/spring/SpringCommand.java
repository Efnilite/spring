package dev.efnilite.spring;

import dev.efnilite.spring.menu.ResourcesMenu;
import dev.efnilite.vilib.command.ViCommand;
import dev.efnilite.vilib.util.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SpringCommand extends ViCommand {

    public static final String MAIN_COLOUR = "#001149";
    public static final String ACCENT_COLOUR = "#2B003F";
    public static final String PREFIX = "<gradient:%s:%s><bold>Spring</bold></gradient>".formatted(MAIN_COLOUR, ACCENT_COLOUR);

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        switch (args.length) {
            case 0 -> {
                sendPrefixed(sender, "Welcome");
                send(sender, "<%s>/spring menu <gray>View the menu".formatted(MAIN_COLOUR));
                send(sender, "<%s>/spring add <url> <gray>Add a plugin".formatted(MAIN_COLOUR));
                send(sender, "<%s>/spring remove <name> <gray>Remove a plugin".formatted(MAIN_COLOUR));
                send(sender, "<%s>/spring list <gray>View all added plugins".formatted(MAIN_COLOUR));
            }
            case 1 -> {
                if (args[0].equalsIgnoreCase("menu")) {

                    if (!sender.hasPermission("spring.menu")) {
                        sendPrefixed(sender, "<red>You can't do this.");
                        return true;
                    }

                    new ResourcesMenu().open((Player) sender);
                }
            }
            case 2 -> {
                if (args[0].equalsIgnoreCase("add")) {
                    Spring.getFactory().addFromUrl(sender, args[1]);
                }
            }
        }

        return true;
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(Strings.colour(message));
    }

    public static void sendPrefixed(CommandSender sender, String message) {
        sender.sendMessage(Strings.colour("%s <gray>%s".formatted(PREFIX, message)));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
