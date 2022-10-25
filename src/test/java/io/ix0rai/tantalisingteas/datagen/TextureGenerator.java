package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.util.NbtUtil;
import io.ix0rai.tantalisingteas.util.TeaColourUtil;
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
            // get paths
            String path = AssetGenerator.OVERLAY_GENERATED + "/" + colour.getId();
            String sourcePath = AssetGenerator.TEMPLATES + "/grayscale_tea";

            for (int strength = 1; strength <= NbtUtil.MAX_STRENGTH; strength ++) {
                // get images
                BufferedImage sourceImage = ImageIO.read(new File(sourcePath + "." + FORMAT));
                BufferedImage newImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

                int alpha = TeaColourUtil.getAlpha(strength);
                applyPixelTransforms(sourceImage, newImage, (argb) -> {
                    int r = argb.r;
                    int g = argb.g;
                    int b = argb.b;

                    // checks if the pixel is grayscale
                    if (Objects.equals(r, g) && Objects.equals(g, b)) {
                        // decrease the colour strength by half of the maximum rgb value
                        int subtract = 255 / 2;
                        r -= subtract;
                        g -= subtract;
                        b -= subtract;

                        // modify the colour according to the passed TeaColour
                        // we also subtract 40 to make the colours more muted
                        r = MathHelper.clamp(r + colour.getRed() - 40, 0, 255);
                        g = MathHelper.clamp(g + colour.getGreen() - 40, 0, 255);
                        b = MathHelper.clamp(b + colour.getBlue() - 40, 0, 255);
                    }

                    // return the new colour
                    return new ARGB(alpha, r, g, b);
                });

                // write the image to a file
                File file = new File(path + "_s" + strength + "." + FORMAT);
                writeImage(file, newImage);
            }
        }
    }

    static void generateCauldronOverlays() throws IOException {
        for (TeaColour colour : TeaColour.values()) {
            for (int strength = 0; strength <= NbtUtil.MAX_STRENGTH; strength++) {
                // get paths
                String path = AssetGenerator.CAULDRON_TEXTURES_GENERATED + "/" + colour.getId() + "_tea_cauldron_s" + strength;
                String sourcePath = AssetGenerator.TEMPLATES + "/water_texture";

                // get images
                BufferedImage sourceImage = ImageIO.read(new File(sourcePath + "." + FORMAT));
                BufferedImage newImage = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

                // modify alpha and colour
                int alpha = TeaColourUtil.getAlpha(strength);
                applyPixelTransforms(sourceImage, newImage, (argb) -> {
                    int r = argb.r;
                    int g = argb.g;
                    int b = argb.b;

                    // checks if the pixel is grayscale
                    if (Objects.equals(r, g) && Objects.equals(g, b)) {
                        // decrease the colour strength by half of the maximum rgb value
                        int subtract = 255 / 2;
                        r -= subtract;
                        g -= subtract;
                        b -= subtract;

                        // modify the colour according to the passed TeaColour
                        r = MathHelper.clamp(r + colour.getRed(), 0, 255);
                        g = MathHelper.clamp(g + colour.getGreen(), 0, 255);
                        b = MathHelper.clamp(b + colour.getBlue(), 0, 255);
                    }

                    // return the new colour
                    return new ARGB(alpha, r, g, b);
                });

                // write image
                writeImage(new File(path + "." + FORMAT), newImage);
                // write mcmeta (identical for all files)
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

    static void applyPixelTransforms(BufferedImage sourceImage, BufferedImage resultImage, RgbTransformer transformer) {
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

                    ARGB newArgb = transformer.transform(new ARGB(alpha, red, green, blue));
                    int newRgb = (newArgb.a << 24) | (newArgb.r << 16) | (newArgb.g << 8) | newArgb.b;

                    // set the rgb of the pixel with the new rgb
                    resultImage.setRGB(x, y, newRgb);
                }
            }
        }
    }

    private interface RgbTransformer {
        ARGB transform(ARGB argb);
    }

    private record ARGB(int a, int r, int g, int b) {
    }

    private static void writeImage(File file, BufferedImage image) throws IOException {
        // noinspection ResultOfMethodCallIgnored aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
        file.getParentFile().mkdirs();
        // noinspection ResultOfMethodCallIgnored aaaaaaaaaaaaaaaaaaaaaaaaaaaaaab
        file.createNewFile();
        ImageIO.write(image, FORMAT, file);
    }
}
