package io.ix0rai.tantalisingteas.mixin.render;

import com.mojang.blaze3d.texture.NativeImage;
import net.minecraft.unmapped.C_yxnjfniw;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C_yxnjfniw.class)
public interface ThingAccessor {
    @Accessor("f_paosgjng")
    NativeImage getImage();
}
