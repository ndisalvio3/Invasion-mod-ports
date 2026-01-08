package invmod.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntitySFX extends Entity {
    private static final int DEFAULT_LIFESPAN = 200;

    private int lifespan = DEFAULT_LIFESPAN;

    public EntitySFX(EntityType<? extends EntitySFX> type, Level level) {
        super(type, level);
    }

    public void setup(double x, double y, double z) {
        setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        if (lifespan-- <= 0) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.lifespan = tag.getIntOr("Lifespan", DEFAULT_LIFESPAN);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Lifespan", lifespan);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
}
