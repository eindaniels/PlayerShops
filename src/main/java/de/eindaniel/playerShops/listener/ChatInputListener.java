package de.eindaniel.playerShops.listener;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.util.ChatInputHandler;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

public class ChatInputListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        ChatInputHandler handler = ChatInputHandler.getHandler(p);
        if (handler == null) return;

        e.setCancelled(true);

        String msg = PlainTextComponentSerializer.plainText().serialize(e.message()).trim();

        if (msg.equalsIgnoreCase("abbrechen") || msg.equalsIgnoreCase("cancel")) {
            p.sendMessage(Main.prefix().append(MiniMessage.miniMessage().deserialize("<gray>Du hast den Vorgang abgebrochen.")));
            ChatInputHandler.removeHandler(p);
            return;
        }
        handler.handleInput(msg);
    }
}
