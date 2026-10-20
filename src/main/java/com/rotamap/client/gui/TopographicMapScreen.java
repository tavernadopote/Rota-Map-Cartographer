package com.rotamap.client.gui;

import com.rotacore.RotaCore;
import com.rotamap.compat.RotaRoadsCompat;
import com.rotamap.topography.ContourSegment;
import com.rotamap.topography.TopographicService;
import com.rotamap.topography.TopographicTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Tela de Cartografia Topográfica com curvas de nível matemáticas (Marching Squares),
 * LOD adaptativo, anti-aliasing por descarte de sub-pixel e rede de estradas.
 */
public class TopographicMapScreen extends Screen {

    private final Screen parentScreen;

    // Coordenadas da câmera no mundo (centro da visualização)
    private double cameraX;
    private double cameraZ;

    // Fator de ampliação (pixels por bloco)
    private double zoom = 1.0;

    // Controle de arrasto com o mouse (pan)
    private boolean isDragging = false;
    private double dragLastMouseX = 0;
    private double dragLastMouseY = 0;

    // Alternadores de camadas visuais
    private boolean showContours = true;
    private boolean showRoads = true;
    private boolean showVillages = true;
    private boolean showGrid = true;
    private boolean showPlayer = true;

    // Botões de interface
    private Button btnContours;
    private Button btnRoads;
    private Button btnVillages;
    private Button btnGrid;

