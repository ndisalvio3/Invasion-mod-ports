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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class EntityIMWolf extends Monster {
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMWolf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMWolf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMWolf.class, EntityDataSerializers.INT);

    private int tier;
    private int flavour;

    public EntityIMWolf(EntityType<? extends EntityIMWolf> type, Level level) {
        super(type, level);
        this.tier = 1;
        this.flavour = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.ATTACK_DAMAGE, 4.0D)
            .add(Attributes.FOLLOW_RANGE, Invasion.getNightMobSightRange());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.9D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public void setTier(int tier) {
        this.tier = tier;
        entityData.set(DATA_TIER, tier);
        double health = 20.0D + Math.max(0, tier - 1) * 6.0D;
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
