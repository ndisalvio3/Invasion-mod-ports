package com.whammich.invasion.network;

import com.whammich.invasion.network.payload.BroadcastMessagePayload;
import com.whammich.invasion.network.payload.ConfigSyncPayload;
import invmod.Invasion;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "2";

    private NetworkHandler() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC, NetworkHandler::handleConfigSync);
        registrar.playToClient(BroadcastMessagePayload.TYPE, BroadcastMessagePayload.STREAM_CODEC, NetworkHandler::handleBroadcastMessage);
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
}
