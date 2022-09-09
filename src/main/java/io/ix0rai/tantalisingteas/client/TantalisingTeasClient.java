package io.ix0rai.tantalisingteas.client;

import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TantalisingNetworking;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.data.TeaColourUtil;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.BiomeKeys;

import java.util.Objects;

/**
 * main initializer for tantalising teas on the client
 */
@Environment(EnvType.CLIENT)
public class TantalisingTeasClient implements ClientModInitializer {
    private boolean dataReady = false;
    private boolean canSend = false;

    @Override
    public void onInitializeClient() {
        ModelPredicateProviderRegistry.register(
                TantalisingItems.TEA_BOTTLE, new Identifier("id"),
                (stack, world, entity, seed) -> {
                    NbtCompound nbt = stack.getNbt();
                    TeaColour colour = TeaColourUtil.getFromIngredients(NbtUtil.getIngredients(nbt));
                    return colour.ordinal();
                }
        );

        ModelPredicateProviderRegistry.register(
                TantalisingItems.TEA_BOTTLE, new Identifier("strength"),
                (stack, world, entity, seed) -> {
                    NbtCompound nbt = stack.getNbt();
                    return NbtUtil.getOverallStrength(NbtUtil.getIngredients(nbt));
                }
        );

        BlockRenderLayerMap.INSTANCE.putBlock(TantalisingBlocks.TEA_CAULDRON, RenderLayer.getTranslucent());

        // colour providers are only used when there are no ingredients present
        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> getWaterColour(view, pos), TantalisingBlocks.TEA_CAULDRON);


        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            ClientTeaColourUtil.cacheTeaColours();
            dataReady = true;
            if (canSend) {
                TantalisingNetworking.sendColourDataPacket();
            }
        });

        ClientPlayConnectionEvents.JOIN.register((a, b, c) -> {
            if (dataReady) {
                TantalisingNetworking.sendColourDataPacket();
            } else {
                canSend = true;
            }
        });
    }

    private static int getWaterColour(BlockRenderView view, BlockPos pos) {
        if (view != null) {
            return BiomeColors.getWaterColor(view, pos);
        } else {
            return Objects.requireNonNull(BuiltinRegistries.BIOME.get(BiomeKeys.OCEAN)).getWaterColor();
        }
    }
}
