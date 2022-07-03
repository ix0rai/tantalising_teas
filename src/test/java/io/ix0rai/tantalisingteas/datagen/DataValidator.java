package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DataValidator {
    //todo: fix validator
    public static void main(String[] args) throws IOException {
        validateTeaColours();
        validateJsonData();
    }

    private static void validateJsonData() throws IOException {
        for (TeaColour colour : TeaColour.values()) {
            for (int strength = 1; strength < NbtUtil.MAX_STRENGTH; strength ++) {
                File file = new File(ItemModelGenerator.MODEL_PATH + "/item/" + ItemModelGenerator.getName(colour, strength) + "_tea_model.json");
                validateJson(file, ItemModelGenerator.getJson(colour, strength));
            }
        }

        File file = new File(ItemModelGenerator.MODEL_PATH + "item/tea_bottle.json");
        validateJson(file, ItemModelGenerator.getLatestTeaBottleJson());
    }

    private static void validateJson(File file, ItemModelGenerator.ItemModelJson json) throws FileNotFoundException {
        if (!file.exists()) {
            throw new IllegalStateException("necessary file " + file + " does not exist");
        }

        if (!ItemModelGenerator.GSON.fromJson(new FileReader(file), ItemModelGenerator.ItemModelJson.class).equals(json)) {
            throw new IllegalStateException("json of file " + file + " has been edited: \n"
                    + "should be: \n" + ItemModelGenerator.GSON.toJson(ItemModelGenerator.getLatestTeaBottleJson())
                    + "\nwas: \n" + ItemModelGenerator.GSON.toJson(json));
        }
    }

    private static void validateTeaColours() throws IOException {
        File file = new File("src/test/resources/data/" + TantalisingTeas.MOD_ID + "/validation/tea_colours.json");
        JsonTeaColour[] colours = ItemModelGenerator.GSON.fromJson(new FileReader(file), JsonTeaColour[].class);

        for (int i = 0; i < colours.length; i ++) {
            JsonTeaColour oldColour = colours[i];
            TeaColour newColour = TeaColour.values()[i];
            if (oldColour.numericalId == newColour.getNumericalId() && !oldColour.id.equals(newColour.getId())) {
                throw new IllegalStateException("configuration of items in TeaColour enum has been changed\n" +
                        "error: id of colour with numerical id " + oldColour.numericalId + " (previously id " + oldColour.id + ") has been changed to " + newColour.getId());
            } else if (oldColour.numericalId != newColour.getNumericalId() && oldColour.id.equals(newColour.getId())) {
                throw new IllegalStateException("configuration of items in TeaColour enum has been changed\n" +
                        "error: numerical id of colour " + oldColour.id + " (previously numerical id " + oldColour.numericalId + ") has been changed to " + newColour.getNumericalId());
            }
        }

        try (FileWriter writer = new FileWriter(file)) {
            ItemModelGenerator.GSON.toJson(JsonTeaColour.fromTeaColours(TeaColour.values()), writer);
        }
    }

    private static class JsonTeaColour {
        public final int numericalId;
        public final String id;

        public JsonTeaColour(TeaColour colour) {
            this.numericalId = colour.getNumericalId();
            this.id = colour.getId();
        }

        public static JsonTeaColour[] fromTeaColours(TeaColour[] colours) {
            JsonTeaColour[] jsonColours = new JsonTeaColour[colours.length];

            for (int i = 0; i < TeaColour.values().length; i ++) {
                jsonColours[i] = new JsonTeaColour(colours[i]);
            }

            return jsonColours;
        }
    }
}
