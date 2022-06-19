package io.ix0rai.tantalisingteas.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ix0rai.tantalisingteas.Tantalisingteas;
import io.ix0rai.tantalisingteas.items.rendering.TeaColour;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * generator for item model data
 * <p> runs on gradle build and creates individual tea item models and the full tea bottle model </p>
 * <p> this ensures that json data for tea models will always be up to date with code changes </p>
 */
public class ItemModelGenerator {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    static final String MODEL_PATH = "src/main/resources/assets/" + Tantalisingteas.MOD_ID + "/models/";

    public static void main(String[] args) throws IOException {
        generateTeaColourModels();
        generateTeaBottleModel();
    }

    private static void generateTeaBottleModel() throws IOException {
        File file = new File(MODEL_PATH + "item/tea_bottle.json");

        Override[] overrides = new Override[TeaColour.values().length];
        for (int i = 0; i < TeaColour.values().length; i ++) {
            TeaColour colour = TeaColour.values()[i];
            overrides[i] = new Override(new Predicate(colour.getNumericalId()), colour.getId());
        }

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(new ItemModelJson("item/generated", new Textures("minecraft:item/glass_bottle", Tantalisingteas.MOD_ID + ":item/tea_bottle_overlay"), overrides), writer);
        }
    }

    private static void generateTeaColourModels() throws IOException {
        for (TeaColour colour : TeaColour.values()) {
            File file = new File(MODEL_PATH + "item/" + colour.getId() + "_tea_model.json");
            if (file.createNewFile()) {
                try (FileWriter writer = new FileWriter(file)) {
                    GSON.toJson(new ItemModelJson("item/generated", new Textures("minecraft:item/glass_bottle", Tantalisingteas.MOD_ID + ":tea_overlay/" + colour.getId()), null), writer);
                }
            }
        }
    }

    private static class ItemModelJson {
        public String parent;
        public Textures textures;
        public Override[] overrides;

        public ItemModelJson(String parent, Textures textures, Override[] overrides) {
            this.parent = parent;
            this.textures = textures;
            this.overrides = overrides;
        }
    }

    private static class Textures {
        public String layer0;
        public String layer1;

        public Textures(String layer0, String layer1) {
            this.layer0 = layer0;
            this.layer1 = layer1;
        }
    }

    private static class Override {
        public Predicate predicate;
        public String model;

        public Override(Predicate predicate, String model) {
            this.predicate = predicate;
            this.model = Tantalisingteas.MOD_ID + ":item/" + model + "_tea_model";
        }
    }

    private static class Predicate {
        public int id;

        public Predicate(int id) {
            this.id = id;
        }
    }
}
