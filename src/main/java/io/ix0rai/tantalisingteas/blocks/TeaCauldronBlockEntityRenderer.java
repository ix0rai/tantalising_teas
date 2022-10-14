package io.ix0rai.tantalisingteas.blocks;

import io.ix0rai.tantalisingteas.client.TantalisingTeasClient;
import io.ix0rai.tantalisingteas.util.CountMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class TeaCauldronBlockEntityRenderer implements BlockEntityRenderer<TeaCauldronBlockEntity> {
    private final CountMap<BlockPos> ticks = new CountMap<>();

    public TeaCauldronBlockEntityRenderer() {
        // nothing to do
    }

    @Override
    public void render(TeaCauldronBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        for (Pair<BlockPos, ItemStack> entry : TantalisingTeasClient.stacksToRender) {
            if (entry.getLeft().equals(blockEntity.getPos())) {
                ItemStack stack = entry.getRight();
                BlockPos pos = entry.getLeft();

                // increment the item's tick count
                ticks.increment(pos);

                World world = Objects.requireNonNull(blockEntity.getWorld());
                int level = world.getBlockState(pos).get(TeaCauldron.LEVEL);

                // before starting to render, we have to push
                matrices.push();

                // move
                matrices.translate(0.5, level / 3D - ticks.get(pos) / 100D, 0.5);

                // rotate the item so it's lying flat
                matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));

                int lightAbove = WorldRenderer.getLightmapCoordinates(world, pos.up());
                MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, lightAbove, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 0);

                // after rendering, we pop
                matrices.pop();

                if (ticks.get(pos) >= 60) {
                    TantalisingTeasClient.stacksToRender.remove(entry);
                    ticks.reset(pos);
                }
            }
        }
    }
}
