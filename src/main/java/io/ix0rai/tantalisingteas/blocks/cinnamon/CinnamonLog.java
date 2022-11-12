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
    public static final int AGE_INCREASE_HEIGHT = 4;
    public static final EnumProperty<CinnamonTreeLeaves> LEAVES = EnumProperty.of("leaves", CinnamonTreeLeaves.class);
    public static final IntProperty AGE = Properties.AGE_1;
    public static final IntProperty STAGE = Properties.STAGE;

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
        BlockState blockState = world.getBlockState(pos.down());
        if (blockState.isIn(TantalisingBlocks.CINNAMON_LOGS)) {
            if (state.get(LEAVES) == CinnamonTreeLeaves.NONE) {
                world.setBlockState(pos, state.with(LEAVES, CinnamonTreeLeaves.SMALL));
            } else {
                world.setBlockState(pos, state.with(LEAVES, CinnamonTreeLeaves.random(random)));
            }
        }




        // todo: rewrite this
//        BlockState blockState = world.getBlockState(pos.down());
//        BlockPos blockPos = pos.down(2);
//        BlockState blockState2 = world.getBlockState(blockPos);
//        CinnamonTreeLeaves bambooLeaves = CinnamonTreeLeaves.NONE;
//
//        if (height >= 1) {
//            if (blockState.isIn(TantalisingBlocks.CINNAMON_LOGS) && blockState.get(LEAVES) != CinnamonTreeLeaves.NONE) {
//                if (blockState.isIn(TantalisingBlocks.CINNAMON_LOGS) && blockState.get(LEAVES) != CinnamonTreeLeaves.NONE) {
//                    bambooLeaves = CinnamonTreeLeaves.LARGE;
//                    if (blockState2.isIn(TantalisingBlocks.CINNAMON_LOGS)) {
//                        world.setBlockState(pos.down(), blockState.with(LEAVES, CinnamonTreeLeaves.SMALL), 3);
//                        world.setBlockState(blockPos, blockState2.with(LEAVES, CinnamonTreeLeaves.NONE), 3);
//                    }
//                }
//            } else {
//                bambooLeaves = CinnamonTreeLeaves.SMALL;
//            }
//        }
//
//        int i = state.get(AGE) != 1 && !blockState2.isIn(TantalisingBlocks.CINNAMON_LOGS) ? 0 : 1;
//        int j = (height < MAX_HEIGHT / 2 || random.nextFloat() >= 0.25F) && height != MAX_HEIGHT - 1 ? 0 : 1;
//        world.setBlockState(pos.up(), this.getDefaultState().with(AGE, i).with(LEAVES, bambooLeaves).with(STAGE, j), 3);
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
        int logsAbove = this.countLogsAbove(world, pos);
        int logsBelow = this.countLogsBelow(world, pos);
        // height is amount of logs below + amount of logs above + current log
        int height = logsAbove + logsBelow + 1;
        int l = 1 + random.nextInt(2);

        for (int m = 0; m < l; m ++) {
            BlockPos newPos = pos.up(logsAbove);
            BlockState stateBelow = world.getBlockState(newPos.down());

            if (height < MAX_HEIGHT && world.isAir(newPos)) {
                world.setBlockState(newPos, this.getDefaultState().with(AGE, 1).with(LEAVES, CinnamonTreeLeaves.NONE).with(STAGE, 1), Block.NOTIFY_ALL);
            }

            this.updateLeaves(stateBelow, world, newPos.down(), random, height);
            logsAbove++;
            height ++;
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator random) {
        if (state.get(STAGE) == 0 && random.nextInt(3) == 0 && world.isAir(pos.up()) && world.getBaseLightLevel(pos.up(), 0) >= 9) {
            int i = this.countLogsBelow(world, pos) + 1;
            if (i < 16) {
                this.updateLeaves(state, world, pos, random, i);
            }
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
        int logsAbove = this.countLogsAbove(world, pos);
        int logsBelow = this.countLogsBelow(world, pos);
        return logsAbove + logsBelow + 1 < MAX_HEIGHT;
    }

    protected int countLogsBelow(BlockView world, BlockPos pos) {
        // done
        int count = 0;

        while (count < MAX_HEIGHT && world.getBlockState(pos.down(count + 1)).isIn(TantalisingBlocks.CINNAMON_LOGS)) {
            count++;
        }

        return count;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        // break if no logs are below
        if (!state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }

        // increase age with the rest of the tree
        if ((direction == Direction.UP || direction == Direction.DOWN) && neighborState.isIn(TantalisingBlocks.CINNAMON_LOGS) && neighborState.get(AGE) == 1) {
            world.setBlockState(pos, state.with(AGE, 1), Block.NOTIFY_LISTENERS);
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}
