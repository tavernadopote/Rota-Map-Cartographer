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

    public record PieceBox(int minX, int maxX, int minZ, int maxZ) {}

    public record MappedNode(
            BlockPos center,
            String category,
            String structureId,
            boolean isPrimary,
            String tier,
            int pieceCount,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            List<PieceBox> pieces,
            List<BlockPos> streetEndpoints
    ) {
        public MappedNode(BlockPos center, String category, String structureId, boolean isPrimary, String tier, int pieceCount) {
            this(center, category, structureId, isPrimary, tier, pieceCount,
                    center.getX() - 32, center.getX() + 32, center.getZ() - 32, center.getZ() + 32,
                    Collections.emptyList(), Collections.emptyList());
        }
    }

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
        if (level == null) {
            return Collections.emptyList();
        }

        if (isRoadsLoaded()) {
            try {
                var networkData = com.rotaroads.world.data.RoadNetworkData.get(level);
                if (networkData != null && !networkData.getNodes().isEmpty()) {
                    List<MappedNode> nodes = new ArrayList<>();
                    for (var node : networkData.getNodes().values()) {
                        List<PieceBox> pieces = new ArrayList<>();
                        for (var p : node.getPieces()) {
                            pieces.add(new PieceBox(p.minX(), p.maxX(), p.minZ(), p.maxZ()));
                        }
                        List<BlockPos> endpoints = new ArrayList<>(node.getStreetEndpoints());

                        nodes.add(new MappedNode(
                                node.getCenter(),
                                node.getCategory(),
                                node.getStructureId().toString(),
                                node.isPrimary(),
                                node.getTier().name(),
                                node.getPieceCount(),
                                node.getMinX(),
                                node.getMaxX(),
                                node.getMinZ(),
                                node.getMaxZ(),
                                pieces,
                                endpoints
                        ));
                    }
                    return nodes;
                }
            } catch (Throwable ignored) {}
        }

        return loadNodesFromJson(level);
    }

    private static List<MappedNode> loadNodesFromJson(ServerLevel level) {
        if (level == null || level.getServer() == null) {
            return Collections.emptyList();
        }
        try {
            java.nio.file.Path dataDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data");
            java.nio.file.Path mapFile = dataDir.resolve("rota_roads_vector_map.json");
            if (!java.nio.file.Files.exists(mapFile)) {
                mapFile = dataDir.resolve("rota_roads_map.json");
            }
            if (!java.nio.file.Files.exists(mapFile)) {
                return Collections.emptyList();
            }

            String content = java.nio.file.Files.readString(mapFile);
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(content).getAsJsonObject();
            if (!root.has("nodes")) {
                return Collections.emptyList();
            }

            com.google.gson.JsonArray nodesArr = root.getAsJsonArray("nodes");
            List<MappedNode> list = new ArrayList<>();
            for (com.google.gson.JsonElement el : nodesArr) {
                com.google.gson.JsonObject nObj = el.getAsJsonObject();
                com.google.gson.JsonObject centerObj = nObj.getAsJsonObject("center");
                BlockPos center = new BlockPos(centerObj.get("x").getAsInt(), centerObj.get("y").getAsInt(), centerObj.get("z").getAsInt());
                String category = nObj.get("category").getAsString();
                String structId = nObj.get("structure_id").getAsString();
                boolean isPrimary = nObj.get("is_primary").getAsBoolean();
                String tier = nObj.get("tier").getAsString();
                int pieceCount = nObj.get("piece_count").getAsInt();

                int minX = center.getX() - 32;
                int maxX = center.getX() + 32;
                int minZ = center.getZ() - 32;
                int maxZ = center.getZ() + 32;
                if (nObj.has("bounding_box")) {
                    com.google.gson.JsonObject bb = nObj.getAsJsonObject("bounding_box");
                    minX = bb.get("min_x").getAsInt();
                    maxX = bb.get("max_x").getAsInt();
                    minZ = bb.get("min_z").getAsInt();
                    maxZ = bb.get("max_z").getAsInt();
                }

                List<PieceBox> pieces = new ArrayList<>();
                if (nObj.has("pieces")) {
                    for (com.google.gson.JsonElement pEl : nObj.getAsJsonArray("pieces")) {
                        com.google.gson.JsonObject pObj = pEl.getAsJsonObject();
                        pieces.add(new PieceBox(
                                pObj.get("min_x").getAsInt(),
                                pObj.get("max_x").getAsInt(),
                                pObj.get("min_z").getAsInt(),
                                pObj.get("max_z").getAsInt()
                        ));
                    }
                }

                List<BlockPos> endpoints = new ArrayList<>();
                if (nObj.has("street_endpoints")) {
                    for (com.google.gson.JsonElement epEl : nObj.getAsJsonArray("street_endpoints")) {
                        com.google.gson.JsonObject epObj = epEl.getAsJsonObject();
                        endpoints.add(new BlockPos(
                                epObj.get("x").getAsInt(),
                                epObj.get("y").getAsInt(),
                                epObj.get("z").getAsInt()
                        ));
                    }
                }

                list.add(new MappedNode(center, category, structId, isPrimary, tier, pieceCount, minX, maxX, minZ, maxZ, pieces, endpoints));
            }
            return list;
        } catch (Throwable ignored) {
            return Collections.emptyList();
        }
    }
}
