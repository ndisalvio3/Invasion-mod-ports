package invmod.common.entity;

import com.whammich.invasion.registry.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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

public class EntitySFX extends Entity {
    private static final int DEFAULT_LIFESPAN = 200;
    private static final int EFFECT_NONE = 0;
    private static final int EFFECT_ZAP = 1;
    private static final int EFFECT_SCRAPE = 2;
    private static final int EFFECT_EGG_HATCH = 3;
    private static final int EFFECT_FIREBALL = 4;
    private static final int EFFECT_BIG_ZOMBIE = 5;

    private static final EntityDataAccessor<Integer> DATA_EFFECT_TYPE = SynchedEntityData.defineId(EntitySFX.class, EntityDataSerializers.INT);

    private int lifespan = DEFAULT_LIFESPAN;
    private boolean effectPlayed;

    public EntitySFX(EntityType<? extends EntitySFX> type, Level level) {
        super(type, level);
    }

    public void setup(double x, double y, double z) {
        setPos(x, y, z);
    }

    public void setup(double x, double y, double z, int effectType) {
        setPos(x, y, z);
        setEffectType(effectType);
    }

    @Override
    public void tick() {
        super.tick();
        if (!effectPlayed && getEffectType() != EFFECT_NONE) {
            playEffect();
            effectPlayed = true;
        }
        if (lifespan-- <= 0) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_EFFECT_TYPE, EFFECT_NONE);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.lifespan = tag.getIntOr("Lifespan", DEFAULT_LIFESPAN);
        setEffectType(tag.getIntOr("EffectType", EFFECT_NONE));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Lifespan", lifespan);
        tag.putInt("EffectType", getEffectType());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (DATA_EFFECT_TYPE.equals(accessor)) {
            effectPlayed = false;
        }
    }

    public int getEffectType() {
        return entityData.get(DATA_EFFECT_TYPE);
    }

    public void setEffectType(int effectType) {
        if (effectType != getEffectType()) {
            entityData.set(DATA_EFFECT_TYPE, effectType);
            effectPlayed = false;
        }
    }

    public void setLifespan(int lifespan) {
        this.lifespan = lifespan;
    }

    private void playEffect() {
        switch (getEffectType()) {
            case EFFECT_ZAP -> {
                playEffectSound(getZapSound(), 1.0F, 1.0F);
                spawnParticles(ParticleTypes.ELECTRIC_SPARK, 8, 0.2D, 0.4D);
            }
            case EFFECT_SCRAPE -> {
                playEffectSound(getScrapeSound(), 0.8F, 0.9F + random.nextFloat() * 0.2F);
                spawnParticles(ParticleTypes.SMOKE, 6, 0.25D, 0.2D);
            }
            case EFFECT_EGG_HATCH -> {
                playEffectSound(getEggHatchSound(), 1.0F, 0.9F + random.nextFloat() * 0.2F);
                spawnParticles(ParticleTypes.POOF, 8, 0.3D, 0.3D);
            }
            case EFFECT_FIREBALL -> {
                playEffectSound(ModSounds.FIREBALL.get(), 0.7F, 0.9F + random.nextFloat() * 0.2F);
                spawnParticles(ParticleTypes.FLAME, 10, 0.25D, 0.2D);
            }
            case EFFECT_BIG_ZOMBIE -> {
                playEffectSound(ModSounds.BIG_ZOMBIE.get(), 1.0F, 0.8F + random.nextFloat() * 0.2F);
                spawnParticles(ParticleTypes.SMOKE, 10, 0.35D, 0.25D);
            }
            default -> {
            }
        }
    }

    private void playEffectSound(SoundEvent sound, float volume, float pitch) {
        if (!level().isClientSide) {
            level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.HOSTILE, volume, pitch);
        }
    }

    private void spawnParticles(ParticleOptions particle, int count, double horizontalSpread, double verticalSpread) {
        for (int i = 0; i < count; i++) {
            double xOffset = (random.nextDouble() - 0.5D) * horizontalSpread;
            double yOffset = (random.nextDouble() - 0.5D) * verticalSpread;
            double zOffset = (random.nextDouble() - 0.5D) * horizontalSpread;
            level().addParticle(
                particle,
                getX() + xOffset,
                getY() + yOffset,
                getZ() + zOffset,
                0.0D,
                0.0D,
                0.0D
            );
        }
    }

    private SoundEvent getZapSound() {
        return switch (random.nextInt(3)) {
            case 1 -> ModSounds.ZAP_2.get();
            case 2 -> ModSounds.ZAP_3.get();
            default -> ModSounds.ZAP_1.get();
        };
    }

    private SoundEvent getScrapeSound() {
        return switch (random.nextInt(3)) {
            case 1 -> ModSounds.SCRAPE_2.get();
            case 2 -> ModSounds.SCRAPE_3.get();
            default -> ModSounds.SCRAPE_1.get();
        };
    }

    private SoundEvent getEggHatchSound() {
        return random.nextBoolean() ? ModSounds.EGG_HATCH_1.get() : ModSounds.EGG_HATCH_2.get();
    }
}
