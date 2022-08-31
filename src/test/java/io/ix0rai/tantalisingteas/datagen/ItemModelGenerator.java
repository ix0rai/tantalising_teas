package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;

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
    static void generateTeaBottleModel() throws IOException {
        AssetGenerator.write(new File(AssetGenerator.ITEM_MODELS + "/tea_bottle.json"), getLatestTeaBottleJson());
    }

    static ItemModelJson getLatestTeaBottleJson() {
        return new ItemModelJson("item/generated", new Textures("minecraft:item/glass_bottle", TantalisingTeas.MOD_ID + ":item/tea_bottle_overlay"), generateLatestOverrides());
    }

    static JsonOverride[] generateLatestOverrides() {
        List<JsonOverride> jsonOverrides = new ArrayList<>();

        for (int i = 0; i < TeaColour.values().length; i ++) {
            TeaColour colour = TeaColour.values()[i];
            for (int strength = 1; strength <= NbtUtil.MAX_STRENGTH; strength ++) {
                jsonOverrides.add(new JsonOverride(new Predicate(colour.getNumericalId(), strength), getName(colour, strength)));
            }
        }

        return jsonOverrides.toArray(new JsonOverride[TeaColour.values().length * NbtUtil.MAX_STRENGTH]);
    }

    static ItemModelJson getJson(TeaColour colour, int strength) {
        return new ItemModelJson("item/generated", new Textures("minecraft:item/glass_bottle", TantalisingTeas.MOD_ID + ":overlay/generated/" + getName(colour, strength)), null);
    }

    static String getName(TeaColour colour, int strength) {
        return colour.getId() + "_s" + strength;
    }

    static void generateTeaColourModels() throws IOException {
        for (TeaColour colour : TeaColour.values()) {
            for (int strength = 1; strength <= NbtUtil.MAX_STRENGTH; strength ++) {
                String modelName = "item/" + getName(colour, strength) + "_tea_model.json";
                File file = new File(AssetGenerator.MODELS + "/" + modelName);

                if (!file.exists()) {
                    // noinspection ResultOfMethodCallIgnored
                    file.getParentFile().mkdirs();
                    // noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                }

                try (FileWriter writer = new FileWriter(file)) {
                    AssetGenerator.GSON.toJson(getJson(colour, strength), writer);
                }
            }
        }
    }

    static class ItemModelJson {
        private final String parent;
        private final Textures textures;
        private final JsonOverride[] overrides;

        public ItemModelJson(String parent, Textures textures, JsonOverride[] overrides) {
            this.parent = parent;
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
