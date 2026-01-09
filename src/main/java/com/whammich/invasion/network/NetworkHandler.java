package com.whammich.invasion.network;

import com.whammich.invasion.network.payload.BroadcastMessagePayload;
import com.whammich.invasion.network.payload.ConfigSyncPayload;
import com.whammich.invasion.network.payload.CustomEffectPayload;
import com.whammich.invasion.network.payload.ItemInteractionPayload;
import invmod.Invasion;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "3";

    private NetworkHandler() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC, NetworkHandler::handleConfigSync);
        registrar.playToClient(BroadcastMessagePayload.TYPE, BroadcastMessagePayload.STREAM_CODEC, NetworkHandler::handleBroadcastMessage);
        registrar.playToClient(ItemInteractionPayload.TYPE, ItemInteractionPayload.STREAM_CODEC, NetworkHandler::handleItemInteraction);
        registrar.playToClient(CustomEffectPayload.TYPE, CustomEffectPayload.STREAM_CODEC, NetworkHandler::handleCustomEffect);
    }

    public static void sendConfigSync(net.minecraft.server.level.ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, ConfigSyncPayload.fromConfig());
    }

    public static void broadcastMessage(String message) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            Invasion.LOGGER.warn("Skipping broadcast before server is available: {}", message);
            return;
        }
        PacketDistributor.sendToAllPlayers(new BroadcastMessagePayload(message, false));
    }

    public static void sendItemInteraction(Player player, String message, boolean actionBar) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new ItemInteractionPayload(message, actionBar));
        } else if (player != null) {
            player.displayClientMessage(Component.literal(message), actionBar);
        }
    }

    public static void sendCustomEffect(Player player, net.minecraft.core.BlockPos pos, CustomEffectPayload.EffectType effect) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new CustomEffectPayload(pos, effect));
        }
    }

    private static void handleConfigSync(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> Invasion.applyServerConfig(payload.toSnapshot()));
    }

    private static void handleBroadcastMessage(BroadcastMessagePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                player.displayClientMessage(Component.literal(payload.message()), payload.actionBar());
            }
        });
    }

    private static void handleItemInteraction(ItemInteractionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                player.displayClientMessage(Component.literal(payload.message()), payload.actionBar());
            }
        });
    }

    private static void handleCustomEffect(CustomEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) {
                return;
            }
            Level level = player.level();
            RandomSource random = level.getRandom();
            double x = payload.pos().getX() + 0.5D;
            double y = payload.pos().getY() + 1.0D;
            double z = payload.pos().getZ() + 0.5D;

            switch (payload.effect()) {
                case NEXUS_REPAIR -> spawnParticles(level, random, ParticleTypes.HAPPY_VILLAGER, x, y, z, 6);
                case NEXUS_ADJUST -> spawnParticles(level, random, ParticleTypes.END_ROD, x, y, z, 8);
                case NEXUS_MATERIAL_APPLIED -> spawnParticles(level, random, ParticleTypes.ENCHANT, x, y, z, 10);
                case NONE -> {
                }
            }
        });
    }

    private static void spawnParticles(Level level, RandomSource random, net.minecraft.core.particles.ParticleOptions particle, double x, double y, double z, int count) {
        for (int i = 0; i < count; i++) {
            double xo = (random.nextDouble() - 0.5D) * 0.6D;
            double yo = random.nextDouble() * 0.6D;
            double zo = (random.nextDouble() - 0.5D) * 0.6D;
            level.addParticle(particle, x + xo, y + yo, z + zo, 0.0D, 0.02D, 0.0D);
        }
    }
}