    public TopographicMapScreen(Screen parentScreen) {
        super(Component.literal("Carta Topográfica Regional"));
        this.parentScreen = parentScreen;

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            this.cameraX = player.getX();
            this.cameraZ = player.getZ();
        } else {
            this.cameraX = 0;
            this.cameraZ = 0;
        }
    }

    @Override
    protected void init() {
        int btnW = 80;
        int btnH = 20;

        // Botões no Cabeçalho Superior Direito
        int topBtnY = 5;
        int curX = this.width - btnW - 10;

        this.addRenderableWidget(Button.builder(Component.literal("Fechar"), btn -> this.onClose())
                .bounds(curX, topBtnY, btnW, btnH).build());

        curX -= (btnW + 4);
        this.addRenderableWidget(Button.builder(Component.literal("Jogador"), btn -> centerOnPlayer())
                .bounds(curX, topBtnY, btnW, btnH).build());

        curX -= (btnW + 4);
        this.addRenderableWidget(Button.builder(Component.literal("Spawn"), btn -> centerOnSpawn())
                .bounds(curX, topBtnY, btnW, btnH).build());

        // Botões de Camadas no Rodapé (lado esquerdo)
        int footerY = this.height - 25;
        int layerBtnW = 70;
        int startLayerX = 8;

        btnContours = Button.builder(Component.literal("Curvas: ON"), btn -> {
            showContours = !showContours;
            btn.setMessage(Component.literal("Curvas: " + (showContours ? "ON" : "OFF")));
        }).bounds(startLayerX, footerY, layerBtnW, btnH).build();
        this.addRenderableWidget(btnContours);

        btnRoads = Button.builder(Component.literal("Estradas: ON"), btn -> {
            showRoads = !showRoads;
            btn.setMessage(Component.literal("Estradas: " + (showRoads ? "ON" : "OFF")));
        }).bounds(startLayerX + (layerBtnW + 2), footerY, layerBtnW, btnH).build();
        this.addRenderableWidget(btnRoads);

        btnVillages = Button.builder(Component.literal("Vilas: ON"), btn -> {
            showVillages = !showVillages;
            btn.setMessage(Component.literal("Vilas: " + (showVillages ? "ON" : "OFF")));
        }).bounds(startLayerX + (layerBtnW + 2) * 2, footerY, layerBtnW, btnH).build();
        this.addRenderableWidget(btnVillages);

        btnGrid = Button.builder(Component.literal("Grade: ON"), btn -> {
            showGrid = !showGrid;
            btn.setMessage(Component.literal("Grade: " + (showGrid ? "ON" : "OFF")));
        }).bounds(startLayerX + (layerBtnW + 2) * 3, footerY, layerBtnW, btnH).build();
        this.addRenderableWidget(btnGrid);

        // Botões de Zoom no canto inferior direito
        int zoomBtnW = 22;
        int zoomX = this.width - (zoomBtnW * 2 + 10);

        this.addRenderableWidget(Button.builder(Component.literal("+"), btn -> adjustZoom(1.3))
                .bounds(zoomX, footerY, zoomBtnW, btnH).build());

        this.addRenderableWidget(Button.builder(Component.literal("-"), btn -> adjustZoom(1.0 / 1.3))
                .bounds(zoomX + zoomBtnW + 2, footerY, zoomBtnW, btnH).build());
    }

    private void centerOnPlayer() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            this.cameraX = player.getX();
            this.cameraZ = player.getZ();
        }
    }

    private void centerOnSpawn() {
        ServerLevel level = getActiveServerLevel();
        if (level != null) {
            BlockPos spawn = level.getSharedSpawnPos();
            this.cameraX = spawn.getX();
            this.cameraZ = spawn.getZ();
        }
    }

    private void adjustZoom(double factor) {
        this.zoom = Math.max(0.06, Math.min(6.0, this.zoom * factor));
    }

    private ServerLevel getActiveServerLevel() {
        var server = Minecraft.getInstance().getSingleplayerServer();
        if (server != null && Minecraft.getInstance().level != null) {
            return server.getLevel(Minecraft.getInstance().level.dimension());
        }
        return null;
    }

    private int getViewportCenterX() {
        return this.width / 2;
    }

    private int getViewportCenterY() {
        return this.height / 2;
    }

    private float worldToScreenX(double worldX) {
        return (float) (getViewportCenterX() + (worldX - cameraX) * zoom);
    }

    private float worldToScreenY(double worldZ) {
        return (float) (getViewportCenterY() + (worldZ - cameraZ) * zoom);
    }

    private double screenToWorldX(double screenX) {
        return cameraX + (screenX - getViewportCenterX()) / zoom;
    }

    private double screenToWorldZ(double screenY) {
        return cameraZ + (screenY - getViewportCenterY()) / zoom;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int headerH = 30;
        int footerH = 30;
        int mapY = headerH;
        int mapH = this.height - headerH - footerH;

        // 1. Fundo de Papel Cartográfico (Off-white / Pergaminho Rústico)
        graphics.fill(0, 0, this.width, this.height, 0xFFFAF8F5);

        // 2. Limita o desenho à área útil do mapa
        graphics.enableScissor(0, mapY, this.width, mapY + mapH);

        ServerLevel serverLevel = getActiveServerLevel();

        double minWorldX = screenToWorldX(0);
        double maxWorldX = screenToWorldX(this.width);
        double minWorldZ = screenToWorldZ(mapY);
        double maxWorldZ = screenToWorldZ(mapY + mapH);

        // 3. Grade Regional Determinística (38 x 38 chunks)
        if (showGrid) {
            renderRegionalGrid(graphics, minWorldX, minWorldZ, maxWorldX, maxWorldZ);
        }

        // 4. Curvas de Nível Topográficas (Marching Squares com LOD e Anti-aliasing)
        if (showContours) {
            renderContours(graphics, serverLevel, (int) minWorldX, (int) minWorldZ, (int) maxWorldX, (int) maxWorldZ);
        }

        // 5. Malha de Estradas
        if (showRoads) {
            renderRoads(graphics, serverLevel, minWorldX, minWorldZ, maxWorldX, maxWorldZ);
        }

        // 6. Vilas e Estruturas de Aventura
        RotaRoadsCompat.MappedNode hoveredNode = null;
        if (showVillages) {
            hoveredNode = renderVillages(graphics, serverLevel, mouseX, mouseY);
        }

        // 7. Posição e Direção do Jogador
        if (showPlayer) {
            renderPlayer(graphics);
        }

        graphics.disableScissor();

        // 8. Cabeçalho Superior
        graphics.fill(0, 0, this.width, headerH, 0xFFEFECE6);
        graphics.fill(0, headerH - 1, this.width, headerH, 0xFFD0CCC2);
        graphics.drawString(this.font, "§8§lCARTA TOPOGRÁFICA§r §7| Rota Map", 12, 11, 0xFF333333, false);

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            String pInfo = String.format("§8Posição: §6X=%.0f, Y=%.0f, Z=%.0f", player.getX(), player.getY(), player.getZ());
            int pInfoWidth = this.font.width(pInfo);
            int maxPInfoRight = this.width - 260; // Não invade os botões da direita
            int pInfoX = Math.min(200, maxPInfoRight - pInfoWidth);
            if (pInfoX > 165) {
                graphics.drawString(this.font, pInfo, pInfoX, 11, 0xFF555555, false);
            }
        }

        // 9. Rodapé Inferior
        graphics.fill(0, this.height - footerH, this.width, this.height, 0xFFEFECE6);
        graphics.fill(0, this.height - footerH, this.width, this.height - footerH + 1, 0xFFD0CCC2);

        // Coordenadas sob o cursor do mouse e elevação estimada (centro-esquerda)
        int scaleBarX = this.width - 150;
        if (mouseY >= mapY && mouseY <= mapY + mapH) {
            int cursorWX = (int) Math.round(screenToWorldX(mouseX));
            int cursorWZ = (int) Math.round(screenToWorldZ(mouseY));
            int elevY = TopographicService.getInstance().getEstimatedElevation(serverLevel, cursorWX, cursorWZ);
            String cursorInfo = String.format("§8X: §e%d §8Z: §e%d §7| §8Y: §a%dm", cursorWX, cursorWZ, elevY);
            if (302 + this.font.width(cursorInfo) < scaleBarX) {
                graphics.drawString(this.font, cursorInfo, 302, this.height - 19, 0xFF444444, false);
            }
        }

        // Régua de Escala Cartográfica (canto direito antes dos botões de zoom)
        MapCanvasRenderer.drawScaleBar(graphics, this.font, scaleBarX, this.height - 24, this.zoom, 0xFF444444, 0xFF888888);

        // 10. Desenha os Widgets (Botões) diretamente sem acionar fundo do Minecraft
        for (var widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }

        // 11. Tooltip ao passar o cursor sobre uma vila ou posto
        if (hoveredNode != null) {
            renderNodeTooltip(graphics, hoveredNode, mouseX, mouseY);
        }
    }

    private void renderRegionalGrid(GuiGraphics graphics, double minX, double minZ, double maxX, double maxZ) {
        int cellSizeChunks = RotaCore.getConfig().regionalCellSizeChunks;
        if (cellSizeChunks < 16) cellSizeChunks = 38;
        int cellSizeBlocks = cellSizeChunks * 16;

        int startCellX = (int) Math.floor(minX / cellSizeBlocks);
        int endCellX = (int) Math.floor(maxX / cellSizeBlocks);
        int startCellZ = (int) Math.floor(minZ / cellSizeBlocks);
        int endCellZ = (int) Math.floor(maxZ / cellSizeBlocks);

        int gridColor = 0xFFD8D4CA;

        for (int cx = startCellX; cx <= endCellX + 1; cx++) {
            float sx = worldToScreenX(cx * cellSizeBlocks);
            MapCanvasRenderer.drawDashedLine(graphics, sx, 0, sx, this.height, 1.0f, 6, 4, gridColor);
        }

        for (int cz = startCellZ; cz <= endCellZ + 1; cz++) {
            float sy = worldToScreenY(cz * cellSizeBlocks);
            MapCanvasRenderer.drawDashedLine(graphics, 0, sy, this.width, sy, 1.0f, 6, 4, gridColor);
        }
    }

    private void renderContours(GuiGraphics graphics, ServerLevel level, int minX, int minZ, int maxX, int maxZ) {
        List<TopographicTile> tiles = TopographicService.getInstance().getTilesForArea(level, minX, minZ, maxX, maxZ);

        // --- LOD Cartográfico Inteligente ---
        // Se zoom < 0.55 (panorâmico): apenas curvas mestras (50m) e água (63)
        // Se 0.55 <= zoom < 1.2 (médio): curvas a cada 25m e água (63)
        // Se zoom >= 1.2 (próximo): todas as curvas (10m)
        boolean isMacroZoom = this.zoom < 0.55;
        boolean isMediumZoom = this.zoom >= 0.55 && this.zoom < 1.2;

        for (TopographicTile tile : tiles) {
            if (!tile.isReady()) {
                continue;
            }

            for (ContourSegment seg : tile.getContours()) {
                // Filtro de LOD: oculta curvas densas conforme a distância
                if (isMacroZoom) {
                    if (!seg.isWater() && seg.elevation() % 50 != 0) {
                        continue;
                    }
                } else if (isMediumZoom) {
                    if (!seg.isWater() && seg.elevation() % 25 != 0) {
                        continue;
                    }
                }

                // Anti-Aliasing: descarta microsegmentos menores que 1.1 pixel na tela
                float segLenPx = seg.length() * (float) this.zoom;
                if (segLenPx < 1.1f && !seg.isWater()) {
                    continue;
                }

                float sx1 = worldToScreenX(seg.x1());
                float sy1 = worldToScreenY(seg.z1());
                float sx2 = worldToScreenX(seg.x2());
                float sy2 = worldToScreenY(seg.z2());

                // Culling fora da janela visual
                if ((sx1 < 0 && sx2 < 0) || (sx1 > this.width && sx2 > this.width)
                        || (sy1 < 30 && sy2 < 30) || (sy1 > this.height - 30 && sy2 > this.height - 30)) {
                    continue;
                }

                int color;
                float thickness;

                if (seg.isWater()) {
                    color = 0xFF7697A8; // Azul acinzentado de costa/lago
                    thickness = 1.3f;
                } else if (seg.isIndex()) {
                    // Curva mestra: suavizada no zoom distante para evitar serrilhado
                    color = isMacroZoom ? 0xFF656560 : 0xFF545450;
                    thickness = isMacroZoom ? 1.2f : 1.5f;
                } else {
                    color = 0xFFB8B8B2; // Curva intermediária fina
                    thickness = 1.0f;
                }

                MapCanvasRenderer.drawLine(graphics, sx1, sy1, sx2, sy2, thickness, color);
            }
        }
    }

    private void renderRoads(GuiGraphics graphics, ServerLevel level, double minX, double minZ, double maxX, double maxZ) {
        List<RotaRoadsCompat.MappedRoute> routes = RotaRoadsCompat.getRoutes(level);

        for (var route : routes) {
            List<BlockPos> waypoints = route.waypoints();

            if (!waypoints.isEmpty()) {
                for (int i = 0; i < waypoints.size() - 1; i++) {
                    BlockPos p1 = waypoints.get(i);
                    BlockPos p2 = waypoints.get(i + 1);

                    float sx1 = worldToScreenX(p1.getX());
                    float sy1 = worldToScreenY(p1.getZ());
                    float sx2 = worldToScreenX(p2.getX());
                    float sy2 = worldToScreenY(p2.getZ());

                    if (route.isConstructed()) {
                        MapCanvasRenderer.drawLine(graphics, sx1, sy1, sx2, sy2, 2.0f, 0xFF2A2A2A);
                    } else if (route.isTunnel()) {
                        MapCanvasRenderer.drawDashedLine(graphics, sx1, sy1, sx2, sy2, 1.8f, 4, 3, 0xFF7A4B3A);
                    } else {
                        MapCanvasRenderer.drawDashedLine(graphics, sx1, sy1, sx2, sy2, 1.6f, 6, 3, 0xFF4E3728);
                    }
                }
            } else {
                float sx1 = worldToScreenX(route.start().getX());
                float sy1 = worldToScreenY(route.start().getZ());
                float sx2 = worldToScreenX(route.end().getX());
                float sy2 = worldToScreenY(route.end().getZ());
                MapCanvasRenderer.drawDashedLine(graphics, sx1, sy1, sx2, sy2, 1.0f, 4, 4, 0xFFA09080);
            }
        }
    }

    private RotaRoadsCompat.MappedNode renderVillages(GuiGraphics graphics, ServerLevel level, int mouseX, int mouseY) {
        RotaRoadsCompat.MappedNode hovered = null;
        List<RotaRoadsCompat.MappedNode> nodes = RotaRoadsCompat.getNodes(level);

        boolean drawLabels = this.zoom >= 0.65;
        boolean drawHouses = this.zoom >= 0.75;

        for (var node : nodes) {
            BlockPos center = node.center();
            float sx = worldToScreenX(center.getX());
            float sy = worldToScreenY(center.getZ());

            if (sx < -20 || sx > this.width + 20 || sy < 10 || sy > this.height - 10) {
                continue;
            }

            if (node.isPrimary()) {
                MapCanvasRenderer.drawVillageMarker(graphics, sx, sy, 0xFF222222, 0xFF4A4A4A, drawHouses);
                if (drawLabels) {
                    graphics.drawString(this.font, "§8" + node.category(), (int) sx + 12, (int) sy - 4, 0xFF333333, false);
                }
            } else {
                MapCanvasRenderer.drawSecondaryMarker(graphics, sx, sy, 0xFF8B2500);
                if (drawLabels) {
                    graphics.drawString(this.font, "§7" + node.category(), (int) sx + 7, (int) sy - 4, 0xFF555555, false);
                }
            }

            if (Math.abs(mouseX - sx) <= 12 && Math.abs(mouseY - sy) <= 12) {
                hovered = node;
            }
        }

        return hovered;
    }

    private void renderPlayer(GuiGraphics graphics) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        float sx = worldToScreenX(player.getX());
        float sy = worldToScreenY(player.getZ());

        if (sx >= -10 && sx <= this.width + 10 && sy >= 20 && sy <= this.height - 20) {
            MapCanvasRenderer.drawPlayerPointer(graphics, sx, sy, player.getYRot(), 0xFFD32F2F);
        }
    }

    private void renderNodeTooltip(GuiGraphics graphics, RotaRoadsCompat.MappedNode node, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("§6§l" + node.category() + "§r " + (node.isPrimary() ? "§a(Vila)" : "§e(Secundária)")));
        tooltip.add(Component.literal(String.format("§7Coordenadas: §fX=%d, Y=%d, Z=%d",
                node.center().getX(), node.center().getY(), node.center().getZ())));
        tooltip.add(Component.literal("§7ID da Estrutura: §8" + node.structureId()));
        tooltip.add(Component.literal("§7Porte: §f" + node.tier() + " (" + node.pieceCount() + " peças)"));

        graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY > 30 && mouseY < this.height - 30) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1 || button == GLFW.GLFW_MOUSE_BUTTON_2) {
                isDragging = true;
                dragLastMouseX = mouseX;
                dragLastMouseY = mouseY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            cameraX -= dragX / zoom;
            cameraZ -= dragY / zoom;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseY > 30 && mouseY < this.height - 30) {
            double worldXBefore = screenToWorldX(mouseX);
            double worldZBefore = screenToWorldZ(mouseY);

            double factor = (scrollY > 0) ? 1.25 : (1.0 / 1.25);
            adjustZoom(factor);

            cameraX = worldXBefore - (mouseX - getViewportCenterX()) / zoom;
            cameraZ = worldZBefore - (mouseY - getViewportCenterY()) / zoom;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_M || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        double moveSpeed = 40.0 / zoom;
        if (keyCode == GLFW.GLFW_KEY_W || keyCode == GLFW.GLFW_KEY_UP) {
            cameraZ -= moveSpeed;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_S || keyCode == GLFW.GLFW_KEY_DOWN) {
            cameraZ += moveSpeed;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_LEFT) {
            cameraX -= moveSpeed;
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_D || keyCode == GLFW.GLFW_KEY_RIGHT) {
            cameraX += moveSpeed;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Bloqueia intencionalmente o fundo do menu e o blur shader do Minecraft
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        // Bloqueia explicitamente o pós-processamento de blur do GameRenderer
    }

    @Override
    protected void renderMenuBackground(GuiGraphics graphics) {
        // Bloqueia o preenchimento escuro de fundo de menu
    }

    @Override
    protected void renderMenuBackground(GuiGraphics graphics, int x, int y, int width, int height) {
        // Bloqueia o preenchimento escuro de fundo de menu
    }

    @Override
    public void renderTransparentBackground(GuiGraphics graphics) {
        // Bloqueia qualquer escurecimento transparente
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }
}
