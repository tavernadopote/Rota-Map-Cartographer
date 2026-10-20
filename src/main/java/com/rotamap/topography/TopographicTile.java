package com.rotamap.topography;

import java.util.Collections;
import java.util.List;

/**
 * Setor quadrado de relevo topográfico em cache (256 x 256 blocos).
 */
public class TopographicTile {
    public static final int TILE_SIZE = 256;
    public static final float SAMPLE_STEP = 4.0f;
    public static final int GRID_POINTS = (int) (TILE_SIZE / SAMPLE_STEP); // 64 células (65 pontos)

    private final int tileX;
    private final int tileZ;
    private final int minWorldX;
    private final int minWorldZ;
    private final int maxWorldX;
    private final int maxWorldZ;

    private short[][] heights;
    private short minHeight = 0;
    private short maxHeight = 0;

    private List<ContourSegment> contours = Collections.emptyList();
    private volatile boolean ready = false;

    public TopographicTile(int tileX, int tileZ) {
        this.tileX = tileX;
        this.tileZ = tileZ;
        this.minWorldX = tileX * TILE_SIZE;
        this.minWorldZ = tileZ * TILE_SIZE;
        this.maxWorldX = minWorldX + TILE_SIZE;
        this.maxWorldZ = minWorldZ + TILE_SIZE;
    }

    public static long packKey(int tileX, int tileZ) {
        return (((long) tileX) << 32) | (tileZ & 0xFFFFFFFFL);
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileZ() {
        return tileZ;
    }

    public int getMinWorldX() {
        return minWorldX;
    }

    public int getMinWorldZ() {
        return minWorldZ;
    }

    public int getMaxWorldX() {
        return maxWorldX;
    }

    public int getMaxWorldZ() {
        return maxWorldZ;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public List<ContourSegment> getContours() {
        return contours;
    }

    public void setContours(List<ContourSegment> contours) {
        this.contours = (contours != null) ? List.copyOf(contours) : Collections.emptyList();
    }

    public short[][] getHeights() {
        return heights;
    }

    public void setHeights(short[][] heights, short minHeight, short maxHeight) {
        this.heights = heights;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
    }

    public short getMinHeight() {
        return minHeight;
    }

    public short getMaxHeight() {
        return maxHeight;
    }

    public int getHeightAt(int worldX, int worldZ) {
        if (!ready || heights == null) {
            return 64;
        }
        int gx = Math.round((worldX - minWorldX) / SAMPLE_STEP);
        int gz = Math.round((worldZ - minWorldZ) / SAMPLE_STEP);
        gx = Math.max(0, Math.min(GRID_POINTS, gx));
        gz = Math.max(0, Math.min(GRID_POINTS, gz));
        return heights[gx][gz];
    }
}
