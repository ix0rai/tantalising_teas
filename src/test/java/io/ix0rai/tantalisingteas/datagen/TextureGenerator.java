package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import net.minecraft.util.math.MathHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class TextureGenerator {
    private static final String FORMAT = "png";

    static void generateTeaOverlays() throws IOException {
        for (TeaColour colour : TeaColour.values()) {
            String path = AssetGenerator.OVERLAY_GENERATED + "/" + colour.getId();
            String sourcePath = AssetGenerator.OVERLAY_SOURCE + "/" + colour.getId();

            for (int strength = 1; strength <= NbtUtil.MAX_STRENGTH; strength++) {
                BufferedImage newImage = withAlpha(ImageIO.read(new File(sourcePath + "." + FORMAT)), TeaColour.getAlpha(strength));

                // write the image to a file
                File file = new File(path + "_s" + strength + "." + FORMAT);
                writeImage(file, newImage);
            }
        }
    }

    // todo: alpha
    static void generateCauldronOverlays() throws IOException {
        for (TeaColour colour : TeaColour.values()) {
            for (int s = 0; s < NbtUtil.MAX_STRENGTH; s++) {
                String path = AssetGenerator.CAULDRON_TEXTURES + "/" + colour.getId() + "_tea_cauldron_s" + s;
                String sourcePath = AssetGenerator.CAULDRON_TEXTURES_SOURCE + "/water_texture";

                BufferedImage sourceImage = ImageIO.read(new File(sourcePath + "." + FORMAT));
                BufferedImage newImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

                applyPixelTransforms(sourceImage, newImage, (a, r, g, b) -> {
                    if (Objects.equals(r, g) && Objects.equals(g, b)) {
                        int subtract = 255 / 2;
                        r -= subtract;
                        g -= subtract;
                        b -= subtract;

                        r = MathHelper.clamp(r + colour.getRed(), 0, 255);
                        g = MathHelper.clamp(g + colour.getGreen(), 0, 255);
                        b = MathHelper.clamp(b + colour.getBlue(), 0, 255);
                    }

                    return (a << 24) | (r << 16) | (g << 8) | b;
                });

                writeImage(new File(path + "." + FORMAT), withAlpha(newImage, TeaColour.getAlpha(s)));
                try (FileWriter writer = new FileWriter(path + "." + FORMAT + ".mcmeta")) {
                    writer.write("""
                            {
                              "animation": {
                                "frametime": 2
                              }
                            }
                            
                            """
                    );
                }
            }
        }
    }

    private interface RgbTransformer<A, R, G, B> {
        int accept(A a, R r, G g, B b);
    }

    static void applyPixelTransforms(BufferedImage sourceImage, BufferedImage resultImage, RgbTransformer<Integer, Integer, Integer, Integer> consumer) {
        // iterate over every pixel in the image
        for (int x = 0; x < sourceImage.getWidth(); x++) {
            for (int y = 0; y < sourceImage.getHeight(); y++) {
                int rgb = sourceImage.getRGB(x, y);

                // ensure that we don't mess with already-transparent pixels
                if (rgb != 0) {
                    // we need to reconstruct the entire rgb value so that we can modify it
                    int alpha = rgb & 0xff;
                    int blue = rgb & 0x00ff;
                    int green = (rgb & 0x00ff00) >> 8;
                    int red = (rgb & 0x00ff0000) >> 16;

                    int newRgb = consumer.accept(alpha, red, green, blue);

                    // set the rgb of the pixel with the new rgb
                    resultImage.setRGB(x, y, newRgb);
                }
            }
        }
    }

    private static void writeImage(File file, BufferedImage image) throws IOException {
        if (!file.exists()) {
            // noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            // noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            ImageIO.write(image, FORMAT, file);
        }
    }

    private static BufferedImage withAlpha(BufferedImage image, int alpha) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        applyPixelTransforms(image, newImage, (a, r, g, b) -> (alpha << 24) | (r << 16) | (g << 8) | b);

        return newImage;
    }

    // todo
    // todo texture info: r = g = b on water overlay grayscale
    // todo shouldn't be too hard to modify rgb, preserve alpha and write
}
