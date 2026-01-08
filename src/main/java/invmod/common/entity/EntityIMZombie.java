package invmod.common.entity;

import com.whammich.invasion.registry.ModSounds;
import invmod.Invasion;
import invmod.common.IBlockAccessExtended;
import invmod.common.entity.ai.AttackNexusGoal;
import invmod.common.entity.ai.IMNearestAttackableTargetGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import invmod.common.entity.PathAction;
import invmod.common.entity.PathNode;

public class EntityIMZombie extends EntityIMMob {
    private static final int MAX_TIER = 3;
    private static final int DIG_COOLDOWN_TICKS = 12;
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMZombie.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMZombie.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMZombie.class, EntityDataSerializers.INT);

    private int tier;
    private int flavour;
    private int flammability;
    private int digCooldown;
    private boolean fireballTriggered;
    private ItemStack defaultHeldItem = ItemStack.EMPTY;
    private Item itemDrop;
    private float dropChance;

    public EntityIMZombie(EntityType<? extends EntityIMZombie> type, Level level) {
        super(type, level);
        this.tier = 1;
        this.flavour = 0;
        applyAttributes();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
            .add(Attributes.MAX_HEALTH, 18.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.38D)
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

    @Override
    protected SoundEvent getAmbientSound() {
        if (getTier() == 3) {
            return ModSounds.BIG_ZOMBIE.get();
        }
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
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

    public void updateAnimation(boolean swinging) {
        if (swinging) {
            swing(InteractionHand.MAIN_HAND, true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (digCooldown > 0) {
            digCooldown--;
        }
        if (!level().isClientSide) {
            if (shouldAttemptDig()) {
                tryDigForward();
            }
            if (isTarZombie() && isInWater()) {
                setAirSupply(getMaxAirSupply());
            }
            if (isAlive() && Invasion.getNightMobsBurnInDay() && isSunBurnTick()) {
                if (isTarZombie()) {
                    hurt(damageSources().onFire(), 3.0F);
                } else {
                    igniteForSeconds(8.0F);
                }
            }
            if (!fireballTriggered && flammability >= 20 && isOnFire()) {
                fireballTriggered = true;
                doFireball();
            }
        }
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
        applyDefaultTextureIfUnset();
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_TIER.equals(key)) {
            tier = entityData.get(DATA_TIER);
        } else if (DATA_FLAVOUR.equals(key)) {
            flavour = entityData.get(DATA_FLAVOUR);
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        if (getTier() == 3 && isSprinting()) {
            float damage = (float) (getAttributeValue(Attributes.ATTACK_DAMAGE) + 3.0D);
            boolean hit = target.hurtServer(level, damageSources().mobAttack(this), damage);
            if (hit) {
                float yaw = getYRot() * Mth.DEG_TO_RAD;
                target.push(-Mth.sin(yaw) * 2.0D, 0.4D, Mth.cos(yaw) * 2.0D);
                setSprinting(false);
                level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_BIG_FALL, getSoundSource(), 1.0F, 1.0F);
            }
            return hit;
        }
        return super.doHurtTarget(level, target);
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (getTier() == 3) {
            return;
        }
        super.knockback(strength, x, z);
    }

    @Override
    public boolean isPushable() {
        return getTier() != 3 && !onClimbable();
    }

    @Override
    public boolean fireImmune() {
        return isPigmanZombie() || super.fireImmune();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, wasRecentlyHit);
        if (random.nextFloat() < 0.35F) {
            spawnAtLocation(level, new ItemStack(Items.ROTTEN_FLESH));
        }
        if (itemDrop != null && !itemDrop.equals(Items.AIR) && random.nextFloat() < dropChance) {
            spawnAtLocation(level, new ItemStack(itemDrop));
        }
    }

    @Override
    public float getBlockPathCost(PathNode from, PathNode to, BlockGetter level) {
        if (isTarZombie() && to.action == PathAction.SWIM) {
            float multiplier = 1.0F;
            if (level instanceof IBlockAccessExtended extended) {
                int mobDensity = extended.getLayeredData(to.xCoord, to.yCoord, to.zCoord) & 7;
                multiplier += mobDensity * 3;
            }
            return from.distanceTo(to) * 1.2F * multiplier;
        }
        return super.getBlockPathCost(from, to, level);
    }

    private boolean isTarZombie() {
        return getTier() == 2 && getFlavour() == 2;
    }

    private boolean isPigmanZombie() {
        return getTier() == 2 && getFlavour() == 3;
    }

    private void applyDefaultTextureIfUnset() {
        if (getTextureId() != 0) {
            return;
        }
        if (tier == 1) {
            setTextureId(random.nextBoolean() ? 0 : 1);
        } else if (tier == 2) {
            if (flavour == 2) {
                setTextureId(5);
            } else if (flavour == 3) {
                setTextureId(3);
            } else {
                setTextureId(random.nextBoolean() ? 2 : 4);
            }
        } else if (tier == 3) {
            setTextureId(6);
        }
    }

    private void applyAttributes() {
        float moveSpeed = 0.38F;
        float attackStrength = 4.0F;
        float maxHealth = 18.0F;
        flammability = 3;
        defaultHeldItem = ItemStack.EMPTY;
        itemDrop = Items.AIR;
        dropChance = 0.0F;

        if (tier == 1 && flavour == 1) {
            attackStrength = 6.0F;
            defaultHeldItem = new ItemStack(Items.WOODEN_SWORD);
            itemDrop = Items.WOODEN_SWORD;
            dropChance = 0.2F;
        } else if (tier == 2) {
            maxHealth = 35.0F;
            attackStrength = 7.0F;
            flammability = 4;
            itemDrop = Items.IRON_CHESTPLATE;
            dropChance = 0.25F;
            if (flavour == 1) {
                maxHealth = 40.0F;
                attackStrength = 10.0F;
                defaultHeldItem = new ItemStack(Items.IRON_SWORD);
                itemDrop = Items.IRON_SWORD;
            } else if (flavour == 2) {
                maxHealth = 30.0F;
                attackStrength = 5.0F;
                flammability = 30;
                itemDrop = Items.AIR;
                dropChance = 0.0F;
            } else if (flavour == 3) {
                maxHealth = 30.0F;
                attackStrength = 8.0F;
                moveSpeed = 0.5F;
                defaultHeldItem = new ItemStack(Items.GOLDEN_SWORD);
                itemDrop = Items.AIR;
                dropChance = 0.0F;
            }
        } else if (tier == 3) {
            maxHealth = 65.0F;
            attackStrength = 18.0F;
            moveSpeed = 0.34F;
            flammability = 4;
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

        if (!level().isClientSide) {
            setItemSlot(EquipmentSlot.MAINHAND, defaultHeldItem.isEmpty() ? ItemStack.EMPTY : defaultHeldItem.copy());
        }
    }

    private boolean shouldAttemptDig() {
        return digCooldown == 0 && horizontalCollision && (getNexus() != null || getTarget() != null) && canDig();
    }

    private boolean canDig() {
        return getDestructiveness() > 0;
    }

    private int getDestructiveness() {
        if (tier == 1) {
            return flavour == 1 ? 0 : 2;
        }
        if (tier == 2) {
            if (flavour == 1) {
                return 0;
            }
            return 2;
        }
        return tier == 3 ? 2 : 0;
    }

    private void tryDigForward() {
        digCooldown = DIG_COOLDOWN_TICKS;
        Direction direction = Direction.fromYRot(getYRot());
        BlockPos targetPos = blockPosition().relative(direction);
        BlockState state = level().getBlockState(targetPos);
        if (state.isAir()) {
            return;
        }
        if (!isBlockTypeDestructible(state, targetPos)) {
            return;
        }
        if (level().destroyBlock(targetPos, Invasion.getDestructedBlocksDrop(), this)) {
            SoundEvent scrape = getScrapeSound();
            level().playSound(null, targetPos, scrape, getSoundSource(), 0.7F, 0.9F + random.nextFloat() * 0.2F);
        }
    }

    private boolean isBlockTypeDestructible(BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block == Blocks.BEDROCK || block == Blocks.LADDER || block == Invasion.blockNexus) {
            return false;
        }
        if (state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS)) {
            return true;
        }
        if (!state.blocksMotion()) {
            return false;
        }
        return state.getDestroySpeed(level(), pos) >= 0.0F;
    }

    private void doFireball() {
        level().playSound(null, blockPosition(), ModSounds.FIREBALL.get(), getSoundSource(), 1.0F, 0.9F + random.nextFloat() * 0.2F);
        int baseX = Mth.floor(getX());
        int baseY = Mth.floor(getY());
        int baseZ = Mth.floor(getZ());
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                for (int k = -1; k < 2; k++) {
                    BlockPos pos = new BlockPos(baseX + i, baseY + j, baseZ + k);
                    BlockState state = level().getBlockState(pos);
                    if (state.isAir() || state.isFlammable(level(), pos, Direction.UP)) {
                        level().setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
            }
        }

        for (Entity entity : level().getEntities(this, getBoundingBox().inflate(1.5D))) {
            entity.setRemainingFireTicks(8 * 20);
        }
        hurt(damageSources().inFire(), 500.0F);
    }

    private SoundEvent getScrapeSound() {
        int roll = random.nextInt(3);
        return switch (roll) {
            case 1 -> ModSounds.SCRAPE_2.get();
            case 2 -> ModSounds.SCRAPE_3.get();
            default -> ModSounds.SCRAPE_1.get();
        };
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
