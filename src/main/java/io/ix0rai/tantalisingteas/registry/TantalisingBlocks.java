package io.ix0rai.tantalisingteas.registry;

import io.ix0rai.tantalisingteas.Tantalisingteas;
import io.ix0rai.tantalisingteas.blocks.TeaCauldronBlockEntity;
import io.ix0rai.tantalisingteas.blocks.TeaCauldron;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TantalisingBlocks {
    public static final TeaCauldron TEA_CAULDRON = new TeaCauldron(AbstractBlock.Settings.copy(Blocks.CAULDRON), LeveledCauldronBlock.RAIN_PREDICATE, TeaCauldron.BEHAVIOUR);
    public static final BlockEntityType<TeaCauldronBlockEntity> TEA_CAULDRON_ENTITY = FabricBlockEntityTypeBuilder.create(TeaCauldronBlockEntity::new, TEA_CAULDRON).build(null);

    public static void register() {
        Identifier id = Tantalisingteas.id("tea_cauldron");
        Registry.register(Registry.BLOCK_ENTITY_TYPE, Tantalisingteas.id(id.getPath() + "_entity"), (BlockEntityType<?>) TantalisingBlocks.TEA_CAULDRON_ENTITY);
        Registry.register(Registry.BLOCK, id, TEA_CAULDRON);
    }
}
