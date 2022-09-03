package io.ix0rai.tantalisingteas;

import io.ix0rai.tantalisingteas.blocks.TeaCauldronBehaviour;
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
            throw new UnsupportedOperationException("translatable text must be in the format 'category.key'");
        }
        return Text.of(MOD_ID + "." + key);
    }

    @Override
    public void onInitialize() {
        TantalisingBlocks.register();
        TantalisingItems.registerItems();

        // ensure that boiling cauldron behaviour is registered for all tea ingredients
        // we call this on server start because that is when tags are finished being loaded
        ServerLifecycleEvents.SERVER_STARTED.register(server -> TeaCauldronBehaviour.addBehaviour());
    }
}
