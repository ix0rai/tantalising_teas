package io.ix0rai.tantalisingteas.registry;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.items.DrinkableTeaItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.unmapped.C_nusqeapl;
import net.minecraft.util.registry.Registry;

/**
 * items registered by tantalising teas
 */
public class TantalisingItems {
    // todo texture
    public static Item cinnamonSeeds;
    public static Item teaBottle;
    // todo texture
    public static Item cinnamonStick;

    public static void register() {
        cinnamonSeeds = register("cinnamon_seeds", new AliasedBlockItem(TantalisingBlocks.CINNAMON_SAPLING, new FabricItemSettings()));
        cinnamonStick = register("cinnamon_stick", new Item(new FabricItemSettings()));
        teaBottle = register("tea_bottle", new DrinkableTeaItem(new FabricItemSettings().food(new FoodComponent.Builder().saturationModifier(3.0f).hunger(1).alwaysEdible().build())));
    }

    private static Item register(String id, Item item) {
        return Registry.register(C_nusqeapl.f_blfmzmyy, TantalisingTeas.id(id), item);
    }
}
