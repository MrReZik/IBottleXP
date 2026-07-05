package me.mrrezik.ibottlexp.models;

import java.util.List;

public class Bottle {

    private final String id;
    private final String name;
    private final int levels;
    private final List<String> lore;
    private final String sound;
    private final float soundVolume;
    private final float soundPitch;
    private final int cooldown;
    private final String actionBar;

    public Bottle(String id, String name, int levels, List<String> lore,
                  String sound, float soundVolume, float soundPitch,
                  int cooldown, String actionBar) {
        this.id = id;
        this.name = name;
        this.levels = levels;
        this.lore = lore;
        this.sound = sound;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
        this.cooldown = cooldown;
        this.actionBar = actionBar;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getLevels() { return levels; }
    public List<String> getLore() { return lore; }
    public String getSound() { return sound; }
    public float getSoundVolume() { return soundVolume; }
    public float getSoundPitch() { return soundPitch; }
    public int getCooldown() { return cooldown; }
    public String getActionBar() { return actionBar; }
}
