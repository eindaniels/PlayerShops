package de.eindaniel.playerShops.util;

import de.eindaniel.playerShops.Main;
import de.eindaniel.playerShops.config.Internationalization;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.text.MessageFormat;

public class MessageUtils {
    private final Internationalization internationalization;

    public MessageUtils(Main plugin) {
        this.internationalization = new Internationalization(plugin);
    }

    public void sendLocalizedMessage(Player player, String messageKey, Object... args) {
        String messageTemplate = internationalization.getLanguageConfig().getString(messageKey);
        String prefix = internationalization.getLanguageConfig().getString("prefix");

        if (messageTemplate != null) {
            String formattedMessage = MessageFormat.format(messageTemplate, args);
            Component messageComponent = MiniMessage.miniMessage().deserialize(formattedMessage);
            Component pref = MiniMessage.miniMessage().deserialize(prefix);
            player.sendMessage(pref.append(messageComponent));
        } else {
            Component messageComponent = MiniMessage.miniMessage().deserialize("<#ff1717>Could not find key: " + messageKey + ". Please check the messages.yml or report that to the plugin developer.");
            player.sendMessage(messageComponent);
        }
    }
}
