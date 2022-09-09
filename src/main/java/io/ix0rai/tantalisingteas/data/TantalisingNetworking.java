package io.ix0rai.tantalisingteas.data;

import io.ix0rai.tantalisingteas.TantalisingTeas;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class TantalisingNetworking {
    public static final Identifier COLOUR_DATA_PACKET_ID = TantalisingTeas.id("tea_colour_data");

    public static void sendColourDataPacket() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeMap(TeaColourUtil.ITEM_COLOURS, PacketByteBuf::writeIdentifier, PacketByteBuf::writeEnumConstant);

        ClientPlayNetworking.send(TantalisingNetworking.COLOUR_DATA_PACKET_ID, buf);
    }
}
