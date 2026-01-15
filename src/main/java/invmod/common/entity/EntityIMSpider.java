package invmod.common.entity;

import com.whammich.invasion.registry.ModEntities;
import com.whammich.invasion.registry.ModSounds;
import invmod.Invasion;
import invmod.common.entity.ai.AttackNexusGoal;
import invmod.common.entity.ai.EntityAILayEgg;
import invmod.common.entity.ai.EntityAIPounce;
import invmod.common.entity.ai.IMNearestAttackableTargetGoal;
import invmod.common.nexus.INexusAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EntityIMSpider extends Spider implements IHasNexus, ISpawnsOffspring {
    private static final int MAX_TIER = 3;
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMSpider.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMSpider.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMSpider.class, EntityDataSerializers.INT);

    private int tier;
    private int flavour;
    private INexusAccess nexus;
    private int airborneTime;
    private Item itemDrop;
    private float dropChance;

    public EntityIMSpider(EntityType<? extends EntityIMSpider> type, Level level) {
        super(type, level);
        this.tier = 1;
        this.flavour = 0;
        this.airborneTime = 0;
        this.itemDrop = Items.AIR;
        this.dropChance = 0.0F;
        applyAttributes();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
            .add(Attributes.MAX_HEALTH, 16.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.ATTACK_DAMAGE, 2.0D)
            .add(Attributes.FOLLOW_RANGE, Invasion.getNightMobSightRange());
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(3, new AttackNexusGoal(this, this, 2, 2.5D));
        goalSelector.addGoal(4, new EntityAIPounce(this, 0.4F, 1.2F, 60));
        goalSelector.addGoal(5, new EntityAILayEgg(this, 2));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSenseRange(), false));
        targetSelector.addGoal(3, new IMNearestAttackableTargetGoal<>(this, Player.class, Invasion.getNightMobSightRange(), true));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.SPIDER_HISS.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    public void setTier(int tier) {
        int normalizedTier = Mth.clamp(tier, 1, MAX_TIER);
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

    public int getAirborneTime() {
        return airborneTime;
    }

    public void setAirborneTime(int time) {
        this.airborneTime = time;
    }

    @Override
    public Entity[] getOffspring(Entity paramEntity) {
        if (level() instanceof ServerLevel serverLevel) {
            EntityIMSpider offspring = ModEntities.IM_SPIDER.get().create(serverLevel, EntitySpawnReason.EVENT);
            if (offspring != null) {
                offspring.setTier(Math.max(1, tier - 1));
                offspring.setFlavour(flavour);
                return new Entity[] { offspring };
            }
        }
        return null;
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
        this.tier = Mth.clamp(loadedTier, 1, MAX_TIER);
        this.flavour = Math.max(0, loadedFlavour);
        entityData.set(DATA_TIER, this.tier);
        entityData.set(DATA_FLAVOUR, this.flavour);
        setTextureId(loadedTexture);
        applyAttributes();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Tier", getTier());
        tag.putInt("Flavour", getFlavour());
        tag.putInt("Texture", getTextureId());
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, wasRecentlyHit);
        if (random.nextFloat() < 0.35F) {
            spawnAtLocation(level, new ItemStack(Items.STRING));
        }
        if (itemDrop != null && !itemDrop.equals(Items.AIR) && random.nextFloat() < dropChance) {
            spawnAtLocation(level, new ItemStack(itemDrop));
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return super.hurtServer(level, source, amount);
    }

    private void applyAttributes() {
        float moveSpeed = 0.3F;
        float attackStrength = 2.0F;
        float maxHealth = 16.0F;
        itemDrop = Items.AIR;
        dropChance = 0.0F;

        if (tier == 1) {
            if (flavour == 1) {
                attackStrength = 3.0F;
                itemDrop = Items.STRING;
                dropChance = 0.3F;
            }
        } else if (tier == 2) {
            maxHealth = 32.0F;
            attackStrength = 4.0F;
            moveSpeed = 0.35F;
            itemDrop = Items.SPIDER_EYE;
            dropChance = 0.25F;
            if (flavour == 1) {
                maxHealth = 40.0F;
                attackStrength = 5.0F;
                itemDrop = Items.FERMENTED_SPIDER_EYE;
                dropChance = 0.3F;
            }
        } else if (tier == 3) {
            maxHealth = 48.0F;
            attackStrength = 6.0F;
            moveSpeed = 0.4F;
            itemDrop = Items.SPIDER_EYE;
            dropChance = 0.4F;
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
