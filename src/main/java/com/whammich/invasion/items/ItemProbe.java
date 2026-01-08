package com.whammich.invasion.items;

import invmod.common.nexus.TileEntityNexus;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ItemProbe extends Item {
    public enum Type {
        NEXUS_ADJUSTER,
        MATERIAL
    }

    private final Type type;

    public ItemProbe(Type type, Properties properties) {
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
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(context.getClickedPos());
        if (type == Type.NEXUS_ADJUSTER) {
            BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
            if (blockEntity instanceof TileEntityNexus nexus) {
                boolean newState = !nexus.isActive();
                nexus.setActive(newState);
                player.displayClientMessage(Component.literal("Nexus " + (newState ? "activated" : "deactivated")), true);
                return InteractionResult.SUCCESS;
            }
        }

        player.displayClientMessage(Component.literal("Block: " + state.getBlock().getName().getString()), true);
        return InteractionResult.SUCCESS;
    }
}
