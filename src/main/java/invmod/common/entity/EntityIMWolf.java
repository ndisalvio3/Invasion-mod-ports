package invmod.common.entity;

import invmod.Invasion;
import invmod.common.SparrowAPI;
import invmod.common.entity.ai.AttackNexusGoal;
import invmod.common.entity.ai.IMNearestAttackableTargetGoal;
import invmod.common.nexus.INexusAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Optional;

public class EntityIMWolf extends Monster implements IHasNexus, SparrowAPI {
    public static final int TEXTURE_TAMED = 0;
    public static final int TEXTURE_NEXUS = 1;
    public static final int TEXTURE_WILD = 2;

    private static final WolfSoundVariant SOUND_VARIANT = SoundEvents.WOLF_SOUNDS.get(WolfSoundVariants.SoundSet.CLASSIC);

    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMWolf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMWolf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMWolf.class, EntityDataSerializers.INT);

    private int tier;
    private int flavour;
    private INexusAccess nexus;

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
        goalSelector.addGoal(3, new AttackNexusGoal(this, this, 2, 2.5D));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.9D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSenseRange(), false));
        targetSelector.addGoal(3, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSightRange(), true));
        targetSelector.addGoal(4, new IMNearestAttackableTargetGoal<>(this, Animal.class, Invasion.getNightMobSightRange(), true));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SOUND_VARIANT.ambientSound().value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SOUND_VARIANT.hurtSound().value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SOUND_VARIANT.deathSound().value();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
    }

    public void setTier(int tier) {
        int normalizedTier = Math.max(1, tier);
        this.tier = normalizedTier;
        entityData.set(DATA_TIER, normalizedTier);
        double health = 20.0D + Math.max(0, tier - 1) * 6.0D;
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        setHealth((float) health);
        applyDefaultTextureIfUnset();
    }

    public int getTier() {
        return tier;
    }

    public void setFlavour(int flavour) {
        this.flavour = Math.max(0, flavour);
        entityData.set(DATA_FLAVOUR, this.flavour);
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

    @Override
    public INexusAccess getNexus() {
        return nexus;
    }

    @Override
    public void acquiredByNexus(INexusAccess nexus) {
        this.nexus = nexus;
        applyDefaultTextureIfUnset();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TIER, tier);
        builder.define(DATA_FLAVOUR, flavour);
        builder.define(DATA_TEXTURE, TEXTURE_TAMED);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        int loadedTier = readTagInt(tag, "Tier", "tier", 1);
        int loadedFlavour = readTagInt(tag, "Flavour", "flavour", 0);
        int loadedTexture = readTagInt(tag, "Texture", "textureId", TEXTURE_TAMED);
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
    public boolean isStupidToAttack() {
        return false;
    }

    @Override
    public boolean doNotVaporize() {
        return false;
    }

    @Override
    public boolean isPredator() {
        return true;
    }

    @Override
    public boolean isHostile() {
        return true;
    }

    @Override
    public boolean isPeaceful() {
        return false;
    }

    @Override
    public boolean isPrey() {
        return false;
    }

    @Override
    public boolean isNeutral() {
        return false;
    }

    @Override
    public boolean isUnkillable() {
        return false;
    }

    @Override
    public boolean isThreatTo(Entity entity) {
        return entity instanceof Player || entity instanceof Animal;
    }

    @Override
    public boolean isFriendOf(Entity entity) {
        return entity instanceof IHasNexus;
    }

    @Override
    public boolean isNPC() {
        return false;
    }

    @Override
    public int isPet() {
        return 0;
    }

    @Override
    public Entity getPetOwner() {
        return null;
    }

    @Override
    public Component getName() {
        return getDisplayName();
    }

    @Override
    public Entity getAttackingTarget() {
        return getTarget();
    }

    @Override
    public float getSize() {
        return getBbWidth();
    }

    @Override
    public String getSpecies() {
        return "invasion";
    }

    @Override
    public int getGender() {
        return 0;
    }

    @Override
    public String customStringAndResponse(String input) {
        return "";
    }

    @Override
    public String getSimplyID() {
        return "";
    }

    private void applyDefaultTextureIfUnset() {
        if (getTextureId() != TEXTURE_TAMED) {
            return;
        }
        if (nexus != null) {
            setTextureId(TEXTURE_NEXUS);
        }
    }

    private int readTagInt(CompoundTag tag, String primary, String fallback, int defaultValue) {
        Optional<Integer> primaryValue = tag.getInt(primary);
        if (primaryValue.isPresent()) {
            return primaryValue.get();
        }
        Optional<Integer> fallbackValue = tag.getInt(fallback);
        return fallbackValue.orElse(defaultValue);
    }
}
