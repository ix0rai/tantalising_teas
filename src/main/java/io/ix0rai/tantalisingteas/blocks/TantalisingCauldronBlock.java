package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.data.TeaColour;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

/**
 * combines {@link LeveledCauldronBlock} with {@link BlockWithEntity}
 */
@SuppressWarnings("deprecation")
public abstract class TantalisingCauldronBlock extends LeveledCauldronBlock implements BlockEntityProvider {
    protected static final EnumProperty<TeaColour> COLOUR = EnumProperty.of("colour", TeaColour.class);
    protected static final IntProperty STRENGTH = IntProperty.of("strength", 0, 3);

    protected TantalisingCauldronBlock(Settings settings, Predicate<Biome.Precipitation> precipitationPredicate, Map<Item, CauldronBehavior> behaviour) {
        super(settings, precipitationPredicate, behaviour);
        this.setDefaultState(this.getDefaultState().with(LEVEL, 0).with(COLOUR, TeaColour.WHITE).with(STRENGTH, 0));
    }

    @Override
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        return blockEntity.onSyncedBlockEvent(type, data);
    }

    @Override
    @Nullable
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity instanceof NamedScreenHandlerFactory screenHandlerFactory ? screenHandlerFactory : null;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    protected static void useCauldronWith(PlayerEntity player, ItemStack stack) {
        player.incrementStat(Stats.USE_CAULDRON);
        player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
    }


    protected static boolean isStateFull(BlockState state) {
        try {
            return (state.getBlock() instanceof TantalisingCauldronBlock || state.getBlock() instanceof LeveledCauldronBlock) && state.get(LEVEL) >= 3;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    protected static boolean isStateEmpty(BlockState state) {
        try {
            return (state.getBlock() instanceof TantalisingCauldronBlock || state.getBlock() instanceof LeveledCauldronBlock) && state.get(LEVEL) <= 0;
        } catch (IllegalArgumentException ignored) {
            return true;
        }
    }
}
