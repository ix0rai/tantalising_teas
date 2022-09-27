package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.mixin.BlockWithEntityInvoker;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.util.NbtUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;

import java.util.function.Predicate;

/**
 * combines {@link LeveledCauldronBlock} with {@link BlockWithEntity} and adds some properties and behaviour on top
 */
@SuppressWarnings("deprecation")
public class TeaCauldron extends LeveledCauldronBlock implements BlockEntityProvider {
    public static final EnumProperty<TeaColour> COLOUR = EnumProperty.of("colour", TeaColour.class);
    public static final IntProperty STRENGTH = IntProperty.of("strength", 0, NbtUtil.MAX_STRENGTH);
    public static final BooleanProperty BOILING = BooleanProperty.of("boiling");

    public TeaCauldron(Settings settings, Predicate<Biome.Precipitation> precipitationPredicate) {
        super(settings, precipitationPredicate, TeaCauldronBehaviour.BEHAVIOUR);

        this.setDefaultState(this.getStateManager().getDefaultState()
                .with(LEVEL, 1)
                .with(COLOUR, TeaColour.BLUE)
                .with(STRENGTH, 0)
                .with(BOILING, false)
        );
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos fromPos) {
        // if fire is placed beneath an existing (still) cauldron, set it to boiling
        if (direction == Direction.DOWN && !state.get(BOILING) && world.getBlockState(fromPos).isIn(BlockTags.FIRE)) {
            return state.with(BOILING, true);
        } else {
            return state;
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(Blocks.CAULDRON);
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
        // copied from {@link BlockWithEntity#onSyncedBlockEvent(BlockState, World, BlockPos, int, int)}
        super.onSyncedBlockEvent(state, world, pos, type, data);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.onSyncedBlockEvent(type, data);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    protected static void useCauldronWith(PlayerEntity player, ItemStack stack) {
        player.incrementStat(Stats.USE_CAULDRON);
        player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
    }

    protected static boolean isNotFull(BlockState state) {
        try {
            return (!(state.getBlock() instanceof LeveledCauldronBlock)) || state.get(LEVEL) < 3;
        } catch (IllegalArgumentException ignored) {
            return true;
        }
    }

    protected static boolean isEmpty(BlockState state) {
        try {
            return (state.getBlock() instanceof LeveledCauldronBlock) && state.get(LEVEL) <= 0;
        } catch (IllegalArgumentException ignored) {
            return true;
        }
    }
}
