package com.whammich.invasion.network.payload;

import com.whammich.invasion.Reference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record NexusStatusPayload(
    int activationTimer,
    int mode,
    int currentWave,
    int nexusLevel,
    int nexusKills,
    int spawnRadius,
    int generation,
    int powerLevel,
    int cookTime
) implements CustomPacketPayload {
    public static final Type<NexusStatusPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Reference.MODID, "nexus_status")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, NexusStatusPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.activationTimer);
            buf.writeVarInt(payload.mode);
            buf.writeVarInt(payload.currentWave);
            buf.writeVarInt(payload.nexusLevel);
            buf.writeVarInt(payload.nexusKills);
            buf.writeVarInt(payload.spawnRadius);
            buf.writeVarInt(payload.generation);
            buf.writeVarInt(payload.powerLevel);
            buf.writeVarInt(payload.cookTime);
        },
        buf -> new NexusStatusPayload(
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt()
        )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
