package invmod.common.entity;

import invmod.common.INotifyTask;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.CoordsInt;
import invmod.common.util.Distance;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathNavigationRegion;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class NavigatorIM implements INotifyTask, INavigation {
    protected static final int XZPATH_HORIZONTAL_SEARCH = 1;
    protected static final double ENTITY_TRACKING_TOLERANCE = 0.1D;
    protected static final double MINIMUM_PROGRESS = 0.01D;
    protected final EntityIMLiving theEntity;
    protected IPathSource pathSource;
    protected Path path;
    protected PathNode activeNode;
    protected Vec3 entityCentre;
    protected Entity pathEndEntity;
    protected Vec3 pathEndEntityLastPos;
    protected float moveSpeed;
    protected float pathSearchLimit;
    protected boolean noSunPathfind;
    protected int totalTicks;
    protected Vec3 lastPos;
    private Vec3 holdingPos;
    protected boolean nodeActionFinished;
    private boolean canSwim;
    protected boolean waitingForNotify;
    protected boolean actionCleared;
    protected double lastDistance;
    protected int ticksStuck;
    private boolean maintainPosOnWait;
    private int lastActionResult;
    private boolean haltMovement;
    private boolean autoPathToEntity;

    public NavigatorIM(EntityIMLiving entity, IPathSource pathSource) {
        this.theEntity = entity;
        this.pathSource = pathSource;
        this.noSunPathfind = false;
        this.lastPos = new Vec3(0.0D, 0.0D, 0.0D);
        this.pathEndEntityLastPos = new Vec3(0.0D, 0.0D, 0.0D);
        this.lastDistance = 0.0D;
        this.ticksStuck = 0;
        this.canSwim = false;
        this.waitingForNotify = false;
        this.actionCleared = true;
        this.nodeActionFinished = true;
        this.maintainPosOnWait = false;
        this.haltMovement = false;
        this.lastActionResult = 0;
        this.autoPathToEntity = false;
    }

    @Override
    public PathAction getCurrentWorkingAction() {
        if ((!this.nodeActionFinished) && (!noPath())) {
            return this.activeNode.action;
        }
        return PathAction.NONE;
    }

    protected boolean isMaintainingPos() {
        return this.maintainPosOnWait;
    }

    protected void setNoMaintainPos() {
        this.maintainPosOnWait = false;
    }

    protected void setMaintainPosOnWait(Vec3 pos) {
        this.holdingPos = pos;
        this.maintainPosOnWait = true;
    }

    @Override
    public void setSpeed(float par1) {
        this.moveSpeed = par1;
    }

    public boolean isAutoPathingToEntity() {
        return this.autoPathToEntity;
    }

    @Override
    public Entity getTargetEntity() {
        return this.pathEndEntity;
    }

    @Override
    public Path getPathToXYZ(double x, double y, double z, float targetRadius) {
        if (!canNavigate()) {
            return null;
        }
        return createPath(this.theEntity, Mth.floor(x), (int) y, Mth.floor(z), targetRadius);
    }

    @Override
    public boolean tryMoveToXYZ(double x, double y, double z, float targetRadius, float speed) {
        this.ticksStuck = 0;
        Path newPath = getPathToXYZ(x, y, z, targetRadius);
        if (newPath != null) {
            return setPath(newPath, speed);
        }
        return false;
    }

    @Override
    public Path getPathTowardsXZ(double x, double z, int min, int max, int verticalRange) {
        if (canNavigate()) {
            Vec3 target = findValidPointNear(x, z, min, max, verticalRange);
            if (target != null) {
                Path entityPath = getPathToXYZ(target.x, target.y, target.z, 0.0F);
                if (entityPath != null)
                    return entityPath;
            }
        }
        return null;
    }

    @Override
    public boolean tryMoveTowardsXZ(double x, double z, int min, int max, int verticalRange, float speed) {
        this.ticksStuck = 0;
        Path newPath = getPathTowardsXZ(x, z, min, max, verticalRange);
        if (newPath != null) {
            return setPath(newPath, speed);
        }
        return false;
    }

    @Override
    public Path getPathToEntity(Entity targetEntity, float targetRadius) {
        if (!canNavigate()) {
            return null;
        }
        return createPath(this.theEntity, targetEntity, targetRadius);
    }

    @Override
    public boolean tryMoveToEntity(Entity targetEntity, float targetRadius, float speed) {
        Path newPath = getPathToEntity(targetEntity, targetRadius);
        if (newPath != null) {
            if (setPath(newPath, speed)) {
                this.pathEndEntity = targetEntity;
                return true;
            }

            this.pathEndEntity = null;
            return false;
        }

        return false;
    }

    @Override
    public void autoPathToEntity(Entity target) {
        this.autoPathToEntity = true;
        this.pathEndEntity = target;
    }

    @Override
    public boolean setPath(Path newPath, float speed) {
        if (newPath == null) {
            this.path = null;
            return false;
        }

        this.moveSpeed = speed;
        this.lastDistance = getDistanceToActiveNode();
        this.ticksStuck = 0;
        resetStatus();

        CoordsInt size = this.theEntity.getCollideSize();
        this.entityCentre = new Vec3(size.getXCoord() * 0.5D, 0.0D, size.getZCoord() * 0.5D);

        this.path = newPath;
        this.activeNode = this.path.getPathPointFromIndex(this.path.getCurrentPathIndex());

        if (this.activeNode.action != PathAction.NONE) {
            this.nodeActionFinished = false;
        } else if ((size.getXCoord() <= 1) && (size.getZCoord() <= 1)) {
            this.path.incrementPathIndex();
            if (!this.path.isFinished()) {
                this.activeNode = this.path.getPathPointFromIndex(this.path.getCurrentPathIndex());
                if (this.activeNode.action != PathAction.NONE) {
                    this.nodeActionFinished = false;
                }
            }
        } else {
            while (this.theEntity.distanceToSqr(this.activeNode.xCoord + this.entityCentre.x, this.activeNode.yCoord + this.entityCentre.y, this.activeNode.zCoord + this.entityCentre.z) > this.theEntity.getBbWidth()) {
                this.path.incrementPathIndex();
                if (!this.path.isFinished()) {
                    this.activeNode = this.path.getPathPointFromIndex(this.path.getCurrentPathIndex());
                    if (this.activeNode.action != PathAction.NONE) {
                        this.nodeActionFinished = false;
                    }
                } else {
                    break;
                }

            }

        }

        if (this.noSunPathfind) {
            removeSunnyPath();
        }

        return true;
    }

    @Override
    public Path getPath() {
        return this.path;
    }

    @Override
    public boolean isWaitingForTask() {
        return this.waitingForNotify;
    }

    @Override
    public void onUpdateNavigation() {
        this.totalTicks += 1;
        if (this.autoPathToEntity) {
            updateAutoPathToEntity();
        }

        if (noPath()) {
            noPathFollow();
            return;
        }

        if (this.waitingForNotify) {
            if (isMaintainingPos()) {
                this.theEntity.getMoveHelper().setMoveTo(this.holdingPos.x, this.holdingPos.y, this.holdingPos.z, this.moveSpeed);
            }
            return;
        }

        if ((canNavigate()) && (this.nodeActionFinished)) {
            double distance = getDistanceToActiveNode();
            if (this.lastDistance - distance > MINIMUM_PROGRESS) {
                this.lastDistance = distance;
                this.ticksStuck -= 1;
            } else {
                this.ticksStuck += 1;
            }

            int pathIndex = this.path.getCurrentPathIndex();
            pathFollow();
            if (noPath()) {
                return;
            }
            if (this.path.getCurrentPathIndex() != pathIndex) {
                this.lastDistance = getDistanceToActiveNode();
                this.ticksStuck = 0;
                this.activeNode = this.path.getPathPointFromIndex(this.path.getCurrentPathIndex());
                if (this.activeNode.action != PathAction.NONE) {
                    this.nodeActionFinished = false;
                }
            }
        }

        if (this.nodeActionFinished) {
            if (!isPositionClearFrom(this.theEntity.getXCoord(), this.theEntity.getYCoord(), this.theEntity.getZCoord(), this.activeNode.xCoord, this.activeNode.yCoord, this.activeNode.zCoord, this.theEntity)) {
                if (this.theEntity.onPathBlocked(this.path, this)) {
                    setDoingTaskAndHoldOnPoint();
                }

            }

            if (!this.haltMovement) {
                if ((this.pathEndEntity != null) && (this.pathEndEntity.getY() - this.theEntity.getY() <= 0.0D) && (this.theEntity.distanceToSqr(this.pathEndEntity.getX(), this.pathEndEntity.getBoundingBox().minY, this.pathEndEntity.getZ()) < 4.5D))
                    this.theEntity.getMoveHelper().setMoveTo(this.pathEndEntity.getX(), this.pathEndEntity.getBoundingBox().minY, this.pathEndEntity.getZ(), this.moveSpeed);
                else {
                    this.theEntity.getMoveHelper().setMoveTo(this.activeNode.xCoord + this.entityCentre.x, this.activeNode.yCoord + this.entityCentre.y, this.activeNode.zCoord + this.entityCentre.z, this.moveSpeed);
                }
            } else {
                this.haltMovement = false;
            }

        } else if (!handlePathAction()) {
            clearPath();
        }
    }

    @Override
    public void notifyTask(int result) {
        this.waitingForNotify = false;
        this.lastActionResult = result;
    }

    @Override
    public int getLastActionResult() {
        return this.lastActionResult;
    }

    @Override
    public boolean noPath() {
        return (this.path == null) || (this.path.isFinished());
    }

    @Override
    public int getStuckTime() {
        return this.ticksStuck;
    }

    @Override
    public float getLastPathDistanceToTarget() {
        if (noPath()) {
            if ((this.path != null) && (this.path.getIntendedTarget() != null)) {
                PathNode node = this.path.getIntendedTarget();
                return (float) this.theEntity.getDistance(node.xCoord, node.yCoord, node.zCoord);
            }
            return 0.0F;
        }

        return this.path.getFinalPathPoint().distanceTo(this.path.getIntendedTarget());
    }

    @Override
    public void clearPath() {
        this.path = null;
        this.autoPathToEntity = false;
        resetStatus();
    }

    @Override
    public void haltForTick() {
        this.haltMovement = true;
    }

    @Override
    public String getStatus() {
        String s = "";
        if (this.autoPathToEntity) {
            s = s + "Auto:";
        }
        if (noPath()) {
            s = s + "NoPath:";
            return s;
        }
        s = s + "Pathing:";
        s = s + "Node[" + this.path.getCurrentPathIndex() + "/" + this.path.getCurrentPathLength() + "]:";
        if ((!this.nodeActionFinished) && (this.activeNode != null)) {
            s = s + "Action[" + this.activeNode.action + "]:";
        }
        return s;
    }

    protected Path createPath(EntityIMLiving entity, Entity target, float targetRadius) {
        return createPath(entity, Mth.floor(target.getX()), Mth.floor(target.getBoundingBox().minY), Mth.floor(target.getZ()), targetRadius);
    }

    protected Path createPath(EntityIMLiving entity, int x, int y, int z, float targetRadius) {
        BlockGetter terrainCache = getChunkCache(entity.getXCoord(), entity.getYCoord(), entity.getZCoord(), x, y, z, 16.0F);
        INexusAccess nexus = entity.getNexus();
        if (nexus != null) {
            terrainCache = nexus.getAttackerAI().wrapEntityData(terrainCache);
        }
        float maxSearchRange = 12.0F + (float) Distance.distanceBetween(entity.getX(), entity.getY(), entity.getZ(), x, y, z);
        if (this.pathSource.canPathfindNice(IPathSource.PathPriority.HIGH, maxSearchRange, this.pathSource.getSearchDepth(), this.pathSource.getQuickFailDepth())) {
            return this.pathSource.createPath(entity, x, y, z, targetRadius, maxSearchRange, terrainCache);
        }
        return null;
    }

    protected void pathFollow() {
        Vec3 vec3d = getEntityPosition();
        int maxNextLegIndex = this.path.getCurrentPathIndex() - 1;

        PathNode nextPoint = this.path.getPathPointFromIndex(this.path.getCurrentPathIndex());
        if ((nextPoint.yCoord == (int) vec3d.y) && (maxNextLegIndex < this.path.getCurrentPathLength() - 1)) {
            maxNextLegIndex++;

            boolean canConsolidate = true;
            int prevIndex = maxNextLegIndex - 2;
            if ((prevIndex >= 0) && (this.path.getPathPointFromIndex(prevIndex).action != PathAction.NONE)) {
                canConsolidate = false;
            }
            if ((canConsolidate) && (this.theEntity.canStandAt(this.theEntity.level(), Mth.floor(this.theEntity.getX()), Mth.floor(this.theEntity.getY()), Mth.floor(this.theEntity.getZ())))) {
                while ((maxNextLegIndex < this.path.getCurrentPathLength() - 1) && (this.path.getPathPointFromIndex(maxNextLegIndex).yCoord == (int) vec3d.y) && (this.path.getPathPointFromIndex(maxNextLegIndex).action == PathAction.NONE)) {
                    maxNextLegIndex++;
                }
            }

        }

        float fa = this.theEntity.getBbWidth() * 0.5F;
        fa *= fa;
        for (int j = this.path.getCurrentPathIndex(); j <= maxNextLegIndex; j++) {
            if (vec3d.distanceToSqr(this.path.getPositionAtIndex(this.theEntity, j)) < fa) {
                this.path.setCurrentPathIndex(j + 1);
            }
        }

        int xSize = (int) Math.ceil(this.theEntity.getBbWidth());
        int ySize = (int) this.theEntity.getBbHeight() + 1;
        int zSize = xSize;
        int index = maxNextLegIndex;

        while (index > this.path.getCurrentPathIndex()) {
            if (isDirectPathBetweenPoints(vec3d, this.path.getPositionAtIndex(this.theEntity, index), xSize, ySize, zSize)) {
                break;
            }
            index--;
        }

        for (int i = this.path.getCurrentPathIndex() + 1; i < index; i++) {
            if (this.path.getPathPointFromIndex(i).action != PathAction.NONE) {
                index = i;
                break;
            }

        }

        if (this.path.getCurrentPathIndex() < index)
            this.path.setCurrentPathIndex(index);
    }

    protected void noPathFollow() {
    }

    protected void updateAutoPathToEntity() {
        if (this.pathEndEntity == null)
            return;
        boolean wantsUpdate;
        if (noPath()) {
            wantsUpdate = true;
        } else {
            double d1 = Distance.distanceBetween(this.pathEndEntity, this.pathEndEntityLastPos);
            double d2 = Distance.distanceBetween(this.theEntity, this.pathEndEntityLastPos);
            if (d1 / d2 > ENTITY_TRACKING_TOLERANCE)
                wantsUpdate = true;
            else {
                wantsUpdate = false;
            }
        }
        if (wantsUpdate) {
            Path newPath = getPathToEntity(this.pathEndEntity, 0.0F);
            if (newPath != null) {
                if (setPath(newPath, this.moveSpeed)) {
                    this.pathEndEntityLastPos = new Vec3(this.pathEndEntity.getX(), this.pathEndEntity.getY(), this.pathEndEntity.getZ());
                }
            }
        }
    }

    protected double getDistanceToActiveNode() {
        if (this.activeNode != null) {
            double dX = this.activeNode.xCoord + 0.5D - this.theEntity.getX();
            double dY = this.activeNode.yCoord - this.theEntity.getY();
            double dZ = this.activeNode.zCoord + 0.5D - this.theEntity.getZ();
            return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        }
        return 0.0D;
    }

    protected boolean handlePathAction() {
        this.nodeActionFinished = true;
        return true;
    }

    protected boolean setDoingTask() {
        this.waitingForNotify = true;
        this.actionCleared = false;
        return true;
    }

    protected boolean setDoingTaskAndHold() {
        this.waitingForNotify = true;
        this.actionCleared = false;
        setMaintainPosOnWait(new Vec3(this.theEntity.getX(), this.theEntity.getY(), this.theEntity.getZ()));
        return true;
    }

    protected boolean setDoingTaskAndHoldOnPoint() {
        this.waitingForNotify = true;
        this.actionCleared = false;
        setMaintainPosOnWait(new Vec3(this.activeNode.getXCoord() + 0.5D, this.activeNode.getYCoord(), this.activeNode.getZCoord() + 0.5D));
        return true;
    }

    protected void resetStatus() {
        setNoMaintainPos();
        this.nodeActionFinished = true;
        this.actionCleared = true;
        this.waitingForNotify = false;
    }

    protected Vec3 getEntityPosition() {
        return new Vec3(this.theEntity.getX(), getPathableYPos(), this.theEntity.getZ());
    }

    protected EntityIMLiving getEntity() {
        return this.theEntity;
    }

    private int getPathableYPos() {
        if ((!this.theEntity.isInWater()) || (!this.canSwim)) {
            return (int) (this.theEntity.getBoundingBox().minY + 0.5D);
        }

        int i = (int) this.theEntity.getBoundingBox().minY;
        Level level = this.theEntity.level();
        BlockPos pos = new BlockPos(Mth.floor(this.theEntity.getX()), i, Mth.floor(this.theEntity.getZ()));
        BlockState state = level.getBlockState(pos);
        int k = 0;

        while (state.getFluidState().is(FluidTags.WATER)) {
            i++;
            pos = pos.above();
            state = level.getBlockState(pos);

            k++;
            if (k > 16) {
                return (int) this.theEntity.getBoundingBox().minY;
            }
        }

        return i;
    }

    protected boolean canNavigate() {
        return true;
    }

    protected boolean isInLiquid() {
        return (this.theEntity.isInWater()) || (this.theEntity.isInLava());
    }

    protected Vec3 findValidPointNear(double x, double z, int min, int max, int verticalRange) {
        double xOffset = x - this.theEntity.getX();
        double zOffset = z - this.theEntity.getZ();
        double h = Math.sqrt(xOffset * xOffset + zOffset * zOffset);

        if (h < 0.5D) {
            return null;
        }

        int range = Math.max(1, max - min);
        double distance = min + this.theEntity.getRNG().nextInt(range);
        int xi = Mth.floor(xOffset * (distance / h) + this.theEntity.getX());
        int zi = Mth.floor(zOffset * (distance / h) + this.theEntity.getZ());
        int y = Mth.floor(this.theEntity.getY());

        for (int vertical = 0; vertical < verticalRange; vertical = vertical > 0 ? vertical * -1 : vertical * -1 + 1) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (this.theEntity.canStandAtAndIsValid(this.theEntity.level(), xi + i, y + vertical, zi + j)) {
                        return new Vec3(xi + i, y + vertical, zi + j);
                    }
                }
            }
        }

        return null;
    }

    protected void removeSunnyPath() {
        Level level = this.theEntity.level();
        BlockPos entityPos = new BlockPos(Mth.floor(this.theEntity.getX()), (int) (this.theEntity.getBoundingBox().minY + 0.5D), Mth.floor(this.theEntity.getZ()));
        if (level.canSeeSky(entityPos)) {
            return;
        }

        for (int i = 0; i < this.path.getCurrentPathLength(); i++) {
            PathNode pathpoint = this.path.getPathPointFromIndex(i);
            if (level.canSeeSky(new BlockPos(pathpoint.xCoord, pathpoint.yCoord, pathpoint.zCoord))) {
                this.path.setCurrentPathLength(i - 1);
                return;
            }
        }
    }

    protected boolean isDirectPathBetweenPoints(Vec3 pos1, Vec3 pos2, int xSize, int ySize, int zSize) {
        int x = Mth.floor(pos1.x);
        int z = Mth.floor(pos1.z);
        double dX = pos2.x - pos1.x;
        double dZ = pos2.z - pos1.z;
        double dXZsq = dX * dX + dZ * dZ;

        if (dXZsq < 1.0E-008D) {
            return false;
        }

        double scale = 1.0D / Math.sqrt(dXZsq);
        dX *= scale;
        dZ *= scale;
        xSize += 2;
        zSize += 2;

        if (!isSafeToStandAt(x, (int) pos1.y, z, xSize, ySize, zSize, pos1, dX, dZ)) {
            return false;
        }

        xSize -= 2;
        zSize -= 2;
        double xIncrement = 1.0D / Math.abs(dX);
        double zIncrement = 1.0D / Math.abs(dZ);
        double xOffset = x * 1 - pos1.x;
        double zOffset = z * 1 - pos1.z;

        if (dX >= 0.0D) {
            xOffset += 1.0D;
        }

        if (dZ >= 0.0D) {
            zOffset += 1.0D;
        }

        xOffset /= dX;
        zOffset /= dZ;
        byte xDirection = (byte) (dX >= 0.0D ? 1 : -1);
        byte zDirection = (byte) (dZ >= 0.0D ? 1 : -1);
        int x2 = Mth.floor(pos2.x);
        int z2 = Mth.floor(pos2.z);
        int xDiff = x2 - x;

        for (int i = z2 - z; (xDiff * xDirection > 0) || (i * zDirection > 0);) {
            if (xOffset < zOffset) {
                xOffset += xIncrement;
                x += xDirection;
                xDiff = x2 - x;
            } else {
                zOffset += zIncrement;
                z += zDirection;
                i = z2 - z;
            }

            if (!isSafeToStandAt(x, (int) pos1.y, z, xSize, ySize, zSize, pos1, dX, dZ)) {
                return false;
            }
        }

        return true;
    }

    protected boolean isSafeToStandAt(int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, Vec3 entityPostion, double par8, double par10) {
        int i = xOffset - xSize / 2;
        int j = zOffset - zSize / 2;

        if (!isPositionClear(i, yOffset, j, xSize, ySize, zSize, entityPostion, par8, par10)) {
            return false;
        }

        Level level = this.theEntity.level();
        for (int k = i; k < i + xSize; k++) {
            for (int l = j; l < j + zSize; l++) {
                double d = k + 0.5D - entityPostion.x;
                double d1 = l + 0.5D - entityPostion.z;

                if (d * par8 + d1 * par10 >= 0.0D) {
                    BlockPos pos = new BlockPos(k, yOffset - 1, l);
                    BlockState state = level.getBlockState(pos);

                    if (state.isAir()) {
                        return false;
                    }

                    if (state.getFluidState().is(FluidTags.WATER) && (!this.theEntity.isInWater())) {
                        return false;
                    }

                    if (state.getFluidState().is(FluidTags.LAVA)) {
                        return false;
                    }

                    if (!state.isCollisionShapeFullBlock(level, pos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected boolean isPositionClear(int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, Vec3 entityPostion, double vecX, double vecZ) {
        Level level = this.theEntity.level();
        for (int i = xOffset; i < xOffset + xSize; i++) {
            for (int j = yOffset; j < yOffset + ySize; j++) {
                for (int k = zOffset; k < zOffset + zSize; k++) {
                    double d = i + 0.5D - entityPostion.x;
                    double d1 = k + 0.5D - entityPostion.z;

                    if (d * vecX + d1 * vecZ >= 0.0D) {
                        BlockPos pos = new BlockPos(i, j, k);
                        BlockState state = level.getBlockState(pos);

                        if ((!state.isAir()) && (!state.getCollisionShape(level, pos, CollisionContext.of(this.theEntity)).isEmpty())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    protected boolean isPositionClearFrom(int x1, int y1, int z1, int x2, int y2, int z2, EntityIMLiving entity) {
        if (y2 > y1) {
            Level level = entity.level();
            BlockPos pos = new BlockPos(x1, y1 + entity.getCollideSize().getYCoord(), z1);
            BlockState state = level.getBlockState(pos);
            if ((!state.isAir()) && (!state.getCollisionShape(level, pos, CollisionContext.of(entity)).isEmpty())) {
                return false;
            }
        }

        return isPositionClear(x2, y2, z2, entity);
    }

    protected boolean isPositionClear(int x, int y, int z, EntityIMLiving entity) {
        CoordsInt size = entity.getCollideSize();
        return isPositionClear(x, y, z, size.getXCoord(), size.getYCoord(), size.getZCoord());
    }

    protected boolean isPositionClear(int x, int y, int z, int xSize, int ySize, int zSize) {
        Level level = this.theEntity.level();
        for (int i = x; i < x + xSize; i++) {
            for (int j = y; j < y + ySize; j++) {
                for (int k = z; k < z + zSize; k++) {
                    BlockPos pos = new BlockPos(i, j, k);
                    BlockState state = level.getBlockState(pos);

                    if ((!state.isAir()) && (!state.getCollisionShape(level, pos, CollisionContext.of(this.theEntity)).isEmpty())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    protected PathNavigationRegion getChunkCache(int x1, int y1, int z1, int x2, int y2, int z2, float axisExpand) {
        Level level = this.theEntity.level();
        if (level == null) {
            return null;
        }
        int d = (int) axisExpand;
        int cX2;
        int cX1;
        if (x1 < x2) {
            cX1 = x1 - d;
            cX2 = x2 + d;
        } else {
            cX2 = x1 + d;
            cX1 = x2 - d;
        }
        int cY2;
        int cY1;
        if (y1 < y2) {
            cY1 = y1 - d;
            cY2 = y2 + d;
        } else {
            cY2 = y1 + d;
            cY1 = y2 - d;
        }
        int cZ2;
        int cZ1;
        if (z1 < z2) {
            cZ1 = z1 - d;
            cZ2 = z2 + d;
        } else {
            cZ2 = z1 + d;
            cZ1 = z2 - d;
        }
        int minY = level.getMinY();
        int maxY = level.getMaxY() - 1;
        BlockPos start = new BlockPos(cX1, Math.max(minY, cY1), cZ1);
        BlockPos end = new BlockPos(cX2, Math.min(maxY, cY2), cZ2);
        return new PathNavigationRegion(level, start, end);
    }
}
