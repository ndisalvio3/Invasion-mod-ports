package invmod.common.entity;

import com.whammich.invasion.registry.ModSounds;
import invmod.Invasion;
import invmod.common.entity.ai.AttackNexusGoal;
import invmod.common.entity.ai.IMNearestAttackableTargetGoal;
import invmod.common.nexus.INexusAccess;
import invmod.common.util.PosRotate3D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Deque;

public class EntityIMBurrower extends Monster implements IHasNexus {
    private static final int NUMBER_OF_SEGMENTS = 16;
    private static final int SEGMENT_TICK_SPACING = 4;
    private static final int DIG_COOLDOWN_TICKS = 6;
    private static final EntityDataAccessor<Integer> DATA_TIER = SynchedEntityData.defineId(EntityIMBurrower.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FLAVOUR = SynchedEntityData.defineId(EntityIMBurrower.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TEXTURE = SynchedEntityData.defineId(EntityIMBurrower.class, EntityDataSerializers.INT);

    private int tier;
    private int flavour;
    private INexusAccess nexus;
    private int digCooldown;
    private final Deque<PosRotate3D> segmentHistory;
    private final PosRotate3D[] segments3D;
    private final PosRotate3D[] segments3DLastTick;
    private float rotX;
    private float rotY;
    private float rotZ;
    private float prevRotX;
    private float prevRotY;
    private float prevRotZ;

    public EntityIMBurrower(EntityType<? extends EntityIMBurrower> type, Level level) {
        super(type, level);
        this.tier = 1;
        this.flavour = 0;
        this.segmentHistory = new ArrayDeque<>();
        this.segments3D = new PosRotate3D[NUMBER_OF_SEGMENTS];
        this.segments3DLastTick = new PosRotate3D[NUMBER_OF_SEGMENTS];
        for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
            this.segments3D[i] = new PosRotate3D();
            this.segments3DLastTick[i] = new PosRotate3D();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 22.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.22D)
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
        this.tier = tier;
        entityData.set(DATA_TIER, tier);
        double health = 22.0D + Math.max(0, tier - 1) * 8.0D;
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

    public float getRotX() {
        return rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public float getPrevRotX() {
        return prevRotX;
    }

    public float getPrevRotY() {
        return prevRotY;
    }

    public float getPrevRotZ() {
        return prevRotZ;
    }

    public PosRotate3D[] getSegments3D() {
        return segments3D;
    }

    public PosRotate3D[] getSegments3DLastTick() {
        return segments3DLastTick;
    }

    public void setSegment(int index, PosRotate3D pos) {
        if (index < segments3D.length) {
            copyPosRotate(segments3DLastTick[index], segments3D[index]);
            copyPosRotate(segments3D[index], pos);
        }
    }

    public void setHeadRotation(PosRotate3D pos) {
        prevRotX = rotX;
        prevRotY = rotY;
        prevRotZ = rotZ;
        rotX = pos.getRotX();
        rotY = pos.getRotY();
        rotZ = pos.getRotZ();
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
    public void tick() {
        super.tick();
        updateSegmentTrail();
        if (digCooldown > 0) {
            digCooldown--;
        }
        if (!level().isClientSide && shouldAttemptDig()) {
            tryDigForward();
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_TIER.equals(key)) {
            tier = entityData.get(DATA_TIER);
        } else if (DATA_FLAVOUR.equals(key)) {
            flavour = entityData.get(DATA_FLAVOUR);
        }
    }

    private void updateSegmentTrail() {
        updateHeadRotation();
        PosRotate3D headSnapshot = new PosRotate3D(getX(), getY(), getZ(), rotX, rotY, rotZ);
        segmentHistory.addFirst(headSnapshot);
        int maxHistory = NUMBER_OF_SEGMENTS * SEGMENT_TICK_SPACING + 1;
        while (segmentHistory.size() > maxHistory) {
            segmentHistory.removeLast();
        }
        if (segmentHistory.size() == 1) {
            while (segmentHistory.size() < maxHistory) {
                segmentHistory.addLast(headSnapshot);
            }
        }

        for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
            PosRotate3D sample = getHistorySample((i + 1) * SEGMENT_TICK_SPACING);
            copyPosRotate(segments3DLastTick[i], segments3D[i]);
            copyPosRotate(segments3D[i], sample);
        }
    }

    private void updateHeadRotation() {
        prevRotX = rotX;
        prevRotY = rotY;
        prevRotZ = rotZ;
        rotX = (float) (getXRot() * Mth.DEG_TO_RAD);
        rotY = (float) (getYRot() * Mth.DEG_TO_RAD);
        rotZ = 0.0F;
    }

    private PosRotate3D getHistorySample(int index) {
        int i = 0;
        PosRotate3D fallback = segmentHistory.peekLast();
        for (PosRotate3D pos : segmentHistory) {
            if (i == index) {
                return pos;
            }
            i++;
        }
        return fallback == null ? new PosRotate3D(getX(), getY(), getZ(), rotX, rotY, rotZ) : fallback;
    }

    private static void copyPosRotate(PosRotate3D target, PosRotate3D source) {
        target.setPosX(source.getPosX());
        target.setPosY(source.getPosY());
        target.setPosZ(source.getPosZ());
        target.setRotX(source.getRotX());
        target.setRotY(source.getRotY());
        target.setRotZ(source.getRotZ());
    }

    private boolean shouldAttemptDig() {
        return digCooldown == 0 && horizontalCollision && (nexus != null || getTarget() != null) && canDig();
    }

    private boolean canDig() {
        return tier > 0;
    }

    private void tryDigForward() {
        digCooldown = DIG_COOLDOWN_TICKS;
        Direction direction = Direction.fromYRot(getYRot());
        BlockPos targetPos = blockPosition().relative(direction);
        boolean dug = destroyIfDestructible(targetPos);
        dug |= destroyIfDestructible(targetPos.above());
        if (getDeltaMovement().y < -0.05D) {
            dug |= destroyIfDestructible(targetPos.below());
        }
        if (dug) {
            level().playSound(null, targetPos, getScrapeSound(), getSoundSource(), 0.7F, 0.9F + random.nextFloat() * 0.2F);
        }
    }

    private boolean destroyIfDestructible(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        if (!isBlockTypeDestructible(state, pos)) {
            return false;
        }
        return level().destroyBlock(pos, Invasion.getDestructedBlocksDrop(), this);
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
