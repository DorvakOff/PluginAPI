package com.dorvak.pluginapi;

import com.dorvak.pluginapi.gui.GuiManager;
import com.dorvak.pluginapi.modules.ModuleManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

public abstract class APIPlugin extends JavaPlugin {

    private static APIPlugin instance;
    private GuiManager guiManager;
    private ModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;
        guiManager = new GuiManager(this);
        moduleManager = new ModuleManager(this);

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(guiManager, this);

        onStart();
    }

    @Override
    public void onDisable() {
        onStop();
    }

    abstract public void onStart();
    abstract public void onStop();


    public static void sendMessage(CommandSender player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getDescription().getPrefix() + message));
    }

    public static void broadcastMessage(String message) {
        instance.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', instance.getDescription().getPrefix() + message));
    }

    @Override
    @Nonnull
    public Logger getLogger() {
        return Logger.getLogger(getPlugin(this.getClass()).getName());
    }

    public static GuiManager getGuiManager() {
        return instance.guiManager;
    }

    public static ModuleManager getModuleManager() {
        return instance.moduleManager;
    }
}
