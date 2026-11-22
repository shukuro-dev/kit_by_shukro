package shkr.kit_by_shukro.managers;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitManager {

    private final Plugin plugin;
    private final File kitsFile;
    private FileConfiguration kitsConfig;

    private final Map<UUID, Map<Integer, PlayerKit>> playerKits = new HashMap<>();

    public KitManager(Plugin plugin) {
        this.plugin = plugin;
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        loadKitsFromDisk();
    }

    public List<Integer> getAllowedSlots() {
        List<Integer> allowed = plugin.getConfig().getIntegerList("allowed-slots");
        if (allowed.isEmpty()) {
            return List.of(1, 2, 3, 4);
        }
        return allowed;
    }

    public boolean isValidSlot(int slot) {
        return getAllowedSlots().contains(slot);
    }

    public Inventory createSupplyMenu() {
        String rawTitle = plugin.getConfig().getString("menu-title", "&8PvP Kit Supply");
        String title = rawTitle.replace('&', '§');
        Inventory inv = Bukkit.createInventory(null, 54, LegacyComponentSerializer.legacySection().deserialize(title));

        List<ItemStack> supply = List.of(
                new ItemStack(Material.NETHERITE_HELMET, 1),
                new ItemStack(Material.NETHERITE_CHESTPLATE, 1),
                new ItemStack(Material.NETHERITE_LEGGINGS, 1),
                new ItemStack(Material.NETHERITE_BOOTS, 1),
                new ItemStack(Material.NETHERITE_SWORD, 1),
                new ItemStack(Material.NETHERITE_AXE, 1),
                new ItemStack(Material.NETHERITE_PICKAXE, 1),
                new ItemStack(Material.SHIELD, 1),
                new ItemStack(Material.END_CRYSTAL, 64),
                new ItemStack(Material.OBSIDIAN, 64),
                new ItemStack(Material.RESPAWN_ANCHOR, 64),
                new ItemStack(Material.GLOWSTONE, 64),
                new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 64),
                new ItemStack(Material.GOLDEN_APPLE, 64),
                new ItemStack(Material.TOTEM_OF_UNDYING, 1),
                new ItemStack(Material.ENDER_PEARL, 16),
                new ItemStack(Material.EXPERIENCE_BOTTLE, 64),
                new ItemStack(Material.ARROW, 64),
                new ItemStack(Material.BOW, 1),
                new ItemStack(Material.CROSSBOW, 1),
                new ItemStack(Material.FIREWORK_ROCKET, 64),
                new ItemStack(Material.ELYTRA, 1),
                new ItemStack(Material.COBWEB, 64),
                new ItemStack(Material.COOKED_BEEF, 64)
        );

        int index = 0;
        for (ItemStack item : supply) {
            if (index >= inv.getSize()) break;
            inv.setItem(index++, item);
        }
        return inv;
    }

    public void saveKit(Player player, int slot) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] storage = cloneItems(inv.getStorageContents());
        ItemStack[] armor = cloneItems(inv.getArmorContents());

        PlayerKit kit = new PlayerKit(storage, armor);
        playerKits.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(slot, kit);

        String basePath = "players." + player.getUniqueId() + ".kits." + slot;
        kitsConfig.set(basePath + ".storage", storage);
        kitsConfig.set(basePath + ".armor", armor);
        saveKitsToDisk();
    }

    public boolean hasKit(UUID playerId, int slot) {
        Map<Integer, PlayerKit> map = playerKits.get(playerId);
        return map != null && map.containsKey(slot);
    }

    public boolean applyKit(Player player, int slot) {
        Map<Integer, PlayerKit> map = playerKits.get(player.getUniqueId());
        if (map == null || !map.containsKey(slot)) {
            return false;
        }

        PlayerKit kit = map.get(slot);
        PlayerInventory inv = player.getInventory();

        ItemStack[] armor = kit.armor();
        for (int i = 0; i < armor.length; i++) {
            ItemStack piece = armor[i];
            if (piece == null || piece.getType().isAir()) continue;

            ItemStack clone = piece.clone();
            if (i == 0 && (inv.getBoots() == null || inv.getBoots().getType().isAir())) {
                inv.setBoots(clone);
            } else if (i == 1 && (inv.getLeggings() == null || inv.getLeggings().getType().isAir())) {
                inv.setLeggings(clone);
            } else if (i == 2 && (inv.getChestplate() == null || inv.getChestplate().getType().isAir())) {
                inv.setChestplate(clone);
            } else if (i == 3 && (inv.getHelmet() == null || inv.getHelmet().getType().isAir())) {
                inv.setHelmet(clone);
            } else {
                giveOrDrop(player, clone);
            }
        }

        for (ItemStack item : kit.storage()) {
            if (item == null || item.getType().isAir()) continue;
            giveOrDrop(player, item.clone());
        }

        return true;
    }

    private void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
        for (ItemStack drop : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

    private ItemStack[] cloneItems(ItemStack[] source) {
        ItemStack[] copy = new ItemStack[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = (source[i] == null || source[i].getType().isAir()) ? null : source[i].clone();
        }
        return copy;
    }

    public void loadKitsFromDisk() {
        playerKits.clear();
        if (!kitsFile.exists()) {
            try {
                kitsFile.getParentFile().mkdirs();
                kitsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create kits.yml: " + e.getMessage());
            }
        }
        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);

        ConfigurationSection playersSec = kitsConfig.getConfigurationSection("players");
        if (playersSec == null) return;

        for (String uuidStr : playersSec.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            ConfigurationSection kitsSec = playersSec.getConfigurationSection(uuidStr + ".kits");
            if (kitsSec == null) continue;

            Map<Integer, PlayerKit> slotMap = new HashMap<>();
            for (String slotStr : kitsSec.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(slotStr);
                } catch (NumberFormatException ignored) {
                    continue;
                }

                List<?> storageRaw = kitsSec.getList(slotStr + ".storage");
                List<?> armorRaw = kitsSec.getList(slotStr + ".armor");

                ItemStack[] storage = toItemArray(storageRaw, 36);
                ItemStack[] armor = toItemArray(armorRaw, 4);

                slotMap.put(slot, new PlayerKit(storage, armor));
            }

            if (!slotMap.isEmpty()) {
                playerKits.put(uuid, slotMap);
            }
        }
    }

    private ItemStack[] toItemArray(List<?> rawList, int expectedSize) {
        ItemStack[] result = new ItemStack[expectedSize];
        if (rawList == null) return result;

        for (int i = 0; i < Math.min(rawList.size(), expectedSize); i++) {
            Object obj = rawList.get(i);
            if (obj instanceof ItemStack stack && !stack.getType().isAir()) {
                result[i] = stack.clone();
            }
        }
        return result;
    }

    private void saveKitsToDisk() {
        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save kits.yml: " + e.getMessage());
        }
    }

    public void clearAll() {
        saveKitsToDisk();
        playerKits.clear();
    }

    private record PlayerKit(ItemStack[] storage, ItemStack[] armor) {}
}