package com.stashwalker.rendering;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;

import com.mojang.blaze3d.systems.RenderSystem;
import com.stashwalker.constants.Constants;

import net.minecraft.world.chunk.Chunk;
import java.util.Collection;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

public class Renderer {

    public void drawLine (WorldRenderContext context, Vec3d end, int r, int g, int b, int alpha) {

        Vec3d cameraPos = Constants.MC_CLIENT_INSTANCE.gameRenderer.getCamera().getPos();
        Matrix4f matrix4f = context.matrixStack().peek().getPositionMatrix();
        Vector3f pos = new Vector3f(0, 0, 1);

        // Use a single shared MatrixStack instance if applicable
        if (Constants.MC_CLIENT_INSTANCE.options.getBobView().getValue()) {

            MatrixStack bobViewMatrices = new MatrixStack(); // Consider reusing or managing a pool of these
            bobView(bobViewMatrices);
            pos.mulPosition(bobViewMatrices.peek().getPositionMatrix().invert());
        }

        Vec3d start = new Vec3d(pos.x, -pos.y, pos.z)
                .rotateX(-(float) Math.toRadians(Constants.MC_CLIENT_INSTANCE.gameRenderer.getCamera().getPitch()))
                .rotateY(-(float) Math.toRadians(Constants.MC_CLIENT_INSTANCE.gameRenderer.getCamera().getYaw()))
                .add(Constants.MC_CLIENT_INSTANCE.gameRenderer.getCamera().getPos());
        start = new Vec3d(start.x - cameraPos.x, start.y - cameraPos.y, start.z - cameraPos.z);

        end = end.subtract(cameraPos);

        RenderSystem.setShaderColor(r / 255.0f, g / 255.0f, b / 255.0f, alpha / 255.0f); // Normalize colors

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.0f);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);
        bufferBuilder.vertex(matrix4f, (float) start.x, (float) start.y, (float) start.z).next();
        bufferBuilder.vertex(matrix4f, (float) end.x, (float) end.y, (float) end.z).next();
        tessellator.draw();

        end();
    }

    // Helper method to draw a square around a chunk
    public void drawChunkSquare (
        WorldRenderContext context, 
        Collection<Chunk> chunks, 
        int rectangleLevel,
        int rectableHeight, 
        int r, 
        int g, 
        int b, 
        int alpha
    ) {

        MatrixStack matrixStack = context.matrixStack();
        Vec3d cameraPos = Constants.MC_CLIENT_INSTANCE.gameRenderer.getCamera().getPos();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);

        double topHeight = (rectangleLevel + rectableHeight) - cameraPos.y; // height 100 in world coordinates

        for (Chunk chunk : chunks) {

            double startX = chunk.getPos().getStartX() - cameraPos.x;
            double endX = chunk.getPos().getEndX() + 1 - cameraPos.x;
            double startZ = chunk.getPos().getStartZ() - cameraPos.z;
            double endZ = chunk.getPos().getEndZ() + 1 - cameraPos.z;
            double y = rectangleLevel - cameraPos.y;

            // Draw the base square around the chunk
            bufferBuilder.vertex(matrix, (float) startX, (float) y, (float) startZ).next();
            bufferBuilder.vertex(matrix, (float) endX, (float) y, (float) startZ).next();
            bufferBuilder.vertex(matrix, (float) endX, (float) y, (float) startZ).next();
            bufferBuilder.vertex(matrix, (float) endX, (float) y, (float) endZ).next();
            bufferBuilder.vertex(matrix, (float) endX, (float) y, (float) endZ).next();
            bufferBuilder.vertex(matrix, (float) startX, (float) y, (float) endZ).next();
            bufferBuilder.vertex(matrix, (float) startX, (float) y, (float) endZ).next();
            bufferBuilder.vertex(matrix, (float) startX, (float) y, (float) startZ).next();

            // Draw vertical lines from each corner to the top height
            bufferBuilder.vertex(matrix, (float) startX, (float) y, (float) startZ).next();
            bufferBuilder.vertex(matrix, (float) startX, (float) topHeight, (float) startZ).next();

            bufferBuilder.vertex(matrix, (float) endX, (float) y, (float) startZ).next();
            bufferBuilder.vertex(matrix, (float) endX, (float) topHeight, (float) startZ).next();

            bufferBuilder.vertex(matrix, (float) endX, (float) y, (float) endZ).next();
            bufferBuilder.vertex(matrix, (float) endX, (float) topHeight, (float) endZ).next();

            bufferBuilder.vertex(matrix, (float) startX, (float) y, (float) endZ).next();
            bufferBuilder.vertex(matrix, (float) startX, (float) topHeight, (float) endZ).next();

            // Draw the top square at height 100
            bufferBuilder.vertex(matrix, (float) startX, (float) topHeight, (float) startZ).next();
            bufferBuilder.vertex(matrix, (float) endX, (float) topHeight, (float) startZ).next();

            bufferBuilder.vertex(matrix, (float) endX, (float) topHeight, (float) startZ).next();
            bufferBuilder.vertex(matrix, (float) endX, (float) topHeight, (float) endZ).next();

            bufferBuilder.vertex(matrix, (float) endX, (float) topHeight, (float) endZ).next();
            bufferBuilder.vertex(matrix, (float) startX, (float) topHeight, (float) endZ).next();

            bufferBuilder.vertex(matrix, (float) startX, (float) topHeight, (float) endZ).next();
            bufferBuilder.vertex(matrix, (float) startX, (float) topHeight, (float) startZ).next();
        }

        RenderSystem.setShaderColor(r / 255.0f, g / 255.0f, b / 255.0f, alpha / 255.0f);
        tessellator.draw();

        end();
    }

    // Based on code from Meteor client
    private static void bobView (MatrixStack matrices) {

        Entity cameraEntity = Constants.MC_CLIENT_INSTANCE.getCameraEntity();

        if (cameraEntity instanceof PlayerEntity playerEntity) {

            float f = Constants.MC_CLIENT_INSTANCE.getTickDelta();
            float g = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
            float h = -(playerEntity.horizontalSpeed + g * f);
            float i = MathHelper.lerp(f, playerEntity.prevStrideDistance, playerEntity.strideDistance);

            matrices.translate(-(MathHelper.sin(h * 3.1415927f) * i * 0.5),
                    Math.abs(MathHelper.cos(h * 3.1415927f) * i), 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(h * 3.1415927f) * i * 3));
            matrices.multiply(
                    RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(h * 3.1415927f - 0.2f) * i) * 5));
        }
    }

    public void sendClientSideMessage (String message) {

        Constants.MC_CLIENT_INSTANCE.inGameHud.getChatHud().addMessage(Text.literal(message));
    }

    public void renderHUDText (DrawContext drawContext, String text, int x, int y, int color) {

        drawContext.drawText(Constants.MC_CLIENT_INSTANCE.textRenderer, text, x, y, color, false);
    }

    private void end () {

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);

		RenderSystem.setShaderColor(1, 1, 1, 1);

        RenderSystem.setShader(() -> null); // Reset shader to allow for cleanup
    }
}
