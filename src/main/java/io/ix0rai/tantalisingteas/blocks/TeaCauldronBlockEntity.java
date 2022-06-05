package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.items.TeaBottle;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class TeaCauldronBlockEntity extends BlockEntity {
    private final NbtList items;

    public TeaCauldronBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TantalisingBlocks.TEA_CAULDRON_ENTITY, blockPos, blockState);
        items = new NbtList();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.of(this);
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
        NbtList nbt = new NbtList();

        for (NbtElement element : items) {
            if (element.getNbtType() == NbtCompound.TYPE) {
                nbt.add(element);
            }
        }

        tag.put(TeaBottle.INGREDIENTS_KEY, nbt);
    }

    public void addData(NbtCompound compound) {
        // assume that the item is already in the tea_ingredients tag
        if (compound != null && !compound.isEmpty()) {
            items.add(compound);
        }
    }

    public void addStack(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            NbtCompound compound = new NbtCompound();
            compound.putString(TeaBottle.ID_KEY, Registry.ITEM.getId(stack.getItem()).toString());
            items.add(compound);
        }
    }

    public NbtList getItems() {
        return items;
    }
}
