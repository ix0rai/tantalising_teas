package io.ix0rai.tantalisingteas.mixin;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractCauldronBlock.class)
public interface AbstractCauldronBlockAccessor {
    @Accessor("RAYCAST_SHAPE")
    static VoxelShape getRaycastShape() {
        throw new UnsupportedOperationException();
    }

    @Accessor("OUTLINE_SHAPE")
    static VoxelShape getOutlineShape() {
        throw new UnsupportedOperationException();
    }

    @Invoker
    boolean invokeIsEntityTouchingFluid(BlockState state, BlockPos pos, Entity entity);
}
