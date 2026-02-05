package invmod.common;

import invmod.Invasion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigInvasion extends Config {
    public void saveConfig(File saveFile, HashMap<Integer, Float> strengthOverrides, boolean debug) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            writer.write("# Invasion Mod config");
            writer.newLine();
            writer.write("# Delete this file to restore defaults");
            writer.newLine();
            writer.newLine();
            writer.write("# General settings and IDs");
            writer.newLine();
            writeProperty(writer, "update-messages-enabled");
            writeProperty(writer, "destructed-blocks-drop");
            writeProperty(writer, "craft-items-enabled");
            writeProperty(writer, "guiID-Nexus");
            if (debug) {
                writeProperty(writer, "debug");
            }
            writer.newLine();

            writer.write("# Nexus Continuous Mode");
            writer.newLine();
            writeProperty(writer, "min-days-to-attack");
            writeProperty(writer, "max-days-to-attack");
            writer.newLine();

            writer.write("# Mob health during invasion");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : Invasion.mobHealthInvasion.entrySet()) {
                writeProperty(writer, entry.getKey());
            }
            writer.newLine();

            writer.write("# Nighttime mob spawning behaviour (does not affect the nexus)");
            writer.newLine();
            writer.write("# mob-limit-override: The maximum number of randomly spawned mobs that may exist in the world. This applies to ALL of minecraft (default: 70)");
            writer.newLine();
            writeProperty(writer, "mob-limit-override");
            writer.newLine();
            writer.write("# night-spawns-enabled: Currently does not remove any default mobs, only adds new spawns");
            writer.newLine();
            writeProperty(writer, "night-spawns-enabled");
            writer.newLine();
            writer.write("# night-mob-spawn-chance: Higher number means mobs are more common");
            writer.newLine();
            writeProperty(writer, "night-mob-spawn-chance");
            writer.newLine();
            writer.write("# night-mob-group-size: The maximum number of mobs that may spawn together");
            writer.newLine();
            writeProperty(writer, "night-mob-max-group-size");
            writer.newLine();
            writer.write("# night-mob-sight-range: How far mobs can see a player from");
            writer.newLine();
            writeProperty(writer, "night-mob-sight-range");
            writer.newLine();
            writer.write("# night-mob-sense-range: How far mobs can smell a player (through walls)");
            writer.newLine();
            writeProperty(writer, "night-mob-sense-range");
            writer.newLine();
            writer.newLine();

            writer.write("# Nightime mob spawning tables (also does not affect the nexus)");
            writer.newLine();
            writer.write("# A spawnpool contains mobs that can possibly spawn, and the probability weight of them spawning.");
            writer.newLine();
            writer.write("# Explanation: zombie_t2_any_basic has all T2, zombie_t2_plain excludes tar zombies");
            writer.newLine();
            for (int i = 0; i < Invasion.DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length; i++) {
                writeProperty(writer, "nm-spawnpool1-slot" + (1 + i));
                writeProperty(writer, "nm-spawnpool1-slot" + (1 + i) + "-weight");
            }
            writer.newLine();

            writer.write("# Nightspawn mob health");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : Invasion.mobHealthNightspawn.entrySet()) {
                writeProperty(writer, entry.getKey());
            }

            writer.flush();
        } catch (IOException e) {
            Invasion.log(e.getMessage());
        }
    }
}
