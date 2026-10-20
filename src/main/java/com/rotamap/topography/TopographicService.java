package com.rotamap.topography;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Serviço assíncrono para amostragem matemática do relevo do mundo e extração
 * de curvas de nível em segundo plano sem bloquear o jogo.
 */
public class TopographicService {
    private static final Logger LOGGER = LoggerFactory.getLogger("rota_map/topography");
    private static final TopographicService INSTANCE = new TopographicService();

    private final Map<Long, TopographicTile> tileCache = new ConcurrentHashMap<>(256);
    private final ExecutorService worker = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private int count = 0;
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "RotaMap-TopographyWorker-" + (++count));
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        }
    });

    public static TopographicService getInstance() {
        return INSTANCE;
    }

    private TopographicService() {}

    public void clearCache() {
        tileCache.clear();
    }

    public TopographicTile getOrQueueTile(ServerLevel level, int tileX, int tileZ) {
        long key = TopographicTile.packKey(tileX, tileZ);
        TopographicTile tile = tileCache.get(key);
        if (tile != null) {
            return tile;
        }

        TopographicTile newTile = new TopographicTile(tileX, tileZ);
        TopographicTile existing = tileCache.putIfAbsent(key, newTile);
        if (existing != null) {
            return existing;
        }

        worker.submit(() -> computeTile(level, newTile));
        return newTile;
    }

    public List<TopographicTile> getTilesForArea(ServerLevel level, int minX, int minZ, int maxX, int maxZ) {
        int startTileX = Math.floorDiv(minX, TopographicTile.TILE_SIZE);
        int endTileX = Math.floorDiv(maxX, TopographicTile.TILE_SIZE);
        int startTileZ = Math.floorDiv(minZ, TopographicTile.TILE_SIZE);
        int endTileZ = Math.floorDiv(maxZ, TopographicTile.TILE_SIZE);

        List<TopographicTile> tiles = new ArrayList<>();
        for (int tx = startTileX; tx <= endTileX; tx++) {
            for (int tz = startTileZ; tz <= endTileZ; tz++) {
                tiles.add(getOrQueueTile(level, tx, tz));
            }
        }
        return tiles;
    }

    public int getEstimatedElevation(ServerLevel level, int worldX, int worldZ) {
        int tx = Math.floorDiv(worldX, TopographicTile.TILE_SIZE);
        int tz = Math.floorDiv(worldZ, TopographicTile.TILE_SIZE);
        long key = TopographicTile.packKey(tx, tz);
        TopographicTile tile = tileCache.get(key);
        if (tile != null && tile.isReady()) {
            return tile.getHeightAt(worldX, worldZ);
        }

        if (level != null) {
            try {
                ChunkGenerator gen = level.getChunkSource().getGenerator();
                RandomState rs = level.getChunkSource().randomState();
                return gen.getBaseHeight(worldX, worldZ, Heightmap.Types.WORLD_SURFACE_WG, level, rs);
            } catch (Exception ignored) {}
        }
        return 64;
    }

    private void computeTile(ServerLevel level, TopographicTile tile) {
        try {
            ServerLevel activeLevel = level;
            if (activeLevel == null) {
                var server = Minecraft.getInstance().getSingleplayerServer();
                if (server != null && Minecraft.getInstance().level != null) {
                    activeLevel = server.getLevel(Minecraft.getInstance().level.dimension());
                }
            }

            if (activeLevel == null) {
                return;
            }

            ChunkGenerator generator = activeLevel.getChunkSource().getGenerator();
            RandomState randomState = activeLevel.getChunkSource().randomState();
            int seaLevel = activeLevel.getSeaLevel();

            int points = TopographicTile.GRID_POINTS;
            float step = TopographicTile.SAMPLE_STEP;
            int originX = tile.getMinWorldX();
            int originZ = tile.getMinWorldZ();

            short[][] heights = new short[points + 1][points + 1];
            short minH = Short.MAX_VALUE;
            short maxH = Short.MIN_VALUE;

            for (int gx = 0; gx <= points; gx++) {
                int wx = Math.round(originX + gx * step);
                for (int gz = 0; gz <= points; gz++) {
                    int wz = Math.round(originZ + gz * step);

                    int h = generator.getBaseHeight(wx, wz, Heightmap.Types.WORLD_SURFACE_WG, activeLevel, randomState);
                    short sh = (short) Math.max(-64, Math.min(320, h));
                    heights[gx][gz] = sh;

                    if (sh < minH) minH = sh;
                    if (sh > maxH) maxH = sh;
                }
            }

            // Amostragem com curvas normais a cada 10m e mestras a cada 50m
            List<ContourSegment> contours = MarchingSquares.extractContours(
                    heights,
                    originX,
                    originZ,
                    step,
                    10,
                    50,
                    seaLevel
            );

            tile.setHeights(heights, minH, maxH);
            tile.setContours(contours);
            tile.setReady(true);

        } catch (Throwable t) {
            LOGGER.debug("Erro ao calcular tile topográfico [{}, {}]: {}", tile.getTileX(), tile.getTileZ(), t.getMessage());
        }
    }
}
