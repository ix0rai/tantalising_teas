package io.ix0rai.tantalisingteas.blocks.cinnamon;

import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.WorldView;

public class StrippedCinnamonLog extends CinnamonLog {
    public StrippedCinnamonLog(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, RandomGenerator random, BlockPos pos, BlockState state) {
        world.setBlockState(pos, copyStateToCinnamonLog(state));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
        if (random.nextInt(3) == 0) {
            world.setBlockState(pos, copyStateToCinnamonLog(state), 2);
        }
    }

    private BlockState copyStateToCinnamonLog(BlockState state) {
        // create a cinnamon log block state with identical properties
        return TantalisingBlocks.CINNAMON_LOG.getDefaultState().with(STAGE, state.get(STAGE)).with(AGE, state.get(AGE)).with(LEAVES, state.get(LEAVES));
    }
}
