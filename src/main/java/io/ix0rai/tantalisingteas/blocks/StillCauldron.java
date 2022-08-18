package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.mixin.ChunkAccessor;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class StillCauldron extends TantalisingCauldronBlock {
    private static final Map<Item, CauldronBehavior> BEHAVIOUR = CauldronBehavior.createMap();

    static {
        BEHAVIOUR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> BoilingCauldron.decreaseLevel(state, world, pos, player, hand, stack, new ItemStack(TantalisingItems.TEA_BOTTLE)));
        BEHAVIOUR.put(TantalisingItems.TEA_BOTTLE, StillCauldron::increaseLevel);
    }

    public StillCauldron(Settings settings, Predicate<Biome.Precipitation> precipitationPredicate) {
        super(settings, precipitationPredicate, BEHAVIOUR);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BoilingCauldronBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    public static ActionResult increaseLevel(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!world.isClient && !isStateFull(state)) {
            Optional<BoilingCauldronBlockEntity> entity = world.getBlockEntity(pos, TantalisingBlocks.BOILING_CAULDRON_ENTITY);

            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));

            if (entity.isPresent()) {
                int level = state.get(LEVEL);
                world.setBlockState(pos, state.with(LEVEL, level + 1));
            } else {
                BlockState newState = TantalisingBlocks.STILL_CAULDRON.getDefaultState().with(LEVEL, 1);

                // create block entity
                WorldChunk chunk = world.getWorldChunk(pos);
                BoilingCauldronBlockEntity blockEntity = new BoilingCauldronBlockEntity(pos, newState);
                blockEntity.addData(stack.getNbt());
                ((ChunkAccessor) chunk).getBlockEntities().put(pos, blockEntity);

                // set block state
                world.setBlockState(pos, newState, Block.NOTIFY_ALL);

                // this reloads the block's colour provider
                world.getBlockState(pos).getBlock().neighborUpdate(newState, world, pos, Blocks.AIR, pos.up(), false);
            }

            world.playSound(player, pos, SoundEvents.ENTITY_AXOLOTL_SPLASH, SoundCategory.BLOCKS, 1.0f, 1.0f);
            useCauldronWith(player, stack);
        }

        return ActionResult.success(world.isClient);
    }
}
