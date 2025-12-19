package com.ssws.quickstock;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class ScannerHudOverlay implements HudRenderCallback {

    private static long flashUntil = 0;

    public static void triggerFlash() {
        flashUntil = System.currentTimeMillis() + 250; // 1 second
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (!QuickStockKeyHandler.isScannerEnabled()) return;

        boolean flashing = System.currentTimeMillis() < flashUntil;

        Text text = Text.literal("Scanner: ON");
        int color = flashing ? 0xFFFF5555 : 0xFF55FF55;

        int padding = 4;
        int textWidth = client.textRenderer.getWidth(text);
        int textHeight = client.textRenderer.fontHeight;

        int x = client.getWindow().getScaledWidth() - textWidth - padding * 2 - 10;
        int y = 10;

        // Background (semi-transparent)
        context.fill(
                x - padding,
                y - padding,
                x + textWidth + padding,
                y + textHeight + padding,
                0x88000000
        );

        context.drawTextWithShadow(
                client.textRenderer,
                text,
                x,
                y,
                color
        );
    }
}
