package com.lemenok.cobblemontrialsedition.neoforge.events;

import com.lemenok.cobblemontrialsedition.client.ClientScreenHelper;
import com.lemenok.cobblemontrialsedition.neoforge.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.screen.OpenSpawnerConfigS2CPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = CobblemonTrialsEdition.MODID)
public class ModPayloadHandlersEvents {
    @SubscribeEvent
    public static void registerNetwork(final net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        event.registrar(CobblemonTrialsEdition.MODID)
                .playToClient(
                        OpenSpawnerConfigS2CPacket.TYPE,
                        OpenSpawnerConfigS2CPacket.STREAM_CODEC,
                        (payload, context) -> {
                            context.enqueueWork(() -> {
                                ClientScreenHelper.openTrialSpawnerScreen(payload.pos(), payload.properties());
                            });
                        }
                );
    }
}
