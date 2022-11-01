package io.ix0rai.tantalisingteas.registry;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.blocks.CinnamonLog;
import io.ix0rai.tantalisingteas.blocks.CinnamonSapling;
import io.ix0rai.tantalisingteas.blocks.TeaCauldron;
import io.ix0rai.tantalisingteas.blocks.TeaCauldronBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.registry.Registry;

/**
 * blocks and block entities registered by tantalising teas
 */
public class TantalisingBlocks {
    public static final TeaCauldron TEA_CAULDRON = new TeaCauldron(AbstractBlock.Settings.copy(Blocks.CAULDRON), LeveledCauldronBlock.RAIN_PREDICATE);
    public static final BlockEntityType<TeaCauldronBlockEntity> TEA_CAULDRON_ENTITY = FabricBlockEntityTypeBuilder.create(TeaCauldronBlockEntity::new, TEA_CAULDRON).build(null);

    public static final CinnamonLog CINNAMON_LOG = new CinnamonLog(AbstractBlock.Settings.copy(Blocks.BAMBOO));
    public static final Block CINNAMON_SAPLING = new CinnamonSapling(AbstractBlock.Settings.of(Material.BAMBOO_SAPLING).ticksRandomly().breakInstantly().noCollision().strength(1.0F).sounds(BlockSoundGroup.BAMBOO_SAPLING).offsetType(AbstractBlock.OffsetType.XZ));

    public static void register() {
        String teaCauldron = "tea_cauldron";
        Registry.register(Registry.BLOCK, TantalisingTeas.id("cinnamon_log"), CINNAMON_LOG);
        Registry.register(Registry.BLOCK, TantalisingTeas.id("cinnamon_sapling"), CINNAMON_SAPLING);
        Registry.register(Registry.BLOCK, TantalisingTeas.id(teaCauldron), TEA_CAULDRON);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, TantalisingTeas.id(teaCauldron + "_entity"), TEA_CAULDRON_ENTITY);
    }
}
