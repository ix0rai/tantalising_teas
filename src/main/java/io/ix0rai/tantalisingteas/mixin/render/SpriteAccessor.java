package io.ix0rai.tantalisingteas.mixin.render;

import com.mojang.blaze3d.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sprite.class)
public interface SpriteAccessor {
    @Accessor("images")
    NativeImage[] getImages();
}
