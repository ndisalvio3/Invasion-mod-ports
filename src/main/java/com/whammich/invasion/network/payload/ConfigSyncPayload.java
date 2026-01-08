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
    boolean mobsDropSmallRemnants,
    boolean craftItemsEnabled,
    boolean debugMode,
    int guiIdNexus,
    int minContinuousModeDays,
    int maxContinuousModeDays,
    boolean nightSpawnsEnabled,
    int nightMobSpawnChance,
    int nightMobMaxGroupSize,
    int mobLimitOverride,
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
            buf.writeBoolean(payload.mobsDropSmallRemnants);
            buf.writeBoolean(payload.craftItemsEnabled);
            buf.writeBoolean(payload.debugMode);
            buf.writeVarInt(payload.guiIdNexus);
            buf.writeVarInt(payload.minContinuousModeDays);
            buf.writeVarInt(payload.maxContinuousModeDays);
            buf.writeBoolean(payload.nightSpawnsEnabled);
            buf.writeVarInt(payload.nightMobSpawnChance);
            buf.writeVarInt(payload.nightMobMaxGroupSize);
            buf.writeVarInt(payload.mobLimitOverride);
            buf.writeBoolean(payload.nightMobsBurnInDay);
            buf.writeVarInt(payload.nightMobSightRange);
            buf.writeVarInt(payload.nightMobSenseRange);
        },
        buf -> new ConfigSyncPayload(
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readBoolean(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
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
            InvasionConfig.COMMON.mobsDropSmallRemnants.get(),
            InvasionConfig.COMMON.craftItemsEnabled.get(),
            InvasionConfig.COMMON.debugMode.get(),
            InvasionConfig.COMMON.guiIdNexus.get(),
            InvasionConfig.COMMON.minContinuousModeDays.get(),
            InvasionConfig.COMMON.maxContinuousModeDays.get(),
            InvasionConfig.COMMON.nightSpawnsEnabled.get(),
            InvasionConfig.COMMON.nightMobSpawnChance.get(),
            InvasionConfig.COMMON.nightMobMaxGroupSize.get(),
            InvasionConfig.COMMON.mobLimitOverride.get(),
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
            mobsDropSmallRemnants,
            craftItemsEnabled,
            debugMode,
            guiIdNexus,
            minContinuousModeDays,
            maxContinuousModeDays,
            nightSpawnsEnabled,
            nightMobSpawnChance,
            nightMobMaxGroupSize,
            mobLimitOverride,
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
