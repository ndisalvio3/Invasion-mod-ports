package invmod.common.entity;

import invmod.Invasion;
import com.whammich.invasion.registry.ModSounds;
import invmod.common.entity.ai.AttackNexusGoal;
import invmod.common.entity.ai.IMNearestAttackableTargetGoal;
import invmod.common.nexus.INexusAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityIMImp extends Monster implements IHasNexus, RangedAttackMob {
    private static final int MAX_TIER = 3;
    private static final float BASE_SCALE = 0.75F;
    private static final int TELEPORT_COOLDOWN_TICKS = 80;
    private static final double TELEPORT_MIN_DISTANCE_SQ = 36.0D;
    private static final double TELEPORT_MAX_DISTANCE_SQ = 196.0D;
    private static final double TELEPORT_SPREAD = 6.0D;
    private static final float RANGED_ATTACK_RANGE = 12.0F;
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMImp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMImp.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMImp.class, EntityDataSerializers.INT);

    private int tier;
    private int flavour;
    private INexusAccess nexus;
    private int teleportCooldown;
    private MeleeAttackGoal meleeGoal;
    private RangedAttackGoal rangedGoal;

    public EntityIMImp(EntityType<? extends EntityIMImp> type, Level level) {
        super(type, level);
        this.tier = 1;
        this.flavour = 0;
        applyAttributes();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 12.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35D)
            .add(Attributes.ATTACK_DAMAGE, 2.0D)
            .add(Attributes.FOLLOW_RANGE, Invasion.getNightMobSightRange())
            .add(Attributes.SCALE, BASE_SCALE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        meleeGoal = new MeleeAttackGoal(this, 1.2D, true);
        rangedGoal = new RangedAttackGoal(this, 1.1D, 35, 55, RANGED_ATTACK_RANGE);
        goalSelector.addGoal(2, meleeGoal);
        updateSpecialGoals();
        goalSelector.addGoal(3, new AttackNexusGoal(this, this, 2, 2.5D));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSenseRange(), false));
        targetSelector.addGoal(3, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSightRange(), true));
    }

    public void setTier(int tier) {
        int normalizedTier = Math.min(Math.max(1, tier), MAX_TIER);
        this.tier = normalizedTier;
        entityData.set(DATA_TIER, normalizedTier);
        applyAttributes();
    }

    public int getTier() {
        return tier;
    }

    public void setFlavour(int flavour) {
        this.flavour = Math.max(0, flavour);
        entityData.set(DATA_FLAVOUR, this.flavour);
        applyAttributes();
        updateSpecialGoals();
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
        int loadedTier = readTagInt(tag, "Tier", "tier", 1);
        int loadedFlavour = readTagInt(tag, "Flavour", "flavour", 0);
        int loadedTexture = readTagInt(tag, "Texture", "textureId", 0);
        this.tier = Math.min(Math.max(1, loadedTier), MAX_TIER);
        this.flavour = Math.max(0, loadedFlavour);
        entityData.set(DATA_TIER, this.tier);
        entityData.set(DATA_FLAVOUR, this.flavour);
        setTextureId(loadedTexture);
        applyAttributes();
        updateSpecialGoals();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Tier", getTier());
        tag.putInt("Flavour", getFlavour());
        tag.putInt("Texture", getTextureId());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (teleportCooldown > 0) {
                teleportCooldown--;
            }
            tryTeleportTowardsTarget();
        }
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
        if (level().isClientSide) {
            return;
        }
        Vec3 direction = target.getEyePosition().subtract(getEyePosition());
        SmallFireball fireball = new SmallFireball(level(), this, direction);
        fireball.setPos(getX(), getEyeY() - 0.1D, getZ());
        level().addFreshEntity(fireball);
        level().playSound(null, blockPosition(), ModSounds.FIREBALL.get(), getSoundSource(), 0.7F, 0.9F + random.nextFloat() * 0.2F);
    }

    private void applyAttributes() {
        float moveSpeed = 0.45F;
        float attackStrength = 2.0F;
        float maxHealth = 12.0F;
        float scale = BASE_SCALE;

        if (tier == 2) {
            maxHealth = 16.0F;
            attackStrength = 3.0F;
            moveSpeed = 0.42F;
            scale = 0.85F;
        } else if (tier == 3) {
            maxHealth = 20.0F;
            attackStrength = 4.0F;
            moveSpeed = 0.39F;
            scale = 0.95F;
        }

        AttributeInstance maxHealthAttr = getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(maxHealth);
            if (getHealth() > maxHealth || getHealth() == 0.0F) {
                setHealth(maxHealth);
            }
        }
        AttributeInstance speedAttr = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(moveSpeed);
        }
        AttributeInstance attackAttr = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.setBaseValue(attackStrength);
        }
        AttributeInstance followAttr = getAttribute(Attributes.FOLLOW_RANGE);
        if (followAttr != null) {
            followAttr.setBaseValue(Invasion.getNightMobSightRange());
        }
        AttributeInstance scaleAttr = getAttribute(Attributes.SCALE);
        if (scaleAttr != null) {
            float currentScale = (float) scaleAttr.getBaseValue();
            if (currentScale != scale) {
                scaleAttr.setBaseValue(scale);
                refreshDimensions();
            }
        }
    }

    private void updateSpecialGoals() {
        if (rangedGoal == null) {
            return;
        }
        goalSelector.removeGoal(rangedGoal);
        if (usesRangedAttack()) {
            goalSelector.addGoal(1, rangedGoal);
        }
    }

    private boolean usesRangedAttack() {
        return true;
    }

    private boolean usesTeleport() {
        return true;
    }

    private void tryTeleportTowardsTarget() {
        if (teleportCooldown > 0 || !usesTeleport()) {
            return;
        }
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        double distanceSq = distanceToSqr(target);
        if (distanceSq < TELEPORT_MIN_DISTANCE_SQ || distanceSq > TELEPORT_MAX_DISTANCE_SQ) {
            return;
        }
        if (!getSensing().hasLineOfSight(target)) {
            return;
        }
        double x = target.getX() + (random.nextDouble() - 0.5D) * TELEPORT_SPREAD;
        double y = target.getY();
        double z = target.getZ() + (random.nextDouble() - 0.5D) * TELEPORT_SPREAD;
        if (randomTeleport(x, y, z, true)) {
            teleportCooldown = TELEPORT_COOLDOWN_TICKS;
        }
    }

    private int readTagInt(CompoundTag tag, String primary, String fallback, int defaultValue) {
        if (tag.contains(primary)) {
            return tag.getIntOr(primary, defaultValue);
        }
        if (tag.contains(fallback)) {
            return tag.getIntOr(fallback, defaultValue);
        }
        return defaultValue;
    }
}
