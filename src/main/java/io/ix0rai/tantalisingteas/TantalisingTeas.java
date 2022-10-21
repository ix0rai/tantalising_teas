package io.ix0rai.tantalisingteas;

import io.ix0rai.tantalisingteas.blocks.TeaCauldronBehaviour;
import io.ix0rai.tantalisingteas.data.TantalisingNetworking;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import io.ix0rai.tantalisingteas.util.Constants;
import io.ix0rai.tantalisingteas.util.TeaColourUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * main server-side initializer for tantalising teas
 */
public class TantalisingTeas implements ModInitializer {
    public static Identifier id(String id) {
        return new Identifier(Constants.MOD_ID, id);
    }

    @Override
    public void onInitialize() {
        TantalisingBlocks.register();
        TantalisingItems.registerItems();

        // ensure that boiling cauldron behaviour is registered for all tea ingredients
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> TeaCauldronBehaviour.addBehaviour());

        // code to receive the tea colour data packet on the server
        ServerPlayNetworking.registerGlobalReceiver(TantalisingNetworking.COLOUR_DATA_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            // read data and pass to item colour map
            Map<Identifier, TeaColour> colours = buf.readMap(PacketByteBuf::readIdentifier, packet -> packet.readEnumConstant(TeaColour.class));

            server.execute(() -> {
                // add all received mappings to the item colour map
                // also collect new ones for logging
                List<Identifier> receivedColourMappings = new ArrayList<>();

                for (Map.Entry<Identifier, TeaColour> entry : colours.entrySet()) {
                    if (!TeaColourUtil.ITEM_COLOURS.containsKey(entry.getKey())) {
                        receivedColourMappings.add(entry.getKey());
                        TeaColourUtil.ITEM_COLOURS.put(entry.getKey(), entry.getValue());
                    }
                }

                // log
                if (!receivedColourMappings.isEmpty()) {
                    Constants.LOGGER.info("received new tea colour mappings for items: " + receivedColourMappings);
                }
            });
        });
    }
}
