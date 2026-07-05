package me.mrrezik.ibottlexp.managers;

import me.mrrezik.ibottlexp.IBottleXP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatisticsManager {

    private final IBottleXP plugin;
    private File statsFile;
    private FileConfiguration statsConfig;

    // UUID -> total levels gained
    private final Map<UUID, Long> totalLevels = new HashMap<>();
    // UUID -> total bottles used
    private final Map<UUID, Long> bottlesUsed = new HashMap<>();

    public StatisticsManager(IBottleXP plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        statsFile = new File(plugin.getDataFolder(), "statistics.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать statistics.yml: " + e.getMessage());
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);

        // Load existing data
        if (statsConfig.contains("players") && statsConfig.isConfigurationSection("players")) {
            var playersSection = statsConfig.getConfigurationSection("players");
            if (playersSection == null) return;
            for (String uuidStr : playersSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    long levels = statsConfig.getLong("players." + uuidStr + ".total-levels", 0);
                    long bottles = statsConfig.getLong("players." + uuidStr + ".bottles-used", 0);
                    totalLevels.put(uuid, levels);
                    bottlesUsed.put(uuid, bottles);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void save() {
        for (Map.Entry<UUID, Long> entry : totalLevels.entrySet()) {
            statsConfig.set("players." + entry.getKey() + ".total-levels", entry.getValue());
        }
        for (Map.Entry<UUID, Long> entry : bottlesUsed.entrySet()) {
            statsConfig.set("players." + entry.getKey() + ".bottles-used", entry.getValue());
        }
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить статистику: " + e.getMessage());
        }
    }

    public void addLevels(UUID uuid, int levels) {
        if (!plugin.getConfigManager().isStatisticsEnabled()) return;
        totalLevels.merge(uuid, (long) levels, Long::sum);
        bottlesUsed.merge(uuid, 1L, Long::sum);

        // Auto-save every 50 uses
        long used = bottlesUsed.getOrDefault(uuid, 0L);
        if (used % 50 == 0) save();
    }

    public long getTotalLevels(UUID uuid) {
        return totalLevels.getOrDefault(uuid, 0L);
    }

    public long getBottlesUsed(UUID uuid) {
        return bottlesUsed.getOrDefault(uuid, 0L);
    }
}
