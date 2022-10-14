package io.ix0rai.tantalisingteas.client;

import io.ix0rai.tantalisingteas.blocks.TeaCauldronBlockEntityRenderer;
import io.ix0rai.tantalisingteas.data.TantalisingNetworking;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import io.ix0rai.tantalisingteas.util.NbtUtil;
import io.ix0rai.tantalisingteas.util.TeaColourUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.BiomeKeys;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * main initializer for tantalising teas on the client
 */
@Environment(EnvType.CLIENT)
public class TantalisingTeasClient implements ClientModInitializer {
    private boolean dataReady = false;
    private boolean canSend = false;

    public static final List<Pair<BlockPos, ItemStack>> stacksToRender = new CopyOnWriteArrayList<>();

    @Override
    public void onInitializeClient() {
        // tea bottle model providers
        ModelPredicateProviderRegistry.register(
                TantalisingItems.TEA_BOTTLE, new Identifier("id"),
                (stack, world, entity, seed) -> {
                    // note: updating this on every render is not ideal
                    // in the future this should be running less often
                    ClientTeaColourUtil.updateCustomName(stack);
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

        // tea cauldron translucency
        BlockRenderLayerMap.INSTANCE.putBlock(TantalisingBlocks.TEA_CAULDRON, RenderLayer.getTranslucent());

        // colour providers are only used when there are no ingredients present
        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> getWaterColour(view, pos), TantalisingBlocks.TEA_CAULDRON);

        // tea colour packet sending
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

        ClientPlayNetworking.registerGlobalReceiver(TantalisingNetworking.INGREDIENT_ADDITION_ANIMATION_CUE_ID, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            ItemStack stack = buf.readItemStack();
            stacksToRender.add(new Pair<>(pos, stack));
        });

        // tea cauldron block entity renderer
        BlockEntityRendererRegistry.register(TantalisingBlocks.TEA_CAULDRON_ENTITY, context -> new TeaCauldronBlockEntityRenderer());
    }

    private static int getWaterColour(BlockRenderView view, BlockPos pos) {
        if (view != null) {
            return BiomeColors.getWaterColor(view, pos);
        } else {
            return Objects.requireNonNull(BuiltinRegistries.BIOME.get(BiomeKeys.OCEAN)).getWaterColor();
        }
    }
}
