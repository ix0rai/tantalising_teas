package io.ix0rai.tantalisingteas.registry;

import io.ix0rai.tantalisingteas.Tantalisingteas;
import io.ix0rai.tantalisingteas.items.TeaBottle;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public class TantalisingItems {
    public static Item TEA_LEAVES;
    public static Item TEA_BOTTLE;

    public static void registerItems() {
        TEA_LEAVES = register("tea_leaves", new Item(new Item.Settings().group(ItemGroup.BREWING)));
        TEA_BOTTLE = register("tea_bottle", new TeaBottle(new Item.Settings().group(ItemGroup.BREWING).food(new FoodComponent.Builder().saturationModifier(3.0f).hunger(1).alwaysEdible().build())));
    }

    private static Item register(String id, Item item) {
        return Registry.register(Registry.ITEM, Tantalisingteas.id(id), item);
    }
}
