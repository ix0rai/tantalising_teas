package io.ix0rai.tantalisingteas.datagen;

import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.util.NbtUtil;

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
                    String variant = String.format("colour=%s,level=%d,strength=%d", colour.asString(), l, s);
                    String modelName = String.format("%s_tea_cauldron_l%d_s%d", colour.asString(), l, s);

                    final String boilingVariant = variant + ",boiling=true";
                    final String boilingModelName = modelName + "_boiling";
                    final String stillVariant = variant + ",boiling=false";
                    final String stillModelName = modelName + "_still";

                    variants.put(boilingVariant, new ModelJsonProperty(AssetGenerator.modelReference("generated/block/" + boilingModelName)));
                    variants.put(stillVariant, new ModelJsonProperty(AssetGenerator.modelReference("generated/block/" + stillModelName)));

                    Map<String, String> textures = new HashMap<>();
                    textures.put("bottom", "minecraft:block/cauldron_bottom");
                    textures.put("side", "minecraft:block/cauldron_side");
                    textures.put("top", "minecraft:block/cauldron_top");
                    textures.put("inside", "minecraft:block/cauldron_inner");
                    textures.put("particle", "minecraft:block/cauldron_side");
                    textures.put("content", AssetGenerator.modelReference("block/generated/cauldron/" + String.format("%s_tea_cauldron_s%d", colour.asString(), s)));

                    // special-casing for level 3 being named "full" in minecraft's models
                    BlockModelJson stillModel = new BlockModelJson("minecraft:block/template_cauldron_" + (l == 3 ? "full" : "level" + l), textures);
                    File file = new File(AssetGenerator.BLOCK_MODELS + "/" + stillModelName + ".json");
                    AssetGenerator.write(file, stillModel);

                    textures.put("boiling", AssetGenerator.modelReference("block/cauldron/boiling_effect"));

                    BlockModelJson boilingModel = new BlockModelJson(AssetGenerator.modelReference("cauldron/boiling_cauldron_level" + l), textures);
                    file = new File(AssetGenerator.BLOCK_MODELS + "/" + boilingModelName + ".json");
                    AssetGenerator.write(file, boilingModel);
                }
            }
        }

        // for some ungodly reason gson doesn't properly encode the equals sign
        // I hate this so much
        String json = AssetGenerator.GSON.toJson(new BlockStateJson(variants));
        json = json.replace("\\u003d", "=");
        File file = new File(AssetGenerator.BLOCKSTATES + "/tea_cauldron.json");
        AssetGenerator.write(file, json);
    }

    @SuppressWarnings({"unused", "ClassCanBeRecord"})
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

    @SuppressWarnings({"unused", "ClassCanBeRecord"})
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

    @SuppressWarnings({"unused", "ClassCanBeRecord"})
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
