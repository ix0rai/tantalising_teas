package io.ix0rai.tantalisingteas.client;

import io.ix0rai.tantalisingteas.blocks.StillCauldronBlockEntity;
import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TantalisingTeasClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(
                TantalisingItems.TEA_BOTTLE, new Identifier("id"),
                (stack, world, entity, seed) -> {
                    NbtCompound nbt = stack.getNbt();
                    TeaColour colour = TeaColour.getFromIngredients(NbtUtil.getIngredients(nbt));
                    return colour.getNumericalId();
                }
        );

        ModelPredicateProviderRegistry.register(
                TantalisingItems.TEA_BOTTLE, new Identifier("strength"),
                (stack, world, entity, seed) -> {
                    NbtCompound nbt = stack.getNbt();
                    return NbtUtil.getOverallStrength(NbtUtil.getIngredients(nbt));
                }
        );

        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> {
            long hex = 0xff000000;

            if (view != null) {
                //todo: reliably get the block entity - it's not always present and we need to find a way to fix that
                var entity = view.getBlockEntity(pos, TantalisingBlocks.STILL_CAULDRON_ENTITY);
                if (entity.isPresent()) {
                    StillCauldronBlockEntity blockEntity = entity.get();
                    String hexString = TeaColour.getFromIngredients(blockEntity.getIngredients()).getHex(NbtUtil.getOverallStrength(blockEntity.getIngredients()));
                    hex = Long.parseLong(hexString, 16);
                }
            }

            return (int) hex;
        }, TantalisingBlocks.STILL_CAULDRON);
    }
}
