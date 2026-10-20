package com.rotamap.topography;

import java.util.ArrayList;
import java.util.List;

/**
 * Algoritmo Marching Squares com interpolação linear para extração de curvas de nível contínuas.
 */
public class MarchingSquares {

    public static List<ContourSegment> extractContours(
            short[][] heights,
            float originX,
            float originZ,
            float step,
            int contourInterval,
            int indexInterval,
            int seaLevel
    ) {
        List<ContourSegment> results = new ArrayList<>();
        if (heights == null || heights.length < 2 || heights[0].length < 2) {
            return results;
        }

        int gridW = heights.length - 1;
        int gridH = heights[0].length - 1;

        short globalMin = Short.MAX_VALUE;
        short globalMax = Short.MIN_VALUE;

        for (int gx = 0; gx <= gridW; gx++) {
            for (int gz = 0; gz <= gridH; gz++) {
                short h = heights[gx][gz];
                if (h < globalMin) globalMin = h;
                if (h > globalMax) globalMax = h;
            }
        }

        if (globalMax <= globalMin) {
            return results;
        }

        List<Integer> elevationLevels = new ArrayList<>();
        int startY = ((globalMin / contourInterval) * contourInterval);
        if (startY < globalMin) startY += contourInterval;

        for (int y = startY; y <= globalMax; y += contourInterval) {
            elevationLevels.add(y);
        }

        if (seaLevel >= globalMin && seaLevel <= globalMax && !elevationLevels.contains(seaLevel)) {
            elevationLevels.add(seaLevel);
            elevationLevels.sort(Integer::compareTo);
        }

        for (int gx = 0; gx < gridW; gx++) {
            float x0 = originX + gx * step;
            float x1 = x0 + step;

            for (int gz = 0; gz < gridH; gz++) {
                float z0 = originZ + gz * step;
                float z1 = z0 + step;

                short h0 = heights[gx][gz];         // Top-Left
                short h1 = heights[gx + 1][gz];     // Top-Right
                short h2 = heights[gx + 1][gz + 1]; // Bottom-Right
                short h3 = heights[gx][gz + 1];     // Bottom-Left

                short cellMin = (short) Math.min(Math.min(h0, h1), Math.min(h2, h3));
                short cellMax = (short) Math.max(Math.max(h0, h1), Math.max(h2, h3));

                if (cellMin == cellMax) {
                    continue;
                }

                for (int y : elevationLevels) {
                    if (y < cellMin || y > cellMax) {
                        continue;
                    }

                    boolean isIndex = (y % indexInterval == 0);
                    boolean isWater = (y == seaLevel);

                    int cellIndex = 0;
                    if (h0 >= y) cellIndex |= 1;
                    if (h1 >= y) cellIndex |= 2;
                    if (h2 >= y) cellIndex |= 4;
                    if (h3 >= y) cellIndex |= 8;

                    if (cellIndex == 0 || cellIndex == 15) {
                        continue;
                    }

                    float t0 = (h1 == h0) ? 0.5f : ((float) (y - h0) / (h1 - h0));
                    float e0_x = x0 + Math.max(0.0f, Math.min(1.0f, t0)) * step;
                    float e0_z = z0;

                    float t1 = (h2 == h1) ? 0.5f : ((float) (y - h1) / (h2 - h1));
                    float e1_x = x1;
                    float e1_z = z0 + Math.max(0.0f, Math.min(1.0f, t1)) * step;

                    float t2 = (h2 == h3) ? 0.5f : ((float) (y - h3) / (h2 - h3));
                    float e2_x = x0 + Math.max(0.0f, Math.min(1.0f, t2)) * step;
                    float e2_z = z1;

                    float t3 = (h3 == h0) ? 0.5f : ((float) (y - h0) / (h3 - h0));
                    float e3_x = x0;
                    float e3_z = z0 + Math.max(0.0f, Math.min(1.0f, t3)) * step;

                    switch (cellIndex) {
                        case 1:
                        case 14:
                            results.add(new ContourSegment(e3_x, e3_z, e0_x, e0_z, y, isIndex, isWater));
                            break;
                        case 2:
                        case 13:
                            results.add(new ContourSegment(e0_x, e0_z, e1_x, e1_z, y, isIndex, isWater));
                            break;
                        case 3:
                        case 12:
                            results.add(new ContourSegment(e3_x, e3_z, e1_x, e1_z, y, isIndex, isWater));
                            break;
                        case 4:
                        case 11:
                            results.add(new ContourSegment(e1_x, e1_z, e2_x, e2_z, y, isIndex, isWater));
                            break;
                        case 5: {
                            float centerH = (h0 + h1 + h2 + h3) * 0.25f;
                            if (centerH >= y) {
                                results.add(new ContourSegment(e3_x, e3_z, e0_x, e0_z, y, isIndex, isWater));
                                results.add(new ContourSegment(e1_x, e1_z, e2_x, e2_z, y, isIndex, isWater));
                            } else {
                                results.add(new ContourSegment(e0_x, e0_z, e1_x, e1_z, y, isIndex, isWater));
                                results.add(new ContourSegment(e3_x, e3_z, e2_x, e2_z, y, isIndex, isWater));
                            }
                            break;
                        }
                        case 6:
                        case 9:
                            results.add(new ContourSegment(e0_x, e0_z, e2_x, e2_z, y, isIndex, isWater));
                            break;
                        case 7:
                        case 8:
                            results.add(new ContourSegment(e3_x, e3_z, e2_x, e2_z, y, isIndex, isWater));
                            break;
                        case 10: {
                            float centerH = (h0 + h1 + h2 + h3) * 0.25f;
                            if (centerH >= y) {
                                results.add(new ContourSegment(e0_x, e0_z, e1_x, e1_z, y, isIndex, isWater));
                                results.add(new ContourSegment(e3_x, e3_z, e2_x, e2_z, y, isIndex, isWater));
                            } else {
                                results.add(new ContourSegment(e3_x, e3_z, e0_x, e0_z, y, isIndex, isWater));
                                results.add(new ContourSegment(e1_x, e1_z, e2_x, e2_z, y, isIndex, isWater));
                            }
                            break;
                        }
                    }
                }
            }
        }

        return results;
    }
}
