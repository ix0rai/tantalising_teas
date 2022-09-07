package io.ix0rai.tantalisingteas.data;

import com.mojang.blaze3d.texture.NativeImage;
import io.ix0rai.tantalisingteas.client.TantalisingTeasClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.MathHelper;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

public class TeaColourUtil {
    /**
     * @param colours an array of tea colours to analyse
     * @return the tea colour with the highest configured priority. this is manually configured in the enum.
     */
    public static TeaColour getHighestPriority(TeaColour[] colours) {
        TeaColour highestPriority = colours[0];

        for (TeaColour colour : colours) {
            if (colour != null && colour.getPriority() < highestPriority.getPriority()) {
                highestPriority = colour;
            }
        }

        return highestPriority;
    }

    /**
     * runs over a texture and finds the most common colours
     * @param texture the texture to analyse
     * @return a map of colours and their occurrences, having been converted to {@link TeaColour}s
     */
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

                TeaColour colour = getClosest(r, g, b);
                colours.put(colour, colours.getOrDefault(colour, 0) + 1);
            }
        }

        return colours;
    }

    /**
     * removes colours from the map with less than the average number of occurrences
     * @param colours a map of colours and their number of occurrences
     */
    public static void cleanupRareColours(Map<TeaColour, Integer> colours) {
        // get average reoccurrences of colour
        int averageOccurrences = 0;
        for (int number : colours.values()) {
            averageOccurrences += number;
        }
        averageOccurrences /= MathHelper.clamp(colours.size() - 1, 1, Integer.MAX_VALUE);

        // purge map of rare colours
        Iterator<TeaColour> iterator = colours.keySet().iterator();
        while (iterator.hasNext()) {
            int occurrences = colours.get(iterator.next());
            if (occurrences < averageOccurrences) {
                iterator.remove();
            }
        }
    }

    /**
     * checks over a map of tea colours and their occurrences and finds the most saturated colours
     * @param colours a map of colours and their number of occurrences to pull from
     *                <p>occurrences is unused but is there for convenience as this method is only used once.
     *                the api can be cleaned up later when I have a reason to</p>
     * @return the three colours in the map that have the highest RGB values
     */
    public static TeaColour[] collectMostSaturatedColours(Map<TeaColour, Integer> colours) {
        // collect a list of three random colours from the map
        TeaColour[] mostSaturatedColours = colours.keySet().toArray(new TeaColour[3]);

        int counter = 0;
        for (TeaColour colour : colours.keySet()) {
            // if the colour is more saturated than the current colour, replace it
            if (counter < 3) {
                if (colour.getRgbSum() > mostSaturatedColours[counter].getRgbSum()) {
                    mostSaturatedColours[counter] = colour;
                }

                counter ++;
            } else {
                // otherwise iterate over the whole list of saturated colours to see if our colour should replace any of them
                for (int i = 0; i < mostSaturatedColours.length; i++) {
                    if (colour.getRgbSum() > mostSaturatedColours[i].getRgbSum()) {
                        mostSaturatedColours[i] = colour;
                    }
                }
            }
        }

        // return three colours with highest rgb sums
        return mostSaturatedColours;
    }

    /**
     * gets a colour that represents the given list of ingredients
     * @param ingredients the list of ingredients to pull the colours from
     * @return the closest colour to the average of the given ingredients' rgb values
     */
    public static TeaColour getFromIngredients(NbtList ingredients) {
        // fallback colour is black
        if (ingredients.isEmpty()) {
            return TeaColour.BLACK;
        }

        int[] averageRgb = new int[]{0, 0, 0};

        // get the average rgb values of each ingredient in the list
        for (int i = 0; i < ingredients.size(); i ++) {
            NbtCompound ingredient = ingredients.getCompound(i);
            TeaColour colour = TantalisingTeasClient.ITEM_COLOURS.get(NbtUtil.getIngredientId(ingredient));

            averageRgb[0] += colour.getRed();
            averageRgb[1] += colour.getGreen();
            averageRgb[2] += colour.getBlue();
        }

        for (int i = 0; i < averageRgb.length; i++) {
            averageRgb[i] /= ingredients.size();
        }

        // return the closest tea colour to the averages
        return getClosest(averageRgb[0], averageRgb[1], averageRgb[2]);
    }

    private static TeaColour getClosest(int r, int g, int b) {
        // fallback colour is black
        TeaColour closest = TeaColour.BLACK;

        // get the colour with the minimum total difference between its rgb values and the given rgb values
        for (TeaColour colour : TeaColour.values()) {
            if (colour != closest && colour.getTotalDiff(r, g, b) < closest.getTotalDiff(r, g, b)) {
                closest = colour;
            }
        }

        return closest;
    }

    public static int getAlpha(int strength) {
        return (int) (255 / (Math.abs(strength - NbtUtil.MAX_STRENGTH) + (strength != NbtUtil.MAX_STRENGTH ? 0.5 : 0)));
    }
}
