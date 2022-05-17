package io.ix0rai.tantalisingteas;

import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Tantalisingteas implements ModInitializer {
    public static final String MOD_ID = "tantalisingteas";

    public static Identifier id(String id) {
        return new Identifier(MOD_ID, id);
    }

    @Override
    public void onInitialize() {
        TantalisingBlocks.register();
        TantalisingItems.registerItems();
    }
}
