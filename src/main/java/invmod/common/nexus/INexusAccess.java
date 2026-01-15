package invmod.common.nexus;

import invmod.common.entity.EntityIMLiving;
import invmod.common.entity.ai.AttackerAI;
import invmod.common.util.IPosition;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface INexusAccess extends IPosition {
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

    default Level getWorld() {
        return getLevel();
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

    default List<EntityIMLiving> getMobList() {
        return Collections.emptyList();
    }

    default void registerMob(EntityIMLiving mob) {
    }

    default void askForRespawn(EntityIMLiving mob) {
    }

    default Map<String, Long> getBoundPlayers() {
        return Collections.emptyMap();
    }

    default AttackerAI getAttackerAI() {
        return null;
    }
}
