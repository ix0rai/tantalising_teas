package io.ix0rai.tantalisingteas.mixin;

import io.ix0rai.tantalisingteas.blocks.TeaCauldron;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * converts water cauldrons into boiling tea cauldrons when fire is placed below them
 * <p>we have to inject into {@link AbstractBlock} because {@link net.minecraft.block.LeveledCauldronBlock} does not override getStateForNeighborUpdate
 */
@Mixin(AbstractBlock.class)
public class LeveledCauldronBlockInjector {
    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    public void getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos fromPos, CallbackInfoReturnable<BlockState> cir) {
        if (state.isOf(Blocks.WATER_CAULDRON) && direction == Direction.DOWN && world.getBlockState(fromPos).isIn(BlockTags.FIRE)) {
            cir.setReturnValue(TantalisingBlocks.TEA_CAULDRON.getDefaultState()
                    .with(TeaCauldron.BOILING, true)
                    .with(TeaCauldron.LEVEL, state.get(TeaCauldron.LEVEL))
                    .with(TeaCauldron.STRENGTH, 0)
                    .with(TeaCauldron.COLOUR, TeaColour.BLUE)
            );
        }
    }
}
