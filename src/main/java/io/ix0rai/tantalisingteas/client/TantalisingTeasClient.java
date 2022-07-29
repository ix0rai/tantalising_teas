package io.ix0rai.tantalisingteas.client;

import io.ix0rai.tantalisingteas.blocks.BoilingCauldronBlockEntity;
import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

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

        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> getHexFor(view, pos, TantalisingBlocks.STILL_CAULDRON_ENTITY), TantalisingBlocks.STILL_CAULDRON);
        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> getHexFor(view, pos, TantalisingBlocks.BOILING_CAULDRON_ENTITY), TantalisingBlocks.BOILING_CAULDRON);
    }

    private int getHexFor(BlockView view, BlockPos pos, BlockEntityType<?> blockEntityType) {
        long hex = 0xff000000;

        if (view != null) {
            var entity = view.getBlockEntity(pos, blockEntityType);
            if (entity.isPresent()) {
                var blockEntity = entity.get();
                NbtList ingredients = ((BoilingCauldronBlockEntity) blockEntity).getIngredients();
                String hexString = TeaColour.getFromIngredients(ingredients).getHex(NbtUtil.getOverallStrength(ingredients));
                hex = Long.parseLong(hexString, 16);
            }
        }

        return (int) hex;
    }
}
