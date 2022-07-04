package io.ix0rai.tantalisingteas.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import net.minecraft.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * generator for item model data
 * <p> runs on gradle build and creates individual tea item models and the full tea bottle model </p>
 * <p> this ensures that json data for tea models will always be up to date with code changes </p>
 */
public class ItemModelGenerator {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final String SRC = "src";
    static final String MAIN = SRC + "/" + "main";
    static final String TEST = SRC + "/" + "test";
    static final String ASSETS = MAIN + "/resources/assets/" + TantalisingTeas.MOD_ID;
    static final String MODELS = ASSETS + "/models";
    static final String TEXTURES = ASSETS + "/textures";

    public static void main(String[] args) throws IOException {
        generateTeaColourModels();
        generateAlternateTransparencyImages();
        generateTeaBottleModel();
    }

    private static void generateAlternateTransparencyImages() {
        for (TeaColour colour : TeaColour.values()) {
            for (int i = 1; i < NbtUtil.MAX_STRENGTH; i ++) {
                try {
                    String path = TEXTURES + "/tea_overlay/" + colour.getId();
                    String format = "png";
                    File file = new File(path + "." + format);
                    BufferedImage image = ImageIO.read(file);

                    // we don't talk about it.
                    // we really don't.
                    int alpha = (int) (255 / (Math.abs(i - NbtUtil.MAX_STRENGTH) + 0.5));
                    for (int x = 0; x < image.getWidth(); x ++) {
                        for (int y = 0; y < image.getHeight(); y ++) {
                            int rgb = image.getRGB(x, y);

                            // ensure that we don't set the rgb of already-transparent pixels
                            if (rgb != 0) {
                                // we need to reconstruct the entire rgb value so that we can modify the alpha
                                int blue = rgb & 0x00ff;
                                int green = (rgb & 0x00ff00) >> 8;
                                int red = (rgb & 0x00ff0000) >> 16;

                                int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;

                                // set the rgb of the pixel with the new rgb
                                image.setRGB(x, y, newRgb);
                            }
                        }
                    }

                    ImageIO.write(image, format, new File(path + "_s" + i + "." + format));
                } catch (IOException ignored) {
                    // this just means we either can't find the source image or can't save the image
                    // in either case we expect the image to be generated manually later
                }
            }
        }
    }

    private static void generateTeaBottleModel() throws IOException {
        File file = new File(MODELS + "/item/tea_bottle.json");

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(getLatestTeaBottleJson(), writer);
        }
    }

    static ItemModelJson getLatestTeaBottleJson() {
        return new ItemModelJson("item/generated", new Textures("minecraft:item/glass_bottle", TantalisingTeas.MOD_ID + ":item/tea_bottle_overlay"), generateLatestOverrides());
    }

    static List<JsonOverride> generateLatestOverrides() {
        List<JsonOverride> jsonOverrides = new ArrayList<>();

        for (int i = 0; i < TeaColour.values().length; i ++) {
            TeaColour colour = TeaColour.values()[i];
            jsonOverrides.add(new JsonOverride(new Predicate(colour.getNumericalId(), NbtUtil.MAX_STRENGTH), colour.getId()));
            for (int strength = 1; strength < NbtUtil.MAX_STRENGTH; strength ++) {
                jsonOverrides.add(new JsonOverride(new Predicate(colour.getNumericalId(), strength), getName(colour, strength)));
            }
        }

        return jsonOverrides;
    }

    static ItemModelJson getJson(TeaColour colour, int strength) {
        return new ItemModelJson("item/generated", new Textures("minecraft:item/glass_bottle", TantalisingTeas.MOD_ID + ":tea_overlay/" + getName(colour, strength)), null);
    }

    static String getName(TeaColour colour, int strength) {
        return colour.getId() + (strength != NbtUtil.MAX_STRENGTH ? "_s" + strength : "");
    }

    private static void generateTeaColourModels() throws IOException {
        for (TeaColour colour : TeaColour.values()) {
            for (int strength = 1; strength <= NbtUtil.MAX_STRENGTH; strength ++) {
                String modelName = "item/" + getName(colour, strength) + "_tea_model.json";
                File file = new File(MODELS + "/" + modelName);

                if (file.createNewFile()) {
                    try (FileWriter writer = new FileWriter(file)) {
                        GSON.toJson(getJson(colour, strength), writer);
                    }
                }
            }
        }
    }

    static class ItemModelJson {
        private final String parent;
        private final Textures textures;
        private final JsonOverride[] overrides;

        public ItemModelJson(String parent, Textures textures, List<JsonOverride> overrides) {
            this.parent = parent;
            this.textures = textures;
            this.overrides = overrides == null ? null : overrides.toArray(new JsonOverride[TeaColour.values().length * NbtUtil.MAX_STRENGTH]);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemModelJson that = (ItemModelJson) o;
            return Objects.equals(parent, that.parent) && Objects.equals(textures, that.textures) && Arrays.equals(overrides, that.overrides);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(parent, textures);
            result = 31 * result + Arrays.hashCode(overrides);
            return result;
        }
    }

    private static final class Textures {
        private final String layer0;
        private final String layer1;

        public Textures(String layer0, String layer1) {
            this.layer0 = layer0;
            this.layer1 = layer1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Textures textures = (Textures) o;
            return Objects.equals(layer0, textures.layer0) && Objects.equals(layer1, textures.layer1);
        }
    }

    private static class JsonOverride {
        private final Predicate predicate;
        private final String model;

        private JsonOverride(Predicate predicate, String model) {
            this.predicate = predicate;
            this.model = TantalisingTeas.MOD_ID + ":item/" + model + "_tea_model";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonOverride that = (JsonOverride) o;
            return Objects.equals(predicate, that.predicate) && Objects.equals(model, that.model);
        }
    }

    private static final class Predicate {
        private final int id;
        private final int strength;

        public Predicate(int id, int strength) {
            this.id = id;
            this.strength = strength;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Predicate predicate = (Predicate) o;
            return id == predicate.id && strength == predicate.strength;
        }
    }
}
