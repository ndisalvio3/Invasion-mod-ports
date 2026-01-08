package com.whammich.invasion.registry;

import invmod.common.nexus.TileEntityNexus;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlockEntities {
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityNexus>> NEXUS = ModRegistries.BLOCK_ENTITY_TYPES.register(
        "nexus",
        () -> new BlockEntityType<>(TileEntityNexus::new, ModBlocks.NEXUS.get())
    );

    public static void init() {
    }

    private ModBlockEntities() {
    }
}
