package io.ix0rai.tantalisingteas.items.rendering;

import com.mojang.blaze3d.texture.NativeImage;
import io.ix0rai.tantalisingteas.items.TeaBottle;
import io.ix0rai.tantalisingteas.mixin.render.SpriteAccessor;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * WARNING: IF ADDING ANY VALUES TO THIS ENUM THAT ARE NOT AT THE VERY END, YOU MUST UPDATE THE TEA MODEL JSON FILE OR ELSE COLOURS WILL BE CORRUPTED
 */
public enum TeaColour {
    VERY_LIGHT_RED(255, 0, 0, 1),
    LIGHT_RED(200, 0, 0, 2),
    RED(128, 0, 0, 2),
    DARK_RED(64, 0, 0, 4),
    VERY_DARK_RED(32, 0, 0, 5),
    VERY_LIGHT_GREEN(0, 255, 0, 10),
    LIGHT_GREEN(0, 200, 0, 9),
    GREEN(0, 128, 0, 9),
    DARK_GREEN(0, 64, 0, 8),
    VERY_DARK_GREEN(0, 32, 0, 7),
    OLIVE(128, 128, 0, 8),
    VERY_LIGHT_BLUE(0, 200, 255, 6),
    LIGHT_BLUE(0, 128, 255, 5),
    BLUE(0, 0, 255, 6),
    DARK_BLUE(0, 0, 128, 5),
    VERY_DARK_BLUE(0, 0, 64, 4),
    CYAN(0, 255, 255, 7),
    TEAL(0, 128, 128, 5),
    YELLOW(255, 255, 0, 3),
    ORANGE(255, 128, 0, 3),
    PINK(255, 0, 255, 3),
    PURPLE(128, 0, 128, 3),
    BLACK(0, 0, 0, 13),
    WHITE(255, 255, 255, 12),
    BROWN(76, 50, 40, 11);

    private final int numericalId;
    private final String id;
    private final int red;
    private final int green;
    private final int blue;
    private final int priority;

    TeaColour(int r, int g, int b, int priority) {
        this.numericalId = this.ordinal();
        this.id = this.name().toLowerCase();
        this.red = r;
        this.green = g;
        this.blue = b;
        this.priority = priority;
    }

    public static TeaColour get(String id) {
        return TeaColour.valueOf(id.toUpperCase());
    }

    public static TeaColour getHighestPriority(TeaColour[] colours) {
        TeaColour highestPriority = BLACK;

        if (colours != null) {
            for (TeaColour colour : colours) {
                if (colour != null && colour.priority < highestPriority.priority) {
                    highestPriority = colour;
                }
            }
        }

        return highestPriority;
    }

    private static Map<TeaColour, Integer> getColourOccurrences(NativeImage texture) {
        Map<TeaColour, Integer> colours = new EnumMap<>(TeaColour.class);

        // assemble a map of colours and their number of occurrences
        for (int x = 0; x < texture.getWidth(); x ++) {
            for (int y = 0; y < texture.getHeight(); y ++) {
                int r = texture.getRed(x, y);
                int g = texture.getGreen(x, y);
                int b = texture.getBlue(x, y);

                // calculate transparency
                try {
                    int t = texture.getPixelOpacity(x, y);
                    r -= t;
                    g -= t;
                    b -= t;

                    // filter out entirely transparent pixels
                    if (r == 0 && g == 0 && b == 0) {
                        continue;
                    }
                } catch (IllegalArgumentException ignored) {
                    // thrown if the texture has no alpha channel
                }

                TeaColour colour = TeaColour.getClosest(r, g, b);
                colours.put(colour, colours.getOrDefault(colour, 0) + 1);
            }
        }

        return colours;
    }

    private static void cleanupRareColours(Map<TeaColour, Integer> colours) {
        // get average reoccurrences of colour
        int averageOccurrences = 0;
        for (int number : colours.values()) {
            averageOccurrences += number;
        }
        averageOccurrences /= colours.size() - 1;

        // purge map of rare colours
        Iterator<TeaColour> iterator = colours.keySet().iterator();
        while (iterator.hasNext()) {
            int occurrences = colours.get(iterator.next());
            if (occurrences < averageOccurrences) {
                iterator.remove();
            }
        }
    }

