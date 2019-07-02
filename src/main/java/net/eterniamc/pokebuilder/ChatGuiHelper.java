package net.eterniamc.pokebuilder;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Utility for allowing players to enter input into modifiers thru chat
 */

public class ChatGuiHelper {
    private static HashMap<UUID, Consumer<Text>> guis = new HashMap<>();

    public static void addGUI(String initialMessage, Player target, Consumer<Text> action) {
        Utils.sendPlayerMessage(target, initialMessage);
        guis.put(target.getUniqueId(), action);
    }

    @Listener
    public void onChat(MessageChannelEvent.Chat event, @First Player src) {
        Optional.ofNullable(guis.get(src.getUniqueId())).ifPresent(action -> {
            event.setCancelled(true);
            guis.remove(src.getUniqueId());
            if (event.getRawMessage().toPlain().equals("cancel")) return;
            action.accept(event.getRawMessage());
        });
    }
}
