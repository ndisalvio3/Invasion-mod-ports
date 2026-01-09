package com.whammich.invasion.network.payload;

import com.whammich.invasion.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CustomEffectPayload(BlockPos pos, EffectType effect) implements CustomPacketPayload {
    public static final Type<CustomEffectPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Reference.MODID, "custom_effect")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CustomEffectPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeBlockPos(payload.pos);
            buf.writeVarInt(payload.effect.id());
        },
        buf -> new CustomEffectPayload(buf.readBlockPos(), EffectType.fromId(buf.readVarInt()))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum EffectType {
        NONE(0),
        NEXUS_REPAIR(1),
        NEXUS_ADJUST(2),
        NEXUS_MATERIAL_APPLIED(3);

        private final int id;

        EffectType(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static EffectType fromId(int id) {
            for (EffectType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return NONE;
        }
    }
}
