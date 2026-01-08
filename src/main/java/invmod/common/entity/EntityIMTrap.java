package invmod.common.entity;

import com.whammich.invasion.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class EntityIMTrap extends Entity {
    public static final int TRAP_DEFAULT = 0;
    public static final int TRAP_RIFT = 1;
    public static final int TRAP_FIRE = 2;
    private static final int ARM_TIME = 60;

    private static final EntityDataAccessor<Integer> DATA_TRAP_TYPE = SynchedEntityData.defineId(EntityIMTrap.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_EMPTY = SynchedEntityData.defineId(EntityIMTrap.class, EntityDataSerializers.BOOLEAN);

    private int ticks;

    public EntityIMTrap(EntityType<? extends EntityIMTrap> type, Level level) {
        super(type, level);
    }


    @Override
    public void tick() {
        super.tick();
        ticks++;
        if (level().isClientSide) {
            return;
        }

        if (!isValidPlacement()) {
            if (level() instanceof ServerLevel serverLevel) {
                spawnAtLocation(serverLevel, ModItems.TRAP_EMPTY.get());
            }
            discard();
            return;
        }

        if (isEmpty() || ticks < ARM_TIME) {
            return;
        }

        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox());
        if (!entities.isEmpty()) {
            for (LivingEntity entity : entities) {
                if (trapEffect(entity)) {
                    setEmpty(true);
                    break;
                }
            }
        }
    }

    private boolean trapEffect(LivingEntity triggerEntity) {
        int type = getTrapType();
        DamageSource source;
        if (type == TRAP_RIFT) {
            source = damageSources().magic();
            triggerEntity.hurt(source, triggerEntity instanceof Player ? 12.0F : 38.0F);
        } else if (type == TRAP_FIRE) {
            source = damageSources().onFire();
            triggerEntity.hurt(source, 8.0F);
            triggerEntity.igniteForSeconds(4.0F);
        } else {
            source = damageSources().generic();
            triggerEntity.hurt(source, 4.0F);
        }
        return true;
    }

    private boolean isValidPlacement() {
        BlockPos below = blockPosition().below();
        BlockState belowState = level().getBlockState(below);
        return belowState.isSolidRender();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && isEmpty()) {
            ItemStack stack = new ItemStack(ModItems.TRAP_EMPTY.get());
            if (player.addItem(stack)) {
                discard();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public boolean isEmpty() {
        return entityData.get(DATA_EMPTY);
    }

    public void setEmpty(boolean empty) {
        entityData.set(DATA_EMPTY, empty);
        ticks = 0;
    }

    public int getTrapType() {
        return entityData.get(DATA_TRAP_TYPE);
    }

    public void setTrapType(int trapType) {
        entityData.set(DATA_TRAP_TYPE, trapType);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setEmpty(tag.getBooleanOr("Empty", false));
        setTrapType(tag.getIntOr("TrapType", TRAP_DEFAULT));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Empty", isEmpty());
        tag.putInt("TrapType", getTrapType());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TRAP_TYPE, TRAP_DEFAULT);
        builder.define(DATA_EMPTY, Boolean.FALSE);
    }
}
