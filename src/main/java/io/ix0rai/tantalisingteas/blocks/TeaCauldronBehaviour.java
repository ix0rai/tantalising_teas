package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.data.Util;
import io.ix0rai.tantalisingteas.mixin.ChunkAccessor;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Holder;
import net.minecraft.util.HolderSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.event.GameEvent;

import java.util.Map;
import java.util.Optional;

public class TeaCauldronBehaviour {
    protected static final Map<Item, CauldronBehavior> BEHAVIOUR = CauldronBehavior.createMap();

    static {
        BEHAVIOUR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> decreaseLevel(state, world, pos, player, hand, stack, new ItemStack(TantalisingItems.TEA_BOTTLE)));
        BEHAVIOUR.put(Items.WATER_BUCKET, (TeaCauldronBehaviour::fillCauldron));
        BEHAVIOUR.put(Items.POTION, (TeaCauldronBehaviour::increaseLevel));
        BEHAVIOUR.put(TantalisingItems.TEA_BOTTLE, (TeaCauldronBehaviour::increaseLevel));
    }

    public static void addBehaviour() {
        HolderSet.NamedSet<Item> teaIngredients = Registry.ITEM.getOrCreateTag(Util.TEA_INGREDIENTS);
        teaIngredients.forEach((Holder<Item> item) -> BEHAVIOUR.put(item.value(), TeaCauldronBehaviour::addIngredient));
        CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(TantalisingItems.TEA_BOTTLE, TeaCauldronBehaviour::createCauldron);
    }

    private static ActionResult addIngredient(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!world.isClient && Boolean.TRUE.equals(state.get(TeaCauldron.BOILING))) {
            Optional<TeaCauldronBlockEntity> entity = world.getBlockEntity(pos, TantalisingBlocks.TEA_CAULDRON_ENTITY);
            if (entity.isPresent()) {
                entity.get().addStack(stack);
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                world.setBlockState(pos, state.with(TeaCauldron.COLOUR, TeaColour.getFromIngredients(entity.get().getIngredients())));

                world.playSound(player, pos, SoundEvents.ENTITY_AXOLOTL_SPLASH, SoundCategory.BLOCKS, 1.0f, 1.0f);
                TeaCauldron.useCauldronWith(player, stack);
            }
        }

        return ActionResult.success(world.isClient);
    }

    private static ActionResult createCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        // assumes the stack is of a tea item
        if (!world.isClient && !TeaCauldron.isStateFull(state)) {
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));

            // create new block state
            NbtList ingredients = NbtUtil.getIngredients(stack.getNbt());
            BlockState newState = TantalisingBlocks.TEA_CAULDRON.getDefaultState()
                    .with(TeaCauldron.LEVEL, 1)
                    .with(TeaCauldron.STRENGTH, NbtUtil.getOverallStrength(ingredients))
                    .with(TeaCauldron.COLOUR, TeaColour.getFromIngredients(ingredients))
                    .with(TeaCauldron.BOILING, false);

            // set block state and create block entity
            createBlockEntity(world, state, pos, stack.getNbt());
            world.setBlockState(pos, newState, Block.NOTIFY_ALL);

            // finish action
            world.playSound(player, pos, SoundEvents.ENTITY_AXOLOTL_SPLASH, SoundCategory.BLOCKS, 1.0f, 1.0f);
            TeaCauldron.useCauldronWith(player, stack);
        }

        return ActionResult.success(world.isClient);
    }

    private static void createBlockEntity(World world, BlockState state, BlockPos pos, NbtCompound nbt) {
        // create block entity
        WorldChunk chunk = world.getWorldChunk(pos);
        TeaCauldronBlockEntity blockEntity = new TeaCauldronBlockEntity(pos, state);
        blockEntity.addData(nbt);
        ((ChunkAccessor) chunk).getBlockEntities().put(pos, blockEntity);
    }

    private static ActionResult increaseLevel(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!stack.isOf(TantalisingItems.TEA_BOTTLE) || PotionUtil.getPotion(stack) != Potions.WATER || TeaCauldron.isStateFull(state)) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            // get entity
            Optional<TeaCauldronBlockEntity> entity = world.getBlockEntity(pos, TantalisingBlocks.TEA_CAULDRON_ENTITY);
            // return glass bottle - everything we currently pass to this method is contained in a glass bottle
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));

            // get properties
            int level = state.get(TeaCauldron.LEVEL);
            int strength = state.get(TeaCauldron.STRENGTH);
            TeaColour colour = state.get(TeaCauldron.COLOUR);

            // add data and reload properties
            if (entity.isPresent()) {
                TeaCauldronBlockEntity cauldron = entity.get();
                if (stack.isOf(TantalisingItems.TEA_BOTTLE) && stack.getNbt() != null && NbtUtil.containsIngredientKey(stack.getNbt())) {
                    cauldron.addData(stack.getNbt());
                    strength = NbtUtil.getOverallStrength(cauldron.getIngredients());
                }
                colour = TeaColour.getFromIngredients(cauldron.getIngredients());
            }

            // set state and finish action
            world.setBlockState(
                    pos,
                    state.with(TeaCauldron.LEVEL, level + 1)
                    .with(TeaCauldron.STRENGTH, strength)
                    .with(TeaCauldron.COLOUR, colour),
                    Block.NOTIFY_LISTENERS
            );
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            TeaCauldron.useCauldronWith(player, stack);
        }

        return ActionResult.success(world.isClient);
    }

    private static ActionResult fillCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!stack.getItem().equals(Items.WATER_BUCKET) || TeaCauldron.isStateFull(state)) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));

                world.setBlockState(pos, state.with(TeaCauldron.LEVEL, 3), 2);

                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }

            return ActionResult.success(world.isClient);
        }
    }

    private static ActionResult decreaseLevel(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, ItemStack output) {
        Optional<TeaCauldronBlockEntity> entity = world.getBlockEntity(pos, TantalisingBlocks.TEA_CAULDRON_ENTITY);

        if (TeaCauldron.isStateEmpty(state)) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                // return water bottle if no ingredients are present
                if (entity.isEmpty() || entity.get().getIngredients().isEmpty()) {
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, output));
                    output = PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                } else {
                    // otherwise, append ingredients from previously existing block entity
                    appendIngredients(output, entity.get().getIngredients(), world.random);
                }

                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, output));

                int level = state.get(TeaCauldron.LEVEL);
                world.setBlockState(
                        pos,
                        level <= 1 ? Blocks.CAULDRON.getDefaultState() : state.with(TeaCauldron.LEVEL, level - 1),
                        2
                );

                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
                TeaCauldron.useCauldronWith(player, stack);
            }

            return ActionResult.success(world.isClient);
        }
    }

    private static void appendIngredients(ItemStack output, NbtList ingredients, RandomGenerator random) {
        for (int i = 0; i < ingredients.size(); i ++) {
            NbtUtil.addIngredient(output, ingredients.getCompound(i), random);
        }
    }
}
