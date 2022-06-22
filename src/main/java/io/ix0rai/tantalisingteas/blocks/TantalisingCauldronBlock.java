package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.mixin.AbstractCauldronBlockAccessor;
import io.ix0rai.tantalisingteas.mixin.CauldronBlockMixin;
import io.ix0rai.tantalisingteas.mixin.LeveledCauldronBlockInvoker;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Map;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public abstract class TantalisingCauldronBlock extends BlockWithEntity {
    private final Map<Item, CauldronBehavior> behaviour;
    public static final IntProperty LEVEL = Properties.LEVEL_3;
    private final Predicate<Biome.Precipitation> precipitationPredicate;

    protected TantalisingCauldronBlock(Settings settings, Predicate<Biome.Precipitation> precipitationPredicate, Map<Item, CauldronBehavior> behaviour) {
        super(settings);
        this.behaviour = behaviour;
        this.precipitationPredicate = precipitationPredicate;
    }

    public static boolean isFull(BlockState state) {
        try {
            return (state.getBlock() instanceof TantalisingCauldronBlock || state.getBlock() instanceof AbstractCauldronBlock) && state.get(LEVEL) >= 3;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public static boolean isEmpty(BlockState state) {
        try {
            return (state.getBlock() instanceof TantalisingCauldronBlock || state.getBlock() instanceof AbstractCauldronBlock) && state.get(LEVEL) <= 0;
        } catch (IllegalArgumentException ignored) {
            return true;
        }
    }

    protected boolean canBeFilledByDripstone(Fluid fluid) {
        return fluid == Fluids.WATER && this.precipitationPredicate == LeveledCauldronBlock.RAIN_PREDICATE;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity.isOnFire() && ((AbstractCauldronBlockAccessor) this).invokeIsEntityTouchingFluid(state, pos, entity)) {
            entity.extinguish();
            if (entity.canModifyAt(world, pos)) {
                ((LeveledCauldronBlockInvoker) this).invokeOnFireCollision(state, world, pos);
            }
        }
    }

    @Override
    public void precipitationTick(BlockState state, World world, BlockPos pos, Biome.Precipitation precipitation) {
        if (!CauldronBlockMixin.invokeCanFillWithPrecipitation(world, precipitation) || state.get(LEVEL) == 3 || !this.precipitationPredicate.test(precipitation)) {
            return;
        }
        world.setBlockState(pos, state.cycle(LEVEL));
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(LEVEL);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    protected void fillFromDripstone(BlockState state, World world, BlockPos pos) {
        if (isFull(state)) {
            return;
        }
        world.setBlockState(pos, state.with(LEVEL, state.get(LEVEL) + 1));
        world.syncWorldEvent(1047, pos, 0);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);
        CauldronBehavior cauldronBehavior = this.behaviour.get(itemStack.getItem());
        return cauldronBehavior.interact(state, world, pos, player, hand, itemStack);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return AbstractCauldronBlockAccessor.getOutlineShape();
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return AbstractCauldronBlockAccessor.getRaycastShape();
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, RandomGenerator randomGenerator) {
        BlockPos blockPos = PointedDripstoneBlock.getDripPos(world, pos);
        if (blockPos == null) {
            return;
        }
        Fluid fluid = PointedDripstoneBlock.getDripFluid(world, blockPos);
        if (fluid != Fluids.EMPTY && this.canBeFilledByDripstone(fluid)) {
            this.fillFromDripstone(state, world, pos);
        }
    }
}
