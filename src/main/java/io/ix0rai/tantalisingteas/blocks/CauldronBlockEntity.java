package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class CauldronBlockEntity extends BlockEntity {
    public CauldronBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TantalisingBlocks.TEA_CAULDRON_ENTITY, blockPos, blockState);
    }
}
