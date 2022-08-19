package io.ix0rai.tantalisingteas.client;

import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class TantalisingTeasClient implements ClientModInitializer {
    public static final Map<Identifier, TeaColour> ITEM_COLOURS = new HashMap<>();

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

        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> getHexFor(view, pos), TantalisingBlocks.STILL_CAULDRON);
        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> getHexFor(view, pos), TantalisingBlocks.BOILING_CAULDRON);
    }

    private int getHexFor(BlockRenderView view, BlockPos pos) {
        long hex = 0xff000000;

        if (view != null) {
            var entity = view.getBlockEntity(pos, TantalisingBlocks.BOILING_CAULDRON_ENTITY);
            if (entity.isPresent()) {
                var blockEntity = entity.get();
                NbtList ingredients = blockEntity.getIngredients();
                if (ingredients.isEmpty()) {
                    return BiomeColors.getWaterColor(view, pos);
                }

                String hexString = TeaColour.getFromIngredients(ingredients).getHex(NbtUtil.getOverallStrength(ingredients));
                hex = Long.parseLong(hexString, 16);
            }
        }

        return (int) hex;
    }
}
