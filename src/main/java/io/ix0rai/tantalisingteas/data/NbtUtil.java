package io.ix0rai.tantalisingteas.data;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.blocks.BoilingCauldron;
import io.ix0rai.tantalisingteas.items.TeaBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class NbtUtil {
    private static final String INGREDIENTS_KEY = "Ingredients";
    private static final String ID_KEY = "id";
    private static final String FLAIR_KEY = "flair";
    private static final String COLOUR_KEY = "colour";
    private static final String NEEDS_UPDATE_KEY = "needsUpdate";
    private static final String STRENGTH_KEY = "strength";

    public static NbtCompound getPrimaryIngredient(ItemStack stack) {
        if (!(stack.getItem() instanceof TeaBottle) || stack.getNbt() == null) {
            return null;
        } else {
            NbtList ingredients = getIngredients(stack.getNbt());
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

    public static void updateCustomName(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();

        if (nbt != null && needsUpdate(nbt)) {
            NbtCompound primaryIngredient = getPrimaryIngredient(stack);
            if (primaryIngredient != null) {
                String name = Util.translate(Util.BOTTLE) + " " + Util.translate(Util.OF) + " "
                        + Util.translate(Util.STRENGTHS[getOverallStrength(nbt)]) + " " + Util.translate(Registry.ITEM.get(new Identifier(primaryIngredient.getString(ID_KEY))).getTranslationKey()) + " " + Util.translate(Util.TEA);
                stack.setCustomName(Text.of(name));
            }
        }
    }

    public static Identifier getIngredientId(NbtCompound ingredient) {
        return new Identifier(ingredient.getString(ID_KEY));
    }

    public static TeaColour getColour(NbtCompound ingredient) {
        return TeaColour.get(ingredient.getString(COLOUR_KEY));
    }

    public static NbtList getIngredients(NbtCompound nbt) {
        if (nbt == null) {
            return new NbtList();
        } else {
            return nbt.getList(INGREDIENTS_KEY, 10);
        }
    }

    private static int getOverallStrength(NbtCompound nbt) {
        NbtList ingredients = nbt.getList(INGREDIENTS_KEY, 10);

        double averageStrength = 0;

        for (int i = 0; i < ingredients.size(); i++) {
            NbtCompound ingredient = ingredients.getCompound(i);
            averageStrength += ingredient.getInt(STRENGTH_KEY);
        }

        // calculate average strength
        return (int) Math.round(averageStrength / ingredients.size() / 2) - 1;
    }

    public static String getFlair(ItemStack stack, NbtCompound nbt, RandomGenerator random, int index) {
        updateFlairNbt(stack, nbt, random);
        final NbtList ingredients = nbt.getList(INGREDIENTS_KEY, 10);

        NbtCompound data = ingredients.getCompound(index);
        if (!data.isEmpty()) {
            return Util.getFlair(data.getInt(FLAIR_KEY));
        } else {
            return Util.getFlair(0);
        }
    }

    public static void updateFlairNbt(ItemStack stack, NbtCompound nbt, RandomGenerator random) {
        final NbtList ingredients = nbt.getList(INGREDIENTS_KEY, 10);

        boolean updatedNbt = false;

        // ensure every ingredient has an associated tooltip flair
        for (NbtElement element : ingredients) {
            if (element.getNbtType().equals(NbtCompound.TYPE)) {
                NbtCompound compound = (NbtCompound) element;

                // check compound for flair data and add if missing
                if (!(compound.contains(FLAIR_KEY))) {
                    compound.putInt(FLAIR_KEY, random.nextInt(Util.FLAIRS.length));
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
                TantalisingTeas.LOGGER.warn("attempted to add tea ingredient that is not in tea_ingredients tag; skipping");
                return;
            }

            // copy and update nbt
            NbtCompound compound = copyAndUpdate(ingredient, random);

            // write nbt
            ingredients.add(compound);
            updateIngredients(ingredients, nbt);
            setUpdated(nbt);
        }
    }

    private static NbtCompound copyAndUpdate(NbtCompound toCopy, RandomGenerator random) {
        NbtCompound newNbt = new NbtCompound();

        newNbt.putString(ID_KEY, toCopy.getString(ID_KEY));

        if (toCopy.contains(FLAIR_KEY)) {
            newNbt.putInt(FLAIR_KEY, toCopy.getInt(FLAIR_KEY));
        } else {
            newNbt.putInt(FLAIR_KEY, random.nextInt(Util.FLAIRS.length));
        }

        if (toCopy.contains(COLOUR_KEY)) {
            newNbt.putString(COLOUR_KEY, toCopy.getString(COLOUR_KEY));
        }

        if (toCopy.contains(STRENGTH_KEY)) {
            newNbt.putInt(STRENGTH_KEY, toCopy.getInt(STRENGTH_KEY));
        } else {
            newNbt.putInt(STRENGTH_KEY, 1);
        }

        return newNbt;
    }

    public static boolean isTeaIngredient(NbtCompound ingredient) {
        Identifier id = new Identifier(ingredient.getString(ID_KEY));
        return Registry.ITEM.get(id).getDefaultStack().isIn(BoilingCauldron.TEA_INGREDIENTS);
    }

    public static void setNeedsUpdate(NbtCompound nbt) {
        nbt.putBoolean(NEEDS_UPDATE_KEY, true);
    }

    public static void setUpdated(NbtCompound nbt) {
        nbt.putBoolean(NEEDS_UPDATE_KEY, false);
    }

    public static boolean needsUpdate(NbtCompound nbt) {
        return nbt.getBoolean(NEEDS_UPDATE_KEY);
    }

    public static boolean hasColour(NbtCompound ingredient) {
        return ingredient.contains(COLOUR_KEY);
    }

    public static void updateIngredients(NbtList ingredients, NbtCompound nbt) {
        nbt.put(INGREDIENTS_KEY, ingredients);
    }

    public static void setColour(NbtCompound ingredient, TeaColour colour) {
        ingredient.putString(COLOUR_KEY, colour.getId());
    }

    public static void setId(NbtCompound ingredient, Identifier id) {
        ingredient.putString(ID_KEY, id.toString());
    }
}
