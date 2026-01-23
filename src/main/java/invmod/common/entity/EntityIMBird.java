package invmod.common.entity;

import invmod.Invasion;
import invmod.common.entity.ai.AttackNexusGoal;
import invmod.common.entity.ai.IMNearestAttackableTargetGoal;
import invmod.common.nexus.INexusAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import java.util.EnumSet;

public class EntityIMBird extends Monster implements IHasNexus {
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMBird.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMBird.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMBird.class, EntityDataSerializers.INT);
    private static final double CARRY_HORIZONTAL_OFFSET = -0.35D;
    private static final double CARRY_VERTICAL_OFFSET = -0.6D;
    private static final double CARRY_RANGE = 1.4D;
    private static final int CARRY_TICKS = 120;

    private int tier;
    private int flavour;
    private INexusAccess nexus;
    private float flap;
    private float flapSpeed;
    private float oFlap;
    private float oFlapSpeed;
    private float flapping = 1.0F;
    public final LegacyWorld worldObj;
    private int debugMode;
    private String renderLabel = "";
    private float maxPoweredFlightSpeed = 1.0F;
    private float thrust;
    private float liftFactor;
    private float thrustComponentRatioMin;
    private float thrustComponentRatioMax;
    private float maxTurnForce;
    private boolean clawsForward;
    private boolean attackingWithWings;
    private invmod.common.entity.Goal aiGoal = invmod.common.entity.Goal.NONE;
    private invmod.common.entity.Goal prevAIGoal = invmod.common.entity.Goal.NONE;
    private final INavigationFlying navigatorNew = new NavigationBirdStub();

    public EntityIMBird(EntityType<? extends EntityIMBird> type, Level level) {
        super(type, level);
        this.tier = 1;
        this.flavour = 0;
        this.worldObj = new LegacyWorld(level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 18.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.FLYING_SPEED, 0.4D)
            .add(Attributes.ATTACK_DAMAGE, 2.0D)
            .add(Attributes.FOLLOW_RANGE, Invasion.getNightMobSightRange());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PickUpTargetGoal());
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true));
        goalSelector.addGoal(3, new AttackNexusGoal(this, this, 2, 2.5D));
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSenseRange(), false));
        targetSelector.addGoal(3, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSightRange(), true));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateFlapping();
    }

    public float getFlap() {
        return flap;
    }

    public float getFlapSpeed() {
        return flapSpeed;
    }

    public float getOldFlap() {
        return oFlap;
    }

    public float getOldFlapSpeed() {
        return oFlapSpeed;
    }

    public boolean isAttackingWithWings() {
        return attackingWithWings;
    }

    public void setAttackingWithWings(boolean value) {
        this.attackingWithWings = value;
    }

    public void setClawsForward(boolean value) {
        this.clawsForward = value;
    }

    public boolean getClawsForward() {
        return clawsForward;
    }

    public invmod.common.entity.Goal getAIGoal() {
        return aiGoal;
    }

    public void setAIGoal(invmod.common.entity.Goal goal) {
        this.prevAIGoal = this.aiGoal;
        this.aiGoal = goal;
    }

    public void transitionAIGoal(invmod.common.entity.Goal goal) {
        setAIGoal(goal);
    }

    public LivingEntity getAttackTarget() {
        return getTarget();
    }

    public void setAttackTarget(LivingEntity target) {
        setTarget(target);
    }

    public INavigationFlying getNavigatorNew() {
        return navigatorNew;
    }

    public float getMaxPoweredFlightSpeed() {
        return maxPoweredFlightSpeed;
    }

    public void doScreech() {
        // no-op for now
    }

    public void setName(String name) {
        setCustomName(Component.literal(name));
    }

    public void setGender(int gender) {
        // no-op for now
    }

    public void setSize(float width, float height) {
        // no-op for now
    }

    public void setGravity(float gravity) {
        // no-op for now
    }

    public float getLegacyGravity() {
        return 0.08F;
    }

    public void setThrust(float thrust) {
        this.thrust = thrust;
    }

    public void setMaxPoweredFlightSpeed(float speed) {
        this.maxPoweredFlightSpeed = speed;
    }

    public void setLiftFactor(float liftFactor) {
        this.liftFactor = liftFactor;
    }

    public void setThrustComponentRatioMin(float value) {
        this.thrustComponentRatioMin = value;
    }

    public void setThrustComponentRatioMax(float value) {
        this.thrustComponentRatioMax = value;
    }

    public void setMaxTurnForce(float value) {
        this.maxTurnForce = value;
    }

    public void setMaxHealthAndHealth(int maxHealth) {
        var attr = getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(maxHealth);
            setHealth(maxHealth);
        }
    }

    public void setBaseMoveSpeedStat(float speed) {
        var attr = getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.setBaseValue(speed);
        }
    }

    public void setDebugMode(int mode) {
        this.debugMode = mode;
    }

    public int getDebugMode() {
        return debugMode;
    }

    public void setRenderLabel(String label) {
        this.renderLabel = label;
    }

    public String getNavString() {
        return "";
    }

    public float getCarriedEntityYawOffset() {
        return 0.0F;
    }

    public void setBeakState(int state) {
        // no-op for now
    }

    public boolean attackEntityAsMob(LivingEntity target, int damage) {
        if (level() instanceof ServerLevel serverLevel) {
            target.hurt(target.damageSources().mobAttack(this), damage);
            return true;
        }
        return false;
    }

    public float getAttackRange() {
        return 1.0F;
    }

    public double getDistanceSq(double x, double y, double z) {
        return distanceToSqr(x, y, z);
    }

    public float getDistanceToEntity(Entity target) {
        return distanceTo(target);
    }

    public void doMeleeSound() {
        // no-op for now
    }

    public FlyState getFlyState() {
        return onGround() ? FlyState.GROUNDED : FlyState.FLYING;
    }

    public boolean isThrustOn() {
        return !onGround();
    }

    public float getThrustEffort() {
        return 1.0F;
    }

    public MoveState getMoveState() {
        return onGround() ? MoveState.RUNNING : MoveState.FLYING;
    }

    private static final class NavigationBirdStub implements INavigationFlying {
        @Override
        public void setMovementType(MoveType moveType) {
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
        }

        @Override
        public void setPitchBias(float min, float max) {
        }

        @Override
        public void enableDirectTarget(boolean enabled) {
        }

        @Override
        public PathAction getCurrentWorkingAction() {
            return null;
        }

        @Override
        public void setSpeed(float paramFloat) {
        }

        @Override
        public Path getPathToXYZ(double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat) {
            return null;
        }

        @Override
        public boolean tryMoveToXYZ(double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat1, float paramFloat2) {
            return false;
        }

        @Override
        public Path getPathTowardsXZ(double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, int paramInt3) {
            return null;
        }

        @Override
        public boolean tryMoveTowardsXZ(double paramDouble1, double paramDouble2, int paramInt1, int paramInt2, int paramInt3, float paramFloat) {
            return false;
        }

        @Override
        public Path getPathToEntity(Entity paramEntity, float paramFloat) {
            return null;
        }

        @Override
        public boolean tryMoveToEntity(Entity paramEntity, float paramFloat1, float paramFloat2) {
            return false;
        }

        @Override
        public void autoPathToEntity(Entity paramEntity) {
        }

        @Override
        public boolean setPath(Path paramPath, float paramFloat) {
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
        }

        @Override
        public int getLastActionResult() {
            return 0;
        }

        @Override
        public boolean noPath() {
            return true;
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
        }

        @Override
        public void haltForTick() {
        }

        @Override
        public Entity getTargetEntity() {
            return null;
        }

        @Override
        public String getStatus() {
            return "idle";
        }

        @Override
        public void notifyTask(int result) {
        }
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        if (this.hasPassenger(passenger)) {
            float yawRad = (float) Math.toRadians(this.getYRot());
            double offsetX = Math.sin(yawRad) * CARRY_HORIZONTAL_OFFSET;
            double offsetZ = -Math.cos(yawRad) * CARRY_HORIZONTAL_OFFSET;
            double yOffset = (this.getBbHeight() * 0.6D) + CARRY_VERTICAL_OFFSET;
            return this.position().add(offsetX, yOffset, offsetZ);
        }
        return super.getPassengerRidingPosition(passenger);
    }

    private void updateFlapping() {
        oFlap = flap;
        oFlapSpeed = flapSpeed;
        flapSpeed = flapSpeed + (!onGround() && !isPassenger() ? 4.0F : -1.0F) * 0.3F;
        flapSpeed = Mth.clamp(flapSpeed, 0.0F, 1.0F);
        if (!onGround() && flapping < 1.0F) {
            flapping = 1.0F;
        }
        flapping *= 0.9F;
        Vec3 delta = getDeltaMovement();
        if (!onGround() && delta.y < 0.0D) {
            setDeltaMovement(delta.multiply(1.0D, 0.6D, 1.0D));
        }
        flap = flap + flapping * 2.0F;
    }

    public void setTier(int tier) {
        this.tier = tier;
        entityData.set(DATA_TIER, tier);
        double health = 18.0D + Math.max(0, tier - 1) * 6.0D;
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        setHealth((float) health);
    }

    public int getTier() {
        return tier;
    }

    public void setFlavour(int flavour) {
        this.flavour = flavour;
        entityData.set(DATA_FLAVOUR, flavour);
    }

    public int getFlavour() {
        return flavour;
    }

    public void setTextureId(int textureId) {
        entityData.set(DATA_TEXTURE, textureId);
    }

    public int getTextureId() {
        return entityData.get(DATA_TEXTURE);
    }

    @Override
    public INexusAccess getNexus() {
        return nexus;
    }

    @Override
    public void acquiredByNexus(INexusAccess nexus) {
        this.nexus = nexus;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TIER, tier);
        builder.define(DATA_FLAVOUR, flavour);
        builder.define(DATA_TEXTURE, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        setTier(tag.getIntOr("Tier", 1));
        setFlavour(tag.getIntOr("Flavour", 0));
        setTextureId(tag.getIntOr("Texture", 0));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Tier", getTier());
        tag.putInt("Flavour", getFlavour());
        tag.putInt("Texture", getTextureId());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return super.hurtServer(level, source, amount);
    }

    private final class PickUpTargetGoal extends Goal {
        private LivingEntity carriedTarget;
        private int carryTicks;

        private PickUpTargetGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!EntityIMBird.this.getPassengers().isEmpty()) {
                return false;
            }
            LivingEntity target = EntityIMBird.this.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (target.isPassenger() || target.isVehicle()) {
                return false;
            }
            if (EntityIMBird.this.distanceTo(target) > CARRY_RANGE) {
                return false;
            }
            return EntityIMBird.this.hasLineOfSight(target);
        }

        @Override
        public boolean canContinueToUse() {
            return carriedTarget != null
                && carriedTarget.isPassengerOfSameVehicle(EntityIMBird.this)
                && carryTicks < CARRY_TICKS;
        }

        @Override
        public void start() {
            carriedTarget = EntityIMBird.this.getTarget();
            carryTicks = 0;
            if (carriedTarget != null) {
                carriedTarget.startRiding(EntityIMBird.this, true);
            }
        }

        @Override
        public void tick() {
            carryTicks++;
            if (carriedTarget == null) {
                return;
            }
            if (EntityIMBird.this.onGround() || carryTicks >= CARRY_TICKS) {
                carriedTarget.stopRiding();
                carriedTarget = null;
            }
        }

        @Override
        public void stop() {
            if (carriedTarget != null) {
                carriedTarget.stopRiding();
                carriedTarget = null;
            }
        }
    }
}
