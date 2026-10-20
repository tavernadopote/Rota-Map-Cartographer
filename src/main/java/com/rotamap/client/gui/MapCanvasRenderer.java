package com.rotamap.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Utilitários de desenho vetorial 2D de alta precisão para a Carta Topográfica.
 */
public class MapCanvasRenderer {

    public static void drawLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, float thickness, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.hypot(dx, dy);
        if (len < 0.5f) return;

        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x1, y1, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(angle));

        int halfT = Math.max(1, Math.round(thickness / 2.0f));
        int l = (int) Math.ceil(len);

        graphics.fill(0, -halfT, l, halfT, color);
        pose.popPose();
    }

    public static void drawDashedLine(GuiGraphics graphics, float x1, float y1, float x2, float y2,
                                      float thickness, float dashLen, float gapLen, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.hypot(dx, dy);
        if (len < 0.5f) return;

        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x1, y1, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(angle));

        int halfT = Math.max(1, Math.round(thickness / 2.0f));
        float step = dashLen + gapLen;

        for (float cur = 0; cur < len; cur += step) {
            float end = Math.min(len, cur + dashLen);
            graphics.fill((int) cur, -halfT, (int) Math.ceil(end), halfT, color);
        }

        pose.popPose();
    }

    public static void drawVillageMarker(GuiGraphics graphics, float cx, float cy, int xColor, int houseColor, boolean drawHouses) {
        float size = 4.0f;
        drawLine(graphics, cx - size, cy - size, cx + size, cy + size, 1.5f, xColor);
        drawLine(graphics, cx - size, cy + size, cx + size, cy - size, 1.5f, xColor);

        if (drawHouses) {
            int hx1 = (int) (cx + 5);
            int hy1 = (int) (cy - 4);
            graphics.fill(hx1, hy1, hx1 + 3, hy1 + 3, houseColor);

            int hx2 = (int) (cx + 9);
            int hy2 = (int) (cy - 2);
            graphics.fill(hx2, hy2, hx2 + 4, hy2 + 3, houseColor);

            int hx3 = (int) (cx + 6);
            int hy3 = (int) (cy + 2);
            graphics.fill(hx3, hy3, hx3 + 3, hy3 + 3, houseColor);
        }
    }

    public static void drawSecondaryMarker(GuiGraphics graphics, float cx, float cy, int color) {
        float r = 4.0f;
        drawLine(graphics, cx, cy - r, cx + r, cy, 1.5f, color);
        drawLine(graphics, cx + r, cy, cx, cy + r, 1.5f, color);
        drawLine(graphics, cx, cy + r, cx - r, cy, 1.5f, color);
        drawLine(graphics, cx - r, cy, cx, cy - r, 1.5f, color);
    }

    public static void drawPlayerPointer(GuiGraphics graphics, float cx, float cy, float yawDegrees, int arrowColor) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(cx, cy, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(yawDegrees + 180f));

        drawLine(graphics, 0, -8, -4, 4, 1.8f, arrowColor);
        drawLine(graphics, 0, -8, 4, 4, 1.8f, arrowColor);
        drawLine(graphics, -4, 4, 0, 2, 1.2f, arrowColor);
        drawLine(graphics, 4, 4, 0, 2, 1.2f, arrowColor);

        graphics.fill(-1, -1, 2, 2, 0xFFFFFFFF);
        pose.popPose();
    }

    public static void drawScaleBar(GuiGraphics graphics, Font font, int x, int y, double zoom, int textColor, int lineColor) {
        int[] roundDistances = {10, 25, 50, 100, 200, 500, 1000, 2000, 5000};
        int selectedDist = 100;
        int barPixelWidth = (int) Math.round(selectedDist * zoom);

        for (int dist : roundDistances) {
            int px = (int) Math.round(dist * zoom);
            if (px >= 40 && px <= 110) {
                selectedDist = dist;
                barPixelWidth = px;
                break;
            }
        }

        graphics.fill(x, y, x + barPixelWidth, y + 2, lineColor);
        graphics.fill(x, y - 3, x + 1, y + 5, lineColor);
        graphics.fill(x + barPixelWidth - 1, y - 3, x + barPixelWidth, y + 5, lineColor);

        String label = selectedDist + " blocos";
        graphics.drawString(font, label, x + (barPixelWidth - font.width(label)) / 2, y + 4, textColor, false);
    }
}
