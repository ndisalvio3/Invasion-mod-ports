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
import net.minecraft.world.phys.Vec3;

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
    private EntityAIPounce pounceGoal;
    private EntityAILayEgg eggGoal;

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
            .add(Attributes.FOLLOW_RANGE, Invasion.getNightMobSightRange())
            .add(Attributes.GRAVITY, 0.08D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(3, new AttackNexusGoal(this, this, 2, 2.5D));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        pounceGoal = new EntityAIPounce(this, 0.2F, 1.55F, 18);
        eggGoal = new EntityAILayEgg(this, 1);
        updateSpecialGoals();

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
        return ModSounds.SPIDER_HISS.get();
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
        updateSpecialGoals();
        applyDefaultTextureIfUnset();
    }

    public int getTier() {
        return tier;
    }

    public void setFlavour(int flavour) {
        this.flavour = Math.max(0, flavour);
        entityData.set(DATA_FLAVOUR, this.flavour);
        applyAttributes();
        updateSpecialGoals();
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

    public int getAirborneTime() {
        return airborneTime;
    }

    public void setAirborneTime(int time) {
        this.airborneTime = time;
    }

    @Override
    public Entity[] getOffspring(Entity paramEntity) {
        if (level() instanceof ServerLevel serverLevel) {
            if (tier == 2 && flavour == 1) {
                Entity[] offspring = new Entity[6];
                for (int i = 0; i < offspring.length; i++) {
                    EntityIMSpider spider = ModEntities.IM_SPIDER.get().create(serverLevel, EntitySpawnReason.EVENT);
                    if (spider == null) {
                        return null;
                    }
                    spider.setTier(1);
                    spider.setFlavour(1);
                    if (nexus != null) {
                        spider.acquiredByNexus(nexus);
                    }
                    offspring[i] = spider;
                }
                return offspring;
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
    public void travel(Vec3 travelVector) {
        if (airborneTime > 0) {
            super.travel(Vec3.ZERO);
            return;
        }
        super.travel(travelVector);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean wasRecentlyHit) {
        if (tier == 1 && flavour == 1) {
            return;
        }
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
        float moveSpeed = 0.29F;
        float attackStrength = 3.0F;
        float maxHealth = 16.0F;
        double gravity = 0.08D;
        itemDrop = Items.AIR;
        dropChance = 0.0F;

        if (tier == 1) {
            if (flavour == 1) {
                maxHealth = 8.0F;
                moveSpeed = 0.34F;
                attackStrength = 1.0F;
            }
        } else if (tier == 2) {
            maxHealth = 32.0F;
            attackStrength = 5.0F;
            moveSpeed = 0.3F;
            itemDrop = Items.SPIDER_EYE;
            dropChance = 0.25F;
            if (flavour == 1) {
                maxHealth = 40.0F;
                attackStrength = 4.0F;
                moveSpeed = 0.22F;
                itemDrop = Items.FERMENTED_SPIDER_EYE;
                dropChance = 0.3F;
            } else {
                gravity = 0.043D;
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
        AttributeInstance gravityAttr = getAttribute(Attributes.GRAVITY);
        if (gravityAttr != null) {
            gravityAttr.setBaseValue(gravity);
        }
    }

    private void applyDefaultTextureIfUnset() {
        if (getTextureId() != 0) {
            return;
        }
        if (tier == 2) {
            if (flavour == 0) {
                setTextureId(1);
            } else if (flavour == 1) {
                setTextureId(2);
            }
        }
    }

    private void updateSpecialGoals() {
        if (pounceGoal == null || eggGoal == null) {
            return;
        }
        goalSelector.removeGoal(pounceGoal);
        goalSelector.removeGoal(eggGoal);
        if (tier == 2 && flavour == 1) {
            goalSelector.addGoal(5, eggGoal);
        } else if (flavour == 1 || tier == 2) {
            goalSelector.addGoal(4, pounceGoal);
        }
    }

    @Override
    public boolean isPushable() {
        return !onClimbable();
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
