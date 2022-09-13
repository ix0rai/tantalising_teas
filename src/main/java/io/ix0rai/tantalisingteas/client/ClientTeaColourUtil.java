package io.ix0rai.tantalisingteas.client;

import com.mojang.blaze3d.texture.NativeImage;
import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.TeaColour;
import io.ix0rai.tantalisingteas.data.TeaColourUtil;
import io.ix0rai.tantalisingteas.data.Util;
import io.ix0rai.tantalisingteas.mixin.render.SpriteAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.HolderSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.EnumMap;
import java.util.Map;

/**9
 * client-only helper methods for dealing with tea colours
 * @author ix0rai
 */
@Environment(EnvType.CLIENT)
public class ClientTeaColourUtil {
    /**
     * runs over a texture and finds the most common colours
     * @param texture the texture to analyse
     * @return a map of colours and their occurrences, having been converted to {@link TeaColour}s
     */
    public static Map<TeaColour, Integer> getColourOccurrences(NativeImage texture) {
        Map<TeaColour, Integer> colours = new EnumMap<>(TeaColour.class);

        // assemble a map of colours and their number of occurrences
        for (int x = 0; x < texture.getWidth(); x ++) {
            for (int y = 0; y < texture.getHeight(); y ++) {
                // get the colour of the pixels and convert them to 0 - 255 values
                int r = texture.getRed(x, y) & 0xFF;
                int g = texture.getGreen(x, y) & 0xFF;
                int b = texture.getBlue(x, y) & 0xFF;

                // calculate transparency
                try {
                    // ignore really light pixels
                    if (r == 0 && g == 0 && b == 0) {
                        continue;
                    }
                } catch (IllegalArgumentException ignored) {
                    // thrown if the texture has no alpha channel
                }

                TeaColour colour = TeaColourUtil.getClosest(r, g, b);
                colours.put(colour, colours.getOrDefault(colour, 0) + 1);
            }
        }

        return colours;
    }

    /**
     * updates the custom stack name of the provided stack
     * @param stack the stack to update
     */
    public static void updateCustomName(ItemStack stack) {
        NbtCompound primaryIngredient = NbtUtil.getPrimaryIngredient(stack);

        if (primaryIngredient != null) {
            NbtCompound nbt = stack.getNbt();
            // format strength so that it can be used to pull from the array of strings
            int strength = (int) (Math.round((double) NbtUtil.getOverallStrength(NbtUtil.getIngredients(nbt)) / 2) - 1);

            String name = Util.translate(Util.BOTTLE) + " " + Util.translate(Util.OF)
                    + (strength == 2 ? "" : " " + Util.translate(Util.STRENGTHS[strength]))
                    + " " + Util.translate(Registry.ITEM.get(NbtUtil.getIngredientId(primaryIngredient)).getTranslationKey()) + " " + Util.translate(Util.TEA);
            stack.setCustomName(Text.of(name));
        }
    }

    /**
     * creates a map of tea ingredient items and their associated colours
     * important: this data is only created on the client and requires tags to be loaded in order to work
     */
    public static void cacheTeaColours() {
        // cache the colours of each texture in the tea ingredient tag
        if (TeaColourUtil.ITEM_COLOURS.isEmpty()) {
            HolderSet.NamedSet<Item> items = Registry.ITEM.getOrCreateTag(Util.TEA_INGREDIENTS);

            //
            if (items.size() == 0) {
                TantalisingTeas.LOGGER.error("tea ingredients cached before tag load!");
                return;
            }

            items.forEach(item -> {
                // get the model
                Identifier id = Registry.ITEM.getId(item.value());
                ModelIdentifier modelId = new ModelIdentifier(id + "#inventory");
                BakedModel model = MinecraftClient.getInstance().getBakedModelManager().getModel(modelId);

                // get the texture and extract the amount of times each colour appears
                NativeImage texture = ((SpriteAccessor) model.getParticleSprite()).getImages()[0];
                Map<TeaColour, Integer> colours = ClientTeaColourUtil.getColourOccurrences(texture);

                // trim the list of colours to the top 3 most saturated
                TeaColourUtil.cleanupRareColours(colours);
                TeaColour[] mostSaturatedColours = TeaColourUtil.collectMostSaturatedColours(colours);

                // pick the colour with the highest priority and then save it to the cache
                TeaColour highestPriority = TeaColourUtil.getHighestPriority(mostSaturatedColours);
                TeaColourUtil.ITEM_COLOURS.put(id, highestPriority);
            });

            TantalisingTeas.LOGGER.info("successfully cached " + items.size() + " tea ingredient colours");
        }
    }
}
