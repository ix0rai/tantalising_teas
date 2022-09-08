package io.ix0rai.tantalisingteas.registry;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.items.DrinkableTeaItem;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

/**
 * items registered by tantalising teas
 */
public class TantalisingItems {
    public static final Item TEA_LEAVES = new Item(new Item.Settings().group(ItemGroup.BREWING));
    public static final Item TEA_BOTTLE = new DrinkableTeaItem(new Item.Settings().group(ItemGroup.BREWING).food(new FoodComponent.Builder().saturationModifier(3.0f).hunger(1).alwaysEdible().build()));

    public static void registerItems() {
        register("tea_leaves", TEA_LEAVES);
        register("tea_bottle", TEA_BOTTLE);
    }

    private static void register(String id, Item item) {
        Registry.register(Registry.ITEM, TantalisingTeas.id(id), item);
    }
}
