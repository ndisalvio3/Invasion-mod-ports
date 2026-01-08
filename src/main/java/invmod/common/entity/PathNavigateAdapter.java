package invmod.common.entity;

import net.minecraft.world.entity.Entity;

public class PathNavigateAdapter {
    private final NavigatorIM navigator;

    public PathNavigateAdapter(NavigatorIM navigator) {
        this.navigator = navigator;
    }

    public void onUpdateNavigation() {
        navigator.onUpdateNavigation();
    }

    public boolean noPath() {
        return navigator.noPath();
    }

    public void clearPathEntity() {
        navigator.clearPath();
    }

    public void setSpeed(double speed) {
        navigator.setSpeed((float) speed);
    }

    public boolean tryMoveToXYZ(double x, double y, double z, double movespeed) {
        return navigator.tryMoveToXYZ(x, y, z, 0.0F, (float) movespeed);
    }

    public boolean tryMoveToEntityLiving(Entity entity, double movespeed) {
        return navigator.tryMoveToEntity(entity, 0.0F, (float) movespeed);
    }

    public boolean setPath(Path path, float movespeed) {
        return navigator.setPath(path, movespeed);
    }
}
