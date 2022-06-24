package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.data.NbtUtil;
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
import net.minecraft.world.World;

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

    public static void tick(World world, BlockPos pos, BlockState state, BoilingCauldronBlockEntity boilingCauldron) {
        // todo: tick function that increases ingredient strength as time passes
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        NbtUtil.updateIngredients(items, nbt);
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        items.clear();

        NbtList ingredients = NbtUtil.getIngredients(tag);

        for (int i = 0; i < ingredients.size(); i ++) {
            NbtCompound compoundTag = ingredients.getCompound(i);
            items.add(compoundTag);
        }
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        NbtUtil.updateIngredients(items, tag);
    }

    public void addData(NbtCompound compound) {
        if (compound != null && !compound.isEmpty() && NbtUtil.isTeaIngredient(compound)) {
            items.add(compound);
        }
    }

    public void addStack(ItemStack stack) {
        if (stack != null) {
            NbtCompound compound = new NbtCompound();
            NbtUtil.setId(compound, Registry.ITEM.getId(stack.getItem()));
            addData(compound);
        }
    }

    public NbtList getItems() {
        return items;
    }
}
