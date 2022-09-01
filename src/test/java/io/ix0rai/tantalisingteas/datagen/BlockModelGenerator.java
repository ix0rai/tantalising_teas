package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BlockModelGenerator {
    public static final int LEVELS = 3;

    static void generateTeaCauldronModels() throws IOException {
        final Map<String, ModelJsonProperty> variants = new HashMap<>();

        for (TeaColour colour : TeaColour.values()) {
            for (int l = 1; l <= LEVELS; l ++) {
                for (int s = 0; s <= NbtUtil.MAX_STRENGTH; s ++) {
                    final String variant = String.format("colour=%s,level=%d,strength=%d", colour.asString(), l, s);
                    final String modelName = String.format("%s_tea_cauldron_l%d_s%d", colour.asString(), l, s);
                    variants.put(variant, new ModelJsonProperty(TantalisingTeas.MOD_ID + ":generated/block/" + modelName));

                    // todo: generate individual models

                    // todo custom parent model with no tint index
                    // todo idk what I'm doing anymore there's too much json
                    // todo can I even put all my assets in /generated does that even work

                    Map<String, String> textures = new HashMap<>();
                    textures.put("bottom", "minecraft:block/cauldron_bottom");
                    textures.put("side", "minecraft:block/cauldron_side");
                    textures.put("top", "minecraft:block/cauldron_top");
                    textures.put("inside", "minecraft:block/cauldron_inner");
                    textures.put("particle", "minecraft:block/cauldron_side");
                    textures.put("content", TantalisingTeas.MOD_ID + ":generated/cauldron/" + String.format("%s_tea_cauldron_s%d", colour.asString(), s));

                    BlockModelJson model = new BlockModelJson(TantalisingTeas.MOD_ID + ":cauldron/tantalising_cauldron_level" + l, textures);

                    File file = new File(AssetGenerator.BLOCK_MODELS + "/" + modelName + ".json");
                    AssetGenerator.write(file, model);
                }
            }
        }

        // for some ungodly reason gson doesn't properly decode the equals sign
        String json = AssetGenerator.GSON.toJson(new BlockStateJson(variants));
        json = json.replace("\\u003d", "=");
        File file = new File(AssetGenerator.BLOCKSTATES + "/still_cauldron.json");
        AssetGenerator.write(file, json);

        // in the future, boiling cauldrons will have different models but for now, they're the same
        file = new File(AssetGenerator.BLOCKSTATES + "/boiling_cauldron.json");
        AssetGenerator.write(file, json);
    }

    @SuppressWarnings("unused")
    private static class BlockModelJson {
        private final String parent;
        private final Map<String, String> textures;

        BlockModelJson(String parent, Map<String, String> textures) {
            this.parent = parent;
            this.textures = textures;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockModelJson that = (BlockModelJson) o;
            return Objects.equals(parent, that.parent) && Objects.equals(textures, that.textures);
        }
    }

    @SuppressWarnings("unused")
    private static class BlockStateJson {
        private final Map<String, ModelJsonProperty> variants;

        public BlockStateJson(Map<String, ModelJsonProperty> variants) {
            this.variants = variants;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockStateJson that = (BlockStateJson) o;
            return Objects.equals(variants, that.variants);
        }
    }

    @SuppressWarnings("unused")
    private static class ModelJsonProperty {
        public final String model;

        public ModelJsonProperty(String model) {
            this.model = model;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModelJsonProperty that = (ModelJsonProperty) o;
            return Objects.equals(model, that.model);
        }
    }
}
