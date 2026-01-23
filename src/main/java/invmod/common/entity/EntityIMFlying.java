package invmod.common.entity;

import invmod.common.IBlockAccessExtended;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.MathUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public abstract class EntityIMFlying extends EntityIMLiving {
    private static final int META_TARGET_X = 29;
    private static final int META_TARGET_Y = 30;
    private static final int META_TARGET_Z = 31;
    private static final int META_THRUST_DATA = 28;
    private static final int META_FLYSTATE = 27;
    private static final EntityDataAccessor<Integer> DATA_TARGET_X = SynchedEntityData.defineId(EntityIMFlying.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_Y = SynchedEntityData.defineId(EntityIMFlying.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_Z = SynchedEntityData.defineId(EntityIMFlying.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_THRUST = SynchedEntityData.defineId(EntityIMFlying.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_FLYSTATE = SynchedEntityData.defineId(EntityIMFlying.class, EntityDataSerializers.INT);
    private final NavigatorFlying navigatorFlying;
    private final IMMoveHelperFlying i;
    private final IMLookHelper h;
    private final IMBodyHelper bn;
    private FlyState flyState;
    private float liftFactor;
    private float maxPoweredFlightSpeed;
    private float thrust;
    private float thrustComponentRatioMin;
    private float thrustComponentRatioMax;
    private float maxTurnForce;
    private float optimalPitch;
    private float landingSpeedThreshold;
    private float maxRunSpeed;
    private float flightAccelX;
    private float flightAccelY;
    private float flightAccelZ;
    private boolean thrustOn;
    private float thrustEffort;
    private boolean flyPathfind;
    private boolean debugFlying;

    public EntityIMFlying(EntityType<? extends EntityIMFlying> type, Level level) {
        this(type, level, null);
    }

    public EntityIMFlying(EntityType<? extends EntityIMFlying> type, Level level, INexusAccess nexus) {
        super(type, level);
        if (nexus != null) {
            acquiredByNexus(nexus);
        }
        this.debugFlying = true;
        this.flyState = FlyState.GROUNDED;
        this.maxPoweredFlightSpeed = 0.28F;
        this.liftFactor = 0.4F;
        this.thrust = 0.08F;
        this.thrustComponentRatioMin = 0.0F;
        this.thrustComponentRatioMax = 0.1F;
        this.maxTurnForce = (getLegacyGravity() * 3.0F);
        this.optimalPitch = 52.0F;
        this.landingSpeedThreshold = (getMoveSpeedStat() * 1.2F);
        this.maxRunSpeed = 0.45F;
        this.thrustOn = false;
        this.thrustEffort = 1.0F;
        this.flyPathfind = true;

        this.i = new IMMoveHelperFlying(this);
        this.h = new IMLookHelper(this);
        this.bn = new IMBodyHelper(this);
        IPathSource pathSource = getPathSource();
        pathSource.setSearchDepth(800);
        pathSource.setQuickFailDepth(200);
        this.navigatorFlying = new NavigatorFlying(this, pathSource);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TARGET_X, 0);
        builder.define(DATA_TARGET_Y, 0);
        builder.define(DATA_TARGET_Z, 0);
        builder.define(DATA_THRUST, (byte) 0);
        builder.define(DATA_FLYSTATE, flyState.ordinal());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (this.debugFlying) {
                Vec3 target = this.navigatorFlying.getTarget();
                float oldTargetX = MathUtil.unpackFloat(entityData.get(DATA_TARGET_X));
                float oldTargetY = MathUtil.unpackFloat(entityData.get(DATA_TARGET_Y));
                float oldTargetZ = MathUtil.unpackFloat(entityData.get(DATA_TARGET_Z));

                if ((!MathUtil.floatEquals(oldTargetX, (float) target.x, 0.1F)) || (!MathUtil.floatEquals(oldTargetY, (float) target.y, 0.1F)) || (!MathUtil.floatEquals(oldTargetZ, (float) target.z, 0.1F))) {
                    entityData.set(DATA_TARGET_X, MathUtil.packFloat((float) target.x));
                    entityData.set(DATA_TARGET_Y, MathUtil.packFloat((float) target.y));
                    entityData.set(DATA_TARGET_Z, MathUtil.packFloat((float) target.z));
                }
            }

            byte thrustData = entityData.get(DATA_THRUST);
            int oldThrustOn = thrustData & 0x1;
            int oldThrustEffortEncoded = thrustData >> 1 & 0xF;
            int thrustEffortEncoded = (int) (this.thrustEffort * 15.0F);
            if (this.thrustOn == oldThrustOn > 0) {
                if (thrustEffortEncoded == oldThrustEffortEncoded)
                    ;
            } else {
                entityData.set(DATA_THRUST, (byte) (thrustEffortEncoded << 1 | oldThrustOn));
            }

        } else {
            if (this.debugFlying) {
                float x = MathUtil.unpackFloat(entityData.get(DATA_TARGET_X));
                float y = MathUtil.unpackFloat(entityData.get(DATA_TARGET_Y));
                float z = MathUtil.unpackFloat(entityData.get(DATA_TARGET_Z));
                this.navigatorFlying.setTarget(x, y, z);
            }

            this.flyState = FlyState.values()[entityData.get(DATA_FLYSTATE)];

            byte thrustData = entityData.get(DATA_THRUST);
            this.thrustOn = ((thrustData & 0x1) > 0);
            this.thrustEffort = ((thrustData >> 1 & 0xF) / 15.0F);
        }
    }

    public FlyState getFlyState() {
        return this.flyState;
    }

    protected void setFlyState(FlyState flyState) {
        this.flyState = flyState;
        if (!level().isClientSide) {
            entityData.set(DATA_FLYSTATE, flyState.ordinal());
        }
    }

    public boolean isThrustOn() {
        return entityData.get(DATA_THRUST) != 0;
    }

    protected void setThrustOn(boolean flag) {
        this.thrustOn = flag;
    }

    public float getThrustEffort() {
        return this.thrustEffort;
    }

    protected void setThrustEffort(float effortFactor) {
        this.thrustEffort = effortFactor;
    }

    public Vec3 getFlyTarget() {
        return this.navigatorFlying.getTarget();
    }

    public INavigationFlying getNavigatorNew() {
        return this.navigatorFlying;
    }

    public IMMoveHelperFlying getMoveHelper() {
        return this.i;
    }

    public IMLookHelper getLookHelper() {
        return this.h;
    }

    public IMBodyHelper getBodyHelper() {
        return this.bn;
    }

    public void moveEntityWithHeading(float x, float z) {
        travel(new Vec3(x, getDeltaMovement().y, z));
    }

    public boolean isOnLadder() {
        return false;
    }

    public boolean hasFlyingDebug() {
        return this.debugFlying;
    }

    protected void setPathfindFlying(boolean flag) {
        this.flyPathfind = flag;
    }

    public float getMaxPoweredFlightSpeed() {
        return this.maxPoweredFlightSpeed;
    }

    protected void setMaxPoweredFlightSpeed(float speed) {
        this.maxPoweredFlightSpeed = speed;
        getNavigatorNew().setFlySpeed(speed);
    }

    protected float getLiftFactor() {
        return this.liftFactor;
    }

    protected void setLiftFactor(float liftFactor) {
        this.liftFactor = liftFactor;
    }

    protected float getThrust() {
        return this.thrust;
    }

    protected void setThrust(float thrust) {
        this.thrust = thrust;
    }

    protected float getThrustComponentRatioMin() {
        return this.thrustComponentRatioMin;
    }

    protected void setThrustComponentRatioMin(float ratio) {
        this.thrustComponentRatioMin = ratio;
    }

    protected float getThrustComponentRatioMax() {
        return this.thrustComponentRatioMax;
    }

    protected void setThrustComponentRatioMax(float ratio) {
        this.thrustComponentRatioMax = ratio;
    }

    protected float getMaxTurnForce() {
        return this.maxTurnForce;
    }

    protected void setMaxTurnForce(float maxTurnForce) {
        this.maxTurnForce = maxTurnForce;
    }

    protected float getMaxPitch() {
        return this.optimalPitch;
    }

    protected float getLandingSpeedThreshold() {
        return this.landingSpeedThreshold;
    }

    protected void setLandingSpeedThreshold(float speed) {
        this.landingSpeedThreshold = speed;
    }

    protected float getMaxRunSpeed() {
        return this.maxRunSpeed;
    }

    protected void setMaxRunSpeed(float speed) {
        this.maxRunSpeed = speed;
    }

    protected void setFlightAccelerationVector(float xAccel, float yAccel, float zAccel) {
        this.flightAccelX = xAccel;
        this.flightAccelY = yAccel;
        this.flightAccelZ = zAccel;
    }

    protected void setOptimalPitch(float pitch) {
        this.optimalPitch = pitch;
    }

    protected void fall(float par1) {
    }

    protected void updateFallState(double par1, boolean par3) {
    }

    @Override
    protected void calcPathOptions(BlockGetter terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        if (!this.flyPathfind)
            super.calcPathOptions(terrainMap, currentNode, pathFinder);
        else
            calcPathOptionsFlying(terrainMap, currentNode, pathFinder);
    }

    protected void calcPathOptionsFlying(BlockGetter terrainMap, PathNode currentNode, PathfinderIM pathFinder) {
        if ((currentNode.yCoord <= 0) || (currentNode.yCoord > 255)) {
            return;
        }

        if (getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord) > 0) {
            pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.NONE);
        }

        if (getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord) > 0) {
            pathFinder.addNode(currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord, PathAction.NONE);
        }

        for (int i = 0; i < 4; i++) {
            if (getCollide(terrainMap, currentNode.xCoord + invmod.common.util.CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + invmod.common.util.CoordsInt.offsetAdjZ[i]) > 0) {
                pathFinder.addNode(currentNode.xCoord + invmod.common.util.CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + invmod.common.util.CoordsInt.offsetAdjZ[i], PathAction.NONE);
            }
        }
        if (canSwimHorizontal()) {
            for (int i = 0; i < 4; i++) {
                if (getCollide(terrainMap, currentNode.xCoord + invmod.common.util.CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + invmod.common.util.CoordsInt.offsetAdjZ[i]) == -1)
                    pathFinder.addNode(currentNode.xCoord + invmod.common.util.CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + invmod.common.util.CoordsInt.offsetAdjZ[i], PathAction.SWIM);
            }
        }
    }

    protected float calcBlockPathCost(PathNode prevNode, PathNode node, BlockGetter terrainMap) {
        float multiplier = 1.0F;
        if ((terrainMap instanceof IBlockAccessExtended)) {
            int mobDensity = ((IBlockAccessExtended) terrainMap).getLayeredData(node.xCoord, node.yCoord, node.zCoord) & 0x7;
            multiplier += mobDensity * 3;
        }

        for (int i = -1; i > -6; i--) {
            Block block = terrainMap.getBlockState(new net.minecraft.core.BlockPos(node.xCoord, node.yCoord + i, node.zCoord)).getBlock();
            if (block != Blocks.AIR) {
                int blockType = getBlockType(block);
                if (blockType != 1) {
                    multiplier += 1.0F - -i * 0.2F;
                    if ((blockType != 2) || (i < -2))
                        break;
                    multiplier = (float) (multiplier + (6.0D - -i * 2.0D));
                    break;
                }

            }

        }

        for (int i = 0; i < 4; i++) {
            for (int j = 1; j <= 2; j++) {
                Block block = terrainMap.getBlockState(new net.minecraft.core.BlockPos(node.xCoord + invmod.common.util.CoordsInt.offsetAdjX[i] * j, node.yCoord, node.zCoord + invmod.common.util.CoordsInt.offsetAdjZ[i] * j)).getBlock();
                int blockType = getBlockType(block);
                if (blockType != 1) {
                    multiplier += 1.5F - j * 0.5F;
                    if ((blockType != 2) || (i < -2))
                        break;
                    multiplier += 6.0F - j * 2.0F;
                    break;
                }

            }

        }

        if (node.action == PathAction.SWIM) {
            Block above = terrainMap.getBlockState(new net.minecraft.core.BlockPos(node.xCoord, node.yCoord + 1, node.zCoord)).getBlock();
            multiplier *= ((node.yCoord <= prevNode.yCoord) && (above != Blocks.AIR) ? 3.0F : 1.0F);
            return prevNode.distanceTo(node) * 1.3F * multiplier;
        }

        Block block = terrainMap.getBlockState(new net.minecraft.core.BlockPos(node.xCoord, node.yCoord, node.zCoord)).getBlock();
        if (EntityIMLiving.blockCosts.containsKey(block)) {
            return prevNode.distanceTo(node) * ((Float) EntityIMLiving.blockCosts.get(block)).floatValue() * multiplier;
        }
        if (block.defaultBlockState().isCollisionShapeFullBlock(terrainMap, new net.minecraft.core.BlockPos(node.xCoord, node.yCoord, node.zCoord))) {
            return prevNode.distanceTo(node) * 3.2F * multiplier;
        }

        return prevNode.distanceTo(node) * 1.0F * multiplier;
    }

    private int getBlockType(Block block) {
        if (block == Blocks.AIR) {
            return 0;
        }
        if (block == Blocks.WATER || block == Blocks.LAVA) {
            return 2;
        }
        return 1;
    }
}
