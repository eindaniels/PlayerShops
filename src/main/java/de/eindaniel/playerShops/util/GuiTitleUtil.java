package de.eindaniel.playerShops.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

/**
 * Utility to center inventory titles in Minecraft (pixel-perfect approximation).
 *
 * - Measures the *plain* text width (strips formatting) and prepends the exact
 *   number of spaces needed to visually center the title.
 * - Preserves the original Component (so colors/formatting are kept).
 * - Provides helpers to compare the original/title without the added padding.
 */
public final class GuiTitleUtil {

    // Mitte-Pixel für Inventar-Titel (gängiger Wert in vielen Plugins)
    private static final int CENTER_PX = 90;

    private GuiTitleUtil() {}

    /**
     * Center a Component title while preserving its formatting.
     */
    public static Component centerComponent(Component title) {
        if (title == null) return Component.empty();
        String plain = PlainTextComponentSerializer.plainText().serialize(title);
        String prefix = buildPaddingFor(plain);
        return Component.text(prefix).append(title);
    }

    /**
     * Center a plain String title (convenience overload).
     */
    public static Component center(String plainTitle) {
        if (plainTitle == null) return Component.empty();
        String prefix = buildPaddingFor(plainTitle);
        return Component.text(prefix + plainTitle);
    }

    /**
     * Create an inventory with a centered title (preserves formatting if you pass a Component).
     */
    public static Inventory createCenteredInventory(int size, Component title) {
        return Bukkit.createInventory(null, size, centerComponent(title));
    }

    public static Inventory createCenteredInventory(int size, String title) {
        return Bukkit.createInventory(null, size, center(title));
    }

    /**
     * Extract the plain title (with padding trimmed).
     */
    public static String getRawTitle(Component titleComponent) {
        if (titleComponent == null) return "";
        return PlainTextComponentSerializer.plainText().serialize(titleComponent).trim();
    }

    /**
     * Compare title equals (ignores the injected padding).
     */
    public static boolean titleEquals(Component titleComponent, String expectedPlain) {
        return getRawTitle(titleComponent).equals(expectedPlain);
    }

    /**
     * Check if title contains a substring (ignores the injected padding).
     */
    public static boolean titleContains(Component titleComponent, String substring) {
        return getRawTitle(titleComponent).contains(substring);
    }

    // ------------------- internals -------------------

    private static String buildPaddingFor(String plain) {
        if (plain == null) plain = "";

        int textPx = getStringWidth(plain);
        int halfTextPx = textPx / 2;
        int toCompensate = CENTER_PX - halfTextPx;

        int spacePx = DefaultFontInfo.SPACE.getLength() + 1; // +1 for inter-char spacing
        int spaces = toCompensate / spacePx;
        if (spaces <= 0) return "";

        StringBuilder sb = new StringBuilder(spaces);
        for (int i = 0; i < spaces; i++) sb.append(' ');
        return sb.toString();
    }

    private static int getStringWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        int width = 0;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            DefaultFontInfo dfi = DefaultFontInfo.getByChar(chars[i]);
            width += dfi.getLength() + 1; // +1 pixel for spacing between chars
        }
        // remove the extra spacing added after the last character
        width = Math.max(0, width - 1);
        return width;
    }
}
