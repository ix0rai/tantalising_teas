package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.util.Constants;
import io.ix0rai.tantalisingteas.util.NbtUtil;

import java.io.File;
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
    static void generateTeaBottleModel() throws IOException {
        // generate overrides for each individual colour / strength
        int overrideAmount = TeaColour.values().length * NbtUtil.MAX_STRENGTH;
        List<JsonOverride> jsonOverrides = new ArrayList<>();

        for (int i = 0; i < TeaColour.values().length; i ++) {
            TeaColour colour = TeaColour.values()[i];
            for (int strength = 1; strength <= NbtUtil.MAX_STRENGTH; strength ++) {
                jsonOverrides.add(new JsonOverride(new Predicate(colour.ordinal(), strength), getName(colour, strength)));
            }
        }

        // create json
        ItemModelJson teaBottleJson = new ItemModelJson(
                new Textures("minecraft:item/glass_bottle", Constants.MOD_ID + ":template/grayscale_tea"),
                jsonOverrides.toArray(new JsonOverride[overrideAmount])
        );

        // create file and write
        File file = new File(AssetGenerator.ITEM_MODELS_ROOT + "/tea_bottle.json");
        AssetGenerator.write(file, teaBottleJson);
    }

    static void generateTeaColourModels() throws IOException {
        for (TeaColour colour : TeaColour.values()) {
            for (int strength = 1; strength <= NbtUtil.MAX_STRENGTH; strength ++) {
                String modelName = getName(colour, strength) + "_tea_model.json";

                File file = new File(AssetGenerator.ITEM_MODELS + "/" + modelName);
                ItemModelJson json = new ItemModelJson(
                        new Textures("minecraft:item/glass_bottle", Constants.MOD_ID + ":generated/overlay/" + getName(colour, strength)),
                        null
                );

                AssetGenerator.write(file, json);
            }
        }
    }

    static String getName(TeaColour colour, int strength) {
        return colour.getId() + "_s" + strength;
    }

    static class ItemModelJson {
        private final String parent;
        private final Textures textures;
        private final JsonOverride[] overrides;

        public ItemModelJson(Textures textures, JsonOverride[] overrides) {
            this.parent = "item/generated";
            this.textures = textures;
            this.overrides = overrides;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemModelJson that = (ItemModelJson) o;
            return Objects.equals(parent, that.parent) && Objects.equals(textures, that.textures) && Arrays.equals(overrides, that.overrides);
        }
    }

    // classes cannot be records because gson cannot decode to a record

    @SuppressWarnings({"unused", "ClassCanBeRecord"})
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

    @SuppressWarnings({"unused", "ClassCanBeRecord"})
    private static class JsonOverride {
        private final Predicate predicate;
        private final String model;

        private JsonOverride(Predicate predicate, String model) {
            this.predicate = predicate;
            this.model = Constants.MOD_ID + ":generated/item/" + model + "_tea_model";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JsonOverride that = (JsonOverride) o;
            return Objects.equals(predicate, that.predicate) && Objects.equals(model, that.model);
        }
    }

    @SuppressWarnings({"unused", "ClassCanBeRecord"})
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
