package me.mrrezik.ibottlexp.commands;

import me.mrrezik.ibottlexp.IBottleXP;
import me.mrrezik.ibottlexp.managers.BottleManager;
import me.mrrezik.ibottlexp.managers.MessageManager;
import me.mrrezik.ibottlexp.managers.StatisticsManager;
import me.mrrezik.ibottlexp.models.Bottle;
import me.mrrezik.ibottlexp.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IBottleXPCommand implements CommandExecutor, TabCompleter {

    private final IBottleXP plugin;

    public IBottleXPCommand(IBottleXP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MessageManager messages = plugin.getMessageManager();

        if (args.length == 0) {
            handleHelp(sender, messages);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> handleHelp(sender, messages);
            case "reload" -> handleReload(sender, messages);
            case "give" -> handleGive(sender, args, messages);
            case "list" -> handleList(sender, messages);
            case "info" -> handleInfo(sender, args, messages);
            case "stats" -> handleStats(sender, args, messages);
            default -> ColorUtils.send(sender, messages.get("unknown-command"));
        }

        return true;
    }

    // ============================
    //   /ibottlexp help
    // ============================
    private void handleHelp(CommandSender sender, MessageManager messages) {
        for (String line : messages.getList("help-header")) {
            sender.sendMessage(line);
        }
        for (String line : messages.getList("help-commands")) {
            sender.sendMessage(line);
        }
        // Показываем команду stats
        ColorUtils.send(sender, "&#00FFFF /ibottlexp stats [игрок] &#aaaaaa- Показать статистику");
        for (String line : messages.getList("help-footer")) {
            sender.sendMessage(line);
        }
    }

    // ============================
    //   /ibottlexp reload
    // ============================
    private void handleReload(CommandSender sender, MessageManager messages) {
        if (!sender.hasPermission("ibottlexp.reload")) {
            ColorUtils.send(sender, messages.get("no-permission"));
            return;
        }

        try {
            plugin.reload();
            ColorUtils.send(sender, messages.get("reload-success"));
        } catch (Exception e) {
            ColorUtils.send(sender, messages.get("reload-failed"));
            plugin.getLogger().severe("Ошибка при перезагрузке: " + e.getMessage());
        }
    }

    // ============================
    //   /ibottlexp give <player> <bottle> [amount]
    // ============================
    private void handleGive(CommandSender sender, String[] args, MessageManager messages) {
        if (!sender.hasPermission("ibottlexp.give")) {
            ColorUtils.send(sender, messages.get("no-permission"));
            return;
        }

        if (args.length < 3) {
            ColorUtils.send(sender, messages.get("give-usage"));
            return;
        }

        String targetName = args[1];
        String bottleId = args[2].toLowerCase();

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount < 1 || amount > 64) {
                    ColorUtils.send(sender, messages.get("give-invalid-amount"));
                    return;
                }
            } catch (NumberFormatException e) {
                ColorUtils.send(sender, messages.get("give-invalid-amount"));
                return;
            }
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            ColorUtils.send(sender, messages.get("give-player-not-found",
                    "%player%", targetName));
            return;
        }

        BottleManager bottleManager = plugin.getBottleManager();
        Bottle bottle = bottleManager.getBottle(bottleId);
        if (bottle == null) {
            ColorUtils.send(sender, messages.get("give-unknown-bottle",
                    "%bottle%", bottleId));
            return;
        }

        ItemStack item = bottleManager.createItem(bottleId);
        item.setAmount(amount);

        // Выдаём предмет или бросаем на землю если инвентарь полный
        Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), drop);
            }
        }

        String bottleName = ColorUtils.translate(bottle.getName());
        String senderName = sender instanceof Player ? sender.getName() : "Console";

        ColorUtils.send(sender, messages.get("give-success-sender",
                "%player%", target.getName(),
                "%bottle%", bottleName,
                "%amount%", String.valueOf(amount)));

        ColorUtils.send(target, messages.get("give-success-receiver",
                "%sender%", senderName,
                "%bottle%", bottleName,
                "%amount%", String.valueOf(amount)));
    }

    // ============================
    //   /ibottlexp list
    // ============================
    private void handleList(CommandSender sender, MessageManager messages) {
        BottleManager bottleManager = plugin.getBottleManager();

        if (bottleManager.getAllBottles().isEmpty()) {
            ColorUtils.send(sender, messages.get("list-empty"));
            return;
        }

        ColorUtils.send(sender, messages.get("list-header"));
        for (Map.Entry<String, Bottle> entry : bottleManager.getAllBottles().entrySet()) {
            Bottle b = entry.getValue();
            ColorUtils.send(sender, messages.get("list-item",
                    "%id%", entry.getKey(),
                    "%name%", ColorUtils.translate(b.getName()),
                    "%levels%", String.valueOf(b.getLevels())));
        }
    }

    // ============================
    //   /ibottlexp info <bottle>
    // ============================
    private void handleInfo(CommandSender sender, String[] args, MessageManager messages) {
        if (args.length < 2) {
            ColorUtils.send(sender, "&#FFFFFFИспользование: &e/ibottlexp info <бутылочка>");
            return;
        }

        String bottleId = args[1].toLowerCase();
        BottleManager bottleManager = plugin.getBottleManager();
        Bottle bottle = bottleManager.getBottle(bottleId);

        if (bottle == null) {
            ColorUtils.send(sender, messages.get("info-unknown",
                    "%id%", bottleId));
            return;
        }

        ColorUtils.send(sender, messages.get("info-header", "%id%", bottle.getId()));
        ColorUtils.send(sender, messages.get("info-name", "%name%", ColorUtils.translate(bottle.getName())));
        ColorUtils.send(sender, messages.get("info-levels", "%levels%", String.valueOf(bottle.getLevels())));

        if (bottle.getCooldown() > 0) {
            ColorUtils.send(sender, messages.get("info-cooldown", "%cooldown%", String.valueOf(bottle.getCooldown())));
        } else {
            ColorUtils.send(sender, messages.get("info-no-cooldown"));
        }
    }

    // ============================
    //   /ibottlexp stats [player]
    // ============================
    private void handleStats(CommandSender sender, String[] args, MessageManager messages) {
        Player target;

        if (args.length >= 2) {
            if (!sender.hasPermission("ibottlexp.admin")) {
                ColorUtils.send(sender, messages.get("no-permission"));
                return;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                ColorUtils.send(sender, messages.get("give-player-not-found", "%player%", args[1]));
                return;
            }
        } else {
            if (!(sender instanceof Player p)) {
                ColorUtils.send(sender, messages.get("player-only"));
                return;
            }
            target = p;
        }

        StatisticsManager stats = plugin.getStatisticsManager();
        ColorUtils.send(sender, messages.get("stats-header"));
        ColorUtils.send(sender, messages.get("stats-total-levels",
                "%total%", String.valueOf(stats.getTotalLevels(target.getUniqueId()))));
        ColorUtils.send(sender, messages.get("stats-bottles-used",
                "%count%", String.valueOf(stats.getBottlesUsed(target.getUniqueId()))));
    }

    // ============================
    //   Tab Completion
    // ============================
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = new ArrayList<>(Arrays.asList("help", "reload", "give", "list", "info", "stats"));
            for (String sub : subcommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "give" -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(p.getName());
                        }
                    }
                }
                case "info" -> {
                    for (String id : plugin.getBottleManager().getAllBottles().keySet()) {
                        if (id.startsWith(args[1].toLowerCase())) completions.add(id);
                    }
                }
                case "stats" -> {
                    if (sender.hasPermission("ibottlexp.admin")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(p.getName());
                            }
                        }
                    }
                }
            }
            return completions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (String id : plugin.getBottleManager().getAllBottles().keySet()) {
                if (id.startsWith(args[2].toLowerCase())) completions.add(id);
            }
            return completions;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Arrays.asList("1", "16", "32", "64"));
            return completions;
        }

        return completions;
    }
}
