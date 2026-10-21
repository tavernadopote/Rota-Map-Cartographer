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

    public static void drawRectOutline(GuiGraphics graphics, float x1, float y1, float x2, float y2, float thickness, int color) {
        float left = Math.min(x1, x2);
        float right = Math.max(x1, x2);
        float top = Math.min(y1, y2);
        float bottom = Math.max(y1, y2);

        drawLine(graphics, left, top, right, top, thickness, color);
        drawLine(graphics, right, top, right, bottom, thickness, color);
        drawLine(graphics, right, bottom, left, bottom, thickness, color);
        drawLine(graphics, left, bottom, left, top, thickness, color);
    }

    public static void drawSettlementPerimeter(GuiGraphics graphics, float x1, float y1, float x2, float y2, int borderColor, int fillColor) {
        float left = Math.min(x1, x2);
        float right = Math.max(x1, x2);
        float top = Math.min(y1, y2);
        float bottom = Math.max(y1, y2);

        // Preenchimento sutil translúcido
        if ((fillColor >>> 24) > 0) {
            graphics.fill((int) left, (int) top, (int) Math.ceil(right), (int) Math.ceil(bottom), fillColor);
        }

        // Borda técnica perimétrica tracejada estilo CAD / diagrama cartográfico
        drawDashedLine(graphics, left, top, right, top, 1.2f, 6, 4, borderColor);
        drawDashedLine(graphics, right, top, right, bottom, 1.2f, 6, 4, borderColor);
        drawDashedLine(graphics, right, bottom, left, bottom, 1.2f, 6, 4, borderColor);
        drawDashedLine(graphics, left, bottom, left, top, 1.2f, 6, 4, borderColor);

        // Cantos técnicos sólidos (L-brackets nos 4 cantos)
        float cornerLen = Math.min(8.0f, Math.min(right - left, bottom - top) * 0.25f);
        if (cornerLen >= 2.0f) {
            drawLine(graphics, left, top, left + cornerLen, top, 1.5f, borderColor);
            drawLine(graphics, left, top, left, top + cornerLen, 1.5f, borderColor);

            drawLine(graphics, right, top, right - cornerLen, top, 1.5f, borderColor);
            drawLine(graphics, right, top, right, top + cornerLen, 1.5f, borderColor);

            drawLine(graphics, left, bottom, left + cornerLen, bottom, 1.5f, borderColor);
            drawLine(graphics, left, bottom, left, bottom - cornerLen, 1.5f, borderColor);

            drawLine(graphics, right, bottom, right - cornerLen, bottom, 1.5f, borderColor);
            drawLine(graphics, right, bottom, right, bottom - cornerLen, 1.5f, borderColor);
        }
    }

    public static void drawBuildingBox(GuiGraphics graphics, float x1, float y1, float x2, float y2, int fillColor, int borderColor) {
        float left = Math.min(x1, x2);
        float right = Math.max(x1, x2);
        float top = Math.min(y1, y2);
        float bottom = Math.max(y1, y2);

        // Garante dimensão mínima visível na tela
        if (right - left < 2.0f) right = left + 2.0f;
        if (bottom - top < 2.0f) bottom = top + 2.0f;

        // Preenchimento cinza técnico da edificação
        graphics.fill((int) left, (int) top, (int) Math.ceil(right), (int) Math.ceil(bottom), fillColor);

        // Contorno técnico fino
        drawRectOutline(graphics, left, top, right, bottom, 1.0f, borderColor);
    }

    public static void drawStreetEndpointMarker(GuiGraphics graphics, float cx, float cy, int fillColor, int borderColor) {
        int r = 2;
        // Ponto quadrado com núcleo verde vivo e borda escura de alto contraste
        graphics.fill((int) cx - r, (int) cy - r, (int) cx + r + 1, (int) cy + r + 1, fillColor);
        graphics.fill((int) cx - r - 1, (int) cy - r - 1, (int) cx + r + 2, (int) cy - r, borderColor); // topo
        graphics.fill((int) cx - r - 1, (int) cy + r + 1, (int) cx + r + 2, (int) cy + r + 2, borderColor); // base
        graphics.fill((int) cx - r - 1, (int) cy - r, (int) cx - r, (int) cy + r + 1, borderColor); // esquerda
        graphics.fill((int) cx + r + 1, (int) cy - r, (int) cx + r + 2, (int) cy + r + 1, borderColor); // direita
    }
}
