package de.eindaniel.playerShops.util;

import de.eindaniel.playerShops.Main;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DialogInputHandler {
    private static final Map<UUID, DialogInputHandler> handlers = new HashMap<>();
    private final Player player;
    private final Consumer<String> inputConsumer;
    final Main plugin;

    /**
     * Erstellt einen neuen Dialog Input Handler
     * @param player Der Spieler, dem der Dialog angezeigt werden soll
     * @param inputConsumer Der Callback, der die Eingabe verarbeitet
     */
    public DialogInputHandler(Player player, Consumer<String> inputConsumer, Main plugin) {
        this(player, inputConsumer, "Eingabe", "Bitte gib einen Wert ein", plugin);
    }

    /**
     * Erstellt einen neuen Dialog Input Handler mit angepasstem Titel und Beschreibung
     * @param player Der Spieler, dem der Dialog angezeigt werden soll
     * @param inputConsumer Der Callback, der die Eingabe verarbeitet
     * @param title Der Titel des Dialogs
     * @param description Die Beschreibung im Dialog
     */
    public DialogInputHandler(Player player, Consumer<String> inputConsumer, String title, String description, Main plugin) {
        this.player = player;
        this.inputConsumer = inputConsumer;
        this.plugin = plugin;
        handlers.put(player.getUniqueId(), this);
        showDialog(title, description);
    }

    private void showDialog(String title, String description) {
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(MiniMessage.miniMessage().deserialize("<#FCC500>" + title))
                        .body(List.of(
                                DialogBody.plainMessage(MiniMessage.miniMessage().deserialize("<#fbecab>" + description))
                        ))
                        .inputs(List.of(
                                DialogInput.text("input", Component.text(plugin.i18n().get("dialogInput.input"), TextColor.color(0xfbecab)))
                                        .initial("")
                                        .width(300)
                                        .build()
                        ))
                        .canCloseWithEscape(true)
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.create(
                                Component.text(plugin.i18n().get("dialogInput.confirm"), TextColor.color(0xAEFFC1)),
                                Component.text(plugin.i18n().get("dialogInput.actualConfirm")),
                                100,
                                DialogAction.customClick(
                                        (view, audience) -> handleDialogResponse(view),
                                        ClickCallback.Options.builder()
                                                .uses(1)
                                                .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                                .build()
                                )
                        ),
                        ActionButton.create(
                                Component.text(plugin.i18n().get("dialogInput.cancel"), TextColor.color(0xFFA0B1)),
                                Component.text(plugin.i18n().get("dialogInput.actualCancel")),
                                100,
                                DialogAction.customClick(
                                        (view, audience) -> {
                                            removeHandler(player);
                                            inputConsumer.accept("cancel");
                                        },
                                        ClickCallback.Options.builder()
                                                .uses(1)
                                                .lifetime(ClickCallback.DEFAULT_LIFETIME)
                                                .build()
                                )
                        )
                ))
        );

        player.showDialog(dialog);
    }

    private void handleDialogResponse(DialogResponseView view) {
        if (view == null) {
            player.sendMessage(Main.prefix().append(
                    MiniMessage.miniMessage().deserialize(plugin.i18n().get("dialogInput.error"))
            ));
            removeHandler(player);
            return;
        }

        String input = view.getText("input");
        if (input == null || input.trim().isEmpty()) {
            player.sendMessage(Main.prefix().append(
                    MiniMessage.miniMessage().deserialize(plugin.i18n().get("dialogInput.inputError"))
            ));
            removeHandler(player);
            return;
        }

        handleInput(input.trim());
    }

    public static DialogInputHandler getHandler(Player player) {
        return handlers.get(player.getUniqueId());
    }

    public static void removeHandler(Player player) {
        handlers.remove(player.getUniqueId());
    }

    public Player getPlayer() {
        return player;
    }

    public void handleInput(String input) {
        removeHandler(player);
        inputConsumer.accept(input);
    }
}