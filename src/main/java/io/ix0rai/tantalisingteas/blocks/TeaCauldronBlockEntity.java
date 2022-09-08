package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.data.NbtUtil;
import io.ix0rai.tantalisingteas.registry.TantalisingBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * linked block entity for {@link TeaCauldron}
 */
public class TeaCauldronBlockEntity extends BlockEntity {
    private static final int TICKS_BEFORE_STRENGTH_INCREASE = 1200;
    private final NbtList ingredients = new NbtList();

    private int ticksSinceLastBoil = 0;

    public TeaCauldronBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TantalisingBlocks.TEA_CAULDRON_ENTITY, blockPos, blockState);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.of(this);
    }

    public static void tick(TeaCauldronBlockEntity boilingCauldron) {
        World world = boilingCauldron.getWorld();
        BlockPos pos = boilingCauldron.getPos();

        if (world != null) {
            BlockState state = world.getBlockState(pos);

            if (state.get(TeaCauldron.BOILING)) {
                NbtList items = boilingCauldron.getIngredients();
                for (int i = 0; i < items.size(); i++) {
                    NbtCompound ingredient = items.getCompound(i);
                    int strength = NbtUtil.getStrength(ingredient);
                    int ticks = NbtUtil.getTicksSinceStrengthIncrease(ingredient);

                    if (NbtUtil.getStrength(ingredient) < 6 && ticks >= TICKS_BEFORE_STRENGTH_INCREASE) {
                        NbtUtil.setStrength(ingredient, strength + 1);
                        NbtUtil.setTicksSinceStrengthIncrease(ingredient, 0);

                        world.setBlockState(boilingCauldron.getPos(), state.with(TeaCauldron.STRENGTH, NbtUtil.getOverallStrength(items)));
                    } else {
                        NbtUtil.setTicksSinceStrengthIncrease(ingredient, ticks + 1);
                    }
                }

                if (!world.getBlockState(pos.down()).isIn(BlockTags.FIRE)) {
                    boilingCauldron.ticksSinceLastBoil ++;

                    if (boilingCauldron.ticksSinceLastBoil >= TICKS_BEFORE_STRENGTH_INCREASE) {
                        boilingCauldron.ticksSinceLastBoil = 0;

                        world.setBlockState(pos, state.with(TeaCauldron.BOILING, false), Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        NbtUtil.setIngredients(ingredients, nbt);
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        ingredients.clear();
        NbtList items = NbtUtil.getIngredients(tag);

        for (int i = 0; i < items.size(); i ++) {
            NbtCompound compoundTag = items.getCompound(i);
            this.ingredients.add(compoundTag);
        }
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        NbtUtil.setId(tag, BlockEntityType.getId(this.getType()));
        NbtUtil.setIngredients(ingredients, tag);
    }

    public void addData(NbtCompound data) {
        if (data != null && !data.isEmpty()) {
            if (NbtUtil.containsIngredientKey(data)) {
                ingredients.addAll(NbtUtil.getIngredients(data));
            } else if (NbtUtil.isTeaIngredient(data)) {
                ingredients.add(data);
            }
        } else {
            TantalisingTeas.LOGGER.warn("tried to write empty data to cauldron at pos: " + pos);
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
