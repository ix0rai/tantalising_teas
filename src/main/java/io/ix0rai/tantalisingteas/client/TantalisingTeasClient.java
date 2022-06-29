package io.ix0rai.tantalisingteas.client;

import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TantalisingTeasClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(
                TantalisingItems.TEA_BOTTLE, new Identifier("id"),
                (stack, world, entity, seed) -> TeaColour.getFromIngredients(NbtUtil.getIngredients(stack.getNbt())).getNumericalId()
        );
    }
}
