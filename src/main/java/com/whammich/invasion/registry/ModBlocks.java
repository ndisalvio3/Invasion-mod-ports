package com.whammich.invasion.registry;

import invmod.common.nexus.BlockNexus;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModBlocks {
    public static final DeferredBlock<BlockNexus> NEXUS = ModRegistries.BLOCKS.register("nexus", () -> new BlockNexus(BlockBehaviour.Properties.of()
        .strength(3.0F, 6000000.0F)
        .sound(SoundType.GLASS)
        .noOcclusion()));

    public static final DeferredItem<BlockItem> NEXUS_ITEM = ModRegistries.ITEMS.register("nexus", () -> new BlockItem(NEXUS.get(), new Item.Properties()));

    public static void init() {
    }

    private ModBlocks() {
    }
}
