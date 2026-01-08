package com.whammich.invasion.items;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import invmod.common.entity.EntityIMTrap;
import com.whammich.invasion.registry.ModEntities;

public class ItemTrap extends Item {
    public enum Type {
        EMPTY,
        RIFT,
        FLAME
    }

    private final Type type;

    public ItemTrap(Type type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos().above();
        EntityIMTrap trap = new EntityIMTrap(ModEntities.IM_TRAP.get(), level);
        trap.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        trap.setTrapType(type == Type.RIFT ? EntityIMTrap.TRAP_RIFT : type == Type.FLAME ? EntityIMTrap.TRAP_FIRE : EntityIMTrap.TRAP_DEFAULT);
        if (level.addFreshEntity(trap)) {
            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
