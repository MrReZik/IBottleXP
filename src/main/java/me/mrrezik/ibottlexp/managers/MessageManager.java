package me.mrrezik.ibottlexp.managers;

import me.mrrezik.ibottlexp.IBottleXP;
import me.mrrezik.ibottlexp.utils.ColorUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {

    private final IBottleXP plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessageManager(IBottleXP plugin) {
        this.plugin = plugin;
    }

    public void load() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Merge defaults from jar
        InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            messages.setDefaults(defConfig);
        }
    }

    /**
     * Получить строку из messages.yml с заменой %prefix%.
     */
    public String get(String key) {
        String msg = messages.getString(key, "&#FF4444[IBottleXP] Missing key: " + key);
        String prefix = messages.getString("prefix", "&#00FFFFIBottleXP &8» &f");
        msg = msg.replace("%prefix%", prefix);
        return ColorUtils.translate(msg);
    }

    /**
     * Получить список строк из messages.yml.
     */
    public List<String> getList(String key) {
        List<String> list = messages.getStringList(key);
        String prefix = messages.getString("prefix", "&#00FFFFIBottleXP &8» &f");
        List<String> result = new ArrayList<>();
        for (String line : list) {
            result.add(ColorUtils.translate(line.replace("%prefix%", prefix)));
        }
        return result;
    }

    /**
     * Получить строку с заменой плейсхолдеров.
     */
    public String get(String key, String... replacements) {
        String msg = get(key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }
}
