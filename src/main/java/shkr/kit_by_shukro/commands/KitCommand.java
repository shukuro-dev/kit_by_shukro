package shkr.kit_by_shukro.commands;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import shkr.kit_by_shukro.Kit_by_shukro;
import shkr.kit_by_shukro.managers.KitManager;

public class KitCommand implements CommandExecutor {

    private final Kit_by_shukro plugin;
    private final KitManager kitManager;

    public KitCommand(Kit_by_shukro plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
    }

    private void send(CommandSender sender, String key) {
        String raw = plugin.getConfig().getString("messages." + key, "");
        if (!raw.isEmpty()) {
            sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(raw.replace('&', '§')));
        }
    }

    private void sendSlot(CommandSender sender, String key, int slot) {
        String raw = plugin.getConfig().getString("messages." + key, "");
        if (!raw.isEmpty()) {
            String formatted = raw.replace('&', '§').replace("%slot%", String.valueOf(slot));
            sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(formatted));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("kits") || args.length == 0) {
            if (!(sender instanceof Player player)) {
                send(sender, "only-players");
                return true;
            }
            player.openInventory(kitManager.createSupplyMenu());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("kitbyshukro.admin") && !sender.isOp()) {
                send(sender, "no-permission");
                return true;
            }
            plugin.reloadPluginConfig();
            send(sender, "reloaded");
            return true;
        }

        if (!(sender instanceof Player player)) {
            send(sender, "only-players");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                send(sender, "invalid-slot");
                return true;
            }
            Integer slot = parseSlot(args[1]);
            if (slot == null || !kitManager.isValidSlot(slot)) {
                send(sender, "invalid-slot");
                return true;
            }

            kitManager.saveKit(player, slot);
            sendSlot(player, "kit-saved", slot);
            return true;
        }

        Integer slot = parseSlot(args[0]);
        if (slot == null || !kitManager.isValidSlot(slot)) {
            send(sender, "invalid-slot");
            return true;
        }

        if (!kitManager.hasKit(player.getUniqueId(), slot)) {
            sendSlot(player, "kit-not-found", slot);
            return true;
        }

        if (kitManager.applyKit(player, slot)) {
            sendSlot(player, "kit-claimed", slot);
        }
        return true;
    }

    private Integer parseSlot(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}