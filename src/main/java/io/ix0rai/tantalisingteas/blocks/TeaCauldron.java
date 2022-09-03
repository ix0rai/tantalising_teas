package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.mixin.BlockWithEntityInvoker;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * combines {@link LeveledCauldronBlock} with {@link BlockWithEntity} and adds some properties and behaviour on top
 */
@SuppressWarnings("deprecation")
public class TeaCauldron extends LeveledCauldronBlock implements BlockEntityProvider {
    static final EnumProperty<TeaColour> COLOUR = EnumProperty.of("colour", TeaColour.class);
    static final IntProperty STRENGTH = IntProperty.of("strength", 0, NbtUtil.MAX_STRENGTH);
    static final BooleanProperty BOILING = BooleanProperty.of("boiling");

    public TeaCauldron(Settings settings, Predicate<Biome.Precipitation> precipitationPredicate) {
        super(settings, precipitationPredicate, TeaCauldronBehaviour.BEHAVIOUR);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        // note: this does not work with varargs, individual calls are required
        builder.add(LEVEL).add(COLOUR).add(STRENGTH).add(TeaCauldron.BOILING);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TeaCauldronBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockWithEntityInvoker.invokeCheckType(type, TantalisingBlocks.TEA_CAULDRON_ENTITY, (w, pos, blockState, boilingCauldron) -> TeaCauldronBlockEntity.tick(boilingCauldron));
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
            return (state.getBlock() instanceof LeveledCauldronBlock) && state.get(LEVEL) >= 3;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    protected static boolean isStateEmpty(BlockState state) {
        try {
            return (state.getBlock() instanceof LeveledCauldronBlock) && state.get(LEVEL) <= 0;
        } catch (IllegalArgumentException ignored) {
            return true;
        }
    }
}
