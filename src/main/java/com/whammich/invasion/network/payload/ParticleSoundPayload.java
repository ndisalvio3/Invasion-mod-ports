package com.whammich.invasion.network.payload;

import com.whammich.invasion.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public record ParticleSoundPayload(
    BlockPos pos,
    ResourceLocation particleId,
    int count,
    boolean playSound,
    ResourceLocation soundId,
    SoundSource soundSource,
    float volume,
    float pitch
) implements CustomPacketPayload {
    public static final Type<ParticleSoundPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Reference.MODID, "particle_sound")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleSoundPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeBlockPos(payload.pos);
            buf.writeResourceLocation(payload.particleId);
            buf.writeVarInt(payload.count);
            buf.writeBoolean(payload.playSound);
            if (payload.playSound) {
                buf.writeResourceLocation(payload.soundId);
                buf.writeEnum(payload.soundSource);
                buf.writeFloat(payload.volume);
                buf.writeFloat(payload.pitch);
            }
        },
        buf -> {
            BlockPos pos = buf.readBlockPos();
            ResourceLocation particleId = buf.readResourceLocation();
            int count = buf.readVarInt();
            boolean playSound = buf.readBoolean();
            if (playSound) {
                ResourceLocation soundId = buf.readResourceLocation();
                SoundSource soundSource = buf.readEnum(SoundSource.class);
                float volume = buf.readFloat();
                float pitch = buf.readFloat();
                return new ParticleSoundPayload(pos, particleId, count, true, soundId, soundSource, volume, pitch);
            }
            return new ParticleSoundPayload(pos, particleId, count, false, particleId, SoundSource.AMBIENT, 0.0F, 0.0F);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
