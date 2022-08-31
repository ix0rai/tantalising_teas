package io.ix0rai.tantalisingteas.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ix0rai.tantalisingteas.TantalisingTeas;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AssetGenerator {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
    static final String SRC = "src";
    static final String MAIN = SRC + "/" + "main";
    static final String ASSETS_SOURCE = MAIN + "/resources/assets/" + TantalisingTeas.MOD_ID;
    static final String ASSETS_GENERATED = ASSETS_SOURCE + "/generated/";
    static final String MODELS = ASSETS_GENERATED + "/models";
    static final String BLOCKSTATES = ASSETS_GENERATED + "/blockstates";
    static final String ITEM_MODELS = MODELS + "/item";
    static final String TEXTURES_SOURCE = ASSETS_SOURCE + "/textures";
    static final String OVERLAY_SOURCE = TEXTURES_SOURCE + "/overlay";
    static final String OVERLAY_GENERATED = ASSETS_GENERATED + "/overlay/generated";

    static final String TEST_VALIDATION = SRC + "/" + "test/resources/data/" + TantalisingTeas.MOD_ID + "/validation";

    public static void main(String[] args) throws IOException {
        ItemModelGenerator.generateTeaColourModels();
        ItemModelGenerator.generateTeaBottleModel();

        BlockModelGenerator.generateTeaCauldronModels();

        TextureGenerator.generateTeaOverlays();
    }

    static void write(File file, Object json) throws IOException {
        write(file, GSON.toJson(json));
    }

    static void write(File file, String json) throws IOException {
        // noinspection ResultOfMethodCallIgnored I do not care if the directories exist I just want my file
        file.getParentFile().mkdirs();

        // noinspection ResultOfMethodCallIgnored I also do not care if the file exists just let me write to it
        file.createNewFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json);
        }
    }
}
