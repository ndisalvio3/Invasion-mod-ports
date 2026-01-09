package com.whammich.invasion.network.payload;

import com.whammich.invasion.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EntitySpawnDataPayload(int entityId, CompoundTag data) implements CustomPacketPayload {
    public static final Type<EntitySpawnDataPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Reference.MODID, "entity_spawn_data")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EntitySpawnDataPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeVarInt(payload.entityId);
            buf.writeNbt(payload.data);
        },
        buf -> {
            int entityId = buf.readVarInt();
            CompoundTag tag = buf.readNbt();
            return new EntitySpawnDataPayload(entityId, tag == null ? new CompoundTag() : tag);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
