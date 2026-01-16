package invmod.common.entity;

import com.whammich.invasion.registry.ModSounds;
import invmod.common.nexus.INexusAccess;
import invmod.common.nexus.InvMobConstruct;
import invmod.common.nexus.MobBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityIMEgg extends Entity {
    private static final EntityDataAccessor<Boolean> DATA_HATCHED = SynchedEntityData.defineId(EntityIMEgg.class, EntityDataSerializers.BOOLEAN);

    private int hatchTime;
    private int ticks;
    private boolean hatched;
    private InvMobConstruct[] contents;
    private INexusAccess nexus;

    public EntityIMEgg(EntityType<? extends EntityIMEgg> type, Level level) {
        super(type, level);
    }

    public void setupEgg(InvMobConstruct[] contents, int hatchTime, INexusAccess nexus) {
        this.contents = contents;
        this.hatchTime = hatchTime;
        this.nexus = nexus;
        this.hatched = false;
        this.ticks = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            ticks++;
            if (hatched) {
                if (ticks > hatchTime + 40) {
                    discard();
                }
            } else if (ticks > hatchTime) {
                hatch();
            }
        } else if (!hatched && entityData.get(DATA_HATCHED)) {
            hatched = true;
        }
    }

    private void hatch() {
        level().playSound(null, blockPosition(), getHatchSound(), SoundSource.HOSTILE, 1.0F, 1.0F);
        hatched = true;
        entityData.set(DATA_HATCHED, Boolean.TRUE);
        if (contents != null && level() instanceof ServerLevel serverLevel) {
            MobBuilder mobBuilder = new MobBuilder();
            for (InvMobConstruct construct : contents) {
                Entity entity = mobBuilder.createMobFromConstruct(construct, serverLevel, nexus);
                if (entity == null) {
                    continue;
                }
                entity.setPos(getX(), getY(), getZ());
                serverLevel.addFreshEntity(entity);
            }
        }
    }

    private SoundEvent getHatchSound() {
        return random.nextBoolean() ? ModSounds.EGG_HATCH_1.get() : ModSounds.EGG_HATCH_2.get();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_HATCHED, Boolean.FALSE);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.hatchTime = tag.getIntOr("HatchTime", 0);
        this.ticks = tag.getIntOr("Ticks", 0);
        this.hatched = tag.getBooleanOr("Hatched", false);
        entityData.set(DATA_HATCHED, hatched);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("HatchTime", hatchTime);
        tag.putInt("Ticks", ticks);
        tag.putBoolean("Hatched", hatched);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }
}
