package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class BoilingCauldronBlockEntity extends BlockEntity {
    private static final int TICKS_BEFORE_STRENGTH_INCREASE = 1500;
    private final NbtList ingredients = new NbtList();

    public BoilingCauldronBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TantalisingBlocks.BOILING_CAULDRON_ENTITY, blockPos, blockState);
    }

    protected BoilingCauldronBlockEntity(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState) {
        super(type, blockPos, blockState);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.of(this);
    }

    public static void tick(BoilingCauldronBlockEntity boilingCauldron) {
        NbtList items = boilingCauldron.getIngredients();

        for (int i = 0; i < items.size(); i++) {
            NbtCompound ingredient = items.getCompound(i);
            int strength = NbtUtil.getStrength(ingredient);
            int ticks = NbtUtil.getTicksSinceStrengthIncrease(ingredient);

            if (NbtUtil.getStrength(ingredient) < 6 && ticks >= TICKS_BEFORE_STRENGTH_INCREASE) {
                NbtUtil.setStrength(ingredient, strength + 1);
                NbtUtil.setTicksSinceStrengthIncrease(ingredient, 0);
            } else {
                NbtUtil.setTicksSinceStrengthIncrease(ingredient, ticks + 1);
            }
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        NbtUtil.updateIngredients(ingredients, nbt);
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        ingredients.clear();

        NbtList items = NbtUtil.getIngredients(tag);

        for (int i = 0; i < items.size(); i ++) {
            NbtCompound compoundTag = items.getCompound(i);
            this.ingredients.add(compoundTag);
        }
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        NbtUtil.updateIngredients(ingredients, tag);
    }

    public void addData(NbtCompound data) {
        if (data != null && !data.isEmpty()) {
            if (NbtUtil.containsIngredientKey(data)) {
                ingredients.addAll(NbtUtil.getIngredients(data));
            } else if (NbtUtil.isTeaIngredient(data)) {
                ingredients.add(data);
            }
        }
    }

    public void addStack(ItemStack stack) {
        if (stack != null) {
            NbtCompound compound = new NbtCompound();
            NbtUtil.setId(compound, Registry.ITEM.getId(stack.getItem()));
            addData(compound);
        }
    }

    public NbtList getIngredients() {
        return ingredients;
    }
}
