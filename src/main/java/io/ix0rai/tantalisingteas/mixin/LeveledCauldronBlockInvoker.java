package io.ix0rai.tantalisingteas.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LeveledCauldronBlock.class)
public interface LeveledCauldronBlockInvoker {
    @Invoker
    void invokeOnFireCollision(BlockState state, World world, BlockPos pos);
}
