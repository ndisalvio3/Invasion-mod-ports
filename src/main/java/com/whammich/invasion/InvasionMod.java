package com.whammich.invasion;

import com.mojang.logging.LogUtils;
import com.whammich.invasion.config.InvasionConfig;
import com.whammich.invasion.registry.ModRegistries;
import com.whammich.invasion.registry.ModItems;
import com.whammich.invasion.registry.ModEntities;
import com.whammich.invasion.network.NetworkHandler;
import invmod.Invasion;
import invmod.common.entity.EntityIMZombie;
import invmod.common.entity.EntityIMSpider;
import invmod.common.entity.EntityIMWolf;
import invmod.common.entity.EntityIMZombiePigman;
import invmod.common.entity.EntityIMSkeleton;
import invmod.common.entity.EntityIMImp;
import invmod.common.entity.EntityIMThrower;
import invmod.common.entity.EntityIMBird;
import invmod.common.entity.EntityIMBurrower;
import invmod.common.InvasionCommand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

@Mod(Reference.MODID)
public class InvasionMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public InvasionMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCommands);
        modEventBus.addListener(this::registerAttributes);
        modContainer.registerConfig(ModConfig.Type.COMMON, InvasionConfig.SPEC);
        NetworkHandler.register(modEventBus);
        ModRegistries.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Invasion.bindRegistries();
            ModItems.registerDispenserBehaviors();
        });
    }

    private void registerCommands(RegisterCommandsEvent event) {
        InvasionCommand.register(event.getDispatcher());
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.IM_ZOMBIE.get(), EntityIMZombie.createAttributes().build());
        event.put(ModEntities.IM_SPIDER.get(), EntityIMSpider.createAttributes().build());
        event.put(ModEntities.IM_WOLF.get(), EntityIMWolf.createAttributes().build());
        event.put(ModEntities.IM_ZOMBIE_PIGMAN.get(), EntityIMZombiePigman.createAttributes().build());
        event.put(ModEntities.IM_SKELETON.get(), EntityIMSkeleton.createAttributes().build());
        event.put(ModEntities.IM_IMP.get(), EntityIMImp.createAttributes().build());
        event.put(ModEntities.IM_THROWER.get(), EntityIMThrower.createAttributes().build());
        event.put(ModEntities.IM_BIRD.get(), EntityIMBird.createAttributes().build());
        event.put(ModEntities.IM_BURROWER.get(), EntityIMBurrower.createAttributes().build());
    }

    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.sendConfigSync(player);
        }
    }
}
