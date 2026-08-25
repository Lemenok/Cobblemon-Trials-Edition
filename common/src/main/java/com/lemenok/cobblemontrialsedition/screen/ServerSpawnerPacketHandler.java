package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

public class ServerSpawnerPacketHandler {

    public static void handle(SaveSpawnerC2SPacket packet, ServerPlayer player) {
        // 1. Permission Check
        if (!player.hasPermissions(2)) {
            return;
        }

        BlockPos pos = packet.pos();
        var level = player.serverLevel();

        // 2. Fetch and update the target Block Entity
        if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof CobblemonTrialSpawnerEntity spawnerBE) {

            spawnerBE.applySpawnerProperties(packet.properties());

            // 3. Persist and sync across clients
            spawnerBE.setChanged();
            level.sendBlockUpdated(pos, spawnerBE.getBlockState(), spawnerBE.getBlockState(), Block.UPDATE_ALL);
        }
    }
}
