package invmod.common.entity;

import invmod.Invasion;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;

public class EntityIMSkeleton extends Skeleton implements IHasNexus {
    private static final int MAX_TIER = 3;
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMSkeleton.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMSkeleton.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMSkeleton.class, EntityDataSerializers.INT);

    private int tier;
    private int flavour;
    private INexusAccess nexus;

    public EntityIMSkeleton(EntityType<? extends EntityIMSkeleton> type, Level level) {
        super(type, level);
        this.tier = 1;
        this.flavour = 0;
        applyAttributes();
        ensureRangedEquipment();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Skeleton.createAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D)
            .add(Attributes.FOLLOW_RANGE, Invasion.getNightMobSightRange());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(3, new AttackNexusGoal(this, this, 2, 2.5D));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSenseRange(), false));
        targetSelector.addGoal(3, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSightRange(), true));
    }

    public void setTier(int tier) {
        int normalizedTier = Mth.clamp(tier, 1, MAX_TIER);
        this.tier = normalizedTier;
        entityData.set(DATA_TIER, normalizedTier);
        applyAttributes();
        ensureRangedEquipment();
    }

    public int getTier() {
        return tier;
    }

    public void setFlavour(int flavour) {
        this.flavour = Math.max(0, flavour);
        entityData.set(DATA_FLAVOUR, this.flavour);
        applyAttributes();
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
    protected AbstractArrow getArrow(ItemStack ammoStack, float distanceFactor, ItemStack weaponStack) {
        AbstractArrow arrow = super.getArrow(ammoStack, distanceFactor, weaponStack);
        if (tier > 1) {
            float bonusFactor = tier == 2 ? 0.2F : 0.4F;
            arrow.setBaseDamageFromMob(distanceFactor + bonusFactor);
        }
        return arrow;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return super.hurtServer(level, source, amount);
    }

    private void applyAttributes() {
        float moveSpeed = 0.25F;
        float attackStrength = 3.0F;
        float maxHealth = 20.0F;

        if (tier == 2) {
            maxHealth = 28.0F;
            attackStrength = 4.0F;
            moveSpeed = 0.27F;
        } else if (tier == 3) {
            maxHealth = 36.0F;
            attackStrength = 5.0F;
            moveSpeed = 0.29F;
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
    }

    private void ensureRangedEquipment() {
        if (level().isClientSide) {
            return;
        }
        if (getMainHandItem().isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
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
