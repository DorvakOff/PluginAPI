package com.dorvak.pluginapi.gui;

import com.dorvak.pluginapi.APIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GuiManager implements Listener {

    private final APIPlugin plugin;
    private final Map<String, Class<? extends Gui>> guis;

    public GuiManager(APIPlugin plugin) {
        this.plugin = plugin;
        this.guis = new HashMap<>();
        init();
    }

    private void init() {
        this.guis.clear();

        Reflections reflections = new Reflections();
        reflections.getSubTypesOf(Gui.class).forEach(gui -> {
            if (gui.isAnnotationPresent(CustomGui.class)) {
                String guiName = gui.getAnnotation(CustomGui.class).name();
                if (this.guis.containsKey(guiName)) {
                    plugin.getLogger().severe("Duplicate gui name " + guiName + " found!");
                }
                this.guis.put(guiName, gui);
            } else {
                plugin.getLogger().severe("Gui " + gui.getName() + " does not have a CustomGui annotation!");
            }
        });
    }

    public void open(String guiName, Player player) {
        if (this.guis.containsKey(guiName)) {
            try {
                Gui gui = this.guis.get(guiName).getConstructor(Player.class).newInstance(player);
                gui.open();
                plugin.getLogger().info("Opened gui " + guiName + " for player " + player.getName() + "!");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to open gui " + guiName + " for player " + player.getName() + "!");
            }
        } else {
            plugin.getLogger().severe("Gui " + guiName + " not found!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof GuiHolder)) {
            return;
        }

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        String action = meta.getCustomTagContainer().getCustomTag(PluginKeys.GUI_ACTION, ItemTagType.STRING);
        String guiName = meta.getCustomTagContainer().getCustomTag(PluginKeys.GUI_NAME, ItemTagType.STRING);

        if (Objects.toString(action, "").trim().isEmpty() || Objects.toString(guiName, "").trim().isEmpty()) {
            return;
        }

        if ("close".equalsIgnoreCase(action)) {
            event.getWhoClicked().closeInventory();
        } else {
            boolean success = false;
            try {
                Gui gui = this.guis.get(guiName).getConstructor(Player.class).newInstance(player);
                success = gui.onAction(action, event);
            } catch (Exception e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to handle action " + action + " for gui " + guiName + "!");
            }

            if (success) {
                player.playSound(player.getLocation(), "ui.button.click", 1, 1);
            } else {
                player.playSound(player.getLocation(), "ui.button.click", 1, 0.5f);
            }
        }
    }

}
