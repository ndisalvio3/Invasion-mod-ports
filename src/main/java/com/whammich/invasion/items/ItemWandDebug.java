package com.whammich.invasion.items;

import com.whammich.invasion.network.NetworkHandler;
import invmod.common.nexus.TileEntityNexus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ItemWandDebug extends Item {
    public ItemWandDebug(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            Player player = context.getPlayer();
            if (player != null) {
                BlockPos pos = context.getClickedPos();
                BlockState state = level.getBlockState(pos);
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof TileEntityNexus nexus) {
                    NetworkHandler.sendItemInteraction(
                        player,
                        "Nexus debug: active=" + nexus.isActive()
                            + " level=" + nexus.getNexusLevel()
                            + " wave=" + nexus.getCurrentWave()
                            + " power=" + nexus.getNexusPowerLevel()
                            + " radius=" + nexus.getSpawnRadius(),
                        true
                    );
                } else {
                    String blockEntityId = blockEntity != null ? blockEntity.getType().toString() : "none";
                    NetworkHandler.sendItemInteraction(
                        player,
                        "Block debug: " + state.getBlock().getName().getString()
                            + " pos=" + pos.getX() + "," + pos.getY() + "," + pos.getZ()
                            + " be=" + blockEntityId,
                        true
                    );
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
