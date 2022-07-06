package io.ix0rai.tantalisingteas.registry;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.blocks.StillCauldron;
import io.ix0rai.tantalisingteas.blocks.TantalisingCauldronBlockEntity;
import io.ix0rai.tantalisingteas.blocks.BoilingCauldron;
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
    public static final BlockEntityType<TantalisingCauldronBlockEntity> BOILING_CAULDRON_ENTITY = FabricBlockEntityTypeBuilder.create(TantalisingCauldronBlockEntity::new, BOILING_CAULDRON).build(null);

    public static final StillCauldron STILL_CAULDRON = new StillCauldron(AbstractBlock.Settings.copy(Blocks.CAULDRON), LeveledCauldronBlock.RAIN_PREDICATE);
    public static final BlockEntityType<TantalisingCauldronBlockEntity> STILL_CAULDRON_ENTITY = FabricBlockEntityTypeBuilder.create(TantalisingCauldronBlockEntity::new, STILL_CAULDRON).build(null);

    public static void register() {
        registerBlockWithEntity("boiling_cauldron", BOILING_CAULDRON, BOILING_CAULDRON_ENTITY);
        registerBlockWithEntity("still_cauldron", STILL_CAULDRON, STILL_CAULDRON_ENTITY);
    }

    private static void registerBlockWithEntity(String id, Block block, BlockEntityType<?> entityType) {
        Identifier identifier = TantalisingTeas.id(id);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, TantalisingTeas.id(identifier.getPath() + "_entity"), entityType);
        Registry.register(Registry.BLOCK, id, block);
    }
}
