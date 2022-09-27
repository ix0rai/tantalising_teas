package io.ix0rai.tantalisingteas.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * helper methods for dealing with nbt
 */
public class NbtUtil {
    public static final int MAX_STRENGTH = 6;

    private static final String INGREDIENTS_KEY = "Ingredients";
    private static final String ID_KEY = "id";
    private static final String STRENGTH_KEY = "Strength";
    private static final String TICKS_SINCE_STRENGTH_INCREASE_KEY = "TicksSinceStrengthIncrease";

    /**
     * gets the primary ingredient of the provided stack's nbt
     * @param stack the stack to get the primary ingredient of
     * @return the primary ingredient: the ingredient that occurs the most often in the nbt
     */
    public static NbtCompound getPrimaryIngredient(ItemStack stack) {
        if (stack.getNbt() == null) {
            return null;
        } else {
            NbtList ingredients = getIngredients(stack.getNbt());
            CountMap<NbtElement> counts = new CountMap<>();

            // count how many occurrences of each ingredient there are and return the one with the most
            ingredients.forEach(counts::increment);
            return (NbtCompound) counts.highestValue();
        }
    }

    public static Identifier getIngredientId(NbtCompound ingredient) {
        return new Identifier(ingredient.getString(ID_KEY));
    }

    /**
     * gets a tantalising teas ingredient list from the nbt compound
     * @param nbt the nbt compound to get the ingredient list from
     * @return the {@link NbtList} of ingredients if present, an empty list if not
     */
    public static NbtList getIngredients(NbtCompound nbt) {
        if (nbt == null) {
            return new NbtList();
        } else {
            return nbt.getList(INGREDIENTS_KEY, 10);
        }
    }

    /**
     * gets the overall strength of the nbt's ingredient values
     * @param ingredients the nbt ingredients to get the strength of
     * @return the overall strength of the nbt, formatted as an index of {@link LanguageUtil#STRENGTHS}
     */
    public static int getOverallStrength(NbtList ingredients) {
        // protect from / by zero error
        if (ingredients.isEmpty()) {
            return 0;
        }

        int averageStrength = 0;

        for (int i = 0; i < ingredients.size(); i++) {
            NbtCompound ingredient = ingredients.getCompound(i);
            averageStrength += getStrength(ingredient);
        }

        // calculate average strength
        averageStrength /= ingredients.size();
        return averageStrength;
    }

    /**
     * adds an ingredient to the nbt of the given stack
     * @param stack the stack to add the ingredient to
     * @param ingredient the ingredient to add to the stack
     */
    public static void addIngredient(ItemStack stack, NbtCompound ingredient) {
        if (ingredient != null && !ingredient.isEmpty() && ingredient.contains(ID_KEY)) {
            // get nbt
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtList ingredients = getIngredients(nbt);

            // ensure item is in tea ingredient tag
            if (!isTeaIngredient(ingredient)) {
                Constants.LOGGER.warn("attempted to add tea ingredient that is not in tea_ingredients tag; skipping");
                return;
            }

            // update nbt
            updateNbt(ingredient);

            // write nbt
            ingredients.add(ingredient);
            setIngredients(ingredients, nbt);
        }
    }

    /**
     * since flair, strength, and ticks since last strength update can be automatically generated if they are not set,
     * this method sets those values if they are not already present
     * @param ingredient an ingredient to update
     */
    public static void updateNbt(NbtCompound ingredient) {
        if (!ingredient.contains(STRENGTH_KEY)) {
            setStrength(ingredient, 1);
        }

        if (!ingredient.contains(TICKS_SINCE_STRENGTH_INCREASE_KEY)) {
            setTicksSinceStrengthIncrease(ingredient, 0);
        }
    }

    public static boolean isTeaIngredient(NbtCompound ingredient) {
        Identifier id = getIngredientId(ingredient);
        return Registry.ITEM.get(id).getDefaultStack().isIn(Constants.TEA_INGREDIENTS);
    }

    public static boolean containsIngredientKey(NbtCompound nbt) {
        return nbt.contains(INGREDIENTS_KEY);
    }

    public static int getTicksSinceStrengthIncrease(NbtCompound ingredient) {
        return ingredient.getInt(TICKS_SINCE_STRENGTH_INCREASE_KEY);
    }

    public static int getStrength(NbtCompound ingredient) {
        return ingredient.getInt(STRENGTH_KEY);
    }

    public static void setStrength(NbtCompound ingredient, int strength) {
        if (strength > MAX_STRENGTH) {
            return;
        }
        setSafe(nbtCompound -> nbtCompound.putInt(STRENGTH_KEY, strength), ingredient);
    }

    public static void setId(NbtCompound ingredient, Identifier id) {
        setSafe(nbtCompound -> nbtCompound.putString(ID_KEY, id.toString()), ingredient);
    }

    public static void setTicksSinceStrengthIncrease(NbtCompound ingredient, int ticksSinceStrengthIncrease) {
        setSafe(nbtCompound -> nbtCompound.putInt(TICKS_SINCE_STRENGTH_INCREASE_KEY, ticksSinceStrengthIncrease), ingredient);
    }

    private static void setSafeWithReturn(Function<NbtCompound, NbtElement> function, NbtCompound nbt) {
        function.apply(createNbtIfNotPresent(nbt));
    }

    private static void setSafe(Consumer<NbtCompound> function, NbtCompound nbt) {
        function.accept(createNbtIfNotPresent(nbt));
    }

    private static NbtCompound createNbtIfNotPresent(NbtCompound nbt) {
        return Objects.requireNonNullElseGet(nbt, NbtCompound::new);
    }

    public static void setIngredients(NbtList ingredients, NbtCompound nbt) {
        setSafeWithReturn(nbtCompound -> nbtCompound.put(INGREDIENTS_KEY, ingredients), nbt);
    }
}
