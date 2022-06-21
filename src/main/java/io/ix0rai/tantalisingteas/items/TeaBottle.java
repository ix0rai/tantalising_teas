package io.ix0rai.tantalisingteas.items;

import io.ix0rai.tantalisingteas.Tantalisingteas;
import io.ix0rai.tantalisingteas.blocks.BoilingCauldron;
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
    public static final String ID_KEY = "id";
    public static final String FLAIR_KEY = "flair";
    public static final String COLOUR_KEY = "colour";
    public static final String NEEDS_UPDATE_KEY = "needsUpdate";
    public static final Text BAD_NBT = Tantalisingteas.translatableText("error.bad_nbt");
    public static final Text NO_NBT = Tantalisingteas.translatableText("error.no_nbt");
    public static final Text TEA = Tantalisingteas.translatableText("word.tea");
    public static final Text OF = Tantalisingteas.translatableText("word.of");
    public static final Text BOTTLE = Tantalisingteas.translatableText("word.bottle");
    public static final Text[] FLAIRS = new Text[] {
            Tantalisingteas.translatableText("flair.with_an_infusion"),
            Tantalisingteas.translatableText("flair.with_hints"),
            Tantalisingteas.translatableText("flair.with_undertones"),
            Tantalisingteas.translatableText("flair.with_a_taste")
    };

    public TeaBottle(Settings settings) {
        super(settings);
    }

    public static List<NbtCompound> getIngredients(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();

        if (nbt == null) {
            return new ArrayList<>();
        } else {
            ArrayList<NbtCompound> ingredients = new ArrayList<>();
            NbtList nbtList = nbt.getList(INGREDIENTS_KEY, 10);

            for (int i = 0; i < nbtList.size(); i ++) {
                ingredients.add(nbtList.getCompound(i));
            }

            return ingredients;
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        // we handle changing the stack
        ItemStack stack1 = stack.copy();
        super.finishUsing(stack, world, user);
        stack = stack1;

        if (user instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
        }

        // return empty bottle, or throw it away if it does not fit
        // also decrement the stack size if it will not be entirely consumed
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

    public static void addIngredient(ItemStack stack, NbtCompound ingredient, RandomGenerator random) {
        if (ingredient != null && !ingredient.isEmpty()  && ingredient.contains(ID_KEY)) {
            // get nbt
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtList ingredients;

            if (!nbt.isEmpty()) {
                ingredients = nbt.getList(INGREDIENTS_KEY, 10);
            } else {
                ingredients = new NbtList();
            }

            // ensure item is in tea ingredient tag
            if (!isTeaIngredient(ingredient)) {
                Tantalisingteas.LOGGER.warn("attempted to add tea ingredient that is not in tea_ingredients tag; skipping");
                return;
            }

            // save nbt
            NbtCompound compound = new NbtCompound();
            compound.putString(ID_KEY, ingredient.getString(ID_KEY));
            if (ingredient.contains(FLAIR_KEY)) {
                compound.putInt(FLAIR_KEY, ingredient.getInt(FLAIR_KEY));
            } else {
                compound.putInt(FLAIR_KEY, random.nextInt(FLAIRS.length));
            }
            if (ingredient.contains(COLOUR_KEY)) {
                compound.putString(COLOUR_KEY, ingredient.getString(COLOUR_KEY));
            }

            // write nbt
            ingredients.add(compound);
            nbt.put(INGREDIENTS_KEY, ingredients);
            nbt.putBoolean(NEEDS_UPDATE_KEY, true);
            stack.setNbt(nbt);
        }
    }

    public static boolean isTeaIngredient(NbtCompound ingredient) {
        Identifier id = new Identifier(ingredient.getString(ID_KEY));
        return Registry.ITEM.get(id).getDefaultStack().isIn(BoilingCauldron.TEA_INGREDIENTS);
    }

    public static NbtCompound getPrimaryIngredient(ItemStack stack) {
        if (!(stack.getItem() instanceof TeaBottle) || stack.getNbt() == null) {
            return null;
        } else {
            NbtList ingredients = stack.getNbt().getList(INGREDIENTS_KEY, 10);
            HashMap<NbtElement, Integer> counts = new HashMap<>();

            for (NbtElement ingredient : ingredients) {
                counts.put(ingredient, counts.getOrDefault(ingredient, 0) + 1);
            }

            NbtElement primary = null;
            int number = 0;

            for (Map.Entry<NbtElement, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > number) {
                    primary = entry.getKey();
                }
            }

            return (NbtCompound) primary;
        }
    }

    public static void setCustomName(ItemStack stack) {
        NbtCompound primaryIngredient = getPrimaryIngredient(stack);
        if (primaryIngredient != null) {
            String name = translate(BOTTLE) + " " + translate(OF) + " "
                    + translate(Registry.ITEM.get(new Identifier(primaryIngredient.getString(ID_KEY))).getTranslationKey()) + " " + translate(TEA);
            stack.setCustomName(Text.of(name));
        }
    }

    public String getFlair(ItemStack stack, NbtCompound nbt, RandomGenerator random, int index) {
        updateFlairNbt(stack, nbt, random);
        final NbtList ingredients = nbt.getList(INGREDIENTS_KEY, 10);

        NbtCompound data = ingredients.getCompound(index);
        if (!data.isEmpty()) {
            return getFlair(data.getInt(FLAIR_KEY));
        } else {
            return getFlair(0);
        }
    }

    private String getFlair(int index) {
        return translate(FLAIRS[index]) + " " + translate(OF).toLowerCase();
    }

    private static String translate(Text text) {
        return translate(text.getString());
    }

    private static String translate(String text) {
        return Language.getInstance().get(text);
    }

    public void updateFlairNbt(ItemStack stack, NbtCompound nbt, RandomGenerator random) {
        final NbtList ingredients = nbt.getList(INGREDIENTS_KEY, 10);

        boolean updatedNbt = false;

        // ensure every ingredient has an associated tooltip flair
        for (NbtElement element : ingredients) {
            if (element.getNbtType().equals(NbtCompound.TYPE)) {
                NbtCompound compound = (NbtCompound) element;

                // check compound for flair data and add if missing
                if (!(compound.contains(FLAIR_KEY))) {
                    compound.putInt(FLAIR_KEY, random.nextInt(FLAIRS.length));
                    updatedNbt = true;
                }
            }
        }

        if (updatedNbt) {
            // save updated nbt to stack
            nbt.put(INGREDIENTS_KEY, ingredients);
            stack.setNbt(nbt);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        NbtCompound nbt = stack.getNbt();

        if (nbt == null) {
            tooltip.add(NO_NBT);
        } else if (stack.hasNbt()) {
            if (!stack.hasCustomName()) {
                setCustomName(stack);
            }

           NbtList ingredients = nbt.getList(INGREDIENTS_KEY, 10);
           for (int i = 0; i < ingredients.size(); i ++) {
               NbtElement element = ingredients.get(i);

               if (element.getNbtType() != NbtCompound.TYPE) {
                   tooltip.add(BAD_NBT);
               } else {
                   Identifier id = new Identifier(((NbtCompound) element).getString(ID_KEY));
                   Item ingredient = Registry.ITEM.get(id);
                   RandomGenerator random = world == null ? RandomGenerator.createThreaded() : world.random;
                   tooltip.add(Text.of(getFlair(stack, nbt, random, i) + " " + translate(ingredient.getTranslationKey())));
               }
           }
        }
    }
}
