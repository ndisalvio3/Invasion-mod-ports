package com.whammich.invasion.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class ItemWandDebug extends Item {
    public ItemWandDebug(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            Player player = context.getPlayer();
            if (player != null) {
                player.displayClientMessage(Component.literal("Debug wand used at " + context.getClickedPos()), true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
