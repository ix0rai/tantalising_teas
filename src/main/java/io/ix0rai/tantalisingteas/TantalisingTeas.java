package io.ix0rai.tantalisingteas;

import io.ix0rai.tantalisingteas.blocks.TeaCauldronBehaviour;
import io.ix0rai.tantalisingteas.data.TantalisingNetworking;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.data.TeaColourUtil;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * main server-side initializer for tantalising teas
 */
public class TantalisingTeas implements ModInitializer {
    public static final String MOD_ID = "tantalising_teas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }

    public static Text translatableText(String key) {
        if (!key.contains(".")) {
            throw new UnsupportedOperationException("translatable text must be in the format 'category.key'");
        }
        return Text.of(MOD_ID + "." + key);
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
            server.execute(() -> TeaColourUtil.ITEM_COLOURS.putAll(colours));

            // log
            LOGGER.info("received tea colour data packet from client with " + colours.size() + " entries");
        });
    }
}
