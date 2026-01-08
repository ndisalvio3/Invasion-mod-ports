package invmod.common.entity;

import invmod.Invasion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class EntityIMSpawnProxy extends PathfinderMob {
    public EntityIMSpawnProxy(EntityType<? extends EntityIMSpawnProxy> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            Entity[] entities = Invasion.getNightMobSpawns1(level());
            for (Entity entity : entities) {
                entity.setPos(getX(), getY(), getZ());
                entity.setYRot(getYRot());
                entity.setXRot(getXRot());
                level().addFreshEntity(entity);
            }
            discard();
        }
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
}
