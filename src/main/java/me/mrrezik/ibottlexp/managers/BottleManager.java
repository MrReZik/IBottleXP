package me.mrrezik.ibottlexp.managers;

import me.mrrezik.ibottlexp.IBottleXP;
import me.mrrezik.ibottlexp.models.Bottle;
import me.mrrezik.ibottlexp.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.*;

public class BottleManager {

    private final IBottleXP plugin;
    private final Map<String, Bottle> bottles = new LinkedHashMap<>();
    private NamespacedKey bottleKey;

    public BottleManager(IBottleXP plugin) {
        this.plugin = plugin;
        this.bottleKey = new NamespacedKey(plugin, "bottle_id");
    }

    public void load() {
        bottles.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("bottles");
        if (section == null) {
            plugin.getLogger().warning("Секция 'bottles' не найдена в config.yml!");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection bottleSection = section.getConfigurationSection(id);
            if (bottleSection == null) continue;

            String name = bottleSection.getString("name", "&fБутылочка");
            int levels = bottleSection.getInt("levels", 10);
            List<String> rawLore = bottleSection.getStringList("lore");
            String sound = bottleSection.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
            float soundVolume = (float) bottleSection.getDouble("sound-volume", 1.0);
            float soundPitch = (float) bottleSection.getDouble("sound-pitch", 1.0);
            int cooldown = bottleSection.getInt("cooldown", 0);
            String actionBar = bottleSection.getString("action-bar", "&#00FF00+%levels% уровней опыта!");
            actionBar = actionBar.replace("%levels%", String.valueOf(levels));

            Bottle bottle = new Bottle(id, name, levels, rawLore, sound, soundVolume, soundPitch, cooldown, actionBar);
            bottles.put(id.toLowerCase(), bottle);
        }

        plugin.getLogger().info("Загружено бутылочек: " + bottles.size());
    }

    /**
     * Создаёт ItemStack для бутылочки с указанным id.
     */
    public ItemStack createItem(String id) {
        Bottle bottle = bottles.get(id.toLowerCase());
        if (bottle == null) return null;

        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ColorUtils.translate(bottle.getName()));
        meta.setLore(ColorUtils.translateList(bottle.getLore()));

        // Записываем id бутылочки в PersistentData
        meta.getPersistentDataContainer().set(bottleKey, PersistentDataType.STRING, id.toLowerCase());

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Определяет id бутылочки по ItemStack (через PersistentData).
     */
    public String getBottleId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(bottleKey, PersistentDataType.STRING)) return null;
        return meta.getPersistentDataContainer().get(bottleKey, PersistentDataType.STRING);
    }

    public Bottle getBottle(String id) {
        return bottles.get(id.toLowerCase());
    }

    public Map<String, Bottle> getAllBottles() {
        return Collections.unmodifiableMap(bottles);
    }

    public int getBottleCount() {
        return bottles.size();
    }

    public NamespacedKey getBottleKey() {
        return bottleKey;
    }
}
