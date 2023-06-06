package dev.efnilite.spring.util;

import dev.efnilite.vilib.ViMain;
import dev.efnilite.vilib.event.EventWatcher;
import dev.efnilite.vilib.util.Strings;
import dev.efnilite.vilib.util.Task;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

/**
 * Class for making listening to player answers in chat really easy
 */
public class ChatAnswer implements EventWatcher {

    /**
     * The amount of chars that have to be added, removed or changed to get the change message;
     */
    private static final int MATCH_DISTANCE = 2;

    /**
     * The player
     */
    private final Player player;

    /**
     * The text, when entered, which will disable the instance of this class
     */
    private final String cancelText = "cancel";

    /**
     * What to do after the message has been sent. This BiConsumer provides the answer and the player instance.
     */
    private Consumer<String> onAnswer;

    /**
     * What to do if the message is cancelled
     */
    private Runnable onCancel;

    public static ChatAnswer from(Player player) {
        return new ChatAnswer(player);
    }

    private ChatAnswer(Player player) {
        this.player = player;

        register();
    }

    /**
     * What will happen after the answer is given
     *
     * @param consumer What to do after the answer. The player and the answer are given.
     * @return the instance of this class
     */
    public ChatAnswer onAnswer(Consumer<String> consumer) {
        this.onAnswer = consumer;
        return this;
    }

    /**
     * What will happen if the answer is cancelled
     *
     * @param runnable What to do on cancel.
     * @return the instance of this class
     */
    public ChatAnswer onCancel(Runnable runnable) {
        this.onCancel = runnable;
        return this;
    }

    @ApiStatus.Internal
    @EventHandler(priority = EventPriority.HIGHEST) // highest to prevent interference from chat plugins
    public void chat(AsyncPlayerChatEvent event) {
        if (event.getPlayer() != player) {
            return;
        }

        String message = event.getMessage();
        event.setCancelled(true);

        if (Strings.getLevenshteinDistance(cancelText, message) > MATCH_DISTANCE) {
            if (onAnswer == null) {
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
                return;
            }
            Task.create(ViMain.getPlugin()) // move from async to sync
                    .execute(() -> onAnswer.accept(message))
                    .run();
        } else {
            if (onCancel == null) {
                AsyncPlayerChatEvent.getHandlerList().unregister(this);
                return;
            }

            Task.create(ViMain.getPlugin()) // move from async to sync
                    .execute(() -> onCancel.run())
                    .run();
        }

        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }
}