package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CinnamonLog extends BambooBlock {
    public static final int MAX_HEIGHT = 16;
    // todo stripping for 1 - 2 cinnamon
    // todo leaves staggered all across the tree
    // todo improve logic

    public CinnamonLog(Settings settings) {
        super(settings);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return state.isIn(BlockTags.DIRT) || state.isOf(TantalisingBlocks.CINNAMON_LOG);
    }

    @Override
    protected void updateLeaves(BlockState state, World world, BlockPos pos, RandomGenerator random, int height) {
        BlockState blockState = world.getBlockState(pos.down());
        BlockPos blockPos = pos.down(2);
        BlockState blockState2 = world.getBlockState(blockPos);
        BambooLeaves bambooLeaves = BambooLeaves.NONE;
        if (height >= 1) {
            if (blockState.isOf(TantalisingBlocks.CINNAMON_LOG) && blockState.get(LEAVES) != BambooLeaves.NONE) {
                if (blockState.isOf(TantalisingBlocks.CINNAMON_LOG) && blockState.get(LEAVES) != BambooLeaves.NONE) {
                    bambooLeaves = BambooLeaves.LARGE;
                    if (blockState2.isOf(TantalisingBlocks.CINNAMON_LOG)) {
                        world.setBlockState(pos.down(), blockState.with(LEAVES, BambooLeaves.SMALL), 3);
                        world.setBlockState(blockPos, blockState2.with(LEAVES, BambooLeaves.NONE), 3);
                    }
                }
            } else {
                bambooLeaves = BambooLeaves.SMALL;
            }
        }

        int i = state.get(AGE) != 1 && !blockState2.isOf(TantalisingBlocks.CINNAMON_LOG) ? 0 : 1;
        int j = (height < MAX_HEIGHT / 2 || random.nextFloat() >= 0.25F) && height != MAX_HEIGHT - 1 ? 0 : 1;
        world.setBlockState(pos.up(), this.getDefaultState().with(AGE, i).with(LEAVES, bambooLeaves).with(STAGE, j), 3);
    }

    @Override
    protected int countBambooAbove(BlockView world, BlockPos pos) {
        int i;
        for(i = 0; i < MAX_HEIGHT && world.getBlockState(pos.up(i + 1)).isOf(TantalisingBlocks.CINNAMON_LOG); ++i) {
        }

        return i;
    }

    @Override
    protected int countBambooBelow(BlockView world, BlockPos pos) {
        int i;
        for(i = 0; i < MAX_HEIGHT && world.getBlockState(pos.down(i + 1)).isOf(TantalisingBlocks.CINNAMON_LOG); i ++) {
        }

        return i;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }

        if (direction == Direction.UP && neighborState.isOf(TantalisingBlocks.CINNAMON_LOG) && neighborState.get(AGE) > state.get(AGE)) {
            world.setBlockState(pos, state.cycle(AGE), 2);
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        if (!fluidState.isEmpty()) {
            return null;
        } else {
            BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos().down());
            if (blockState.isOf(TantalisingBlocks.CINNAMON_SAPLING)) {
                return this.getDefaultState().with(AGE, 0);
            } else if (blockState.isOf(TantalisingBlocks.CINNAMON_LOG)) {
                int i = blockState.get(AGE) > 0 ? 1 : 0;
                return this.getDefaultState().with(AGE, i);
            } else {
                BlockState blockState2 = ctx.getWorld().getBlockState(ctx.getBlockPos().up());
                return blockState2.isOf(TantalisingBlocks.CINNAMON_LOG) ? this.getDefaultState().with(AGE, blockState2.get(AGE)) : TantalisingBlocks.CINNAMON_SAPLING.getDefaultState();
            }
        }
    }
}
