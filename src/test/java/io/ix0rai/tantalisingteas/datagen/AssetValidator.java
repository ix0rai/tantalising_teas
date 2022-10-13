package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.data.TeaColour;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("unused")
public class AssetValidator {
    public static void main(String[] args) {
        //run("tea colour data", AssetValidator::validateTeaColours);
    }

    static void run(String thingsValidating, AssetGenerator.ThrowingRunnable runnable) throws IOException {
        System.out.println("validating " + thingsValidating + "...");
        runnable.run();
        System.out.println("done!");
    }

    private static void validateTeaColours() throws IOException {
        File file = new File(AssetGenerator.TEST_VALIDATION + "/tea_colours.json");
        JsonTeaColour[] colours = AssetGenerator.GSON.fromJson(new FileReader(file), JsonTeaColour[].class);

        for (int i = 0; i < colours.length; i ++) {
            JsonTeaColour oldColour = colours[i];
            TeaColour newColour = TeaColour.values()[i];

            if (oldColour.numericalId == newColour.ordinal() && !oldColour.id.equals(newColour.getId())) {
                throw createChangedEnumException("id of colour with numerical id " + oldColour.numericalId + " (previously id " + oldColour.id + ") has been changed to " + newColour.getId());
            } else if (oldColour.numericalId != newColour.ordinal() && oldColour.id.equals(newColour.getId())) {
                throw createChangedEnumException("numerical id of colour " + oldColour.id + " (previously numerical id " + oldColour.numericalId + ") has been changed to " + newColour.ordinal());
            }
        }

        try (FileWriter writer = new FileWriter(file)) {
            AssetGenerator.GSON.toJson(JsonTeaColour.fromTeaColours(TeaColour.values()), writer);
        }
    }

    private static IllegalStateException createChangedEnumException(String message) {
        throw new IllegalStateException("configuration of items in TeaColour enum has been changed\n" +
                "error: " + message);
    }

    private static class JsonTeaColour {
        public final int numericalId;
        public final String id;

        public JsonTeaColour(TeaColour colour) {
            this.numericalId = colour.ordinal();
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
