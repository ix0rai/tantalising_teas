package io.ix0rai.tantalisingteas.mixin.render;

import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    // todo: this code is probably all invalidated now
    // needsUpdate can probably be ripped out of the code entirely

    @Inject(method = "getHeldItemModel", at = @At("HEAD"))
    public void getHeldItemModel(ItemStack stack, World world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (stack.isOf(TantalisingItems.TEA_BOTTLE) && stack.getNbt() != null && NbtUtil.needsUpdate(stack.getNbt())) {
            NbtUtil.updateCustomName(stack);
            NbtUtil.setNeedsUpdate(stack.getNbt(), false);
            stack.setNbt(stack.getNbt());
        }
    }
}
