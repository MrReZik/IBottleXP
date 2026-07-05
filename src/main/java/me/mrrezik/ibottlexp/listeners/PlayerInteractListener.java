package me.mrrezik.ibottlexp.listeners;

import me.mrrezik.ibottlexp.IBottleXP;
import me.mrrezik.ibottlexp.managers.BottleManager;
import me.mrrezik.ibottlexp.managers.CooldownManager;
import me.mrrezik.ibottlexp.managers.ConfigManager;
import me.mrrezik.ibottlexp.managers.MessageManager;
import me.mrrezik.ibottlexp.managers.StatisticsManager;
import me.mrrezik.ibottlexp.models.Bottle;
import me.mrrezik.ibottlexp.utils.ColorUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private final IBottleXP plugin;
    private final CooldownManager cooldownManager;

    public PlayerInteractListener(IBottleXP plugin) {
        this.plugin = plugin;
        this.cooldownManager = new CooldownManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Только ПКМ основной рукой (блокируем дубль от offhand)
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        BottleManager bottleManager = plugin.getBottleManager();
        MessageManager messages = plugin.getMessageManager();
        ConfigManager config = plugin.getConfigManager();

        String bottleId = bottleManager.getBottleId(item);
        if (bottleId == null) return;

        event.setCancelled(true);

        Bottle bottle = bottleManager.getBottle(bottleId);
        if (bottle == null) return;

        // Проверка прав
        if (!player.hasPermission("ibottlexp.use")) {
            ColorUtils.send(player, messages.get("no-permission"));
            return;
        }

        // Anti-abuse
        if (config.isAntiAbuseEnabled()) {
            boolean abusing = cooldownManager.checkAntiAbuse(player.getUniqueId(), config.getAntiAbuseMaxPerSecond());
            if (abusing) {
                if (config.isAntiAbuseLogEnabled()) {
                    plugin.getLogger().warning(messages.get("anti-abuse-warning",
                            "%player%", player.getName(),
                            "%count%", String.valueOf(cooldownManager.getAbuseCount(player.getUniqueId()))));
                }
                return;
            }
        }

        // Глобальный кулдаун — только проверяем, не устанавливаем ещё
        if (config.isGlobalCooldownEnabled()) {
            long globalRemaining = cooldownManager.getRemainingGlobalCooldown(player.getUniqueId());
            if (globalRemaining > 0) {
                ColorUtils.send(player, messages.get("cooldown-active",
                        "%time%", String.valueOf(globalRemaining),
                        "%bottle%", ColorUtils.translate(bottle.getName())));
                return;
            }
        }

        // Кулдаун бутылочки — только проверяем, не устанавливаем ещё
        if (bottle.getCooldown() > 0) {
            long remaining = cooldownManager.getRemainingCooldown(player.getUniqueId(), bottleId);
            if (remaining > 0) {
                ColorUtils.send(player, messages.get("cooldown-active",
                        "%time%", String.valueOf(remaining),
                        "%bottle%", ColorUtils.translate(bottle.getName())));
                return;
            }
        }

        // Все проверки пройдены — теперь устанавливаем кулдауны и выдаём XP
        if (config.isGlobalCooldownEnabled()) {
            cooldownManager.setGlobalCooldown(player.getUniqueId(), config.getGlobalCooldownSeconds());
        }
        if (bottle.getCooldown() > 0) {
            cooldownManager.setCooldown(player.getUniqueId(), bottleId, bottle.getCooldown());
        }

        // Выдаём XP
        player.giveExpLevels(bottle.getLevels());

        // Уменьшаем количество предметов
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(item);
        }

        // Статистика
        StatisticsManager stats = plugin.getStatisticsManager();
        stats.addLevels(player.getUniqueId(), bottle.getLevels());

        // Сообщения
        ColorUtils.send(player, messages.get("bottle-used",
                "%bottle%", ColorUtils.translate(bottle.getName()),
                "%levels%", String.valueOf(bottle.getLevels())));

        ColorUtils.send(player, messages.get("bottle-used-level-now",
                "%level%", String.valueOf(player.getLevel())));

        // Action bar
        String actionBar = bottle.getActionBar();
        if (actionBar != null && !actionBar.isEmpty()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(ColorUtils.translate(actionBar)));
        }

        // Звук
        playSound(player, bottle);

        // Частицы
        spawnParticles(player, config);
    }

    private void playSound(Player player, Bottle bottle) {
        try {
            Sound sound = Sound.valueOf(bottle.getSound());
            player.playSound(player.getLocation(), sound, bottle.getSoundVolume(), bottle.getSoundPitch());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный звук: " + bottle.getSound() + " для бутылочки " + bottle.getId());
        }
    }

    private void spawnParticles(Player player, ConfigManager config) {
        if (!config.isParticlesEnabled()) return;

        try {
            Particle particle = Particle.valueOf(config.getParticleType());
            Location loc = player.getLocation().add(0, 1, 0);
            player.getWorld().spawnParticle(particle, loc, config.getParticleCount(), 0.3, 0.5, 0.3, 0);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Неизвестный тип частиц: " + config.getParticleType());
        }
    }
}
