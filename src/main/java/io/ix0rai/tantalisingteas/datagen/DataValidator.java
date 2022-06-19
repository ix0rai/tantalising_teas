package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.Tantalisingteas;
import io.ix0rai.tantalisingteas.items.rendering.TeaColour;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DataValidator {
    public static void main(String[] args) throws IOException {
        validateTeaColours();
    }

    private static void validateTeaColours() throws IOException {
        File file = new File("src/main/resources/data/" + Tantalisingteas.MOD_ID + "/validation/tea_colours.json");
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
        public int numericalId;
        public String id;

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
