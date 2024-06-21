package com.dorvak.pluginapi.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Gui {

    protected Player player;
    protected int rows;
    protected String title;
    protected Inventory inventory;
    private final Map<Character, ItemStack> templateContents;
    private final Map<Integer, ItemStack> itemsToSet;

    public Gui(Player player, int rows, String title) {
        this.player = player;
        this.rows = rows;
        this.title = title;
        this.itemsToSet = new HashMap<>();
        this.inventory = Bukkit.createInventory(new GuiHolder(), this.getSize(), this.getTitle());
        this.templateContents = new HashMap<>();
    }

    public void setItem(int slot, String action, ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        CustomItemTagContainer container = meta.getCustomTagContainer();
        String guiName = this.getClass().getAnnotation(CustomGui.class).name();

        container.setCustomTag(PluginKeys.GUI_ACTION, ItemTagType.STRING, action);
        container.setCustomTag(PluginKeys.GUI_NAME, ItemTagType.STRING, guiName);

        itemStack.setItemMeta(meta);

        this.itemsToSet.put(slot, itemStack);
    }

    public String getTitle() {
        return ChatColor.translateAlternateColorCodes('&', this.title);
    }

    public int getSize() {
        if (this.rows < 1) {
            return 9;
        }
        if (this.rows > 6) {
            return 54;
        }
        return this.rows * 9;
    }

    public abstract void init();

    public abstract char[] getTemplate();

    public void open() {
        this.templateContents.clear();
        this.inventory.clear();
        this.init();
        char[] template = this.getTemplate();

        if (template.length != this.getSize()) {
            throw new IllegalArgumentException("Template size must be equal to the inventory size!");
        }

        for (int i = 0; i < template.length; i++) {
            if (Objects.toString(template[i], "").trim().isEmpty()) {
                continue;
            }
            this.inventory.setItem(i, this.templateContents.get(template[i]));
        }

        for (Map.Entry<Integer, ItemStack> entry : this.itemsToSet.entrySet()) {
            this.inventory.setItem(entry.getKey(), entry.getValue());
        }

        this.player.openInventory(this.inventory);
    }

    public void close() {
        if (this.player.getOpenInventory().getTopInventory().getHolder() instanceof GuiHolder) {
            this.player.closeInventory();
        }
    }

    public void setItem(char key, Material material) {
        this.setItem(key, new ItemStack(material));
    }

    public void setItem(char key, ItemStack itemStack) {
        if (this.templateContents.containsKey(key)) {
            return;
        }
        this.templateContents.put(key, itemStack);
    }

    public void setItem(char key, String action, Material material) {
        this.setItem(key, action, new ItemStack(material));
    }

    public void setItem(char key, String action, ItemStack itemStack) {
        if (this.templateContents.containsKey(key)) {
            return;
        }

        if (Objects.toString(action, "").trim().isEmpty()) {
            this.setItem(key, itemStack);
            return;
        }

        ItemMeta meta = itemStack.getItemMeta();
        CustomItemTagContainer container = meta.getCustomTagContainer();
        String guiName = this.getClass().getAnnotation(CustomGui.class).name();

        container.setCustomTag(PluginKeys.GUI_ACTION, ItemTagType.STRING, action);
        container.setCustomTag(PluginKeys.GUI_NAME, ItemTagType.STRING, guiName);

        itemStack.setItemMeta(meta);

        this.setItem(key, itemStack);
    }

    public void setEmptyItem(char key, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        this.setItem(key, item);
    }

    public boolean onAction(String action, InventoryClickEvent event) {
        throw new UnsupportedOperationException("Action " + action + " not supported!");
    }
}
