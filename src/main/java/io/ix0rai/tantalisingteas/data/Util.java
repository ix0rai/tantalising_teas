package io.ix0rai.tantalisingteas.data;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class Util {
    public static final Text BAD_NBT = TantalisingTeas.translatableText("error.bad_nbt");
    public static final Text NO_NBT = TantalisingTeas.translatableText("error.no_nbt");
    public static final Text TEA = TantalisingTeas.translatableText("word.tea");
    public static final Text OF = TantalisingTeas.translatableText("word.of");
    public static final Text BOTTLE = TantalisingTeas.translatableText("word.bottle");
    public static final Text[] FLAIRS = new Text[] {
            TantalisingTeas.translatableText("flair.with_an_infusion"),
            TantalisingTeas.translatableText("flair.with_hints"),
            TantalisingTeas.translatableText("flair.with_undertones"),
            TantalisingTeas.translatableText("flair.with_a_taste"),
    };

    public static final Text[] STRENGTHS = new Text[] {
            TantalisingTeas.translatableText("strength.weak"),
            TantalisingTeas.translatableText("strength.medium"),
            TantalisingTeas.translatableText("strength.strong"),
    };

    public static final TagKey<Item> TEA_INGREDIENTS = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "tea_ingredients"));

    public static String translate(Text text) {
        return translate(text.getString());
    }

    public static String translate(String text) {
        return Language.getInstance().get(text);
    }

    public static String getFlair(int index) {
        return Util.translate(Util.FLAIRS[index]) + " " + Util.translate(Util.OF).toLowerCase();
    }

    public static void appendTeaNbt(ItemStack stack, List<Text> tooltip) {
        NbtCompound nbt = stack.getNbt();

        if (nbt == null) {
            tooltip.add(NO_NBT);
        } else if (stack.hasNbt()) {
            NbtList ingredients = NbtUtil.getIngredients(nbt);
            for (int i = 0; i < ingredients.size(); i ++) {
                NbtCompound element = ingredients.getCompound(i);

                if (element.getNbtType() != NbtCompound.TYPE) {
                    tooltip.add(BAD_NBT);
                } else {
                    Identifier id = NbtUtil.getIngredientId(element);
                    Item ingredient = Registry.ITEM.get(id);
                    tooltip.add(Text.of(NbtUtil.getFlair(nbt, i) + " " + Util.translate(ingredient.getTranslationKey())));
                }
            }
        }
    }
}
