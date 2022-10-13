package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.client.TantalisingTeasClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class TeaCauldronBlockEntityRenderer implements BlockEntityRenderer<TeaCauldronBlockEntity> {
    private int stackTicks = 0;
    private ItemStack lastStack = ItemStack.EMPTY;

    public TeaCauldronBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {

    }

    @Override
    public void render(TeaCauldronBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockPos pos = blockEntity.getPos();

        if (TantalisingTeasClient.stacksToRender.containsKey(pos)) {
            ItemStack stack = TantalisingTeasClient.stacksToRender.get(pos);

            if (!stack.equals(lastStack)) {
                lastStack = stack;
                stackTicks = 0;
            }

            matrices.push();
            double offset = Math.sin((blockEntity.getWorld().getTime() + tickDelta) / 8.0) / 4.0;
            // Move the item
            matrices.translate(0.5, 1.25 + offset, 0.5);

            // Rotate the item
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((blockEntity.getWorld().getTime() + tickDelta) * 4));

            int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
            MinecraftClient.getInstance().getItemRenderer().renderItem(lastStack, ModelTransformation.Mode.GROUND, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

            // Mandatory call after GL calls
            matrices.pop();

            stackTicks ++;
            if (stackTicks >= 60) {
                TantalisingTeasClient.stacksToRender.remove(blockEntity.getPos());
                stackTicks = 0;
            }
        }
    }
}