    private static TeaColour[] collectMostSaturatedColours(Map<TeaColour, Integer> colours) {
        // assemble top three most saturated colours
        TeaColour[] mostSaturatedColours = new TeaColour[3];
        boolean full = false;

        for (TeaColour colour : colours.keySet()) {
            if (!full) {
                for (int j = 0; j < mostSaturatedColours.length; j ++) {
                    boolean contains = false;
                    for (int k = 0; k < j; k ++) {
                        if (mostSaturatedColours[k].getId().equals(colour.getId())) {
                            contains = true;
                            break;
                        }
                    }

                    if (mostSaturatedColours[j] == null && !contains) {
                        mostSaturatedColours[j] = colour;
                        if (j == mostSaturatedColours.length - 1) {
                            full = true;
                        }
                        break;
                    }
                }
            }

            for (int j = 0; j < mostSaturatedColours.length; j ++) {
                if (mostSaturatedColours[j] != null && colour != null && colour.getRgbSum() > mostSaturatedColours[j].getRgbSum()) {
                    mostSaturatedColours[j] = colour;
                    break;
                }
            }
        }

        return mostSaturatedColours;
    }

    /**
     * {@code WARNING: RATHER CURSED}
     * <br> runs through all ingredients in the given stack and ensures that each ingredient has a colour
     * <br> if an ingredient has no colour, it will be assigned what we determine to be the primary colour of the ingredient
     * <br>
     * <br> how this works:
     * <br> 1. get the ingredient's texture using its id and the providided {@link BakedModelManager}
     * <br> 2. run over the full texture and get all colours used, associating with them their number of occurrences
     * <br> 3. purge the map of rare colours, defined as colours that occur less than the average number of occurrences
     * <br> 4. get the top three most saturated colours - these are most likely to be important to the texture
     * <br> 5. assign the ingredient the colour with the highest priority of the top three most saturated colours
     *
     * @param stack the stack to update the data of
     * @param manager a {@link BakedModelManager} to pull the ingredients' textures from
     */
    public static void updateColourValues(ItemStack stack, BakedModelManager manager) {
        NbtCompound nbt = stack.getNbt();
        assert nbt != null;

        NbtList ingredients = nbt.getList(TeaBottle.INGREDIENTS_KEY, 10);
        boolean updated = false;

        for (int i = 0; i < ingredients.size(); i ++) {
            NbtCompound ingredientNbt = ingredients.getCompound(i);
            if (!ingredientNbt.contains(TeaBottle.COLOUR_KEY)) {
                Identifier id = new Identifier(ingredientNbt.getString(TeaBottle.ID_KEY));

                ModelIdentifier modelId = new ModelIdentifier(id + "#inventory");
                BakedModel model = manager.getModel(modelId);

                NativeImage texture = ((SpriteAccessor) model.getParticleSprite()).getImages()[0];
                Map<TeaColour, Integer> colours = getColourOccurrences(texture);

                cleanupRareColours(colours);
                TeaColour[] mostSaturatedColours = collectMostSaturatedColours(colours);

                // save colour
                TeaColour highestPriority = TeaColour.getHighestPriority(mostSaturatedColours);
                ingredientNbt.putString(TeaBottle.COLOUR_KEY, highestPriority.getId());
                updated = true;
            }
        }

        if (updated) {
            nbt.put(TeaBottle.INGREDIENTS_KEY, ingredients);
            nbt.remove(TeaBottle.NEEDS_UPDATE_KEY);
            stack.setNbt(nbt);
        }
    }

    public static TeaColour getFromIngredients(List<NbtCompound> ingredients) {
        int[] averageRgb = new int[]{0, 0, 0};

        for (NbtCompound ingredient : ingredients) {
            TeaColour colour = TeaColour.get(ingredient.getString(TeaBottle.COLOUR_KEY));
            averageRgb[0] += colour.red;
            averageRgb[1] += colour.green;
            averageRgb[2] += colour.blue;
        }

        for (int i = 0; i < averageRgb.length; i++) {
            averageRgb[i] /= ingredients.size();
        }

        return getClosest(averageRgb[0], averageRgb[1], averageRgb[2]);
    }

    public static TeaColour getClosest(int r, int g, int b) {
        TeaColour closest = BLACK;

        for (TeaColour colour : TeaColour.values()) {
            if (colour != closest) {
                int redDiff = Math.abs(colour.red - r);
                int greenDiff = Math.abs(colour.green - g);
                int blueDiff = Math.abs(colour.blue - b);

                int totalDiff = redDiff + greenDiff + blueDiff;

                if (totalDiff < closest.getTotalDiff(r, g, b)) {
                    closest = colour;
                }
            }
        }

        return closest;
    }

    public int getRgbSum() {
        return red + green + blue;
    }

    private int getTotalDiff(int r, int g, int b) {
        return Math.abs(this.red - r) + Math.abs(this.green - g) + Math.abs(this.blue - b);
    }

    public String getId() {
        return this.id;
    }

    public int getNumericalId() {
        return this.numericalId;
    }
}
