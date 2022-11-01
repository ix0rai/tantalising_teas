package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BambooSaplingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CinnamonSapling extends BambooSaplingBlock {
    // todo texture
    // todo model
    public CinnamonSapling(Settings settings) {
        super(settings);
    }

    @Override
    protected void grow(World world, BlockPos pos) {
        world.setBlockState(pos.up(), TantalisingBlocks.CINNAMON_LOG.getDefaultState().with(BambooBlock.LEAVES, BambooLeaves.SMALL), 3);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        } else {
            if (direction == Direction.UP && neighborState.isOf(TantalisingBlocks.CINNAMON_LOG)) {
                world.setBlockState(pos, TantalisingBlocks.CINNAMON_LOG.getDefaultState(), 2);
            }

            return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
    }
}
