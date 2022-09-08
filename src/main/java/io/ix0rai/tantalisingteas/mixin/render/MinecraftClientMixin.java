package io.ix0rai.tantalisingteas.mixin.render;

import com.mojang.blaze3d.texture.NativeImage;
import io.ix0rai.tantalisingteas.client.TantalisingTeasClient;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.data.TeaColourUtil;
import io.ix0rai.tantalisingteas.data.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * builds a map of tea ingredient items and their associated tea colours on world join
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Final
    private BakedModelManager bakedModelManager;

    @Inject(method = "joinWorld", at = @At("TAIL"))
    public void joinWorld(ClientWorld world, CallbackInfo ci) {
        // cache the colours of each texture in the tea ingredient tag
        if (TantalisingTeasClient.ITEM_COLOURS.isEmpty()) {
            Registry.ITEM.getOrCreateTag(Util.TEA_INGREDIENTS).forEach(item -> {
                // get the model
                Identifier id = Registry.ITEM.getId(item.value());
                ModelIdentifier modelId = new ModelIdentifier(id + "#inventory");
                BakedModel model = bakedModelManager.getModel(modelId);

                // get the texture and extract the amount of times each colour appears
                NativeImage texture = ((SpriteAccessor) model.getParticleSprite()).getImages()[0];
                Map<TeaColour, Integer> colours = TeaColourUtil.getColourOccurrences(texture);

                // trim the list of colours to the top 3 most saturated
                TeaColourUtil.cleanupRareColours(colours);
                TeaColour[] mostSaturatedColours = TeaColourUtil.collectMostSaturatedColours(colours);

                // pick the colour with the highest priority and then save it to the cache
                TeaColour highestPriority = TeaColourUtil.getHighestPriority(mostSaturatedColours);
                TantalisingTeasClient.ITEM_COLOURS.put(id, highestPriority);
            });
        }
    }
}
