package io.ix0rai.tantalisingteas;

import io.ix0rai.tantalisingteas.blocks.BoilingCauldron;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TantalisingTeas implements ModInitializer {
    public static final String MOD_ID = "tantalising_teas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }

    public static Text translatableText(String key) {
        if (!key.contains(".")) {
            LOGGER.warn("creating translation key with no category");
        }
        return Text.of(MOD_ID + "." + key);
    }

    @Override
    public void onInitialize() {
        TantalisingBlocks.register();
        TantalisingItems.registerItems();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> BoilingCauldron.addBehaviour());
    }
}
