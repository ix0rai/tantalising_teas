package io.ix0rai.tantalisingteas.blocks.cinnamon;

import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

@SuppressWarnings("deprecation")
public class CinnamonLog extends PillarBlock implements Fertilizable {
    protected static final VoxelShape SMALL_LEAVES_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    protected static final VoxelShape LARGE_LEAVES_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    protected static final VoxelShape NO_LEAVES_SHAPE = Block.createCuboidShape(6.5, 0.0, 6.5, 9.5, 16.0, 9.5);
    public static final int MAX_HEIGHT = 16;
    public static final EnumProperty<CinnamonTreeLeaves> LEAVES = EnumProperty.of("leaves", CinnamonTreeLeaves.class);
    public static final IntProperty AGE = Properties.AGE_1;
    public static final IntProperty STAGE = Properties.STAGE;

    // todo stripping for 1 - 2 cinnamon
    // todo leaves staggered all across the tree
    // todo improve logic

    public CinnamonLog(AbstractBlock.Settings settings) {
        // done
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0).with(LEAVES, CinnamonTreeLeaves.NONE).with(STAGE, 0).with(AXIS, Direction.Axis.Y));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        // done
        builder.add(AGE).add(LEAVES).add(STAGE).add(AXIS);
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        // done
        return true;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // todo
        VoxelShape voxelShape = state.get(LEAVES) == CinnamonTreeLeaves.LARGE ? LARGE_LEAVES_SHAPE : SMALL_LEAVES_SHAPE;
        Vec3d vec3d = state.getModelOffset(world, pos);
        return voxelShape.offset(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        // done
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // todo
        Vec3d vec3d = state.getModelOffset(world, pos);
        return NO_LEAVES_SHAPE.offset(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        // done
        return false;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
        // done
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        // todo - stripping
        return state.get(STAGE) == 0;
    }

    @Override
    public boolean canGrow(World world, RandomGenerator random, BlockPos pos, BlockState state) {
        // todo - hmm
        return true;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        // todo - hmm
        BlockState checkedState = world.getBlockState(pos.down());
        return checkedState.isIn(BlockTags.DIRT) || checkedState.isIn(TantalisingBlocks.CINNAMON_LOGS);
    }

    protected void updateLeaves(BlockState state, World world, BlockPos pos, RandomGenerator random, int height) {
        // todo: rewrite this
        BlockState blockState = world.getBlockState(pos.down());
        BlockPos blockPos = pos.down(2);
        BlockState blockState2 = world.getBlockState(blockPos);
        CinnamonTreeLeaves bambooLeaves = CinnamonTreeLeaves.NONE;

        if (height >= 1) {
            if (blockState.isIn(TantalisingBlocks.CINNAMON_LOGS) && blockState.get(LEAVES) != CinnamonTreeLeaves.NONE) {
                if (blockState.isIn(TantalisingBlocks.CINNAMON_LOGS) && blockState.get(LEAVES) != CinnamonTreeLeaves.NONE) {
                    bambooLeaves = CinnamonTreeLeaves.LARGE;
                    if (blockState2.isIn(TantalisingBlocks.CINNAMON_LOGS)) {
                        world.setBlockState(pos.down(), blockState.with(LEAVES, CinnamonTreeLeaves.SMALL), 3);
                        world.setBlockState(blockPos, blockState2.with(LEAVES, CinnamonTreeLeaves.NONE), 3);
                    }
                }
            } else {
                bambooLeaves = CinnamonTreeLeaves.SMALL;
            }
        }

        int i = state.get(AGE) != 1 && !blockState2.isIn(TantalisingBlocks.CINNAMON_LOGS) ? 0 : 1;
        int j = (height < MAX_HEIGHT / 2 || random.nextFloat() >= 0.25F) && height != MAX_HEIGHT - 1 ? 0 : 1;
        world.setBlockState(pos.up(), this.getDefaultState().with(AGE, i).with(LEAVES, bambooLeaves).with(STAGE, j), 3);
    }

    protected int countLogsAbove(BlockView world, BlockPos pos) {
        // done
        int count = 0;

        while (count < MAX_HEIGHT && world.getBlockState(pos.up(count + 1)).isIn(TantalisingBlocks.CINNAMON_LOGS)) {
            count++;
        }

        return count;
    }

    @Override
    public void grow(ServerWorld world, RandomGenerator random, BlockPos pos, BlockState state) {
        int i = this.countLogsAbove(world, pos);
        int j = this.countBambooBelow(world, pos);
        int k = i + j + 1;
        int l = 1 + random.nextInt(2);

        for(int m = 0; m < l; ++m) {
            BlockPos blockPos = pos.up(i);
            BlockState blockState = world.getBlockState(blockPos);
            if (k >= MAX_HEIGHT || blockState.get(STAGE) == 1 || !world.isAir(blockPos.up())) {
                return;
            }

            this.updateLeaves(blockState, world, blockPos, random, k);
            ++i;
            ++k;
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
        if (state.get(STAGE) == 0 && random.nextInt(3) == 0 && world.isAir(pos.up()) && world.getBaseLightLevel(pos.up(), 0) >= 9) {
            int i = this.countBambooBelow(world, pos) + 1;
            if (i < 16) {
                this.updateLeaves(state, world, pos, random, i);
            }
        }
    }

    @Override
    public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
        int i = this.countLogsAbove(world, pos);
        int j = this.countBambooBelow(world, pos);
        return i + j + 1 < MAX_HEIGHT && world.getBlockState(pos.up(i)).get(STAGE) != 1;
    }

    protected int countBambooBelow(BlockView world, BlockPos pos) {
        // done
        int count = 0;

        while (count < MAX_HEIGHT && world.getBlockState(pos.down(count + 1)).isIn(TantalisingBlocks.CINNAMON_LOGS)) {
            count++;
        }

        return count;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        // todo look at this
        if (!state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }

        if (direction == Direction.UP && neighborState.isIn(TantalisingBlocks.CINNAMON_LOGS) && neighborState.get(AGE) > state.get(AGE)) {
            world.setBlockState(pos, state.cycle(AGE), 2);
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}
