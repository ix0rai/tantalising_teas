package io.ix0rai.tantalisingteas.mixin;

import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    public void getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (state.isOf(Blocks.WATER_CAULDRON) && neighborPos.equals(pos.down()) && neighborState.isOf(Blocks.FIRE)) {
            cir.setReturnValue(TantalisingBlocks.BOILING_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, state.get(LeveledCauldronBlock.LEVEL)));
        }
    }

    @Inject(method = "neighborUpdate", at = @At("HEAD"))
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify, CallbackInfo ci) {
        BlockState neighborState = world.getBlockState(fromPos);
        if (neighborState.isOf(Blocks.WATER_CAULDRON) && world.getBlockState(pos).isIn(BlockTags.FIRE)) {
            world.setBlockState(fromPos, TantalisingBlocks.BOILING_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, neighborState.get(LeveledCauldronBlock.LEVEL)), Block.NOTIFY_ALL);
        }
    }
}
