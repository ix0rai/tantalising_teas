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
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.List;
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
                TantalisingItems.teaBottle, new Identifier("id"),
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
                TantalisingItems.teaBottle, new Identifier("strength"),
                (stack, world, entity, seed) -> {
                    NbtCompound nbt = stack.getNbt();
                    return NbtUtil.getOverallStrength(NbtUtil.getIngredients(nbt));
                }
        );

        // tea cauldron translucency
        BlockRenderLayerMap.INSTANCE.putBlock(TantalisingBlocks.TEA_CAULDRON, RenderLayer.getTranslucent());

        // cutout textures
        BlockRenderLayerMap.INSTANCE.putBlock(TantalisingBlocks.CINNAMON_LOG, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(TantalisingBlocks.CINNAMON_SAPLING, RenderLayer.getCutout());

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
}
