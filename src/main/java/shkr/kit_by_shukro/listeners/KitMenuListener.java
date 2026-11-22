package shkr.kit_by_shukro.listeners;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class KitMenuListener implements Listener {

    private final Plugin plugin;

    public KitMenuListener(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean isSupplyMenu(Inventory topInv) {
        String rawTitle = plugin.getConfig().getString("menu-title", "&8PvP Kit Supply");
        String expected = rawTitle.replace('&', '§');
        String actual = LegacyComponentSerializer.legacySection().serialize(topInv.getViewers().isEmpty()
                ? net.kyori.adventure.text.Component.empty()
                : topInv.getViewers().get(0).getOpenInventory().title());
        return topInv.getSize() == 54 && actual.equals(expected);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!isSupplyMenu(top)) return;

        if (event.getClickedInventory() == top) {
            int rawSlot = event.getRawSlot();
            ItemStack clicked = top.getItem(rawSlot);

            if (clicked == null || clicked.getType().isAir()) {
                event.setCancelled(true);
                return;
            }

            ItemStack snapshot = clicked.clone();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (top.getItem(rawSlot) == null || top.getItem(rawSlot).getType().isAir()) {
                    top.setItem(rawSlot, snapshot);
                }
            });
        } else if (event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!isSupplyMenu(top)) return;

        for (int slot : event.getRawSlots()) {
            if (slot < top.getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }
}