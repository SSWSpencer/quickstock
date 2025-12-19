package com.ssws.quickstock;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RestockHighlightRenderer {

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            render(
                    context.matrixStack(),
                    context.consumers(),
                    context.camera()
            );
        });
    }

    private static void render(
            MatrixStack matrices,
            VertexConsumerProvider consumers,
            Camera camera
    ) {
        if (QuickStockData.getHighlightedPositions().isEmpty()) return;

        Vec3d cam = camera.getPos();
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());

        for (BlockPos pos : QuickStockData.getHighlightedPositions()) {

            Box box = new Box(pos).expand(0.01).offset(
                    -cam.x,
                    -cam.y,
                    -cam.z
            );

            drawBox(matrices, buffer, box);
        }
    }

    private static void drawBox(MatrixStack matrices, VertexConsumer v, Box b) {

        float r = 0.2f;
        float g = 1.0f;
        float bcol = 0.2f;
        float a = 1.0f;

        line(v, matrices, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ, r, g, bcol, a);
        line(v, matrices, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ, r, g, bcol, a);
        line(v, matrices, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ, r, g, bcol, a);
        line(v, matrices, b.minX, b.minY, b.maxZ, b.minX, b.minY, b.minZ, r, g, bcol, a);

        line(v, matrices, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ, r, g, bcol, a);
        line(v, matrices, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ, r, g, bcol, a);
        line(v, matrices, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ, r, g, bcol, a);
        line(v, matrices, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ, r, g, bcol, a);

        line(v, matrices, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ, r, g, bcol, a);
        line(v, matrices, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ, r, g, bcol, a);
        line(v, matrices, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ, r, g, bcol, a);
        line(v, matrices, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ, r, g, bcol, a);
    }

    private static void line(
            VertexConsumer v,
            MatrixStack matrices,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float r, float g, float b, float a
    ) {
        v.vertex(matrices.peek().getPositionMatrix(), (float) x1, (float) y1, (float) z1)
                .color(r, g, b, a)
                .normal(0, 1, 0);

        v.vertex(matrices.peek().getPositionMatrix(), (float) x2, (float) y2, (float) z2)
                .color(r, g, b, a)
                .normal(0, 1, 0);
    }
}
