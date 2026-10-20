package com.rotamap.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adaptador de compatibilidade opcional com o mod Rota Roads: Shaper.
 */
public class RotaRoadsCompat {

    public static boolean isRoadsLoaded() {
        return ModList.get().isLoaded("rota_roads");
    }

    public record MappedRoute(
            BlockPos start,
            BlockPos end,
            List<BlockPos> waypoints,
            boolean isConstructed,
            boolean isTunnel,
            String tier
    ) {}

    public record MappedNode(
            BlockPos center,
            String category,
            String structureId,
            boolean isPrimary,
            String tier,
            int pieceCount
    ) {}

    public static List<MappedRoute> getRoutes(ServerLevel level) {
        if (!isRoadsLoaded() || level == null) {
            return Collections.emptyList();
        }

        try {
            var networkData = com.rotaroads.world.data.RoadNetworkData.get(level);
            if (networkData == null) {
                return Collections.emptyList();
            }

            List<MappedRoute> routes = new ArrayList<>();
            for (var seg : networkData.getAllSegments()) {
                routes.add(new MappedRoute(
                        seg.getStart(),
                        seg.getEnd(),
                        seg.getWaypoints(),
                        seg.isConstructed(),
                        seg.isTunnel(),
                        seg.getTier().name()
                ));
            }
            return routes;
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

    public static List<MappedNode> getNodes(ServerLevel level) {
        if (!isRoadsLoaded() || level == null) {
            return Collections.emptyList();
        }

        try {
            var networkData = com.rotaroads.world.data.RoadNetworkData.get(level);
            if (networkData == null) {
                return Collections.emptyList();
            }

            List<MappedNode> nodes = new ArrayList<>();
            for (var node : networkData.getNodes().values()) {
                nodes.add(new MappedNode(
                        node.getCenter(),
                        node.getCategory(),
                        node.getStructureId().toString(),
                        node.isPrimary(),
                        node.getTier().name(),
                        node.getPieceCount()
                ));
            }
            return nodes;
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }
}
