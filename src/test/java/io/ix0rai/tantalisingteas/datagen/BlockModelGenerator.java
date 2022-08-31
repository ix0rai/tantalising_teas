package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlockModelGenerator {
    private static final int LEVELS = 3;

    static void generateTeaCauldronModels() throws IOException {
        final Map<String, ModelJsonProperty> variants = new HashMap<>();

        for (TeaColour colour : TeaColour.values()) {
            for (int l = 1; l < LEVELS; l ++) {
                for (int s = 0; s < NbtUtil.MAX_STRENGTH; s ++) {
                    final String variant = String.format("colour=%s,level=%d,strength=%d", colour.asString(), l, s);
                    final String model = String.format(TantalisingTeas.MOD_ID + ":block/cauldron/%s_tea_cauldron_l%d_s%d", colour.asString(), l, s);
                    variants.put(variant, new ModelJsonProperty(model));
                }
            }
        }

        // for some ungodly reason gson doesn't properly decode the equals sign
        String json = ModelGenerator.GSON.toJson(new BlockModelJson(variants));
        json = json.replace("\\u003d", "=");
        File file = new File(ModelGenerator.BLOCKSTATES + "/still_cauldron.json");
        ModelGenerator.write(file, json);
        // in the future, boiling cauldrons will have different models but for now, they're the same
        file = new File(ModelGenerator.BLOCKSTATES + "/boiling_cauldron.json");
        ModelGenerator.write(file, json);
    }

    @SuppressWarnings("unused")
    private static class BlockModelJson {
        private final Map<String, ModelJsonProperty> variants;

        public BlockModelJson(Map<String, ModelJsonProperty> variants) {
            this.variants = variants;
        }
    }

    @SuppressWarnings("unused")
    private static class ModelJsonProperty {
        public final String model;

        public ModelJsonProperty(String model) {
            this.model = model;
        }
    }
}
