package io.ix0rai.tantalisingteas.items;

import io.ix0rai.tantalisingteas.Tantalisingteas;
import io.ix0rai.tantalisingteas.blocks.TeaCauldron;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneyBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeaBottle extends HoneyBottleItem {
    public static final String INGREDIENTS_KEY = "Ingredients";
    public static final String FLAIRS_KEY = "Flairs";
    public static final String ID_KEY = "id";
    public static final String NUMBER_KEY = "number";
    public static final Text BAD_NBT = Tantalisingteas.translatableText("error.bad_nbt");
    public static final Text NO_NBT = Tantalisingteas.translatableText("error.no_nbt");
    public static final Text TEA = Tantalisingteas.translatableText("word.tea");
    public static final Text OF = Tantalisingteas.translatableText("word.of");
    public static final Text BOTTLE = Tantalisingteas.translatableText("word.bottle");
    public static final Text[] FLAIRS = new Text[] {
            Tantalisingteas.translatableText("flair.with_an_infusion_of"),
            Tantalisingteas.translatableText("flair.with_hints_of"),
            Tantalisingteas.translatableText("flair.with_undertones_of"),
            Tantalisingteas.translatableText("flair.with_a_taste_of")
    };

    public TeaBottle(Settings settings) {
        super(settings);
    }

    public static List<ItemStack> getIngredients(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();

        if (nbt == null || !(stack.getItem() instanceof TeaBottle)) {
            return new ArrayList<>();
        } else {
            ArrayList<ItemStack> ingredients = new ArrayList<>();
            NbtList nbtList = nbt.getList(INGREDIENTS_KEY, 10);

            for (NbtElement element : nbtList) {
                Identifier id = new Identifier(((NbtCompound) element).getString(ID_KEY));
                ingredients.add(new ItemStack(Registry.ITEM.get(id)));
            }

            return ingredients;
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        //we handle changing the stack
        ItemStack stack1 = stack.copy();
        super.finishUsing(stack, world, user);
        stack = stack1;

        if (user instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
        }

        //return empty bottle, or throw it away if it does not fit
        //also decrement the stack size if it will not be entirely consumed
        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        } else {
            if (user instanceof PlayerEntity playerEntity && !playerEntity.getAbilities().creativeMode) {
                ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!playerEntity.getInventory().insertStack(glassBottle)) {
                    playerEntity.dropItem(glassBottle, false);
                }

                if (stack.getCount() == 1) {
                    return glassBottle;
                } else {
                    stack.decrement(1);
                    return stack;
                }
            }

            return stack;
        }
    }

    public static void addIngredient(ItemStack stack, Item ingredient, RandomGenerator random) {
        if (!new ItemStack(ingredient).isIn(TeaCauldron.TEA_INGREDIENTS)) {
            Tantalisingteas.LOGGER.warn("attempted to add tea ingredient that is not in tea_ingredients tag; skipping");
            return;
        }

        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList ingredients;
        NbtList flairs;

        if (!nbt.isEmpty()) {
            ingredients = nbt.getList(INGREDIENTS_KEY, 10);
            flairs = nbt.getList(FLAIRS_KEY, 10);
        } else {
            ingredients = new NbtList();
            flairs = new NbtList();
        }

        // write nbt
        NbtCompound compound = new NbtCompound();
        compound.putString(ID_KEY, Registry.ITEM.getId(ingredient).toString());
        ingredients.add(compound);
        flairs.add(NbtInt.of(random.nextInt(FLAIRS.length)));
        nbt.put(FLAIRS_KEY, flairs);
        nbt.put(INGREDIENTS_KEY, ingredients);
        stack.setNbt(nbt);
    }

    public static ItemStack getPrimaryIngredient(ItemStack stack) {
        if (!(stack.getItem() instanceof TeaBottle)) {
            return null;
        } else {
            List<ItemStack> ingredients = getIngredients(stack);
            HashMap<ItemStack, Integer> counts = new HashMap<>();

            for (ItemStack ingredient : ingredients) {
                counts.putIfAbsent(ingredient, 0);
                counts.put(ingredient, counts.get(ingredient) + 1);
            }

            ItemStack primary = null;
            int number = 0;

            for (Map.Entry<ItemStack, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > number) {
                    primary = entry.getKey();
                }
            }

            return primary;
        }
    }

    public void setCustomName(ItemStack stack) {
        if (stack.getItem() instanceof TeaBottle && stack.hasNbt() && !stack.hasCustomName()) {
            ItemStack primaryIngredient = getPrimaryIngredient(stack);
            if (primaryIngredient != null) {
                String name = Language.getInstance().get(BOTTLE.getString()) + " " + Language.getInstance().get(OF.getString()) + " "
                        + Language.getInstance().get(primaryIngredient.getTranslationKey()) + " " + Language.getInstance().get(TEA.getString());
                stack.setCustomName(Text.of(name));
            }
        }
    }

    public String getFlair(ItemStack stack, NbtCompound nbt, RandomGenerator random, int index) {
        updateFlairNbt(stack, nbt, random);
        final NbtList ingredients = nbt.getList(INGREDIENTS_KEY, 10);
        NbtList flairs = nbt.getList(FLAIRS_KEY, 10);

        int j = 0;
        for (int i = 0; i < ingredients.size(); i ++) {
            if (j == index) {
                NbtCompound flair = (NbtCompound) flairs.get(j);
                NbtInt flairIndex = (NbtInt) flair.get(NUMBER_KEY);
                if (flairIndex != null) {
                    return Language.getInstance().get(FLAIRS[flairIndex.intValue()].getString());
                }
            }

            j ++;
        }

        return FLAIRS[0].getString();
    }

    public void updateFlairNbt(ItemStack stack, NbtCompound nbt, RandomGenerator random) {
        final NbtList ingredients = nbt.getList(INGREDIENTS_KEY, 10);
        NbtList flairs = nbt.getList(FLAIRS_KEY, 10);

        if (flairs == null) {
            flairs = new NbtList();
            for (int i = 0; i < ingredients.size(); i ++) {
                NbtCompound c = new NbtCompound();
                c.put(NUMBER_KEY, NbtInt.of(random.nextInt(FLAIRS.length)));
                flairs.add(c);
            }

            // save flair nbt to stack
            nbt.put(FLAIRS_KEY, flairs);
            stack.setNbt(nbt);
        } else if (flairs.size() < ingredients.size()) {
            for (int i = flairs.size(); i < ingredients.size(); i ++) {
                NbtCompound c = new NbtCompound();
                c.put(NUMBER_KEY, NbtInt.of(random.nextInt(FLAIRS.length)));
                flairs.add(c);
            }

            // save flair nbt to stack
            nbt.put(FLAIRS_KEY, flairs);
            stack.setNbt(nbt);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        setCustomName(stack);

        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            tooltip.add(NO_NBT);
        } else {
           NbtList ingredients = nbt.getList(INGREDIENTS_KEY, 10);
           for (int i = 0; i < ingredients.size(); i ++) {
               NbtElement element = ingredients.get(i);

               if (element.getNbtType() != NbtCompound.TYPE) {
                   tooltip.add(BAD_NBT);
               } else {
                   Identifier id = new Identifier(((NbtCompound) element).getString(ID_KEY));
                   Item ingredient = Registry.ITEM.get(id);
                   RandomGenerator random = world == null ? RandomGenerator.createThreaded() : world.random;
                   tooltip.add(Text.of(Language.getInstance().get(getFlair(stack, nbt, random, i)) + " " + Language.getInstance().get(ingredient.getTranslationKey())));
               }
           }
        }
    }
}
