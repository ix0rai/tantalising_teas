package io.ix0rai.tantalisingteas.mixin.render;

import com.mojang.blaze3d.texture.NativeImage;
import io.ix0rai.tantalisingteas.client.TantalisingTeasClient;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.data.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
import net.minecraft.util.HolderSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Map;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Final
    private BakedModelManager bakedModelManager;

    @Inject(method = "joinWorld", at = @At("TAIL"))
    public void joinWorld(ClientWorld world, CallbackInfo ci) {
        if (TantalisingTeasClient.ITEM_COLOURS.isEmpty()) {
            HolderSet.NamedSet<Item> teaIngredients = Registry.ITEM.getOrCreateTag(Util.TEA_INGREDIENTS);
            teaIngredients.forEach(item -> {
                Identifier id = Registry.ITEM.getId(item.value());
                System.out.println(id);

                ModelIdentifier modelId = new ModelIdentifier(id + "#inventory");
                BakedModel model = bakedModelManager.getModel(modelId);

                NativeImage texture = ((SpriteAccessor) model.getParticleSprite()).getImages()[0];
                Map<TeaColour, Integer> colours = TeaColour.getColourOccurrences(texture);
                System.out.println(colours);

                TeaColour.cleanupRareColours(colours);
                System.out.println(colours);
                TeaColour[] mostSaturatedColours = TeaColour.collectMostSaturatedColours(colours);
                System.out.println(Arrays.toString(mostSaturatedColours));

                // save colour
                TeaColour highestPriority = TeaColour.getHighestPriority(mostSaturatedColours);
                System.out.println(highestPriority);
                System.out.println(highestPriority + " " + id);
                TantalisingTeasClient.ITEM_COLOURS.put(id, highestPriority);
            });
        }
    }
}
