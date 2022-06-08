package io.ix0rai.tantalisingteas.mixin.render;

import io.ix0rai.tantalisingteas.Tantalisingteas;
import io.ix0rai.tantalisingteas.items.rendering.TeaColour;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    public void create(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int i, CallbackInfo ci) {
        for (TeaColour colour : TeaColour.values()) {
            this.addModel(new ModelIdentifier(Tantalisingteas.id(colour.getId() + "_tea_model"), "inventory"));
        }
    }

    @Shadow
    private void addModel(ModelIdentifier id) {
        throw new UnsupportedOperationException("shadowed method");
    }
}
