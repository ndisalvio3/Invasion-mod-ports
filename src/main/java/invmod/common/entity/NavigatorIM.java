package invmod.common.entity;

import invmod.common.INotifyTask;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

public class NavigatorIM implements INotifyTask, INavigation {
    protected final EntityIMLiving entity;
    protected final PathNavigation navigation;
    protected float moveSpeed;
    protected Entity targetEntity;
    protected int lastActionResult;

    public NavigatorIM(EntityIMLiving entity, IPathSource pathSource) {
        this.entity = entity;
        this.navigation = entity.getNavigation();
    }

    public PathfinderMob getEntity() {
        return entity;
    }

    @Override
    public PathAction getCurrentWorkingAction() {
        return PathAction.NONE;
    }

    @Override
    public void setSpeed(float speed) {
        this.moveSpeed = speed;
    }

    @Override
    public Path getPathToXYZ(double x, double y, double z, float targetRadius) {
        return null;
    }

    @Override
    public boolean tryMoveToXYZ(double x, double y, double z, float targetRadius, float speed) {
        this.moveSpeed = speed;
        navigation.moveTo(x, y, z, speed);
        return true;
    }

    @Override
    public Path getPathTowardsXZ(double x, double z, int min, int max, int verticalRange) {
        return null;
    }

    @Override
    public boolean tryMoveTowardsXZ(double x, double z, int min, int max, int verticalRange, float speed) {
        this.moveSpeed = speed;
        navigation.moveTo(x, entity.getY(), z, speed);
        return true;
    }

    @Override
    public Path getPathToEntity(Entity entity, float targetRadius) {
        return null;
    }

    @Override
    public boolean tryMoveToEntity(Entity entity, float targetRadius, float speed) {
        this.targetEntity = entity;
        this.moveSpeed = speed;
        return navigation.moveTo(entity, speed);
    }

    @Override
    public void autoPathToEntity(Entity entity) {
        this.targetEntity = entity;
    }

    @Override
    public boolean setPath(Path path, float speed) {
        this.moveSpeed = speed;
        return false;
    }

    @Override
    public boolean isWaitingForTask() {
        return false;
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public void onUpdateNavigation() {
        navigation.tick();
    }

    @Override
    public int getLastActionResult() {
        return lastActionResult;
    }

    @Override
    public boolean noPath() {
        return navigation.isDone();
    }

    @Override
    public int getStuckTime() {
        return 0;
    }

    @Override
    public float getLastPathDistanceToTarget() {
        return 0.0F;
    }

    @Override
    public void clearPath() {
        navigation.stop();
    }

    @Override
    public void haltForTick() {
    }

    @Override
    public Entity getTargetEntity() {
        return targetEntity;
    }

    @Override
    public String getStatus() {
        return navigation.isDone() ? "idle" : "moving";
    }

    @Override
    public void notifyTask(int paramInt) {
        lastActionResult = paramInt;
    }
}
