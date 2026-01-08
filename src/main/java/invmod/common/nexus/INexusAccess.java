package invmod.common.nexus;

import net.minecraft.world.level.Level;

public interface INexusAccess {
    default void attackNexus(int damage) {
    }

    default void registerMobDied() {
    }

    default boolean isActivating() {
        return false;
    }

    default int getMode() {
        return 0;
    }

    default int getActivationTimer() {
        return 0;
    }

    default int getSpawnRadius() {
        return 0;
    }

    default int getNexusKills() {
        return 0;
    }

    default int getGeneration() {
        return 0;
    }

    default int getNexusLevel() {
        return 0;
    }

    default int getCurrentWave() {
        return 0;
    }

    default Level getLevel() {
        return null;
    }

    default int getXCoord() {
        return 0;
    }

    default int getYCoord() {
        return 0;
    }

    default int getZCoord() {
        return 0;
    }

    default java.util.List<invmod.common.entity.EntityIMLiving> getMobList() {
        return java.util.Collections.emptyList();
    }

    default java.util.Map<String, Long> getBoundPlayers() {
        return java.util.Collections.emptyMap();
    }

    default invmod.common.entity.ai.AttackerAI getAttackerAI() {
        return null;
    }
}
