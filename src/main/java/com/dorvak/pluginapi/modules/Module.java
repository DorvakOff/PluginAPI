package com.dorvak.pluginapi.modules;

import org.bukkit.event.Listener;

import java.util.List;

public abstract class Module implements Listener {

    private boolean enabled = false;
    private final boolean enabledByDefault;
    private final String name;

    protected Module(String name) {
        this(name, true);
    }

    protected Module(String name, boolean enabledByDefault) {
        this.name = name;
        this.enabledByDefault = enabledByDefault;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isDisabled() {
        return !enabled;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public List<Listener> getListeners() {
        return List.of(this);
    }

    public String getName() {
        return name;
    }
}
