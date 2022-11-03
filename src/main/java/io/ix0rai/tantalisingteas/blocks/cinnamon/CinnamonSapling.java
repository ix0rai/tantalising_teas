package io.ix0rai.tantalisingteas.blocks.cinnamon;

import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.BambooSaplingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class CinnamonSapling extends BambooSaplingBlock {
    // todo texture
    // todo model
    public CinnamonSapling(Settings settings) {
        super(settings);
    }

    @Override
    protected void grow(World world, BlockPos pos) {
        world.setBlockState(pos, TantalisingBlocks.CINNAMON_LOG.getDefaultState().with(CinnamonLog.LEAVES, CinnamonTreeLeaves.random(world.getRandom())), 3);
        world.setBlockState(pos.up(), TantalisingBlocks.CINNAMON_LOG.getDefaultState().with(CinnamonLog.LEAVES, CinnamonTreeLeaves.SMALL), 3);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState checkedState = world.getBlockState(pos.down());
        return checkedState.isIn(BlockTags.DIRT);
    }
}
