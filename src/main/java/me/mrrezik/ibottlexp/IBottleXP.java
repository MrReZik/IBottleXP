package me.mrrezik.ibottlexp;

import me.mrrezik.ibottlexp.commands.IBottleXPCommand;
import me.mrrezik.ibottlexp.listeners.PlayerInteractListener;
import me.mrrezik.ibottlexp.managers.BottleManager;
import me.mrrezik.ibottlexp.managers.ConfigManager;
import me.mrrezik.ibottlexp.managers.MessageManager;
import me.mrrezik.ibottlexp.managers.StatisticsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class IBottleXP extends JavaPlugin {

    private static IBottleXP instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private BottleManager bottleManager;
    private StatisticsManager statisticsManager;

    @Override
    public void onEnable() {
        instance = this;

        // Инициализация менеджеров
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.bottleManager = new BottleManager(this);
        this.statisticsManager = new StatisticsManager(this);

        configManager.load();
        messageManager.load();
        bottleManager.load();

        // Регистрация команды
        IBottleXPCommand command = new IBottleXPCommand(this);
        getCommand("ibottlexp").setExecutor(command);
        getCommand("ibottlexp").setTabCompleter(command);

        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);

        getLogger().info("╔══════════════════════════╗");
        getLogger().info("║  IBottleXP by MrReZik    ║");
        getLogger().info("╚══════════════════════════╝");
        getLogger().info("Версия: " + getDescription().getVersion());
        getLogger().info("Загружено бутылочек: " + bottleManager.getBottleCount());
        getLogger().info("Плагин успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (statisticsManager != null) {
            statisticsManager.save();
        }
        getLogger().info("IBottleXP выключен. До свидания!");
    }

    public void reload() {
        configManager.load();
        messageManager.load();
        bottleManager.load();
    }

    public static IBottleXP getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public BottleManager getBottleManager() {
        return bottleManager;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }
}
