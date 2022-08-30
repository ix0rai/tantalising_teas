package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.data.Util;
import io.ix0rai.tantalisingteas.mixin.BlockWithEntityInvoker;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import io.ix0rai.tantalisingteas.registry.TantalisingItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class BoilingCauldron extends TantalisingCauldronBlock {
    protected static final Map<Item, CauldronBehavior> BEHAVIOUR = CauldronBehavior.createMap();

    static {
        BEHAVIOUR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> decreaseLevel(state, world, pos, player, hand, stack, new ItemStack(TantalisingItems.TEA_BOTTLE)));
        BEHAVIOUR.put(Items.WATER_BUCKET, (BoilingCauldron::fillCauldron));
        BEHAVIOUR.put(Items.POTION, (BoilingCauldron::increaseLevel));
        BEHAVIOUR.put(TantalisingItems.TEA_BOTTLE, (BoilingCauldron::increaseLevel));
    }

    public BoilingCauldron(Settings settings, Predicate<Biome.Precipitation> precipitationPredicate) {
        super(settings, precipitationPredicate, BEHAVIOUR);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BoilingCauldronBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BlockWithEntityInvoker.invokeCheckType(type, TantalisingBlocks.BOILING_CAULDRON_ENTITY, (w, pos, blockState, boilingCauldron) -> BoilingCauldronBlockEntity.tick(boilingCauldron));
    }

    public static void addBehaviour() {
        HolderSet.NamedSet<Item> teaIngredients = Registry.ITEM.getOrCreateTag(Util.TEA_INGREDIENTS);
        teaIngredients.forEach((Holder<Item> item) -> BEHAVIOUR.put(item.value(), BoilingCauldron::addIngredient));
        CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(TantalisingItems.TEA_BOTTLE, StillCauldron::increaseLevel);
    }

    public static ActionResult addIngredient(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!world.isClient) {
            Optional<BoilingCauldronBlockEntity> entity = world.getBlockEntity(pos, TantalisingBlocks.BOILING_CAULDRON_ENTITY);
            if (entity.isPresent()) {
                entity.get().addStack(stack);
                if (!player.getAbilities().creativeMode) {
                    stack.decrement(1);
                }

                world.setBlockState(pos, state.with(COLOUR, TeaColour.getFromIngredients(entity.get().getIngredients())));

                world.playSound(player, pos, SoundEvents.ENTITY_AXOLOTL_SPLASH, SoundCategory.BLOCKS, 1.0f, 1.0f);
                useCauldronWith(player, stack);
            }
        }

        return ActionResult.success(world.isClient);
    }

    public static ActionResult increaseLevel(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (PotionUtil.getPotion(stack) != Potions.WATER || isStateFull(state)) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));

            int level = state.get(LEVEL);
            int strength = state.get(STRENGTH);

            if (stack.isOf(TantalisingItems.TEA_BOTTLE) && stack.getNbt() != null && NbtUtil.containsIngredientKey(stack.getNbt())) {
                Optional<BoilingCauldronBlockEntity> entity = world.getBlockEntity(pos, TantalisingBlocks.BOILING_CAULDRON_ENTITY);
                if (entity.isPresent()) {
                    entity.get().addData(stack.getNbt());
                    strength = NbtUtil.getOverallStrength(entity.get().getIngredients());
                }
            }

            world.setBlockState(pos, state.with(LEVEL, level + 1).with(STRENGTH, strength));
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            useCauldronWith(player, stack);
        }

        return ActionResult.success(world.isClient);
    }

    private static ActionResult fillCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!stack.getItem().equals(Items.WATER_BUCKET) || isStateFull(state)) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));

                world.setBlockState(pos, state.with(LEVEL, 3), 2);

                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }

            return ActionResult.success(world.isClient);
        }
    }

    static ActionResult decreaseLevel(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, ItemStack output) {
        Optional<BoilingCauldronBlockEntity> entity = world.getBlockEntity(pos, TantalisingBlocks.BOILING_CAULDRON_ENTITY);

        if (isStateEmpty(state)) {
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

                int level = state.get(LEVEL);
                world.setBlockState(
                        pos,
                        level <= 1 ? Blocks.CAULDRON.getDefaultState() : state.with(LEVEL, level - 1),
                        2
                );

                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
                useCauldronWith(player, stack);
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
