package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.items.TeaBottle;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

// todo: tick function that increases ingredient strength as time passes
public class BoilingCauldronBlockEntity extends BlockEntity {
    private final NbtList items;

    public BoilingCauldronBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TantalisingBlocks.BOILING_CAULDRON_ENTITY, blockPos, blockState);
        items = new NbtList();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.of(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.put(TeaBottle.INGREDIENTS_KEY, items);
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        items.clear();

        NbtList itemsTag = tag.getList(TeaBottle.INGREDIENTS_KEY, 10);

        for (int i = 0; i < itemsTag.size(); i ++) {
            NbtCompound compoundTag = itemsTag.getCompound(i);
            items.add(compoundTag);
        }
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        tag.put(TeaBottle.INGREDIENTS_KEY, items);
    }

    public void addData(NbtCompound compound) {
        if (compound != null && !compound.isEmpty() && TeaBottle.isTeaIngredient(compound)) {
            items.add(compound);
        }
    }

    public void addStack(ItemStack stack) {
        if (stack != null) {
            NbtCompound compound = new NbtCompound();
            compound.putString(TeaBottle.ID_KEY, Registry.ITEM.getId(stack.getItem()).toString());
            addData(compound);
        }
    }

    public NbtList getItems() {
        return items;
    }
}
