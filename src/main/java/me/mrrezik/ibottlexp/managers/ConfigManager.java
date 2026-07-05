package me.mrrezik.ibottlexp.managers;

import me.mrrezik.ibottlexp.IBottleXP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigManager {

    private final IBottleXP plugin;
    private FileConfiguration config;

    public ConfigManager(IBottleXP plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // Particles
    public boolean isParticlesEnabled() {
        return config.getBoolean("particles.enabled", true);
    }

    public String getParticleType() {
        return config.getString("particles.type", "EXP_BOTTLE");
    }

    public int getParticleCount() {
        return config.getInt("particles.count", 30);
    }

    // Global cooldown
    public boolean isGlobalCooldownEnabled() {
        return config.getBoolean("global-cooldown.enabled", false);
    }

    public int getGlobalCooldownSeconds() {
        return config.getInt("global-cooldown.seconds", 1);
    }

    // Anti-abuse
    public boolean isAntiAbuseEnabled() {
        return config.getBoolean("anti-abuse.enabled", true);
    }

    public boolean isAntiAbuseLogEnabled() {
        return config.getBoolean("anti-abuse.log-to-console", true);
    }

    public int getAntiAbuseMaxPerSecond() {
        return config.getInt("anti-abuse.max-per-second", 3);
    }

    // Statistics
    public boolean isStatisticsEnabled() {
        return config.getBoolean("statistics.enabled", true);
    }
}
