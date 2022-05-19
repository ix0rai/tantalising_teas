package io.ix0rai.tantalisingteas.blocks;

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

import java.util.ArrayList;
import java.util.List;

public class TeaCauldronBlockEntity extends BlockEntity {
    private final List<ItemStack> items;

    public TeaCauldronBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TantalisingBlocks.TEA_CAULDRON_ENTITY, blockPos, blockState);
        items = new ArrayList<>();
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.of(this);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        this.items.clear();

        NbtList itemsTag = tag.getList("items", 10);
        for (int i = 0; i < itemsTag.size(); i ++) {
            NbtCompound compoundTag = itemsTag.getCompound(i);
            items.add(ItemStack.fromNbt(compoundTag));
        }
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        NbtList itemsTag = new NbtList();
        for (int i = 0; i < itemsTag.size(); i ++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                NbtCompound compoundTag = new NbtCompound();
                stack.writeNbt(compoundTag);
                itemsTag.add(compoundTag);
            }
        }

        tag.put("items", itemsTag);
    }

    public void addItem(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            items.add(new ItemStack(stack.getItem()));
        }
    }
}
