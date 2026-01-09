package com.whammich.invasion.items;

import com.whammich.invasion.network.NetworkHandler;
import com.whammich.invasion.network.payload.CustomEffectPayload;
import invmod.common.nexus.TileEntityNexus;
import net.minecraft.core.BlockPos;
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

        NetworkHandler.sendItemInteraction(player, "No nexus found.", true);
        return InteractionResult.SUCCESS;
    }

    private void handleNexusAdjuster(Player player, TileEntityNexus nexus) {
        if (player.isShiftKeyDown()) {
            boolean newState = !nexus.isActive();
            nexus.setActive(newState);
            NetworkHandler.sendItemInteraction(player, "Nexus " + (newState ? "activated" : "deactivated"), true);
            NetworkHandler.sendCustomEffect(player, nexus.getBlockPos(), CustomEffectPayload.EffectType.NEXUS_ADJUST);
        } else {
            boolean adjusted = nexus.adjustSpawnRadius(2);
            if (!adjusted) {
                NetworkHandler.sendItemInteraction(player, "Nexus spawn radius locked while active.", true);
            } else {
                NetworkHandler.sendCustomEffect(player, nexus.getBlockPos(), CustomEffectPayload.EffectType.NEXUS_ADJUST);
            }
        }
        reportNexusStatus(player, nexus);
    }

    private void reportNexusStatus(Player player, TileEntityNexus nexus) {
        NetworkHandler.sendItemInteraction(
            player,
            "Nexus: active=" + nexus.isActive()
                + " level=" + nexus.getNexusLevel()
                + " power=" + nexus.getNexusPowerLevel()
                + " wave=" + nexus.getCurrentWave()
                + " radius=" + nexus.getSpawnRadius()
                + " kills=" + nexus.getNexusKills(),
            true
        );
    }

    private void reportBlockInfo(Player player, BlockState state, Level level, BlockPos pos) {
        float hardness = state.getDestroySpeed(level, pos);
        float resistance = state.getBlock().getExplosionResistance();
        NetworkHandler.sendItemInteraction(
            player,
            "Block: " + state.getBlock().getName().getString()
                + " hardness=" + hardness
                + " blastRes=" + resistance,
            true
        );
    }
}
