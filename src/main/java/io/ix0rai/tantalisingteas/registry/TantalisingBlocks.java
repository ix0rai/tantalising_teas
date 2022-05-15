package io.ix0rai.tantalisingteas.registry;

import io.ix0rai.tantalisingteas.Tantalisingteas;
import io.ix0rai.tantalisingteas.blocks.TeaCauldron;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.util.registry.Registry;

public class TantalisingBlocks {
    public static Block TEA_CAULDRON;

    public static void register() {
        TEA_CAULDRON = register("tea_cauldron", new TeaCauldron(AbstractBlock.Settings.copy(Blocks.CAULDRON), LeveledCauldronBlock.RAIN_PREDICATE, TeaCauldron.BEHAVIOUR));
    }

    private static Block register(String id, Block block) {
        return Registry.register(Registry.BLOCK, Tantalisingteas.id(id), block);
    }
}
