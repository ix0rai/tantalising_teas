package io.ix0rai.tantalisingteas.mixin;

import io.ix0rai.tantalisingteas.blocks.TeaCauldron;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(AbstractCauldronBlock.class)
public class AbstractCauldronBlockMixin {
    @Final
    @Shadow
    private Map<Item, CauldronBehavior> behaviorMap;

    @Inject(method = "onUse", at = @At("HEAD"))
    public void onUse(BlockState cauldronState, World initialWorld, BlockPos cauldronPos, PlayerEntity playerEntity, Hand playerHand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!TeaCauldron.registeredRecipes) {
            TeaCauldron.addBehaviour();
        }

        //add more things to map
        if (!this.behaviorMap.containsKey(TantalisingItems.TEA_BOTTLE)) {
            Identifier id = Registry.BLOCK.getId(cauldronState.getBlock());
            if (id.getPath().equals("water_cauldron")) {
                this.behaviorMap.put(TantalisingItems.TEA_LEAVES, TeaCauldron::convertToTeaCauldron);
                this.behaviorMap.put(TantalisingItems.TEA_BOTTLE, TeaCauldron::convertToTeaCauldron);
            } else if (id.getPath().equals("cauldron")) {
                this.behaviorMap.put(TantalisingItems.TEA_BOTTLE, ((state, world, pos, player, hand, stack) -> TeaCauldron.createTeaCauldron(world, pos, player, hand, stack)));
            }
        }
    }
}
