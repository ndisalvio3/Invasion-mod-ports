package com.whammich.invasion.items;

import invmod.common.nexus.TileEntityNexus;
import net.minecraft.core.BlockPos;
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
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (blockEntity instanceof TileEntityNexus nexus) {
            if (type == Type.NEXUS_ADJUSTER) {
                handleNexusAdjuster(player, nexus);
                return InteractionResult.SUCCESS;
            }
            if (type == Type.MATERIAL) {
                reportNexusStatus(player, nexus);
                return InteractionResult.SUCCESS;
            }
        }

        if (type == Type.MATERIAL) {
        reportBlockInfo(player, state, level, context.getClickedPos());
        return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(Component.literal("No nexus found."), true);
        return InteractionResult.SUCCESS;
    }

    private void handleNexusAdjuster(Player player, TileEntityNexus nexus) {
        if (player.isShiftKeyDown()) {
            boolean newState = !nexus.isActive();
            nexus.setActive(newState);
            player.displayClientMessage(Component.literal("Nexus " + (newState ? "activated" : "deactivated")), true);
        } else {
            boolean adjusted = nexus.adjustSpawnRadius(2);
            if (!adjusted) {
                player.displayClientMessage(Component.literal("Nexus spawn radius locked while active."), true);
            }
        }
        reportNexusStatus(player, nexus);
    }

    private void reportNexusStatus(Player player, TileEntityNexus nexus) {
        player.displayClientMessage(
            Component.literal(
                "Nexus: active=" + nexus.isActive()
                    + " level=" + nexus.getNexusLevel()
                    + " power=" + nexus.getNexusPowerLevel()
                    + " wave=" + nexus.getCurrentWave()
                    + " radius=" + nexus.getSpawnRadius()
                    + " kills=" + nexus.getNexusKills()
            ),
            true
        );
    }

    private void reportBlockInfo(Player player, BlockState state, Level level, BlockPos pos) {
        float hardness = state.getDestroySpeed(level, pos);
        float resistance = state.getBlock().getExplosionResistance();
        player.displayClientMessage(
            Component.literal(
                "Block: " + state.getBlock().getName().getString()
                    + " hardness=" + hardness
                    + " blastRes=" + resistance
            ),
            true
        );
    }
}
