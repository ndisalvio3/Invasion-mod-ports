package invmod.common.entity;

import com.whammich.invasion.network.AdvancedSpawnData;
import com.whammich.invasion.network.NetworkHandler;
import invmod.Invasion;
import invmod.common.nexus.TileEntityNexus;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.entity.projectile.ProjectileUtil;

public class EntityIMBoulder extends Entity implements AdvancedSpawnData {
    private static final int DEFAULT_LIFE = 60;

    public boolean doesArrowBelongToPlayer;
    public int arrowShake;
    public LivingEntity shootingEntity;
    public boolean arrowCritical;
    private BlockPos inGroundPos;
    private BlockState inGroundState;
    private boolean inGround;
    private int life;
    private int ticksInGround;
    private int ticksInAir;
    private boolean sentSpawnData;

    public EntityIMBoulder(EntityType<? extends EntityIMBoulder> type, Level level) {
        super(type, level);
        this.life = DEFAULT_LIFE;
    }

    public void setupBoulder(LivingEntity shooter, float speed, float variance) {
        this.shootingEntity = shooter;
        this.doesArrowBelongToPlayer = shooter instanceof Player;
        setPos(shooter.getX(), shooter.getEyeY(), shooter.getZ());
        setYRot(shooter.getYRot());
        setXRot(shooter.getXRot());

        Vec3 direction = Vec3.directionFromRotation(shooter.getXRot(), shooter.getYRot());
        double vx = direction.x * speed + random.nextGaussian() * variance;
        double vy = direction.y * speed + random.nextGaussian() * variance;
        double vz = direction.z * speed + random.nextGaussian() * variance;
        setDeltaMovement(vx, vy, vz);
        updateRotationFromDelta();
        this.ticksInGround = 0;
        this.ticksInAir = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && !sentSpawnData) {
            sentSpawnData = true;
            CompoundTag tag = new CompoundTag();
            writeSpawnData(tag);
            NetworkHandler.sendEntitySpawnData(this, tag);
        }

        if (life-- <= 0 || inGround) {
            discard();
            return;
        }

        if (getDeltaMovement().lengthSqr() == 0.0D) {
            return;
        }

        ticksInAir++;

        Vec3 start = position();
        Vec3 delta = getDeltaMovement();
        Vec3 end = start.add(delta);

