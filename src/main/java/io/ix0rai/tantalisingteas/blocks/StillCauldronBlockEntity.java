package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class StillCauldronBlockEntity extends BoilingCauldronBlockEntity {
    public StillCauldronBlockEntity(BlockPos pos, BlockState blockState) {
        super(TantalisingBlocks.STILL_CAULDRON_ENTITY, pos, blockState);
    }
}
