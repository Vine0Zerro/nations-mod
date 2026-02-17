package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class BlueMapIntegration {

    private static boolean enabled = false;
    private static Object blueMapAPI = null;
    private static final String MARKER_SET_ID = "nations_towns";

    private static final String ICON_CROWN_BASE64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAIGNIUk0AAHomAACAhAAA+gAAAIDoAAB1MAAA6mAAADqYAAAXcJy6UTwAAAAGYktHRAD/AP8A/6C9p5MAAAAHdElNRQfqAhAVGgIVHGzXAAAGhElEQVRYw+2UWWycVxmGn3P+ZRbPjMfLJLYnEzvxxHacRFGc1W1os9BAVUhD2oQ1oqSoSKgSUqSACkKIm0ZCAgnBDRKIRaKsJcAVkKYlgAhJVFGTqLHjxHGceK+X2edfzn+4iGPVSShSr/1e/f8nne8833tefbCsZS3rfajHSnGm6/u0yxBZGQLaWCui76uXuPeRMUIEWiNFgLlQu6l82o0QGlAEyIUDa6wYTqD4h5sH4HEzQY1pMuAV0WgUGolAAEPKI2vYaASKALFYd5eSXOraz8mmdQCysP5TAHQYYQAupvcDyBOxtej0J9kTSrLbTDR8rbX71It1a071ynj9vlAdOvMcJ2JrAWTfqgMAZI0QAPrwlwDk58MZXqndvnivsWiF8hnzKl3PtrSeemXymv/WxqOD35v4D6+t+ADfzl098Fi08aVbXnHorDM65WgFQuzcG0l9t9kI7ex3C69HhTH81+oId1R5076aFS+fKY1Vf5DsufH7yigzx7/Ci2dffeqJhuYvDxbmB0ZUaeayKgIg7wGsIorts2eDijyfsuwvijd/aHnKp2fyDZkQ1tF2ET8ewjjQKRMMu0WuunMDl1ZUhvoy/tCAM9c/4hXplElsjAPtIn68VlhHHp3+m/SVQvzoZZOy+sJmUfN8c010z47G1KID956b86V3yLneq3+uu/O01VWzZtUFO2UYYuxpOxEM6/wvrFTukFd2R66E8ySqMUQhtOuRI5mskIZ+beD2rkjCPt0XGsKtMnIzUpkZHueXB+N1wdtumVZtriitl2v/MjH6p7Jp/m7ALC4CLDoQlybZLj096M1d3LE10b3v0daTNxwn5CdzeDWlzJZN1fptnfqZn95Q9vnRmdj2Xe0v9G5tiPT2NEZ39na88K/b07GfXdf29g71TM+mSr1fU8roZIHrjmM/vitzcvfO5IZ+d/ZCtLk43RAKPZiBKSWwqVCtqOjWtHc4uz69bX46GD5zM9+3rck48UiH2BwNkYl6+pIh67o+e2zLiU1t81bELEM4nb7x1uS/n8hU0tvbxUv1NSI8OEHh9LB/ek+25dMf+2DzN9TELf/CleJ3ilXv+rnRMjntLgWICRvXCbjpUIgH7kf3d4uWTGdbb2nSiWxorHy4OyMa42HCUosd2Z3bHjt0oCYt3Unwq6QzDZar6je0ieGPrGsSacuEmbwORSKpumOHs1/NmqN1fzg3fe3cKN/SjigqDHLaWwpQwiUpTKZ0UF0dlY90JcubulqIbd3dta9YtRpL82XmipLw6u7GJw+uXRk3bqMDjUYTCkdo39izcuzObOPsxDyzRRNjZVvjs4fW7WuXt2LjQ+P8c1CfvZLzf55EBMPaeTCEAAqXj6+T/tCkHprIa1KT43QmfDqebGPgTj3KsOjc0EAiVsIveHe3ktZIs4GGUJm9T61isL8Ww3PoWG0hZ/u5NTHNRB7G5oMbn8ha/sWbCtRDNiHARtGAa83j+6L3yDbxx70bzcZkvU08YaENC2WHwbYh1ARoQCPQCCtJ4ObRzhSGEUXkxxHKpVjwmZtxeOOyeuc3b+qDpqnP41lc09WHOyAEaCXxA3LKtNzAMpguGOQcgWX4uE6OqjKoTVv4jvMu/ElME/ITOcL2PJap8HyB6xoYlkVgC9cPvHkjEEsnvh/gcjDDOsOihJ4pFIPBqQndEqtRJOoldbUhyr5JY2s9MplidmQYv+KDBiNqkVwRhaYYM3cKRE3N3IxDoRBQLAfkCnqwBLMhLZZMv2QPLOZAKH79OXvScYIrQui7ryQNJucEnqNwSy6VgsBxazGjKaxYCs9NUC0a+A4ExJmaFwgpQYBA41SDy796zp70hbr/uqUO3C2YHPuxl/7QZmuLnbBQtsFYRTCbU+QLCvH2DNJyQZoLGVhIgl9FBx7aCJMIKxpqTWTEwDYURsTt+cxP3JaEaYxB8N4AgdaEpQiXq1q/3ucGtXYgpQ4IPLBFcDcnFB9MMCCEQOt5ZrVkxAQlJAVPumUHPyoJa/QDDtzfg9UhMJVB1Rer4ia7m5Iia0g2NseRWuj1fkCtRmjxkE4ChGmQIxBXx/NBoJS4MpGjb9YPLsZNNWkYMOj8H4BuGWVoZZnWaQtpaI7uCPjm320JQiTwUzY6rHnIKAsMLqKaR06D0F/f7QW/vSRQCiJ+K1VjlAFVfW+Ad6vDiCCRaBGggUD7PPzue0kQC38mApAINJp+VWFZy1rW/9J/AQd641z/rmCoAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDI2LTAyLTE2VDIxOjI1OjE4KzAwOjAwoxa5cAAAACV0RVh0ZGF0ZTptb2RpZnkAMjAyNi0wMi0xNlQyMToyNToxOCswMDowMNJLAcwAAAAodEVYdGRhdGU6dGltZXN0YW1wADIwMjYtMDItMTZUMjE6MjY6MDIrMDA6MDAGs8TAAAAAAElFTkSuQmCC";
    private static final String ICON_TOWN_BASE64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAIGNIUk0AAHomAACAhAAA+gAAAIDoAAB1MAAA6mAAADqYAAAXcJy6UTwAAAEpUExURQAAAM/P0dDQ0svLzczMzvfz9vv7+/z7++Tm5tzd2/r5+fj49/n19/79/fX19P/+/v38/P39/f///+rq6cfHxt7e3qenpt/f3qiop9XU1KalpcC+vqKhocG/v8/PzquqqqGfn6OiocG7v8bFxMLAwNTU07q6uaWjpKqpqaaipa6qrLOusamlp6yoqv///9/h4dja2f////j49u3t7NLU1MbHxevr6vr6+erq6e3t7N3d3MbGxt7e3uDf37e3tqempt/f3t/f37a2tainp9TU09ra2bKxsaWlpL68vMvKydTT09XV1Lu7u7Kysainp5+enry6usHAv7Cwr5yam+np6NjY2NLS0ubm5eXl5d3d3MnJyN/f39vb27+/v7q6ud7e3tva2r+/vv///+VauVgAAABUdFJOUwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKU1MITbP29rNNOe3tOUD29kBA9vZAOe3tOAhNs/f2s0wIClxcCsucjc0AAAABYktHRBJ7vGwAAAAAB3RJTUUH6gIQFR8CaGuYkgAAAIxJREFUGNNjYCASMDIxsyBxWdnYOTi5uHlgfHZePj19Az0+fgGogKChkbGJqZm5oRBUQNjCMiQ0LNzKWgQqIGpjGxESGWVnLwYVEHdwjI6JjXNyloAKSLq4xickxrm5S0EFpD08vbx9fP38ZaACstJyAYFBwfIKijCHKCmrqKqpa2giuVVLW0eXWG8CAKATE9MHZILXAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDI2LTAyLTE2VDIxOjMwOjU0KzAwOjAwx1vJegAAACV0RVh0ZGF0ZTptb2RpZnkAMjAyNi0wMi0xNlQyMTozMDo1NCswMDowMLYGccYAAAAodEVYdGRhdGU6dGltZXN0YW1wADIwMjYtMDItMTZUMjE6MzE6MDIrMDA6MDAl4QB5AAAAAElFTkSuQmCC";

    private static Class<?> clsBlueMapAPI, clsBlueMapMap, clsMarkerSet, clsShapeMarker, clsPOIMarker, clsShape, clsVector2d, clsColor;
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets;
    private static Method mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape, mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor, mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mPOIMarkerBuilder, mPOIMarkerLabel, mPOIMarkerPosition, mPOIMarkerDetail, mPOIMarkerIcon, mPOIMarkerBuild;
    private static Constructor<?> cVector2d, cShape, cColor;

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) return;
        try {
            loadClasses();
            checkApi();
            enabled = true;
            NationsMod.LOGGER.info("BlueMap integration enabled!");
        } catch (Exception e) {
            NationsMod.LOGGER.error("BlueMap init error: " + e.getMessage());
        }
    }

    private static void loadClasses() throws ClassNotFoundException, NoSuchMethodException {
        ClassLoader cl = BlueMapIntegration.class.getClassLoader();
        clsBlueMapAPI = Class.forName("de.bluecolored.bluemap.api.BlueMapAPI", true, cl);
        clsBlueMapMap = Class.forName("de.bluecolored.bluemap.api.BlueMapMap", true, cl);
        clsMarkerSet = Class.forName("de.bluecolored.bluemap.api.markers.MarkerSet", true, cl);
        clsShapeMarker = Class.forName("de.bluecolored.bluemap.api.markers.ShapeMarker", true, cl);
        clsPOIMarker = Class.forName("de.bluecolored.bluemap.api.markers.POIMarker", true, cl);
        clsShape = Class.forName("de.bluecolored.bluemap.api.math.Shape", true, cl);
        clsVector2d = Class.forName("com.flowpowered.math.vector.Vector2d", true, cl);
        clsColor = Class.forName("de.bluecolored.bluemap.api.math.Color", true, cl);

        mGetInstance = clsBlueMapAPI.getMethod("getInstance");
        mGetMaps = clsBlueMapAPI.getMethod("getMaps");
        mGetId = clsBlueMapMap.getMethod("getId");
        mGetMarkerSets = clsBlueMapMap.getMethod("getMarkerSets");

        mMarkerSetBuilder = clsMarkerSet.getMethod("builder");
        Class<?> clsMarkerSetBuilder = mMarkerSetBuilder.getReturnType();
        mMarkerSetLabel = clsMarkerSetBuilder.getMethod("label", String.class);
        mMarkerSetBuild = clsMarkerSetBuilder.getMethod("build");
        mMarkerSetGetMarkers = clsMarkerSet.getMethod("getMarkers");

        mShapeMarkerBuilder = clsShapeMarker.getMethod("builder");
        Class<?> clsShapeMarkerBuilder = mShapeMarkerBuilder.getReturnType();
        mShapeMarkerLabel = clsShapeMarkerBuilder.getMethod("label", String.class);
        mShapeMarkerShape = clsShapeMarkerBuilder.getMethod("shape", clsShape, float.class);
        mShapeMarkerDepthTest = clsShapeMarkerBuilder.getMethod("depthTestEnabled", boolean.class);
        mShapeMarkerFillColor = clsShapeMarkerBuilder.getMethod("fillColor", clsColor);
        mShapeMarkerLineColor = clsShapeMarkerBuilder.getMethod("lineColor", clsColor);
        mShapeMarkerLineWidth = clsShapeMarkerBuilder.getMethod("lineWidth", int.class);
        mShapeMarkerDetail = clsShapeMarkerBuilder.getMethod("detail", String.class);
        mShapeMarkerBuild = clsShapeMarkerBuilder.getMethod("build");

        mPOIMarkerBuilder = clsPOIMarker.getMethod("builder");
        Class<?> clsPOIMarkerBuilder = mPOIMarkerBuilder.getReturnType();
        mPOIMarkerLabel = clsPOIMarkerBuilder.getMethod("label", String.class);
        mPOIMarkerPosition = clsPOIMarkerBuilder.getMethod("position", double.class, double.class, double.class);
        mPOIMarkerDetail = clsPOIMarkerBuilder.getMethod("detail", String.class);
        mPOIMarkerIcon = clsPOIMarkerBuilder.getMethod("icon", String.class, int.class, int.class);
        mPOIMarkerBuild = clsPOIMarkerBuilder.getMethod("build");

        cVector2d = clsVector2d.getConstructor(double.class, double.class);
        cShape = clsShape.getConstructor(clsVector2d.arrayType());
        cColor = clsColor.getConstructor(int.class, int.class, int.class, float.class);
    }

    private static void checkApi() {
        try {
            Optional<?> opt = (Optional<?>) mGetInstance.invoke(null);
            if (opt.isPresent()) blueMapAPI = opt.get();
        } catch (Exception ignored) {}
    }

    public static void updateAllMarkers() {
        if (!enabled) return;
        if (blueMapAPI == null) { checkApi(); if (blueMapAPI == null) return; }

        try {
            Collection<?> maps = (Collection<?>) mGetMaps.invoke(blueMapAPI);
            for (Object map : maps) {
                String mapId = (String) mGetId.invoke(map);
                if (!mapId.toLowerCase().contains("overworld") && !mapId.equals("world")) continue;

                Map<String, Object> markerSets = (Map<String, Object>) mGetMarkerSets.invoke(map);
                Object markerSet = markerSets.get(MARKER_SET_ID);

                if (markerSet == null) {
                    Object builder = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(builder, "Города и Нации");
                    markerSet = mMarkerSetBuild.invoke(builder);
                    markerSets.put(MARKER_SET_ID, markerSet);
                }

                Map<String, Object> markers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(markerSet);
                markers.clear();

                // 1. Рисуем внешние границы наций
                for (Nation nation : NationsData.getAllNations()) {
                    drawNationBorder(nation, markers);
                }

                // 2. Рисуем внутренние границы между городами одной нации
                for (Nation nation : NationsData.getAllNations()) {
                    drawInnerTownBorders(nation, markers);
                }

                // 3. Рисуем города без нации
                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null) {
                        drawStandaloneTown(town, markers);
                    }
                }

                // 4. POI иконки для всех городов
                for (Town town : NationsData.getAllTowns()) {
                    drawTownPOI(town, markers);
                }
            }
        } catch (Exception e) {
            NationsMod.LOGGER.error("BlueMap marker update error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === ВНЕШНЯЯ ГРАНИЦА НАЦИИ ===
    private static void drawNationBorder(Nation nation, Map<String, Object> markers) throws Exception {
        Set<ChunkPos> allChunks = new HashSet<>();
        for (String townName : nation.getTowns()) {
            Town town = NationsData.getTown(townName);
            if (town != null) allChunks.addAll(town.getClaimedChunks());
        }
        if (allChunks.isEmpty()) return;

        Set<String> edges = new HashSet<>();
        for (ChunkPos cp : allChunks) {
            double x1 = cp.x * 16.0;
            double z1 = cp.z * 16.0;
            double x2 = x1 + 16.0;
            double z2 = z1 + 16.0;
            toggleEdge(edges, x1, z1, x2, z1);
            toggleEdge(edges, x2, z1, x2, z2);
            toggleEdge(edges, x2, z2, x1, z2);
            toggleEdge(edges, x1, z2, x1, z1);
        }

        List<List<Point>> polygons = tracePolygons(edges);

        int hex = nation.getColor().getHex();
        int r = (hex >> 16) & 0xFF;
        int g = (hex >> 8) & 0xFF;
        int b = (hex) & 0xFF;

        Object fillColor = cColor.newInstance(r, g, b, 0.25f);
        Object lineColor = cColor.newInstance(r, g, b, 1.0f);

        boolean atWar = false;
        for (String townName : nation.getTowns()) {
            Town town = NationsData.getTown(townName);
            if (town != null && town.isAtWar()) { atWar = true; break; }
        }
        if (atWar) {
            fillColor = cColor.newInstance(255, 0, 0, 0.25f);
            lineColor = cColor.newInstance(255, 0, 0, 1.0f);
        }

        String popup = buildNationPopup(nation, r, g, b);
        int polyIndex = 0;

        for (List<Point> polyPoints : polygons) {
            if (polyPoints.size() < 3) continue;
            Object vectorArray = java.lang.reflect.Array.newInstance(clsVector2d, polyPoints.size());
            for (int i = 0; i < polyPoints.size(); i++) {
                java.lang.reflect.Array.set(vectorArray, i, cVector2d.newInstance(polyPoints.get(i).x, polyPoints.get(i).z));
            }
            Object shape = cShape.newInstance(vectorArray);
            Object builder = mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(builder, nation.getName());
            mShapeMarkerShape.invoke(builder, shape, 64f);
            mShapeMarkerDepthTest.invoke(builder, false);
            mShapeMarkerFillColor.invoke(builder, fillColor);
            mShapeMarkerLineColor.invoke(builder, lineColor);
            mShapeMarkerLineWidth.invoke(builder, 4);
            mShapeMarkerDetail.invoke(builder, popup);
            markers.put("nation_" + nation.getName() + "_" + (polyIndex++), mShapeMarkerBuild.invoke(builder));
        }
    }

    // === ВНУТРЕННИЕ ГРАНИЦЫ МЕЖДУ ГОРОДАМИ ОДНОЙ НАЦИИ ===
    private static void drawInnerTownBorders(Nation nation, Map<String, Object> markers) throws Exception {
        Map<ChunkPos, String> chunkToTown = new HashMap<>();
        for (String townName : nation.getTowns()) {
            Town town = NationsData.getTown(townName);
            if (town == null) continue;
            for (ChunkPos cp : town.getClaimedChunks()) {
                chunkToTown.put(cp, townName);
            }
        }

        // Находим рёбра между разными городами
        Set<String> innerEdges = new HashSet<>();
        for (Map.Entry<ChunkPos, String> entry : chunkToTown.entrySet()) {
            ChunkPos cp = entry.getKey();
            String myTown = entry.getValue();
            double x1 = cp.x * 16.0;
            double z1 = cp.z * 16.0;
            double x2 = x1 + 16.0;
            double z2 = z1 + 16.0;

            addInnerEdgeIfNeeded(innerEdges, chunkToTown, myTown, new ChunkPos(cp.x, cp.z - 1), x1, z1, x2, z1);
            addInnerEdgeIfNeeded(innerEdges, chunkToTown, myTown, new ChunkPos(cp.x, cp.z + 1), x1, z2, x2, z2);
            addInnerEdgeIfNeeded(innerEdges, chunkToTown, myTown, new ChunkPos(cp.x + 1, cp.z), x2, z1, x2, z2);
            addInnerEdgeIfNeeded(innerEdges, chunkToTown, myTown, new ChunkPos(cp.x - 1, cp.z), x1, z1, x1, z2);
        }

        if (innerEdges.isEmpty()) return;

        int hex = nation.getColor().getHex();
        int r = Math.max(0, (int)(((hex >> 16) & 0xFF) * 0.6));
        int g = Math.max(0, (int)(((hex >> 8) & 0xFF) * 0.6));
        int b = Math.max(0, (int)((hex & 0xFF) * 0.6));
        Object lineColor = cColor.newInstance(r, g, b, 0.7f);

        int edgeIndex = 0;
        for (String edge : innerEdges) {
            String[] pts = edge.split(">");
            String[] p1 = pts[0].split(",");
            String[] p2 = pts[1].split(",");
            double ex1 = Double.parseDouble(p1[0]);
            double ez1 = Double.parseDouble(p1[1]);
            double ex2 = Double.parseDouble(p2[0]);
            double ez2 = Double.parseDouble(p2[1]);

            double dx = 0, dz = 0;
            if (Math.abs(ex1 - ex2) < 0.1) dx = 0.3;
            else dz = 0.3;

            Object vectorArray = java.lang.reflect.Array.newInstance(clsVector2d, 4);
            java.lang.reflect.Array.set(vectorArray, 0, cVector2d.newInstance(ex1 - dx, ez1 - dz));
            java.lang.reflect.Array.set(vectorArray, 1, cVector2d.newInstance(ex2 + dx, ez1 - dz));
            java.lang.reflect.Array.set(vectorArray, 2, cVector2d.newInstance(ex2 + dx, ez2 + dz));
            java.lang.reflect.Array.set(vectorArray, 3, cVector2d.newInstance(ex1 - dx, ez2 + dz));

            Object shape = cShape.newInstance(vectorArray);
            Object builder = mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(builder, "Граница городов");
            mShapeMarkerShape.invoke(builder, shape, 64f);
            mShapeMarkerDepthTest.invoke(builder, false);
            mShapeMarkerFillColor.invoke(builder, lineColor);
            mShapeMarkerLineColor.invoke(builder, lineColor);
            mShapeMarkerLineWidth.invoke(builder, 2);
            mShapeMarkerDetail.invoke(builder, "");
            markers.put("inner_" + nation.getName() + "_" + (edgeIndex++), mShapeMarkerBuild.invoke(builder));
        }
    }

    private static void addInnerEdgeIfNeeded(Set<String> innerEdges, Map<ChunkPos, String> chunkToTown,
                                              String myTown, ChunkPos neighbor,
                                              double x1, double z1, double x2, double z2) {
        String neighborTown = chunkToTown.get(neighbor);
        if (neighborTown != null && !neighborTown.equals(myTown)) {
            String key1 = x1 + "," + z1 + ">" + x2 + "," + z2;
            String key2 = x2 + "," + z2 + ">" + x1 + "," + z1;
            if (!innerEdges.contains(key2)) {
                innerEdges.add(key1);
            }
        }
    }

    // === ГОРОД БЕЗ НАЦИИ ===
    private static void drawStandaloneTown(Town town, Map<String, Object> markers) throws Exception {
        Set<ChunkPos> chunks = town.getClaimedChunks();
        if (chunks.isEmpty()) return;

        Set<String> edges = new HashSet<>();
        for (ChunkPos cp : chunks) {
            double x1 = cp.x * 16.0;
            double z1 = cp.z * 16.0;
            double x2 = x1 + 16.0;
            double z2 = z1 + 16.0;
            toggleEdge(edges, x1, z1, x2, z1);
            toggleEdge(edges, x2, z1, x2, z2);
            toggleEdge(edges, x2, z2, x1, z2);
            toggleEdge(edges, x1, z2, x1, z1);
        }

        List<List<Point>> polygons = tracePolygons(edges);

        int r = 136, g = 136, b = 136;
        Object fillColor = cColor.newInstance(r, g, b, 0.4f);
        Object lineColor = cColor.newInstance(r, g, b, 0.9f);

        if (town.isAtWar()) {
            fillColor = cColor.newInstance(255, 0, 0, 0.4f);
            lineColor = cColor.newInstance(255, 0, 0, 1.0f);
        } else if (town.isCaptured()) {
            fillColor = cColor.newInstance(255, 140, 0, 0.4f);
            lineColor = cColor.newInstance(255, 140, 0, 1.0f);
        }

        String popup = buildPopup(town, "Без нации", r, g, b);
        int polyIndex = 0;
        for (List<Point> polyPoints : polygons) {
            if (polyPoints.size() < 3) continue;
            Object vectorArray = java.lang.reflect.Array.newInstance(clsVector2d, polyPoints.size());
            for (int i = 0; i < polyPoints.size(); i++) {
                java.lang.reflect.Array.set(vectorArray, i, cVector2d.newInstance(polyPoints.get(i).x, polyPoints.get(i).z));
            }
            Object shape = cShape.newInstance(vectorArray);
            Object builder = mShapeMarkerBuilder.invoke(null);
            mShapeMarkerLabel.invoke(builder, town.getName());
            mShapeMarkerShape.invoke(builder, shape, 64f);
            mShapeMarkerDepthTest.invoke(builder, false);
            mShapeMarkerFillColor.invoke(builder, fillColor);
            mShapeMarkerLineColor.invoke(builder, lineColor);
            mShapeMarkerLineWidth.invoke(builder, 3);
            mShapeMarkerDetail.invoke(builder, popup);
            markers.put("town_" + town.getName() + "_" + (polyIndex++), mShapeMarkerBuild.invoke(builder));
        }
    }

    // === POI ИКОНКИ ГОРОДОВ ===
    private static void drawTownPOI(Town town, Map<String, Object> markers) throws Exception {
        if (town.getSpawnPos() == null) return;

        boolean isCapital = false;
        String nationName = "Без нации";
        int r = 136, g = 136, b = 136;

        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                int hex = nation.getColor().getHex();
                r = (hex >> 16) & 0xFF;
                g = (hex >> 8) & 0xFF;
                b = (hex) & 0xFF;
                nationName = nation.getName();
                if (nation.getLeader().equals(town.getMayor())) isCapital = true;
            }
        }

        String popup = buildPopup(town, nationName, r, g, b);

        Object builder = mPOIMarkerBuilder.invoke(null);
        mPOIMarkerLabel.invoke(builder, town.getName());
        mPOIMarkerPosition.invoke(builder,
                (double) town.getSpawnPos().getX(),
                (double) town.getSpawnPos().getY() + 2,
                (double) town.getSpawnPos().getZ());
        mPOIMarkerDetail.invoke(builder, popup);

        if (isCapital) {
            mPOIMarkerIcon.invoke(builder, ICON_CROWN_BASE64, 16, 16);
        } else {
            mPOIMarkerIcon.invoke(builder, ICON_TOWN_BASE64, 8, 8);
        }

        markers.put("poi_" + town.getName(), mPOIMarkerBuild.invoke(builder));
    }

    // === УТИЛИТЫ ===
    private static void toggleEdge(Set<String> edges, double x1, double z1, double x2, double z2) {
        String forward = x1 + "," + z1 + ">" + x2 + "," + z2;
        String backward = x2 + "," + z2 + ">" + x1 + "," + z1;
        if (edges.contains(backward)) edges.remove(backward);
        else edges.add(forward);
    }

    private static List<List<Point>> tracePolygons(Set<String> edges) {
        List<List<Point>> polygons = new ArrayList<>();
        Map<Point, Point> pathMap = new HashMap<>();
        for (String edge : edges) {
            String[] parts = edge.split(">");
            String[] p1 = parts[0].split(",");
            String[] p2 = parts[1].split(",");
            pathMap.put(
                new Point(Double.parseDouble(p1[0]), Double.parseDouble(p1[1])),
                new Point(Double.parseDouble(p2[0]), Double.parseDouble(p2[1])));
        }
        while (!pathMap.isEmpty()) {
            List<Point> poly = new ArrayList<>();
            Point start = pathMap.keySet().iterator().next();
            Point current = start;
            int safety = 0;
            while (current != null && safety++ < 10000) {
                poly.add(current);
                Point next = pathMap.remove(current);
                if (next == null || next.equals(start)) break;
                current = next;
            }
            if (poly.size() >= 3) polygons.add(poly);
        }
        return polygons;
    }

    private static class Point {
        double x, z;
        Point(double x, double z) { this.x = x; this.z = z; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point p = (Point) o;
            return Double.compare(p.x, x) == 0 && Double.compare(p.z, z) == 0;
        }
        @Override
        public int hashCode() { return Objects.hash(x, z); }
    }

    // === ПОПАПЫ ===
    private static String buildNationPopup(Nation nation, int r, int g, int b) {
        StringBuilder sb = new StringBuilder();
        String containerStyle = "font-family:'Segoe UI',sans-serif;background:rgba(10,10,15,0.95);" +
                "padding:12px;border-radius:8px;color:#fff;min-width:280px;" +
                "margin:-10px;border:1px solid rgba(255,255,255,0.15);box-shadow:0 4px 15px rgba(0,0,0,0.8);" +
                "position:relative;pointer-events:auto;";
        String closeBtnStyle = "position:absolute;top:2px;right:8px;color:#aaa;font-size:20px;" +
                "cursor:pointer;font-weight:bold;line-height:1;padding:2px 4px;user-select:none;";
        String titleColor = String.format("rgb(%d,%d,%d)", r, g, b);
        String labelStyle = "color:#999;font-weight:500;white-space:nowrap;";
        String valStyle = "font-weight:bold;text-align:left;";
        String gridStyle = "display:grid;grid-template-columns:min-content 1fr;column-gap:10px;row-gap:4px;font-size:14px;";

        sb.append("<div style=\"").append(containerStyle).append("\">");
        sb.append("<div style=\"").append(closeBtnStyle)
          .append("\" onmouseover=\"this.style.color='#fff'\" onmouseout=\"this.style.color='#aaa'\"")
          .append(" onclick=\"var lp=this.closest('.bm-marker-labelpopup');if(lp){lp.style.display='none';}event.stopPropagation();\">×</div>");
        sb.append("<div style=\"font-size:18px;font-weight:900;color:").append(titleColor)
          .append(";text-align:center;margin-bottom:8px;\">🏛 ").append(nation.getName()).append("</div>");
        sb.append("<hr style=\"border:0;border-top:2px solid rgba(255,255,255,0.3);margin:8px 0;\">");
        sb.append("<div style=\"").append(gridStyle).append("\">");
        sb.append("<div style=\"").append(labelStyle).append("\">Городов:</div>");
        sb.append("<div style=\"").append(valStyle).append("color:#FFD700;\">").append(nation.getTowns().size()).append("</div>");
        sb.append("<div style=\"").append(labelStyle).append("\">Жителей:</div>");
        sb.append("<div style=\"").append(valStyle).append("color:#DDD;\">").append(nation.getTotalMembers()).append("</div>");
        sb.append("<div style=\"").append(labelStyle).append("\">Территория:</div>");
        sb.append("<div style=\"").append(valStyle).append("color:#DDD;\">").append(nation.getTotalChunks()).append(" чанков</div>");
        sb.append("<div style=\"").append(labelStyle).append("\">Рейтинг:</div>");
        sb.append("<div style=\"").append(valStyle).append("color:#FFD700;\">⭐ ").append(nation.getRating()).append("</div>");
        sb.append("</div>");
        sb.append("<hr style=\"border:0;border-top:1px solid rgba(255,255,255,0.2);margin:8px 0;\">");
        sb.append("<div style=\"font-size:12px;color:#aaa;\">Города:</div>");
        sb.append("<div style=\"font-size:13px;color:#ddd;margin-top:4px;\">");
        for (String townName : nation.getTowns()) {
            Town t = NationsData.getTown(townName);
            if (t == null) continue;
            boolean isCap = nation.getLeader().equals(t.getMayor());
            sb.append(isCap ? "👑" : "🏠").append(" ").append(townName);
            sb.append(" <span style=\"color:#888;\">(").append(t.getClaimedChunks().size()).append(")</span><br>");
        }
        sb.append("</div></div>");
        return sb.toString();
    }

    private static String buildPopup(Town town, String nationName, int r, int g, int b) {
        StringBuilder sb = new StringBuilder();
        String containerStyle = "font-family:'Segoe UI',sans-serif;background:rgba(10,10,15,0.95);" +
                "padding:12px;border-radius:8px;color:#fff;min-width:260px;" +
                "margin:-10px;border:1px solid rgba(255,255,255,0.15);box-shadow:0 4px 15px rgba(0,0,0,0.8);" +
                "position:relative;pointer-events:auto;";
        String closeBtnStyle = "position:absolute;top:2px;right:8px;color:#aaa;font-size:20px;" +
                "cursor:pointer;font-weight:bold;line-height:1;padding:2px 4px;user-select:none;";
        String gridStyle = "display:grid;grid-template-columns:min-content 1fr;align-items:baseline;" +
                "column-gap:10px;row-gap:4px;font-size:14px;";
        String labelStyle = "color:#999;font-weight:500;white-space:nowrap;";
        String valStyle = "font-weight:bold;text-align:left;";
        String titleColor = String.format("rgb(%d,%d,%d)", r, g, b);
        if (town.isAtWar()) titleColor = "#FF4444";

        sb.append("<div class=\"nations-popup\" style=\"").append(containerStyle).append("\">");
        sb.append("<div class=\"nations-close-btn\" style=\"").append(closeBtnStyle)
          .append("\" onmouseover=\"this.style.color='#fff'\" onmouseout=\"this.style.color='#aaa'\"")
          .append(" onclick=\"var lp=this.closest('.bm-marker-labelpopup');if(lp){lp.style.display='none';}var mp=this.closest('.bm-marker-popup');if(mp){mp.style.display='none';}event.stopPropagation();\">×</div>");
        sb.append("<div style=\"").append(gridStyle).append("\">");
        String natColor = town.getNationName() != null ? titleColor : "#999";
        sb.append("<div style=\"").append(labelStyle).append("\">Нация:</div>");
        sb.append("<div style=\"").append(valStyle).append("color:").append(natColor).append(";\">").append(nationName).append("</div>");
        sb.append("<div style=\"").append(labelStyle).append("\">Город:</div>");
        sb.append("<div style=\"").append(valStyle).append("color:#DDD;\">").append(town.getName()).append("</div>");
        sb.append("</div>");
        sb.append("<hr style=\"border:0;border-top:2px solid rgba(255,255,255,0.3);margin:8px 0;\">");
        sb.append("<div style=\"").append(gridStyle).append("\">");

        String mayorName = "Неизвестно";
        if (NationsData.getServer() != null) {
            var p = NationsData.getServer().getPlayerList().getPlayer(town.getMayor());
            if (p != null) mayorName = p.getName().getString();
        }
        sb.append("<div style=\"").append(labelStyle).append("\">МЭР:</div>");
        sb.append("<div style=\"").append(valStyle).append("color:#FFD700;font-size:13px;\">").append(mayorName).append("</div>");
        sb.append("<div style=\"").append(labelStyle).append("align-self:start;\">Жители:</div>");
        sb.append("<div style=\"").append(valStyle).append("color:#DDD;font-size:13px;line-height:1.3;\">");

        List<String> names = new ArrayList<>();
        int limit = 0;
        for (UUID id : town.getMembers()) {
            if (limit >= 15) { names.add("..."); break; }
            if (NationsData.getServer() != null) {
                var p = NationsData.getServer().getPlayerList().getPlayer(id);
                names.add(p != null ? p.getName().getString() : "оффлайн");
            } else { names.add("?"); }
            limit++;
        }
        sb.append(String.join(", ", names)).append("</div></div>");

        if (town.isAtWar()) {
            sb.append("<div style=\"margin-top:10px;color:#ff5555;font-weight:900;text-align:center;text-transform:uppercase;\">⚠ ИДЕТ ВОЙНА</div>");
        } else if (town.isCaptured()) {
            sb.append("<div style=\"margin-top:10px;color:#ffaa00;font-weight:900;text-align:center;text-transform:uppercase;\">🏴 ЗАХВАЧЕН</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }
}
