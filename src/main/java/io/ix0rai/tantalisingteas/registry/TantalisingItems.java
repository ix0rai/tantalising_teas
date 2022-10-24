package io.ix0rai.tantalisingteas.registry;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.items.DrinkableTeaItem;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

/**
 * items registered by tantalising teas
 */
public class TantalisingItems {
    public static Item cinnamonSeeds;
    public static Item teaBottle;
    public static Item cinnamonStick;

    public static void register() {
        cinnamonSeeds = register("cinnamon_seeds", new AliasedBlockItem(TantalisingBlocks.CINNAMON_LOG, new Item.Settings()));
        cinnamonStick = register("cinnamon_stick", new Item(new Item.Settings()));
        teaBottle = register("tea_bottle", new DrinkableTeaItem(new Item.Settings().food(new FoodComponent.Builder().saturationModifier(3.0f).hunger(1).alwaysEdible().build())));
    }

    private static Item register(String id, Item item) {
        return Registry.register(Registry.ITEM, TantalisingTeas.id(id), item);
    }
}
