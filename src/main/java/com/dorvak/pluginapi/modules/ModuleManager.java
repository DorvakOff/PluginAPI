package com.dorvak.pluginapi.modules;

import com.dorvak.pluginapi.APIPlugin;
import org.bukkit.plugin.PluginManager;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

public class ModuleManager {

    private final APIPlugin plugin;
    private final Map<String, Module> modules;

    public ModuleManager(APIPlugin plugin) {
        this.plugin = plugin;
        modules = new HashMap<>();
        init();
    }

    private void init() {
        modules.clear();

        Reflections reflections = new Reflections();
        reflections.getSubTypesOf(Module.class).forEach(module -> {
            try {
                Module m = module.getDeclaredConstructor().newInstance();

                if (modules.containsKey(m.getName())) {
                    plugin.getLogger().severe("Module " + m.getName() + " is duplicated, skipping...");
                } else {
                    modules.put(m.getName(), m);
                    if (m.isEnabledByDefault()) {
                        m.enable();
                    }

                    PluginManager pluginManager = plugin.getServer().getPluginManager();
                    m.getListeners().forEach(listener -> pluginManager.registerEvents(listener, plugin));

                    plugin.getLogger().info("Module initialized : " + m.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                plugin.getLogger().severe("Failed to initialize module " + module.getSimpleName());
            }
        });
    }

    public Map<String, Module> getModules() {
        return modules;
    }

    public Module getModule(String name) {
        return modules.get(name);
    }
}
