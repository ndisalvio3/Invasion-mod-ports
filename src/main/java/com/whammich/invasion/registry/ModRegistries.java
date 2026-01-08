package com.whammich.invasion.registry;

import com.whammich.invasion.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistries {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Reference.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Reference.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Reference.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Reference.MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, Reference.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Reference.MODID);

    private ModRegistries() {
    }

    public static void register(IEventBus bus) {
        ModBlocks.init();
        ModItems.init();
        ModEntities.init();
        ModBlockEntities.init();
        ModMenus.init();
        ModSounds.init();
        ModCreativeTabs.init();

        ITEMS.register(bus);
        BLOCKS.register(bus);
        ENTITY_TYPES.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        SOUND_EVENTS.register(bus);
        MENUS.register(bus);
        ModCreativeTabs.TABS.register(bus);
    }
}
