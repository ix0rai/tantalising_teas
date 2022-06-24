package io.ix0rai.tantalisingteas.mixin;

import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockWithEntity.class)
public interface BlockWithEntityInvoker {
    @Invoker("checkType")
    static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> invokeCheckType(BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        throw new UnsupportedOperationException();
    }
}
