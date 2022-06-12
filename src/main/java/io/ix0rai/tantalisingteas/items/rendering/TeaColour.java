package io.ix0rai.tantalisingteas.items.rendering;

import io.ix0rai.tantalisingteas.items.TeaBottle;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

public enum TeaColour {
    // reds
    VERY_LIGHT_RED(255, 0, 0, 0, "very_light_red", 1),
    LIGHT_RED(200, 0, 0, 1, "light_red", 2),
    RED(128, 0, 0, 2, "red", 2),
    DARK_RED(64, 0, 0, 3, "dark_red", 4),
    VERY_DARK_RED(32, 0, 0, 4, "very_dark_red", 5),

    // greens
    VERY_LIGHT_GREEN(0, 255, 0, 5, "very_light_green", 10),
    LIGHT_GREEN(0, 200, 0, 6, "light_green", 9),
    GREEN(0, 128, 0, 7, "green", 9),
    DARK_GREEN(0, 64, 0, 8, "dark_green", 8),
    VERY_DARK_GREEN(0, 32, 0, 9, "very_dark_green", 7),

    OLIVE(128, 128, 0, 10, "olive", 8),

    // blues
    VERY_LIGHT_BLUE(0, 200, 255, 11, "very_light_blue", 6),
    LIGHT_BLUE(0, 128, 255, 12, "light_blue", 5),
    BLUE(0, 0, 255, 13, "blue", 6),
    DARK_BLUE(0, 0, 128, 14, "dark_blue", 5),
    VERY_DARK_BLUE(0, 0, 64, 15, "very_dark_blue", 4),

    CYAN(0, 255, 255, 16, "cyan", 7),
    TEAL(0, 128, 128, 17, "teal", 5),

    // misc
    YELLOW(255, 255, 0, 18, "yellow", 3),
    ORANGE(255, 128, 0, 19, "orange", 3),
    PINK(255, 0, 255, 20, "pink", 3),
    PURPLE(128, 0, 128, 21, "purple", 3),
    BLACK(0, 0, 0, 22, "black", 13),
    WHITE(255, 255, 255, 23, "white", 12),
    BROWN(76, 50, 40, 24, "brown", 11);

    private final int numericalId;
    private final String id;
    private final int red;
    private final int green;
    private final int blue;
    private final int priority;

    TeaColour(int r, int g, int b, int numericalId, String id, int priority) {
        this.numericalId = numericalId;
        this.id = id;
        this.red = r;
        this.green = g;
        this.blue = b;
        this.priority = priority;
    }

    public static TeaColour get(String id) {
        TeaColour colour = null;

        try {
            colour = TeaColour.valueOf(id.toUpperCase());
        // fallback: if there's a typo in an id we don't want to crash the game
        } catch (IllegalArgumentException ignored) {
            for (TeaColour c : TeaColour.values()) {
                if (c.getId().equals(id)) {
                    colour = c;
                    break;
                }
            }
        }

        if (colour == null) {
            throw new IllegalArgumentException("no such TeaColour: " + id);
        }

        return colour;
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
