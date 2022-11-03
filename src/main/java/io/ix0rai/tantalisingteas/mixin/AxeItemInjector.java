package io.ix0rai.tantalisingteas.mixin;

import io.ix0rai.tantalisingteas.blocks.cinnamon.CinnamonLog;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * this mixin enables stripping of cinnamon logs with axes
 * the reason that we can't use the fabric stripping registry/vanilla version is because we need to preserve state, and it's impossible to get things like age and leaves from the original block without using mixins
 */
@Mixin(AxeItem.class)
public class AxeItemInjector {
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState targetState = world.getBlockState(pos);

        if (targetState.isOf(TantalisingBlocks.CINNAMON_LOG)) {
            world.m_ktoxvfib(context.getPlayer(), pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);

            ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(TantalisingItems.cinnamonSeeds, world.random.nextInt(2) + 1));
            world.spawnEntity(itemEntity);

            world.setBlockState(pos, TantalisingBlocks.STRIPPED_CINNAMON_LOG.getDefaultState()
                    .with(CinnamonLog.STAGE, targetState.get(CinnamonLog.STAGE))
                    .with(CinnamonLog.AGE, targetState.get(CinnamonLog.AGE))
                    .with(CinnamonLog.LEAVES, targetState.get(CinnamonLog.LEAVES))
            );
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}