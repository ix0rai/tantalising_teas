package io.ix0rai.tantalisingteas.data;

import com.mojang.blaze3d.texture.NativeImage;
import io.ix0rai.tantalisingteas.client.TantalisingTeasClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.security.InvalidParameterException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

public enum TeaColour {
    VERY_LIGHT_RED(255, 0, 0, 1, 0),
    LIGHT_RED(200, 0, 0, 2, 1),
    RED(128, 0, 0, 2, 2),
    DARK_RED(64, 0, 0, 4, 3),
    VERY_DARK_RED(32, 0, 0, 5, 4),
    VERY_LIGHT_GREEN(0, 255, 0, 10, 5),
    LIGHT_GREEN(0, 200, 0, 9, 6),
    GREEN(0, 128, 0, 9, 7),
    DARK_GREEN(0, 64, 0, 8, 8),
    VERY_DARK_GREEN(0, 32, 0, 7, 9),
    OLIVE(128, 128, 0, 8, 10),
    VERY_LIGHT_BLUE(0, 200, 255, 6, 11),
    LIGHT_BLUE(0, 128, 255, 5, 12),
    BLUE(0, 0, 255, 6, 13),
    DARK_BLUE(0, 0, 128, 5, 14),
    VERY_DARK_BLUE(0, 0, 64, 4, 15),
    CYAN(0, 255, 255, 7, 16),
    TEAL(0, 128, 128, 5, 17),
    YELLOW(255, 255, 0, 3, 18),
    ORANGE(255, 128, 0, 3, 19),
    PINK(255, 0, 255, 3, 20),
    PURPLE(128, 0, 128, 3, 21),
    BLACK(0, 0, 0, 13, 22),
    WHITE(255, 255, 255, 12, 23),
    BROWN(76, 50, 40, 11, 24);

    private final int numericalId;
    private final int red;
    private final int green;
    private final int blue;
    private final int priority;

    TeaColour(int r, int g, int b, int priority, int numericalId) {
        this.numericalId = numericalId;
        this.red = r;
        this.green = g;
        this.blue = b;
        this.priority = priority;
    }

    static {
        // validate all tea colours to ensure that their ids are unique -- we don't want the game to run if the list is misconfigured
        for (TeaColour colour : TeaColour.values()) {
            for (TeaColour colour2 : TeaColour.values()) {
                if (colour.numericalId == colour2.numericalId && !colour.getId().equals(colour2.getId())) {
                    throw new InvalidParameterException("cannot have two TeaColours with the same numerical id "
                            + "(offenders: " + colour.getId() + ", " + colour.numericalId + " and " + colour2.getId() + ", " + colour2.numericalId);
                }
            }
        }
    }

    public static TeaColour getHighestPriority(TeaColour[] colours) {
        TeaColour highestPriority = colours[0];

        for (TeaColour colour : colours) {
            if (colour != null && colour.priority < highestPriority.priority) {
                highestPriority = colour;
            }
        }

        return highestPriority;
    }

    public static Map<TeaColour, Integer> getColourOccurrences(NativeImage texture) {
        Map<TeaColour, Integer> colours = new EnumMap<>(TeaColour.class);

        // assemble a map of colours and their number of occurrences
        for (int x = 0; x < texture.getWidth(); x ++) {
            for (int y = 0; y < texture.getHeight(); y ++) {
                // get the colour of the pixels and convert them to 0 - 255 values
                int r = texture.getRed(x, y) & 0xFF;
                int g = texture.getGreen(x, y) & 0xFF;
                int b = texture.getBlue(x, y) & 0xFF;

                // calculate transparency
                try {
                    // ignore really light pixels
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

    public static void cleanupRareColours(Map<TeaColour, Integer> colours) {
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

    public static TeaColour[] collectMostSaturatedColours(Map<TeaColour, Integer> colours) {
        // assemble top three most saturated colours
        TeaColour[] mostSaturatedColours = new TeaColour[3];
        boolean full = false;

        for (TeaColour colour : colours.keySet()) {

            // ensure colour is not null
            if (colour != null) {
                for (int i = 0; i < mostSaturatedColours.length; i++) {
                    if (!full && mostSaturatedColours[i] == null) {
                        boolean contains = false;
                        for (int k = 0; k < i; k++) {
                            if (mostSaturatedColours[k] != null && mostSaturatedColours[k].getId().equals(colour.getId())) {
                                contains = true;
                                break;
                            }
                        }

                        if (!contains) {
                            mostSaturatedColours[i] = colour;
                            if (i == mostSaturatedColours.length - 1) {
                                full = true;
                            }
                        }
                    }

                    if (mostSaturatedColours[i] != null && colour.getRgbSum() > mostSaturatedColours[i].getRgbSum()) {
                        mostSaturatedColours[i] = colour;
                        break;
                    }
                }
            }
        }

        return mostSaturatedColours;
    }

    /**
     * gets a colour that represents the given list of ingredients
     * @param ingredients the list of ingredients to pull the colours from
     * @return the closest colour to the average of the given ingredients' rgb values
     */
    public static TeaColour getFromIngredients(NbtList ingredients) {
        if (ingredients.isEmpty()) {
            return TeaColour.BLACK;
        }

        int[] averageRgb = new int[]{0, 0, 0};

        for (int i = 0; i < ingredients.size(); i ++) {
            NbtCompound ingredient = ingredients.getCompound(i);
            TeaColour colour = TantalisingTeasClient.ITEM_COLOURS.get(NbtUtil.getIngredientId(ingredient));

            averageRgb[0] += colour.red;
            averageRgb[1] += colour.green;
            averageRgb[2] += colour.blue;
        }

        for (int i = 0; i < averageRgb.length; i++) {
            averageRgb[i] /= ingredients.size();
        }

        return getClosest(averageRgb[0], averageRgb[1], averageRgb[2]);
    }

    private static TeaColour getClosest(int r, int g, int b) {
        TeaColour closest = BLACK;

        for (TeaColour colour : TeaColour.values()) {
            if (colour != closest && colour.getTotalDiff(r, g, b) < closest.getTotalDiff(r, g, b)) {
                closest = colour;
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

    public String getHex(int strength) {
        return String.format("%02x%02x%02x%02x", getAlpha(strength), red, green, blue);
    }

    public static int getAlpha(int strength) {
        return (int) (255 / (Math.abs(strength - NbtUtil.MAX_STRENGTH) + (strength != NbtUtil.MAX_STRENGTH ? 0.5 : 0)));
    }

    public String getId() {
        return this.name().toLowerCase();
    }

    public int getNumericalId() {
        return this.numericalId;
    }

    @Override
    public String toString() {
        return this.getId();
    }
}
