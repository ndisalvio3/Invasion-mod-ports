package com.whammich.invasion.network.payload;

import com.whammich.invasion.Reference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ItemInteractionPayload(String message, boolean actionBar) implements CustomPacketPayload {
    public static final Type<ItemInteractionPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Reference.MODID, "item_interaction")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemInteractionPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeUtf(payload.message);
            buf.writeBoolean(payload.actionBar);
        },
        buf -> new ItemInteractionPayload(buf.readUtf(), buf.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
