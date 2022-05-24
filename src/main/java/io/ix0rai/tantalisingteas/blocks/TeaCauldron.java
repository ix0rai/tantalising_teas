package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.items.TeaBottle;
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
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class TeaCauldron extends TantalisingCauldronBlock {
    public static final TagKey<Item> TEA_INGREDIENTS = TagKey.of(Registry.ITEM_KEY, new Identifier("c:tea_ingredients"));
    public static final Map<Item, CauldronBehavior> BEHAVIOUR = CauldronBehavior.createMap();
    private static final IntProperty STRENGTH = IntProperty.of("strength", 1, 3);

    public static boolean registeredRecipes = false;

    static {
        BEHAVIOUR.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> decreaseLevel(state, world, pos, player, hand, stack, new ItemStack(TantalisingItems.TEA_BOTTLE)));
        BEHAVIOUR.put(Items.WATER_BUCKET, (TeaCauldron::fillCauldron));
        BEHAVIOUR.put(Items.POTION, (TeaCauldron::increaseLevel));
    }

    public TeaCauldron(Settings settings, Predicate<Biome.Precipitation> precipitationPredicate, Map<Item, CauldronBehavior> behaviour) {
        super(settings, precipitationPredicate, behaviour);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STRENGTH);
        super.appendProperties(builder);
    }

    public static void addBehaviour() {
        Registry.ITEM.getOrCreateTag(TEA_INGREDIENTS).forEach((Holder<Item> item) -> BEHAVIOUR.put(item.value(), TeaCauldron::increaseStrength));
        registeredRecipes = true;
    }

    public static ActionResult increaseStrength(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (state.get(STRENGTH) >= 3) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                Optional<TeaCauldronBlockEntity> entity = world.getBlockEntity(pos, TantalisingBlocks.TEA_CAULDRON_ENTITY);
                if (entity.isPresent()) {
                    entity.get().addItem(stack);

                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }

                    world.setBlockState(pos, state.with(STRENGTH, state.get(STRENGTH) + 1));

                    world.playSound(player, pos, SoundEvents.ENTITY_AXOLOTL_SPLASH, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
            }

            return ActionResult.success(world.isClient);
        }
    }

    public static ActionResult createTeaCauldron(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!world.isClient) {
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, stack.getItem() instanceof TeaBottle ? new ItemStack(Items.GLASS_BOTTLE) : stack));

            if (!(stack.getItem() instanceof TeaBottle)) {
               stack.decrement(1);
            }

            world.setBlockState(pos, TantalisingBlocks.TEA_CAULDRON.getDefaultState().with(LEVEL, 1).with(STRENGTH, 3));
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        }

        return ActionResult.success(world.isClient);
    }

    public static ActionResult convertToTeaCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (isFull(state)) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));

                int level = state.get(LEVEL);
                //flip over numbers: 3 goes to 1, 1 goes to 3, 2 stays 2
                int strength = Math.abs(level - 4);

                world.setBlockState(pos, TantalisingBlocks.TEA_CAULDRON.getDefaultState().with(LEVEL, level + 1).with(STRENGTH, strength));
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }

            return ActionResult.success(world.isClient);
        }
    }

    public static ActionResult increaseLevel(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (PotionUtil.getPotion(stack) != Potions.WATER || isFull(state)) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));

            int level = state.get(LEVEL);
            int strength = state.get(STRENGTH);

            world.setBlockState(pos, state.with(LEVEL, level + 1).with(STRENGTH, strength > 1 ? strength - 1 : 1));
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return ActionResult.success(world.isClient);
    }

    private static ActionResult fillCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!stack.getItem().equals(Items.WATER_BUCKET) || isFull(state)) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));

                world.setBlockState(pos, state.with(LEVEL, 3).with(STRENGTH, 1), 2);

                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }

            return ActionResult.success(world.isClient);
        }
    }

    private static ActionResult decreaseLevel(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, ItemStack output) {
        if (isEmpty(state)) {
            return ActionResult.PASS;
        } else {
            if (!world.isClient) {
                Optional<TeaCauldronBlockEntity> entity = world.getBlockEntity(pos, TantalisingBlocks.TEA_CAULDRON_ENTITY);

                if (entity.isPresent()) {
                    for (ItemStack ingredient : entity.get().getItems()) {
                        TeaBottle.addIngredient(output, ingredient.getItem());
                    }

                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, output));
                    player.incrementStat(Stats.USE_CAULDRON);
                    player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));

                    int level = state.get(LEVEL);
                    world.setBlockState(pos, level <= 1 ? Blocks.CAULDRON.getDefaultState() : state.with(LEVEL, level - 1), 2);

                    world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
                }
            }

            return ActionResult.success(world.isClient);
        }
    }
}
