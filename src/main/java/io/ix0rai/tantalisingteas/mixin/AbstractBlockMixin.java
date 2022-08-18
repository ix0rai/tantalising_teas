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
    /**
     * @reason create boiling cauldrons
     */
    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    public void getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        // when a water cauldron receives a neighbor update and the neighbor is a fire block, as well as directly below the cauldron, it will begin boiling
        if (isValidCauldron(state) && neighborPos.equals(pos.down()) && neighborState.isOf(Blocks.FIRE)) {
            cir.setReturnValue(TantalisingBlocks.BOILING_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, state.get(LeveledCauldronBlock.LEVEL)));
        }
    }

    /**
     * @reason create boiling cauldrons
     */
    @Inject(method = "neighborUpdate", at = @At("HEAD"))
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify, CallbackInfo ci) {
        // when a fire block receives a neighbor update and the neighbor is water cauldron, as well as directly above the fire, the cauldron will begin boiling
        // when combined with the other injection, this should mean that a cauldron will *always* begin boiling if a fire block is below it
        if (fromPos.equals(pos.up())) {
            BlockState neighborState = world.getBlockState(fromPos);
            if (isValidCauldron(neighborState) && world.getBlockState(pos).isIn(BlockTags.FIRE)) {
                world.setBlockState(fromPos, TantalisingBlocks.BOILING_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, neighborState.get(LeveledCauldronBlock.LEVEL)), Block.NOTIFY_ALL);
            }
        }
    }

    private static boolean isValidCauldron(BlockState state) {
        return state.isOf(Blocks.WATER_CAULDRON) || state.isOf(TantalisingBlocks.STILL_CAULDRON);
    }
}