        HitResult blockHit = level().clip(new net.minecraft.world.level.ClipContext(
            start,
            end,
            net.minecraft.world.level.ClipContext.Block.COLLIDER,
            net.minecraft.world.level.ClipContext.Fluid.NONE,
            this
        ));

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
            level(),
            this,
            start,
            end,
            getBoundingBox().expandTowards(delta).inflate(1.0D),
            entity -> entity.isPickable() && entity != this.shootingEntity
        );

        HitResult hitResult = blockHit;
        if (entityHit != null) {
            double entityDist = start.distanceTo(entityHit.getLocation());
            double blockDist = blockHit == null ? Double.POSITIVE_INFINITY : start.distanceTo(blockHit.getLocation());
            if (entityDist <= blockDist) {
                hitResult = entityHit;
            }
        }

        if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            if (hitResult instanceof EntityHitResult entityHitResult) {
                handleEntityHit(entityHitResult);
            } else if (hitResult instanceof BlockHitResult blockHitResult) {
                handleBlockHit(blockHitResult);
            }
        }

        if (arrowCritical) {
            for (int i = 0; i < 4; i++) {
                Vec3 pos = position().add(delta.scale(i / 4.0D));
                level().addParticle(net.minecraft.core.particles.ParticleTypes.CRIT, pos.x, pos.y, pos.z, -delta.x, -delta.y + 0.2D, -delta.z);
            }
        }

        setPos(getX() + delta.x, getY() + delta.y, getZ() + delta.z);
        updateRotationFromDelta();

        float airResistance = 1.0F;
        if (isInWater()) {
            for (int i = 0; i < 4; i++) {
                Vec3 pos = position().subtract(delta.scale(0.25D));
                level().addParticle(net.minecraft.core.particles.ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, delta.x, delta.y, delta.z);
            }
            airResistance = 0.8F;
        }

        setDeltaMovement(delta.x * airResistance, delta.y * airResistance - 0.025F, delta.z * airResistance);
    }

    private void handleEntityHit(EntityHitResult entityHit) {
        int damage = (int) (Math.max(ticksInAir / 20.0F, 1.0F) * 6.0F);
        if (damage > 14) {
            damage = 14;
        }
        DamageSource source = shootingEntity != null ? damageSources().mobAttack(shootingEntity) : damageSources().generic();
        entityHit.getEntity().hurt(source, damage);
        level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.9F / (random.nextFloat() * 0.2F + 0.9F));
        discard();
    }

    private void handleBlockHit(BlockHitResult blockHit) {
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level().getBlockState(pos);
        this.inGroundPos = pos;
        this.inGroundState = state;
        this.inGround = true;
        Vec3 hit = blockHit.getLocation();
        Vec3 delta = getDeltaMovement();
        double length = delta.length();
        if (length > 0.0D) {
            setPos(hit.x - delta.x / length * 0.05D, hit.y - delta.y / length * 0.05D, hit.z - delta.z / length * 0.05D);
        }

        level().playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.9F / (random.nextFloat() * 0.2F + 0.9F));
        if (state.getBlock() == Invasion.blockNexus) {
            BlockEntity blockEntity = level().getBlockEntity(pos);
            if (blockEntity instanceof TileEntityNexus tileEntityNexus) {
                tileEntityNexus.attackNexus(2);
            }
        } else if (state.getBlock() != Blocks.BEDROCK && state.getBlock() != Blocks.CHEST) {
            if (level() instanceof ServerLevel serverLevel) {
                boolean mobGriefing = serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
                Level.ExplosionInteraction interaction = mobGriefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE;
                serverLevel.explode(this, hit.x, hit.y, hit.z, 0.5F, interaction);
            }
        }
    }

    private void updateRotationFromDelta() {
        Vec3 delta = getDeltaMovement();
        double xz = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (xz == 0.0D && delta.y == 0.0D) {
            return;
        }
        setYRot((float) (Math.atan2(delta.x, delta.z) * 180.0D / Math.PI));
        setXRot((float) (Math.atan2(delta.y, xz) * 180.0D / Math.PI));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.inGround = tag.getBooleanOr("InGround", false);
        this.life = tag.getIntOr("Life", DEFAULT_LIFE);
        this.ticksInGround = tag.getIntOr("TicksInGround", 0);
        this.ticksInAir = tag.getIntOr("TicksInAir", 0);
        this.arrowShake = tag.getIntOr("ArrowShake", 0);
        this.doesArrowBelongToPlayer = tag.getBooleanOr("BelongsToPlayer", false);
        if (tag.contains("InGroundPos")) {
            this.inGroundPos = BlockPos.of(tag.getLongOr("InGroundPos", 0L));
            this.inGroundState = level().getBlockState(inGroundPos);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("InGround", inGround);
        tag.putInt("Life", life);
        tag.putInt("TicksInGround", ticksInGround);
        tag.putInt("TicksInAir", ticksInAir);
        tag.putInt("ArrowShake", arrowShake);
        tag.putBoolean("BelongsToPlayer", doesArrowBelongToPlayer);
        if (inGroundPos != null) {
            tag.putLong("InGroundPos", inGroundPos.asLong());
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    public int getFlightTime() {
        return this.ticksInAir;
    }

    @Override
    public void writeSpawnData(CompoundTag tag) {
        tag.putBoolean("BelongsToPlayer", doesArrowBelongToPlayer);
        tag.putBoolean("ArrowCritical", arrowCritical);
        tag.putInt("ArrowShake", arrowShake);
        tag.putInt("TicksInAir", ticksInAir);
        tag.putInt("Life", life);
        if (shootingEntity != null) {
            tag.putInt("ShooterId", shootingEntity.getId());
        }
    }

    @Override
    public void readSpawnData(CompoundTag tag) {
        doesArrowBelongToPlayer = tag.getBooleanOr("BelongsToPlayer", false);
        arrowCritical = tag.getBooleanOr("ArrowCritical", false);
        arrowShake = tag.getIntOr("ArrowShake", 0);
        ticksInAir = tag.getIntOr("TicksInAir", 0);
        life = tag.getIntOr("Life", DEFAULT_LIFE);
        int shooterId = tag.getIntOr("ShooterId", 0);
        if (shooterId != 0 && level() != null) {
            Entity entity = level().getEntity(shooterId);
            if (entity instanceof LivingEntity living) {
                shootingEntity = living;
            }
        }
    }
}
