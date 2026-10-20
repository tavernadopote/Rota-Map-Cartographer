package com.rotamap.topography;

/**
 * Segmento vetorial 2D representando uma fatia contínua de curva de nível no relevo.
 */
public record ContourSegment(
        float x1, float z1,
        float x2, float z2,
        int elevation,
        boolean isIndex,
        boolean isWater
) {
    /**
     * Calcula o comprimento euclidiano 2D do segmento no mundo em blocos.
     */
    public float length() {
        return (float) Math.hypot(x2 - x1, z2 - z1);
    }
}
