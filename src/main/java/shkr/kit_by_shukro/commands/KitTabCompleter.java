package shkr.kit_by_shukro.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import shkr.kit_by_shukro.managers.KitManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class KitTabCompleter implements TabCompleter {

    private final KitManager kitManager;

    public KitTabCompleter(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (label.equalsIgnoreCase("kits")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (Integer slot : kitManager.getAllowedSlots()) {
                suggestions.add(String.valueOf(slot));
            }
            suggestions.add("create");

            if (sender.hasPermission("kitbyshukro.admin") || sender.isOp()) {
                suggestions.add("reload");
            }
            return filter(suggestions, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            List<String> slots = new ArrayList<>();
            for (Integer slot : kitManager.getAllowedSlots()) {
                slots.add(String.valueOf(slot));
            }
            return filter(slots, args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String opt : options) {
            if (opt.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(opt);
            }
        }
        return result;
    }
}