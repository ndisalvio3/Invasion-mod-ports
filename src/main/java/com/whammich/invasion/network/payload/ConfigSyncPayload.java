package com.whammich.invasion.network.payload;

import com.whammich.invasion.Reference;
import com.whammich.invasion.config.InvasionConfig;
import com.whammich.invasion.config.InvasionConfigSnapshot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConfigSyncPayload(
    boolean enableLogging,
    boolean updateMessagesEnabled,
    boolean destructedBlocksDrop,
    boolean nightMobsBurnInDay,
    int nightMobSightRange,
    int nightMobSenseRange
) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MODID, "config_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeBoolean(payload.enableLogging);
            buf.writeBoolean(payload.updateMessagesEnabled);
            buf.writeBoolean(payload.destructedBlocksDrop);
            buf.writeBoolean(payload.nightMobsBurnInDay);
            buf.writeVarInt(payload.nightMobSightRange);
            buf.writeVarInt(payload.nightMobSenseRange);
        },
        buf -> new ConfigSyncPayload(
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readVarInt(),
            buf.readVarInt()
        )
    );

    public static ConfigSyncPayload fromConfig() {
        return new ConfigSyncPayload(
            InvasionConfig.COMMON.enableLogging.get(),
            InvasionConfig.COMMON.updateMessagesEnabled.get(),
            InvasionConfig.COMMON.destructedBlocksDrop.get(),
            InvasionConfig.COMMON.nightMobsBurnInDay.get(),
            InvasionConfig.COMMON.nightMobSightRange.get(),
            InvasionConfig.COMMON.nightMobSenseRange.get()
        );
    }

    public InvasionConfigSnapshot toSnapshot() {
        return new InvasionConfigSnapshot(
            enableLogging,
            updateMessagesEnabled,
            destructedBlocksDrop,
            nightMobsBurnInDay,
            nightMobSightRange,
            nightMobSenseRange
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
