package io.ix0rai.tantalisingteas.mixin.render;

import io.ix0rai.tantalisingteas.Tantalisingteas;
import io.ix0rai.tantalisingteas.items.TeaBottle;
import io.ix0rai.tantalisingteas.items.rendering.TeaColour;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Final
    @Shadow
    private ItemModels models;

    @Inject(method = "getHeldItemModel", at = @At("HEAD"), cancellable = true)
    public void getHeldItemModel(ItemStack stack, World world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (stack.isOf(TantalisingItems.TEA_BOTTLE)) {
            TeaBottle.updateColourValues(stack, this.models.getModelManager());
            List<NbtCompound> ingredients = TeaBottle.getIngredients(stack);
            TeaColour colour = TeaColour.getFromIngredients(ingredients);

            ModelIdentifier id = new ModelIdentifier(Tantalisingteas.id(colour.getId() + "_tea_model"), "inventory");
            BakedModel teaModel = this.models.getModelManager().getModel(id);
            cir.setReturnValue(teaModel);
        }
    }
}
