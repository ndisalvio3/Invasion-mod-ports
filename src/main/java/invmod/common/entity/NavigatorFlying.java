package invmod.common.entity;

import net.minecraft.world.entity.Entity;

public class NavigatorFlying extends NavigatorIM implements INavigationFlying {
    private MoveType moveType = MoveType.MIXED;

    public NavigatorFlying(EntityIMLiving entity, IPathSource pathSource) {
        super(entity, pathSource);
    }

    @Override
    public void setMovementType(MoveType moveType) {
        this.moveType = moveType;
    }

    @Override
    public void setLandingPath() {
    }

    @Override
    public void setCirclingPath(Entity entity, float radius, float height) {
    }

    @Override
    public void setCirclingPath(double x, double y, double z, float radius, float height) {
    }

    @Override
    public float getDistanceToCirclingRadius() {
        return 0.0F;
    }

    @Override
    public boolean isCircling() {
        return false;
    }

    @Override
    public void setFlySpeed(float speed) {
        setSpeed(speed);
    }

    @Override
    public void setPitchBias(float min, float max) {
    }

    @Override
    public void enableDirectTarget(boolean enabled) {
    }
}
