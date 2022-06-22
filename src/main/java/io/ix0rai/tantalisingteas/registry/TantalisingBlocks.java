package io.ix0rai.tantalisingteas.registry;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.blocks.BoilingCauldronBlockEntity;
import io.ix0rai.tantalisingteas.blocks.BoilingCauldron;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TantalisingBlocks {
    public static final BoilingCauldron BOILING_CAULDRON = new BoilingCauldron(AbstractBlock.Settings.copy(Blocks.CAULDRON), LeveledCauldronBlock.RAIN_PREDICATE, BoilingCauldron.BEHAVIOUR);
    public static final BlockEntityType<BoilingCauldronBlockEntity> BOILING_CAULDRON_ENTITY = FabricBlockEntityTypeBuilder.create(BoilingCauldronBlockEntity::new, BOILING_CAULDRON).build(null);

    public static void register() {
        Identifier id = TantalisingTeas.id("boiling_cauldron");
        Registry.register(Registry.BLOCK_ENTITY_TYPE, TantalisingTeas.id(id.getPath() + "_entity"), (BlockEntityType<?>) TantalisingBlocks.BOILING_CAULDRON_ENTITY);
        Registry.register(Registry.BLOCK, id, BOILING_CAULDRON);
    }
}
