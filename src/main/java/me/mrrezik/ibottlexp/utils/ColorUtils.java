package me.mrrezik.ibottlexp.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Переводит строку с поддержкой &#rrggbb и &-кодов цветов.
     */
    public static String translate(String message) {
        if (message == null) return "";

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Переводит список строк с поддержкой hex и & кодов.
     */
    public static List<String> translateList(List<String> lines) {
        List<String> translated = new ArrayList<>();
        for (String line : lines) {
            translated.add(translate(line));
        }
        return translated;
    }

    /**
     * Отправляет игроку/консоли уже переведённое сообщение.
     */
    public static void send(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return;
        sender.sendMessage(translate(message));
    }
}
