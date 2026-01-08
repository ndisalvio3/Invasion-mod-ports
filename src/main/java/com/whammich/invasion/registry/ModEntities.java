package com.whammich.invasion.registry;

import com.whammich.invasion.Reference;
import invmod.common.entity.EntityIMBolt;
import invmod.common.entity.EntityIMBoulder;
import invmod.common.entity.EntityIMEgg;
import invmod.common.entity.EntityIMPrimedTNT;
import invmod.common.entity.EntityIMSpawnProxy;
import invmod.common.entity.EntityIMTrap;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntitySFX;
import invmod.common.entity.EntityIMWolf;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.EntityIMZombiePigman;
import invmod.common.entity.EntityIMSkeleton;
import invmod.common.entity.EntityIMImp;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMBird;
import invmod.common.entity.EntityIMBurrower;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModEntities {
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMTrap>> IM_TRAP = ModRegistries.ENTITY_TYPES.register(
        "im_trap",
        () -> EntityType.Builder.<EntityIMTrap>of(EntityIMTrap::new, MobCategory.MISC)
            .sized(0.5F, 0.28F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_trap")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMBolt>> IM_BOLT = ModRegistries.ENTITY_TYPES.register(
        "im_bolt",
        () -> EntityType.Builder.<EntityIMBolt>of(EntityIMBolt::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(64)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_bolt")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMBoulder>> IM_BOULDER = ModRegistries.ENTITY_TYPES.register(
        "im_boulder",
        () -> EntityType.Builder.<EntityIMBoulder>of(EntityIMBoulder::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(64)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_boulder")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMPrimedTNT>> IM_PRIMED_TNT = ModRegistries.ENTITY_TYPES.register(
        "im_primed_tnt",
        () -> EntityType.Builder.<EntityIMPrimedTNT>of(EntityIMPrimedTNT::new, MobCategory.MISC)
            .sized(1.0F, 1.0F)
            .clientTrackingRange(64)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_primed_tnt")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMSpawnProxy>> IM_SPAWN_PROXY = ModRegistries.ENTITY_TYPES.register(
        "im_spawn_proxy",
        () -> EntityType.Builder.<EntityIMSpawnProxy>of(EntityIMSpawnProxy::new, MobCategory.MISC)
            .sized(0.6F, 1.8F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_spawn_proxy")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMEgg>> IM_EGG = ModRegistries.ENTITY_TYPES.register(
        "im_egg",
        () -> EntityType.Builder.<EntityIMEgg>of(EntityIMEgg::new, MobCategory.MISC)
            .sized(0.5F, 0.8F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_egg")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMZombie>> IM_ZOMBIE = ModRegistries.ENTITY_TYPES.register(
        "im_zombie",
        () -> EntityType.Builder.<EntityIMZombie>of(EntityIMZombie::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_zombie")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMSpider>> IM_SPIDER = ModRegistries.ENTITY_TYPES.register(
        "im_spider",
        () -> EntityType.Builder.<EntityIMSpider>of(EntityIMSpider::new, MobCategory.MONSTER)
            .sized(1.4F, 0.9F)
            .clientTrackingRange(8)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_spider")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMWolf>> IM_WOLF = ModRegistries.ENTITY_TYPES.register(
        "im_wolf",
        () -> EntityType.Builder.<EntityIMWolf>of(EntityIMWolf::new, MobCategory.MONSTER)
            .sized(0.6F, 0.85F)
            .clientTrackingRange(8)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_wolf")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMZombiePigman>> IM_ZOMBIE_PIGMAN = ModRegistries.ENTITY_TYPES.register(
        "im_zombie_pigman",
        () -> EntityType.Builder.<EntityIMZombiePigman>of(EntityIMZombiePigman::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_zombie_pigman")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMSkeleton>> IM_SKELETON = ModRegistries.ENTITY_TYPES.register(
        "im_skeleton",
        () -> EntityType.Builder.<EntityIMSkeleton>of(EntityIMSkeleton::new, MobCategory.MONSTER)
            .sized(0.6F, 1.99F)
            .clientTrackingRange(8)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_skeleton")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMImp>> IM_IMP = ModRegistries.ENTITY_TYPES.register(
        "im_imp",
        () -> EntityType.Builder.<EntityIMImp>of(EntityIMImp::new, MobCategory.MONSTER)
            .sized(0.6F, 0.95F)
            .clientTrackingRange(8)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_imp")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMThrower>> IM_THROWER = ModRegistries.ENTITY_TYPES.register(
        "im_thrower",
        () -> EntityType.Builder.<EntityIMThrower>of(EntityIMThrower::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_thrower")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMBird>> IM_BIRD = ModRegistries.ENTITY_TYPES.register(
        "im_bird",
        () -> EntityType.Builder.<EntityIMBird>of(EntityIMBird::new, MobCategory.MONSTER)
            .sized(0.9F, 0.9F)
            .clientTrackingRange(8)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_bird")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIMBurrower>> IM_BURROWER = ModRegistries.ENTITY_TYPES.register(
        "im_burrower",
        () -> EntityType.Builder.<EntityIMBurrower>of(EntityIMBurrower::new, MobCategory.MONSTER)
            .sized(0.6F, 0.9F)
            .clientTrackingRange(8)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_burrower")))
    );
    public static final DeferredHolder<EntityType<?>, EntityType<EntitySFX>> IM_SFX = ModRegistries.ENTITY_TYPES.register(
        "im_sfx",
        () -> EntityType.Builder.<EntitySFX>of(EntitySFX::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(32)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Reference.MODID, "im_sfx")))
    );

    private ModEntities() {
    }
}
