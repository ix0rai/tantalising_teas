package io.ix0rai.tantalisingteas.data;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import io.ix0rai.tantalisingteas.util.TeaColourUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TantalisingNetworking {
    public static final Identifier COLOUR_DATA_PACKET_ID = TantalisingTeas.id("tea_colour_data");
    public static final Identifier INGREDIENT_ADDITION_ANIMATION_CUE_ID = TantalisingTeas.id("ingredient_addition_animation_cue");

    public static void sendColourDataPacket() {
        // create packet and write data
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeMap(TeaColourUtil.ITEM_COLOURS, PacketByteBuf::writeIdentifier, PacketByteBuf::writeEnumConstant);

        // send packet
        ClientPlayNetworking.send(COLOUR_DATA_PACKET_ID, buf);
    }

    public static void sendIngredientAnimationCue(ServerWorld world, BlockPos pos, ItemStack stack) {
        // write data
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeItemStack(stack);

        // send packet
        for (ServerPlayerEntity player : PlayerLookup.tracking(world, pos)) {
            ServerPlayNetworking.send(player, INGREDIENT_ADDITION_ANIMATION_CUE_ID, buf);
        }
    }
}
