package io.ix0rai.tantalisingteas.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ix0rai.tantalisingteas.util.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AssetGenerator {
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
    static final String SRC = "src";
    static final String MAIN = SRC + "/" + "main";
    static final String ASSETS = MAIN + "/resources/assets/" + Constants.MOD_ID;
    static final String MODELS = ASSETS + "/models/generated";
    static final String BLOCKSTATES = ASSETS + "/blockstates";
    static final String ITEM_MODELS = MODELS + "/item";
    static final String ITEM_MODELS_ROOT = ASSETS + "/models/item";
    static final String BLOCK_MODELS = MODELS + "/block";
    static final String TEXTURES_SOURCE = ASSETS + "/textures";
    static final String TEXTURES_GENERATED = ASSETS + "/textures/generated";
    static final String OVERLAY_GENERATED = TEXTURES_GENERATED + "/overlay";
    static final String CAULDRON_TEXTURES = TEXTURES_GENERATED + "/cauldron";
    static final String CAULDRON_TEXTURES_SOURCE = TEXTURES_SOURCE + "/cauldron";

    static final String TEST_VALIDATION = SRC + "/" + "test/resources/data/" + Constants.MOD_ID + "/validation";

    public static void main(String[] args) throws IOException {
        run("individual tea colour models", ItemModelGenerator::generateTeaColourModels);
        run("tea bottle model", ItemModelGenerator::generateTeaBottleModel);

        run("tea cauldron models", BlockModelGenerator::generateTeaCauldronModels);

        run("tea overlay textures", TextureGenerator::generateTeaOverlays);
        run("cauldron overlay textures", TextureGenerator::generateCauldronOverlays);
    }

    interface ThrowingRunnable {
        void run() throws IOException;
    }

    static void run(String thingsGenerating, ThrowingRunnable runnable) throws IOException {
        System.out.println("generating " + thingsGenerating + "...");
        runnable.run();
        System.out.println("done!");
    }

    static String modelReference(String path) {
        if (path.contains(":")) {
            throw new IllegalArgumentException("path cannot contains characters of type \":\"");
        }

        return Constants.MOD_ID + ":" + path;
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
