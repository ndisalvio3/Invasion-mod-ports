package invmod.common.entity;

import net.minecraft.world.entity.Entity;

public interface INavigationFlying extends INavigation {
    void setMovementType(MoveType moveType);

    void setLandingPath();

    void setCirclingPath(Entity entity, float radius, float height);

    void setCirclingPath(double x, double y, double z, float radius, float height);

    float getDistanceToCirclingRadius();

    boolean isCircling();

    void setFlySpeed(float speed);

    void setPitchBias(float min, float max);

    void enableDirectTarget(boolean enabled);

    enum MoveType {
        PREFER_WALKING, MIXED, PREFER_FLYING;
    }
}
