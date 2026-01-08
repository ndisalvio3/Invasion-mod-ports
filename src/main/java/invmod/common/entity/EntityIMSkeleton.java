package invmod.common.entity;

import invmod.Invasion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.Level;

public class EntityIMSkeleton extends Skeleton {
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMSkeleton.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMSkeleton.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMSkeleton.class, EntityDataSerializers.INT);

    private int tier;
    private int flavour;

    public EntityIMSkeleton(EntityType<? extends EntityIMSkeleton> type, Level level) {
        super(type, level);
        this.tier = 1;
        this.flavour = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Skeleton.createAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D)
            .add(Attributes.FOLLOW_RANGE, Invasion.getNightMobSightRange());
    }

    public void setTier(int tier) {
        this.tier = tier;
        entityData.set(DATA_TIER, tier);
        double health = 20.0D + Math.max(0, tier - 1) * 8.0D;
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
}
