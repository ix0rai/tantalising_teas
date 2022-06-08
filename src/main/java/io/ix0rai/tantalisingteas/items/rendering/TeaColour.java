package io.ix0rai.tantalisingteas.items.rendering;

import io.ix0rai.tantalisingteas.items.TeaBottle;
import net.minecraft.nbt.NbtCompound;

import java.util.List;

public enum TeaColour {
    // reds
    VERY_LIGHT_RED("very_light_red", 0xff0000, 1),
    LIGHT_RED("light_red", 0xbf0000, 2),
    RED("red", 0x800000, 2),
    DARK_RED("dark_red", 0x400000, 2),
    VERY_DARK_RED("very_dark_red", 0x200000, 2),

    // greens
    VERY_LIGHT_GREEN("very_light_green", 0x00ff00, 10),
    LIGHT_GREEN("light_green", 0x00bf00, 9),
    GREEN("green", 0x008000, 9),
    DARK_GREEN("dark_green", 0x004000, 8),
    VERY_DARK_GREEN("very_dark_green", 0x002000, 7),

    OLIVE("olive", 0x808000, 8),

    // blues
    VERY_LIGHT_BLUE("very_light_blue", 0x00c8ff, 6),
    LIGHT_BLUE("light_blue", 0x0080ff, 5),
    BLUE("blue", 0x0000ff, 6),
    DARK_BLUE("dark_blue", 0x000080, 5),
    VERY_DARK_BLUE("very_dark_blue", 0x000040, 4),

    CYAN("cyan", 0x00ffff, 7),
    TEAL("teal", 0x008080, 5),

    // misc
    YELLOW("yellow", 0xffff00, 5),
    ORANGE("orange", 0xff8000, 3),
    PINK("pink", 0xff00ff, 3),
    PURPLE("purple", 0x800080, 3),
    BLACK("black", 0x000000, 13),
    WHITE("white", 0xffffff, 12),
    BROWN("brown", 0xa52a2a, 11);

    private final String id;
    private final int hex;
    private final int red;
    private final int green;
    private final int blue;
    private final int priority;

    TeaColour(String id, int hex, int priority) {
        this.id = id;
        this.hex = hex;
        this.red = (hex >> 16) & 0xff;
        this.green = (hex >> 8) & 0xff;
        this.blue = hex & 0xff;
        this.priority = priority;
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

    private static TeaColour fromHex(int hex) {
        return getClosest((hex >> 16) & 0xff, (hex >> 8) & 0xff, hex & 0xff);
    }

    public static TeaColour getFromIngredients(List<NbtCompound> ingredients) {
        int[] averageRgb = new int[]{0, 0, 0};

        for (NbtCompound ingredient : ingredients) {
            TeaColour colour = TeaColour.fromHex(ingredient.getInt(TeaBottle.COLOUR_KEY));
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

    public int getHex() {
        return this.hex;
    }

    public String getId() {
        return this.id;
    }
}