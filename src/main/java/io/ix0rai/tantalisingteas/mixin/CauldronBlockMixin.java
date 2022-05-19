package io.ix0rai.tantalisingteas.mixin;

import net.minecraft.block.CauldronBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CauldronBlock.class)
public interface CauldronBlockMixin {
    @Invoker
    static boolean invokeCanFillWithPrecipitation(World world, Biome.Precipitation precipitation) {
        throw new UnsupportedOperationException();
    }
}