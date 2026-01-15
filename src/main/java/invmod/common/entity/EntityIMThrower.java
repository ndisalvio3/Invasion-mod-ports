package invmod.common.entity;

import invmod.Invasion;
import com.whammich.invasion.registry.ModEntities;
import invmod.common.entity.ai.AttackNexusGoal;
import invmod.common.entity.ai.IMNearestAttackableTargetGoal;
import invmod.common.nexus.INexusAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;

public class EntityIMThrower extends Monster implements IHasNexus, RangedAttackMob {
    private static final float RANGED_ATTACK_RANGE = 18.0F;
    private static final float MIN_THROW_RANGE = 4.0F;
    private static final float BOULDER_SPEED = 1.1F;
    private static final float BOULDER_VARIANCE = 0.08F;
    private static final float TNT_SPEED = 1.0F;
    private static final float TNT_VARIANCE = 0.1F;
    private static final int MAX_THROW_COUNT = 3;
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMThrower.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMThrower.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMThrower.class, EntityDataSerializers.INT);

    private int tier;
    private int flavour;
    private INexusAccess nexus;
    private MeleeAttackGoal meleeGoal;
    private RangedAttackGoal rangedGoal;

    public EntityIMThrower(EntityType<? extends EntityIMThrower> type, Level level) {
        super(type, level);
        this.tier = 1;
        this.flavour = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 18.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.22D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D)
            .add(Attributes.FOLLOW_RANGE, Invasion.getNightMobSightRange());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        rangedGoal = new ThrowerRangedAttackGoal(this, 1.0D, 45, 70, RANGED_ATTACK_RANGE, MIN_THROW_RANGE);
        meleeGoal = new MeleeAttackGoal(this, 1.0D, true);
        goalSelector.addGoal(1, rangedGoal);
        goalSelector.addGoal(2, meleeGoal);
        goalSelector.addGoal(3, new AttackNexusGoal(this, this, 2, 2.5D));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSenseRange(), false));
        targetSelector.addGoal(3, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSightRange(), true));
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_TIER.equals(key)) {
            tier = entityData.get(DATA_TIER);
        } else if (DATA_FLAVOUR.equals(key)) {
            flavour = entityData.get(DATA_FLAVOUR);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        float distance = distanceTo(target);
        int missDistance = Math.max(1, Mth.ceil(distance / 10.0F));
        int throwCount = 1 + random.nextInt(MAX_THROW_COUNT);
        Vec3 baseTarget = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D);

        for (int i = 0; i < throwCount; i++) {
            double offsetX = random.nextInt(missDistance * 2 + 1) - missDistance;
            double offsetY = random.nextInt(missDistance * 2 + 1) - missDistance;
            double offsetZ = random.nextInt(missDistance * 2 + 1) - missDistance;
            Vec3 targetPos = baseTarget.add(offsetX, offsetY, offsetZ);
            if (tier <= 1) {
                throwBoulder(serverLevel, targetPos);
            } else {
                throwTnt(serverLevel, targetPos);
            }
        }
        swing(InteractionHand.MAIN_HAND);
    }

    private void throwBoulder(ServerLevel serverLevel, Vec3 targetPos) {
        EntityIMBoulder boulder = ModEntities.IM_BOULDER.get().create(serverLevel, EntitySpawnReason.EVENT);
        if (boulder == null) {
            return;
        }
        aimAt(targetPos);
        boulder.setupBoulder(this, BOULDER_SPEED, BOULDER_VARIANCE);
        serverLevel.addFreshEntity(boulder);
    }

    private void throwTnt(ServerLevel serverLevel, Vec3 targetPos) {
        EntityIMPrimedTNT tnt = ModEntities.IM_PRIMED_TNT.get().create(serverLevel, EntitySpawnReason.EVENT);
        if (tnt == null) {
            return;
        }
        aimAt(targetPos);
        tnt.setupTnt(this, TNT_SPEED, TNT_VARIANCE);
        serverLevel.addFreshEntity(tnt);
    }

    private void aimAt(Vec3 targetPos) {
        Vec3 from = getEyePosition();
        Vec3 delta = targetPos.subtract(from);
        double dx = delta.x;
        double dy = delta.y;
        double dz = delta.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dx, dz) * Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(dy, horiz) * Mth.RAD_TO_DEG);
        setYRot(yaw);
        setXRot(pitch);
        yRotO = yaw;
        xRotO = pitch;
        yBodyRot = yaw;
        yHeadRot = yaw;
    }

    private static final class ThrowerRangedAttackGoal extends RangedAttackGoal {
        private final EntityIMThrower thrower;
        private final float minRangeSq;

        private ThrowerRangedAttackGoal(EntityIMThrower thrower, double speed, int minInterval, int maxInterval, float maxRange, float minRange) {
            super(thrower, speed, minInterval, maxInterval, maxRange);
            this.thrower = thrower;
            this.minRangeSq = minRange * minRange;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = thrower.getTarget();
            if (target != null && thrower.distanceToSqr(target) < minRangeSq) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = thrower.getTarget();
            if (target != null && thrower.distanceToSqr(target) < minRangeSq) {
                return false;
            }
            return super.canContinueToUse();
        }
    }
}
