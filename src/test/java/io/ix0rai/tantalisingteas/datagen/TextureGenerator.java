package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import oshi.util.tuples.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextureGenerator {
    static void generateTeaOverlays() throws IOException {
        final List<Pair<Integer, Integer>> doNotSet = new ArrayList<>();
        doNotSet.add(new Pair<>(10, 10));
        doNotSet.add(new Pair<>(6, 12));

        for (TeaColour colour : TeaColour.values()) {
            String path = AssetGenerator.OVERLAY_GENERATED + "/" + colour.getId();
            String sourcePath = AssetGenerator.OVERLAY + "/" + colour.getId();
            String format = "png";

            BufferedImage originalImage = ImageIO.read(new File(sourcePath + "." + format));
            BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

            // we don't talk about it.
            // we really don't.
            for (int strength = 1; strength <= NbtUtil.MAX_STRENGTH; strength ++) {
                int alpha = TeaColour.getAlpha(strength);

                // iterate over every pixel in the image
                for (int x = 0; x < originalImage.getWidth(); x++) {
                    for (int y = 0; y < originalImage.getHeight(); y++) {
                        if (!doNotSet.contains(new Pair<>(x, y))) {
                            int rgb = originalImage.getRGB(x, y);

                            // ensure that we don't set the rgb of already-transparent pixels
                            if (rgb != 0) {
                                // we need to reconstruct the entire rgb value so that we can modify the alpha
                                int blue = rgb & 0x00ff;
                                int green = (rgb & 0x00ff00) >> 8;
                                int red = (rgb & 0x00ff0000) >> 16;

                                int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;

                                // set the rgb of the pixel with the new rgb
                                newImage.setRGB(x, y, newRgb);
                            }
                        }
                    }
                }

                ImageIO.write(newImage, format, new File(path + "_s" + strength + "." + format));
            }
        }
    }
}
