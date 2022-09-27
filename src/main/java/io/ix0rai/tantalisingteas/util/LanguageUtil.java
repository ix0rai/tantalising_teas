package io.ix0rai.tantalisingteas.util;

import net.minecraft.text.Text;
import net.minecraft.util.Language;

/**
 * generic utility class, including static text objects, helpers for lang, and other things
 */
public class LanguageUtil {
    public static final Text NO_NBT = translatableText("error.no_nbt");
    public static final Text TEA = translatableText("word.tea");
    public static final Text OF = translatableText("word.of");
    public static final Text BOTTLE = translatableText("word.bottle");

    public static final Text[] STRENGTHS = new Text[] {
            translatableText("strength.weak"),
            translatableText("strength.medium"),
            translatableText("strength.strong"),
    };

    public static Text translatableText(String key) {
        if (!key.contains(".")) {
            throw new UnsupportedOperationException("translatable text must be in the format 'category.key'");
        }
        return Text.of(Constants.MOD_ID + "." + key);
    }

    public static String translate(Text text) {
        return translate(text.getString());
    }

    public static String translate(String text) {
        return Language.getInstance().get(text);
    }
}
