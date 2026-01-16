package invmod.common.entity;

import invmod.Invasion;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class EntityIMSpawnProxy extends PathfinderMob {
    public EntityIMSpawnProxy(EntityType<? extends EntityIMSpawnProxy> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level();
        if (!canSpawnHere(serverLevel)) {
            discard();
            return;
        }
        Entity[] entities = Invasion.getNightMobSpawns1(level());
        for (Entity entity : entities) {
            entity.setPos(getX(), getY(), getZ());
            entity.setYRot(getYRot());
            entity.setXRot(getXRot());
            level().addFreshEntity(entity);
        }
        discard();
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 0.5F - level.getMaxLocalRawBrightness(pos) / 15.0F;
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, EntitySpawnReason reason) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return super.checkSpawnRules(level, reason) && Monster.isDarkEnoughToSpawn(serverLevel, blockPosition(), serverLevel.getRandom());
    }

    private boolean canSpawnHere(ServerLevel level) {
        if (!checkSpawnObstruction(level)) {
            return false;
        }
        return Monster.isDarkEnoughToSpawn(level, blockPosition(), level.getRandom());
    }
}
