package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SaveSpawnerC2SPacket(BlockPos pos, SpawnerProperties properties) implements CustomPacketPayload {
    public static final Type<SaveSpawnerC2SPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("cobblemontrialsedition", "save_spawner"));

    // Serialization codecs using Codecs or custom ByteBuf helpers
    public static final StreamCodec<RegistryFriendlyByteBuf, SaveSpawnerC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeBlockPos(packet.pos());
                // Write SpawnerProperties via NBT or custom codec serialization
            },
            buf -> {
                BlockPos pos = buf.readBlockPos();
                // Read SpawnerProperties
                return new SaveSpawnerC2SPacket(pos, null /* Read SpawnerProperties */);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(BlockPos pos, SpawnerProperties properties) {
        // Implement client packet dispatching call here
        // e.g., ClientPlayNetworking.send(new SaveSpawnerC2SPacket(pos, properties));
    }
}
