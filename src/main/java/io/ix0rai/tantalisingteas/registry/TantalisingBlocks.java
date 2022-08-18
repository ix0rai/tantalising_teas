package io.ix0rai.tantalisingteas.registry;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.blocks.BoilingCauldron;
import io.ix0rai.tantalisingteas.blocks.BoilingCauldronBlockEntity;
import io.ix0rai.tantalisingteas.blocks.StillCauldron;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TantalisingBlocks {
    public static final BoilingCauldron BOILING_CAULDRON = new BoilingCauldron(AbstractBlock.Settings.copy(Blocks.CAULDRON), LeveledCauldronBlock.RAIN_PREDICATE);
    public static final BlockEntityType<BoilingCauldronBlockEntity> BOILING_CAULDRON_ENTITY = FabricBlockEntityTypeBuilder.create(BoilingCauldronBlockEntity::new, BOILING_CAULDRON).build(null);

    public static final StillCauldron STILL_CAULDRON = new StillCauldron(AbstractBlock.Settings.copy(Blocks.CAULDRON), LeveledCauldronBlock.RAIN_PREDICATE);

    public static void register() {
        String boilingCauldron = "boiling_cauldron";
        registerBlock(boilingCauldron, BOILING_CAULDRON);
        registerBlock("still_cauldron", STILL_CAULDRON);

        Registry.register(Registry.BLOCK_ENTITY_TYPE, TantalisingTeas.id(boilingCauldron + "_entity"), BOILING_CAULDRON_ENTITY);
    }

    private static void registerBlock(String id, Block block) {
        Identifier identifier = TantalisingTeas.id(id);
        Registry.register(Registry.BLOCK, identifier, block);
    }
}
