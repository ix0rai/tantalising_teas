package io.ix0rai.tantalisingteas.items;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.data.Util;
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

public class TeaBottle extends HoneyBottleItem {
    public static final Text BAD_NBT = TantalisingTeas.translatableText("error.bad_nbt");
    public static final Text NO_NBT = TantalisingTeas.translatableText("error.no_nbt");

    public TeaBottle(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        // we handle changing the stack
        ItemStack stack1 = stack.copy();
        super.finishUsing(stack, world, user);
        stack = stack1;

        if (user instanceof ServerPlayerEntity serverPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger(serverPlayerEntity, stack);
        }

        // return empty bottle, or throw it away if it does not fit
        // also decrement the stack size if it will not be entirely consumed
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
