package io.ix0rai.tantalisingteas.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.items.rendering.TeaColour;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * generator for item model data
 * <p> runs on gradle build and creates individual tea item models and the full tea bottle model </p>
 * <p> this ensures that json data for tea models will always be up to date with code changes </p>
 */
public class ItemModelGenerator {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final String MODEL_PATH = "src/main/resources/assets/" + TantalisingTeas.MOD_ID + "/models/";

    public static void main(String[] args) throws IOException {
        generateTeaColourModels();
        generateTeaBottleModel();
    }

    private static void generateTeaBottleModel() throws IOException {
        File file = new File(MODEL_PATH + "item/tea_bottle.json");

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(getLatestTeaBottleJson(), writer);
        }
    }

    static ItemModelJson getLatestTeaBottleJson() {
        return new ItemModelJson("item/generated", new Textures("minecraft:item/glass_bottle", TantalisingTeas.MOD_ID + ":item/tea_bottle_overlay"), generateLatestOverrides());
    }

    static JsonOverride[] generateLatestOverrides() {
        JsonOverride[] jsonOverrides = new JsonOverride[TeaColour.values().length];

        for (int i = 0; i < TeaColour.values().length; i ++) {
            TeaColour colour = TeaColour.values()[i];
            jsonOverrides[i] = new JsonOverride(new Predicate(colour.getNumericalId()), colour.getId());
        }

        return jsonOverrides;
    }

    static ItemModelJson getJson(TeaColour colour) {
        return new ItemModelJson("item/generated", new Textures("minecraft:item/glass_bottle", TantalisingTeas.MOD_ID + ":tea_overlay/" + colour.getId()), null);
    }

    private static void generateTeaColourModels() throws IOException {
        for (TeaColour colour : TeaColour.values()) {
            File file = new File(MODEL_PATH + "item/" + colour.getId() + "_tea_model.json");
            if (file.createNewFile()) {
                try (FileWriter writer = new FileWriter(file)) {
                    GSON.toJson(getJson(colour), writer);
                }
            }
        }
    }

    record ItemModelJson(String parent, Textures textures, JsonOverride[] overrides) {
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

    private record Textures(String layer0, String layer1) {
        @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Textures textures = (Textures) o;
                return Objects.equals(layer0, textures.layer0) && Objects.equals(layer1, textures.layer1);
            }

    }

    private record JsonOverride(Predicate predicate, String model) {
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

    private record Predicate(int id) {
        @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Predicate predicate = (Predicate) o;
                return id == predicate.id;
            }

    }
}
