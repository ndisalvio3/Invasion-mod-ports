package invmod.common.entity;

import invmod.Invasion;
import com.whammich.invasion.Reference;
import invmod.common.entity.ai.AttackNexusGoal;
import invmod.common.entity.ai.IMNearestAttackableTargetGoal;
import invmod.common.nexus.INexusAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityIMZombiePigman extends Monster implements IHasNexus {
    private static final int MAX_TIER = 3;
    private static final int CHARGE_COOLDOWN_TICKS = 120;
    private static final int CHARGE_DURATION_TICKS = 30;
    private static final ResourceLocation CHARGE_SPEED_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(Reference.MODID, "zombie_pigman_charge_speed");
    private static final AttributeModifier CHARGE_SPEED_MODIFIER =
        new AttributeModifier(CHARGE_SPEED_MODIFIER_ID, 0.45D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMZombiePigman.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMZombiePigman.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMZombiePigman.class, EntityDataSerializers.INT);

    private int tier;
    private int flavour;
    private INexusAccess nexus;
    private boolean charging;
    private int chargeTicks;
    private int chargeCooldown;

    public EntityIMZombiePigman(EntityType<? extends EntityIMZombiePigman> type, Level level) {
        super(type, level);
        this.tier = 1;
        this.flavour = 0;
        applyAttributes();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 24.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.23D)
            .add(Attributes.ATTACK_DAMAGE, 4.0D)
            .add(Attributes.FOLLOW_RANGE, Invasion.getNightMobSightRange());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(3, new AttackNexusGoal(this, this, 2, 2.5D));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSenseRange(), false));
        targetSelector.addGoal(3, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSightRange(), true));
    }

    public void setTier(int tier) {
        int normalizedTier = Mth.clamp(tier, 1, MAX_TIER);
        this.tier = normalizedTier;
        entityData.set(DATA_TIER, normalizedTier);
        applyAttributes();
        applyDefaultTextureIfUnset();
    }

    public int getTier() {
        return tier;
    }

    public void setFlavour(int flavour) {
        this.flavour = Math.max(0, flavour);
        entityData.set(DATA_FLAVOUR, this.flavour);
        applyAttributes();
        applyDefaultTextureIfUnset();
    }

    public int getFlavour() {
        return flavour;
    }

    public void setTextureId(int textureId) {
        entityData.set(DATA_TEXTURE, Math.max(0, textureId));
    }

    public int getTextureId() {
        return entityData.get(DATA_TEXTURE);
    }

    public boolean isCharging() {
        return charging;
    }

    public void setCharging(boolean charging) {
        if (this.charging == charging) {
            return;
        }
        this.charging = charging;
        updateChargeSpeedModifier(charging);
        setSprinting(charging);
        if (!charging) {
            chargeTicks = 0;
        }
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
        int loadedTier = tag.getIntOr("Tier", 1);
        int loadedFlavour = tag.getIntOr("Flavour", 0);
        int loadedTexture = tag.getIntOr("Texture", 0);
        setTextureId(loadedTexture);
        setTier(loadedTier);
        setFlavour(loadedFlavour);
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

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (chargeCooldown > 0) {
            chargeCooldown--;
        }
        if (charging) {
            if (chargeTicks > 0) {
                chargeTicks--;
            }
            Entity target = getTarget();
            if (chargeTicks <= 0 || target == null || !hasLineOfSight(target)) {
                stopCharge();
            }
            return;
        }
        if (chargeCooldown == 0 && shouldStartCharge()) {
            startCharge();
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (charging) {
            float bonusDamage = 2.0F + tier;
            float damage = (float) (getAttributeValue(Attributes.ATTACK_DAMAGE) + bonusDamage);
            boolean hit = target.hurtServer(level, damageSources().mobAttack(this), damage);
            if (hit) {
                float yaw = getYRot() * Mth.DEG_TO_RAD;
                target.push(-Mth.sin(yaw) * 1.5D, 0.35D, Mth.cos(yaw) * 1.5D);
            }
            stopCharge();
            return hit;
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    public boolean isBigRenderTempHack() {
        return getTier() == 3;
    }

    public float scaleAmount() {
        if (getTier() == 2) {
            return 1.12F;
        }
        if (getTier() == 3) {
            return 1.21F;
        }
        return 1.0F;
    }

    private void startCharge() {
        chargeTicks = CHARGE_DURATION_TICKS;
        chargeCooldown = CHARGE_COOLDOWN_TICKS;
        setCharging(true);
    }

    private void stopCharge() {
        setCharging(false);
    }

    private boolean shouldStartCharge() {
        Entity target = getTarget();
        if (target == null || !hasLineOfSight(target) || !onGround()) {
            return false;
        }
        double distance = distanceToSqr(target);
        if (distance < 9.0D || distance > 144.0D) {
            return false;
        }
        return random.nextInt(30) == 0;
    }

    private void updateChargeSpeedModifier(boolean enable) {
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(CHARGE_SPEED_MODIFIER_ID);
        if (enable) {
            speed.addTransientModifier(CHARGE_SPEED_MODIFIER);
        }
    }

    private void applyDefaultTextureIfUnset() {
        if (getTextureId() != 0) {
            return;
        }
        if (tier == 3) {
            setTextureId(2);
        }
    }

    private void applyAttributes() {
        double maxHealth = 24.0D;
        double moveSpeed = 0.23D;
        double attackDamage = 4.0D;

        if (tier == 2) {
            maxHealth = 32.0D;
            moveSpeed = 0.25D;
            attackDamage = 5.0D;
        } else if (tier == 3) {
            maxHealth = 42.0D;
            moveSpeed = 0.28D;
            attackDamage = 7.0D;
        }

        AttributeInstance healthAttribute = getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(maxHealth);
            setHealth((float) maxHealth);
        }
        AttributeInstance speedAttribute = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(moveSpeed);
        }
        AttributeInstance attackAttribute = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute != null) {
            attackAttribute.setBaseValue(attackDamage);
        }
    }
}
