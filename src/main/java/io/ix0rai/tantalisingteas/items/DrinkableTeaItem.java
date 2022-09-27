package io.ix0rai.tantalisingteas.items;

import io.ix0rai.tantalisingteas.util.CountMap;
import io.ix0rai.tantalisingteas.util.LanguageUtil;
import io.ix0rai.tantalisingteas.util.NbtUtil;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneyBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * represents an item that can contain tea
 */
public class DrinkableTeaItem extends HoneyBottleItem {
    public DrinkableTeaItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack stack1 = stack.copy();
        super.finishUsing(stack, world, user);
        stack = stack1;

        if (user instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
        }

        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        } else {
            if (user instanceof PlayerEntity playerEntity && !playerEntity.getAbilities().creativeMode) {
                ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!playerEntity.getInventory().insertStack(glassBottle)) {
                    playerEntity.dropItem(glassBottle, false);
                }

                if (stack.getCount() == 1) {
                    return glassBottle;
                } else {
                    stack.decrement(1);
                    return stack;
                }
            }

            return stack;
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        NbtCompound nbt = stack.getNbt();

        if (nbt == null) {
            tooltip.add(LanguageUtil.NO_NBT);
        } else if (stack.hasNbt()) {
            NbtList ingredients = NbtUtil.getIngredients(nbt);
            CountMap<Item> ingredientCounts = new CountMap<>();

            for (int i = 0; i < ingredients.size(); i ++) {
                NbtCompound element = ingredients.getCompound(i);

                Identifier id = NbtUtil.getIngredientId(element);
                Item ingredient = Registry.ITEM.get(id);
                ingredientCounts.increment(ingredient);
            }

            for (Map.Entry<Item, Integer> entry : ingredientCounts.entrySet()) {
                Item ingredient = entry.getKey();
                int count = entry.getValue();

                tooltip.add(Text.of(LanguageUtil.translate(ingredient.getTranslationKey()) + " x" + count));
            }
        }
    }
}
