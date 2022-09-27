package io.ix0rai.tantalisingteas.util;

import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
    public static final TagKey<Item> TEA_INGREDIENTS = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "tea_ingredients"));
    public static final String MOD_ID = "tantalising_teas";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
}
