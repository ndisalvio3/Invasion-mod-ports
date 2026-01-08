package com.whammich.invasion.registry;

import com.whammich.invasion.items.ItemBowSearing;
import com.whammich.invasion.items.ItemHammerEngineer;
import com.whammich.invasion.items.ItemMaterials;
import com.whammich.invasion.items.ItemProbe;
import com.whammich.invasion.items.ItemStrangeBone;
import com.whammich.invasion.items.ItemSwordInfused;
import com.whammich.invasion.items.ItemTrap;
import com.whammich.invasion.items.ItemWandDebug;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public final class ModItems {
    public static final DeferredItem<Item> CATALYST_MIXTURE_UNSTABLE = ModRegistries.ITEMS.register(
        "catalyst_mixture_unstable",
        () -> new ItemMaterials(ItemMaterials.Type.CATALYST_MIXTURE_UNSTABLE, new Item.Properties())
    );
    public static final DeferredItem<Item> CATALYST_MIXTURE_STABLE = ModRegistries.ITEMS.register(
        "catalyst_mixture_stable",
        () -> new ItemMaterials(ItemMaterials.Type.CATALYST_MIXTURE_STABLE, new Item.Properties())
    );
    public static final DeferredItem<Item> NEXUS_CATALYST_UNSTABLE = ModRegistries.ITEMS.register(
        "nexus_catalyst_unstable",
        () -> new ItemMaterials(ItemMaterials.Type.NEXUS_CATALYST_UNSTABLE, new Item.Properties())
    );
    public static final DeferredItem<Item> NEXUS_CATALYST_STABLE = ModRegistries.ITEMS.register(
        "nexus_catalyst_stable",
        () -> new ItemMaterials(ItemMaterials.Type.NEXUS_CATALYST_STABLE, new Item.Properties())
    );
    public static final DeferredItem<Item> CATALYST_STRONG = ModRegistries.ITEMS.register(
        "catalyst_strong",
        () -> new ItemMaterials(ItemMaterials.Type.CATALYST_STRONG, new Item.Properties())
    );
    public static final DeferredItem<Item> DAMPING_AGENT_WEAK = ModRegistries.ITEMS.register(
        "damping_agent_weak",
        () -> new ItemMaterials(ItemMaterials.Type.DAMPING_AGENT_WEAK, new Item.Properties())
    );
    public static final DeferredItem<Item> DAMPING_AGENT_STRONG = ModRegistries.ITEMS.register(
        "damping_agent_strong",
        () -> new ItemMaterials(ItemMaterials.Type.DAMPING_AGENT_STRONG, new Item.Properties())
    );
    public static final DeferredItem<Item> SMALL_REMNANTS = ModRegistries.ITEMS.register(
        "small_remnants",
        () -> new ItemMaterials(ItemMaterials.Type.SMALL_REMNANTS, new Item.Properties())
    );
    public static final DeferredItem<Item> RIFT_FLUX = ModRegistries.ITEMS.register(
        "rift_flux",
        () -> new ItemMaterials(ItemMaterials.Type.RIFT_FLUX, new Item.Properties())
    );
    public static final DeferredItem<Item> PHASE_CRYSTAL = ModRegistries.ITEMS.register(
        "phase_crystal",
        () -> new ItemMaterials(ItemMaterials.Type.PHASE_CRYSTAL, new Item.Properties())
    );

    public static final DeferredItem<ItemSwordInfused> INFUSED_SWORD = ModRegistries.ITEMS.register("infused_sword", () -> new ItemSwordInfused(new Item.Properties().durability(40).stacksTo(1)));
    public static final DeferredItem<ItemWandDebug> DEBUG_WAND = ModRegistries.ITEMS.register("debug_wand", () -> new ItemWandDebug(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemHammerEngineer> ENGINEER_HAMMER = ModRegistries.ITEMS.register(
        "engineer_hammer",
        () -> new ItemHammerEngineer(new Item.Properties().durability(250).repairable(Items.IRON_INGOT).stacksTo(1))
    );
    public static final DeferredItem<ItemProbe> NEXUS_ADJUSTER = ModRegistries.ITEMS.register("nexus_adjuster", () -> new ItemProbe(ItemProbe.Type.NEXUS_ADJUSTER, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemProbe> MATERIAL_PROBE = ModRegistries.ITEMS.register("material_probe", () -> new ItemProbe(ItemProbe.Type.MATERIAL, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<ItemStrangeBone> STRANGE_BONE = ModRegistries.ITEMS.register("strange_bone", () -> new ItemStrangeBone(new Item.Properties()));
    public static final DeferredItem<ItemBowSearing> SEARING_BOW = ModRegistries.ITEMS.register(
        "searing_bow",
        () -> new ItemBowSearing(new Item.Properties().durability(384).repairable(RIFT_FLUX.get()))
    );
    public static final DeferredItem<ItemTrap> TRAP_EMPTY = ModRegistries.ITEMS.register("trap_empty", () -> new ItemTrap(ItemTrap.Type.EMPTY, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<ItemTrap> TRAP_RIFT = ModRegistries.ITEMS.register("trap_rift", () -> new ItemTrap(ItemTrap.Type.RIFT, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<ItemTrap> TRAP_FLAME = ModRegistries.ITEMS.register("trap_flame", () -> new ItemTrap(ItemTrap.Type.FLAME, new Item.Properties().stacksTo(16)));

    public static final DeferredItem<SpawnEggItem> IM_ZOMBIE_EGG = ModRegistries.ITEMS.register(
        "im_zombie_spawn_egg", () -> new SpawnEggItem(ModEntities.IM_ZOMBIE.get(), new Item.Properties())
    );
    public static final DeferredItem<SpawnEggItem> IM_SPIDER_EGG = ModRegistries.ITEMS.register(
        "im_spider_spawn_egg", () -> new SpawnEggItem(ModEntities.IM_SPIDER.get(), new Item.Properties())
    );
    public static final DeferredItem<SpawnEggItem> IM_WOLF_EGG = ModRegistries.ITEMS.register(
        "im_wolf_spawn_egg", () -> new SpawnEggItem(ModEntities.IM_WOLF.get(), new Item.Properties())
    );
    public static final DeferredItem<SpawnEggItem> IM_ZOMBIE_PIGMAN_EGG = ModRegistries.ITEMS.register(
        "im_zombie_pigman_spawn_egg", () -> new SpawnEggItem(ModEntities.IM_ZOMBIE_PIGMAN.get(), new Item.Properties())
    );
    public static final DeferredItem<SpawnEggItem> IM_SKELETON_EGG = ModRegistries.ITEMS.register(
        "im_skeleton_spawn_egg", () -> new SpawnEggItem(ModEntities.IM_SKELETON.get(), new Item.Properties())
    );
    public static final DeferredItem<SpawnEggItem> IM_IMP_EGG = ModRegistries.ITEMS.register(
        "im_imp_spawn_egg", () -> new SpawnEggItem(ModEntities.IM_IMP.get(), new Item.Properties())
    );
    public static final DeferredItem<SpawnEggItem> IM_THROWER_EGG = ModRegistries.ITEMS.register(
        "im_thrower_spawn_egg", () -> new SpawnEggItem(ModEntities.IM_THROWER.get(), new Item.Properties())
    );
    public static final DeferredItem<SpawnEggItem> IM_BIRD_EGG = ModRegistries.ITEMS.register(
        "im_bird_spawn_egg", () -> new SpawnEggItem(ModEntities.IM_BIRD.get(), new Item.Properties())
    );
    public static final DeferredItem<SpawnEggItem> IM_BURROWER_EGG = ModRegistries.ITEMS.register(
        "im_burrower_spawn_egg", () -> new SpawnEggItem(ModEntities.IM_BURROWER.get(), new Item.Properties())
    );

    public static void init() {
    }

    public static void registerDispenserBehaviors() {
        registerSpawnEggDispenserBehavior(IM_ZOMBIE_EGG.get());
        registerSpawnEggDispenserBehavior(IM_SPIDER_EGG.get());
        registerSpawnEggDispenserBehavior(IM_WOLF_EGG.get());
        registerSpawnEggDispenserBehavior(IM_ZOMBIE_PIGMAN_EGG.get());
        registerSpawnEggDispenserBehavior(IM_SKELETON_EGG.get());
        registerSpawnEggDispenserBehavior(IM_IMP_EGG.get());
        registerSpawnEggDispenserBehavior(IM_THROWER_EGG.get());
        registerSpawnEggDispenserBehavior(IM_BIRD_EGG.get());
        registerSpawnEggDispenserBehavior(IM_BURROWER_EGG.get());
    }

    private static void registerSpawnEggDispenserBehavior(SpawnEggItem egg) {
        if (!DispenserBlock.DISPENSER_REGISTRY.containsKey(egg)) {
            DispenserBlock.registerBehavior(egg, SpawnEggItem.DEFAULT_DISPENSE_BEHAVIOR);
        }
    }

    private ModItems() {
    }
}
