package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class BlueMapIntegration {

    private static boolean enabled = false;
    private static Object blueMapAPI = null;
    private static final String MARKER_SET_ID = "nations_towns";

    // ============================================================
    //  ИКОНКИ В BASE64 (столица 32x32, город 16x16)
    // ============================================================
    private static String CAPITAL_ICON_BASE64 = 
        "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";

    private static String TOWN_ICON_BASE64 = 
        "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAIGNIUk0AAHomAACAhAAA+gAAAIDoAAB1MAAA6mAAADqYAAAXcJy6UTwAAAEpUExURQAAAM/P0dDQ0svLzczMzvfz9vv7+/z7++Tm5tzd2/r5+fj49/n19/79/fX19P/+/v38/P39/f///+rq6cfHxt7e3qenpt/f3qiop9XU1KalpcC+vqKhocG/v8/PzquqqqGfn6OiocG7v8bFxMLAwNTU07q6uaWjpKqpqaaipa6qrLOusamlp6yoqv///9/h4dja2f////j49u3t7NLU1MbHxevr6vr6+erq6e3t7N3d3MbGxt7e3uDf37e3tqempt/f3t/f37a2tainp9TU09ra2bKxsaWlpL68vMvKydTT09XV1Lu7u7Kysainp5+enry6usHAv7Cwr5yam+np6NjY2NLS0ubm5eXl5d3d3MnJyN/f39vb27+/v7q6ud7e3tva2r+/vv///+VauVgAAABUdFJOUwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKU1MITbP29rNNOe3tOUD29kBA9vZAOe3tOAhNs/f2s0wIClxcCsucjc0AAAABYktHRBJ7vGwAAAAAB3RJTUUH6gIQFR8CaGuYkgAAAIxJREFUGNNjYCISMDIxsyBxWdnYOTi5uHlgfHZePj19Az0+fgGogKChkbGJqZm5oRBUQNjCMiQ0LNzKWgQqIGpjGxESGWVnLwYVEHdwjI6JjXNyloAKSLq4xickxrm5S0EFpD08vbx9fP38ZaACstJyAYFBwfIKijCHKCmrqKqpa2giuVVLW0eXWG8CAKATE9MHZILXAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDI2LTAyLTE2VDIxOjMwOjU0KzAwOjAwx1vJegAAACV0RVh0ZGF0ZTptb2RpZnkAMjAyNi0wMi0xNlQyMTozMDo1NCswMDowMLYGccYAAAAodEVYdGRhdGU6dGltZXN0YW1wADIwMjYtMDItMTZUMjE6MzE6MDIrMDA6MDAl4QB5AAAAAElFTkSuQmCC";

    private static Class<?> clsBlueMapAPI, clsBlueMapMap, clsMarkerSet;
    private static Class<?> clsShapeMarker, clsShape, clsVector2d, clsVector3d, clsColor;
    private static Class<?> clsHtmlMarker;
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets;
    private static Method mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape;
    private static Method mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor;
    private static Method mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mHtmlMarkerBuilder, mHtmlMarkerLabel, mHtmlMarkerPosition;
    private static Method mHtmlMarkerHtml, mHtmlMarkerBuild;
    private static boolean htmlUseVec3 = false;
    private static Constructor<?> cVector2d, cVector3d, cShape, cColor;
    private static Method mOnEnable;

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) {
            NationsMod.LOGGER.warn("BlueMap not found, integration disabled.");
            return;
        }
        try {
            tryLoadIconsFromResources();
            loadClasses();
            registerOnEnable();
            NationsMod.LOGGER.info("BlueMap integration registered.");
        } catch (Exception e) {
            NationsMod.LOGGER.error("Failed to init BlueMap: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Пытается загрузить PNG из ресурсов мода.
     * Если файлы найдены — перезаписывает Base64 строки.
     * Если нет — использует встроенные Base64 строки выше.
     */
    private static void tryLoadIconsFromResources() {
        String capitalFromRes = loadResourceAsBase64("/assets/nations/bluemap/capital_icon.png");
        String townFromRes = loadResourceAsBase64("/assets/nations/bluemap/town_icon.png");

        if (capitalFromRes != null) {
            CAPITAL_ICON_BASE64 = capitalFromRes;
            NationsMod.LOGGER.info("Capital icon loaded from resources");
        } else {
            NationsMod.LOGGER.info("Capital icon using embedded Base64");
        }

        if (townFromRes != null) {
            TOWN_ICON_BASE64 = townFromRes;
            NationsMod.LOGGER.info("Town icon loaded from resources");
        } else {
            NationsMod.LOGGER.info("Town icon using embedded Base64");
        }
    }

    private static String loadResourceAsBase64(String path) {
        try (InputStream is = BlueMapIntegration.class.getResourceAsStream(path)) {
            if (is == null) return null;
            byte[] bytes = is.readAllBytes();
            return java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static void loadClasses() throws Exception {
        ClassLoader cl = BlueMapIntegration.class.getClassLoader();

        clsBlueMapAPI = Class.forName("de.bluecolored.bluemap.api.BlueMapAPI", true, cl);
        clsBlueMapMap = Class.forName("de.bluecolored.bluemap.api.BlueMapMap", true, cl);
        clsMarkerSet = Class.forName("de.bluecolored.bluemap.api.markers.MarkerSet", true, cl);
        clsShapeMarker = Class.forName("de.bluecolored.bluemap.api.markers.ShapeMarker", true, cl);
        clsHtmlMarker = Class.forName("de.bluecolored.bluemap.api.markers.HtmlMarker", true, cl);
        clsShape = Class.forName("de.bluecolored.bluemap.api.math.Shape", true, cl);
        clsColor = Class.forName("de.bluecolored.bluemap.api.math.Color", true, cl);

        try {
            clsVector2d = Class.forName("com.flowpowered.math.vector.Vector2d", true, cl);
            clsVector3d = Class.forName("com.flowpowered.math.vector.Vector3d", true, cl);
        } catch (ClassNotFoundException e) {
            clsVector2d = Class.forName("de.bluecolored.bluemap.api.math.Vector2d", true, cl);
            clsVector3d = Class.forName("de.bluecolored.bluemap.api.math.Vector3d", true, cl);
        }

        mGetInstance = clsBlueMapAPI.getMethod("getInstance");
        mGetMaps = clsBlueMapAPI.getMethod("getMaps");
        mGetId = clsBlueMapMap.getMethod("getId");
        mGetMarkerSets = clsBlueMapMap.getMethod("getMarkerSets");

        mMarkerSetBuilder = clsMarkerSet.getMethod("builder");
        Class<?> msb = mMarkerSetBuilder.getReturnType();
        mMarkerSetLabel = msb.getMethod("label", String.class);
        mMarkerSetBuild = msb.getMethod("build");
        mMarkerSetGetMarkers = clsMarkerSet.getMethod("getMarkers");

        mShapeMarkerBuilder = clsShapeMarker.getMethod("builder");
        Class<?> smb = mShapeMarkerBuilder.getReturnType();
        mShapeMarkerLabel = smb.getMethod("label", String.class);
        mShapeMarkerShape = smb.getMethod("shape", clsShape, float.class);
        mShapeMarkerDepthTest = smb.getMethod("depthTestEnabled", boolean.class);
        mShapeMarkerFillColor = smb.getMethod("fillColor", clsColor);
        mShapeMarkerLineColor = smb.getMethod("lineColor", clsColor);
        mShapeMarkerLineWidth = smb.getMethod("lineWidth", int.class);
        mShapeMarkerDetail = smb.getMethod("detail", String.class);
        mShapeMarkerBuild = smb.getMethod("build");

        mHtmlMarkerBuilder = clsHtmlMarker.getMethod("builder");
        Class<?> hmb = mHtmlMarkerBuilder.getReturnType();
        mHtmlMarkerLabel = hmb.getMethod("label", String.class);
        mHtmlMarkerHtml = hmb.getMethod("html", String.class);
        mHtmlMarkerBuild = hmb.getMethod("build");
        try {
            mHtmlMarkerPosition = hmb.getMethod("position", double.class, double.class, double.class);
            htmlUseVec3 = false;
        } catch (NoSuchMethodException e) {
            mHtmlMarkerPosition = hmb.getMethod("position", clsVector3d);
            htmlUseVec3 = true;
        }

        cVector2d = clsVector2d.getConstructor(double.class, double.class);
        cVector3d = clsVector3d.getConstructor(double.class, double.class, double.class);
        cShape = clsShape.getConstructor(clsVector2d.arrayType());
        cColor = clsColor.getConstructor(int.class, int.class, int.class, float.class);
        mOnEnable = clsBlueMapAPI.getMethod("onEnable", Consumer.class);
    }

    private static void registerOnEnable() throws Exception {
        Consumer<Object> callback = api -> {
            blueMapAPI = api;
            enabled = true;
            NationsMod.LOGGER.info("BlueMap API available!");
            try { updateAllMarkers(); } catch (Exception e) {
                NationsMod.LOGGER.error("Initial render error: " + e.getMessage());
            }
        };
        mOnEnable.invoke(null, callback);
        try {
            Optional<?> opt = (Optional<?>) mGetInstance.invoke(null);
            if (opt.isPresent()) { blueMapAPI = opt.get(); enabled = true; }
        } catch (Exception ignored) {}
    }

    public static boolean isEnabled() { return enabled && blueMapAPI != null; }

    // ================================================================
    //                    ГЛАВНЫЙ МЕТОД ОБНОВЛЕНИЯ
    // ================================================================

    @SuppressWarnings("unchecked")
    public static void updateAllMarkers() {
        if (!enabled || blueMapAPI == null) {
            try {
                Optional<?> opt = (Optional<?>) mGetInstance.invoke(null);
                if (opt.isPresent()) { blueMapAPI = opt.get(); enabled = true; }
                else return;
            } catch (Exception e) { return; }
        }

        try {
            Collection<?> maps = (Collection<?>) mGetMaps.invoke(blueMapAPI);
            if (maps.isEmpty()) return;

            for (Object map : maps) {
                String mapId = (String) mGetId.invoke(map);
                String lower = mapId.toLowerCase();
                if (!lower.contains("overworld") && !lower.equals("world") && !lower.contains("мир"))
                    continue;

                Map<String, Object> markerSets = (Map<String, Object>) mGetMarkerSets.invoke(map);

                // --- Набор территорий ---
                Object territorySet = markerSets.get(MARKER_SET_ID);
                if (territorySet == null) {
                    Object msBuilder = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(msBuilder, "Нации и Города");
                    territorySet = mMarkerSetBuild.invoke(msBuilder);
                    markerSets.put(MARKER_SET_ID, territorySet);
                }

                // --- Набор иконок ---
                String iconSetId = MARKER_SET_ID + "_icons";
                Object iconSet = markerSets.get(iconSetId);
                if (iconSet == null) {
                    Object msBuilder = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(msBuilder, "Иконки городов");
                    iconSet = mMarkerSetBuild.invoke(msBuilder);
                    markerSets.put(iconSetId, iconSet);
                }

                Map<String, Object> tMarkers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(territorySet);
                Map<String, Object> iMarkers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(iconSet);
                tMarkers.clear();
                iMarkers.clear();

                // 1. Заливка наций
                for (Nation nation : NationsData.getAllNations()) {
                    try { drawNationFill(nation, tMarkers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Nation error " + nation.getName() + ": " + e.getMessage());
                    }
                }

                // 2. Внутренние границы
                for (Nation nation : NationsData.getAllNations()) {
                    try { drawInnerBorders(nation, tMarkers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Inner border error " + nation.getName() + ": " + e.getMessage());
                    }
                }

                // 3. Города без нации
                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null) {
                        try { drawStandaloneTown(town, tMarkers); } catch (Exception e) {
                            NationsMod.LOGGER.error("Standalone error " + town.getName() + ": " + e.getMessage());
                        }
                    }
                }

                // 4. Иконки (привязаны к spawnPos — при перемещении спавна иконка переедет)
                for (Town town : NationsData.getAllTowns()) {
                    try { drawTownIcon(town, iMarkers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Icon error " + town.getName() + ": " + e.getMessage());
                    }
                }

                NationsMod.LOGGER.info("BlueMap updated: " + tMarkers.size() +
                    " territories + " + iMarkers.size() + " icons on " + mapId);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.error("BlueMap update error: " + e.getMessage());
        }
    }

    // ================================================================
    //                    ТЕРРИТОРИИ НАЦИИ
    // ================================================================

    private static void drawNationFill(Nation nation, Map<String, Object> markers) throws Exception {
        Set<ChunkPos> allChunks = new HashSet<>();
        for (String tn : nation.getTowns()) {
            Town t = NationsData.getTown(tn);
            if (t != null) allChunks.addAll(t.getClaimedChunks());
        }
        if (allChunks.isEmpty()) return;

        Set<String> outerEdges = calcEdges(allChunks);
        List<List<Point>> polygons = tracePolygons(outerEdges);

        int hex = nation.getColor().getHex();
        int cr = (hex >> 16) & 0xFF, cg = (hex >> 8) & 0xFF, cb = hex & 0xFF;

        float fillAlpha = !nation.getWarTargets().isEmpty() ? 0.35f : 0.25f;
        Object fill = cColor.newInstance(cr, cg, cb, fillAlpha);
        Object line = cColor.newInstance(cr, cg, cb, 1.0f);
        String popup = buildNationPopup(nation, cr, cg, cb);

        int i = 0;
        for (List<Point> poly : polygons) {
            if (poly.size() < 3) continue;
            markers.put("nation_" + nation.getName() + "_" + (i++),
                createShapeMarker(nation.getName(), createShape(poly), fill, line, 4, popup));
        }
    }

    // ================================================================
    //               ВНУТРЕННИЕ ГРАНИЦЫ ГОРОДОВ
    // ================================================================

    private static void drawInnerBorders(Nation nation, Map<String, Object> markers) throws Exception {
        Map<ChunkPos, String> chunkToTown = new HashMap<>();
        for (String tn : nation.getTowns()) {
            Town t = NationsData.getTown(tn);
            if (t != null) for (ChunkPos cp : t.getClaimedChunks()) chunkToTown.put(cp, tn);
        }
        if (chunkToTown.size() < 2) return;

        int hex = nation.getColor().getHex();
        int cr = (hex >> 16) & 0xFF, cg = (hex >> 8) & 0xFF, cb = hex & 0xFF;
        int dr = Math.min(255, cr + 40), dg = Math.min(255, cg + 40), db = Math.min(255, cb + 40);
        Object lineCol = cColor.newInstance(dr, dg, db, 0.6f);
        Object noFill = cColor.newInstance(0, 0, 0, 0.0f);

        Set<String> processedEdges = new HashSet<>();
        List<double[]> edgeCoords = new ArrayList<>();

        for (Map.Entry<ChunkPos, String> entry : chunkToTown.entrySet()) {
            ChunkPos cp = entry.getKey();
            String myTown = entry.getValue();
            double x1 = cp.x * 16.0, z1 = cp.z * 16.0;
            double x2 = x1 + 16.0, z2 = z1 + 16.0;

            collectInnerEdge(processedEdges, edgeCoords, chunkToTown, myTown, new ChunkPos(cp.x, cp.z - 1), x1, z1, x2, z1);
            collectInnerEdge(processedEdges, edgeCoords, chunkToTown, myTown, new ChunkPos(cp.x, cp.z + 1), x1, z2, x2, z2);
            collectInnerEdge(processedEdges, edgeCoords, chunkToTown, myTown, new ChunkPos(cp.x + 1, cp.z), x2, z1, x2, z2);
            collectInnerEdge(processedEdges, edgeCoords, chunkToTown, myTown, new ChunkPos(cp.x - 1, cp.z), x1, z1, x1, z2);
        }

        int j = 0;
        for (double[] ec : edgeCoords) {
            double ax = ec[0], az = ec[1], bx = ec[2], bz = ec[3];
            List<Point> linePoly = new ArrayList<>();
            double w = 0.3;
            if (Math.abs(ax - bx) < 0.01) {
                linePoly.add(new Point(ax - w, Math.min(az, bz)));
                linePoly.add(new Point(ax + w, Math.min(az, bz)));
                linePoly.add(new Point(ax + w, Math.max(az, bz)));
                linePoly.add(new Point(ax - w, Math.max(az, bz)));
            } else {
                linePoly.add(new Point(Math.min(ax, bx), az - w));
                linePoly.add(new Point(Math.max(ax, bx), az - w));
                linePoly.add(new Point(Math.max(ax, bx), az + w));
                linePoly.add(new Point(Math.min(ax, bx), az + w));
            }
            markers.put("inner_" + nation.getName() + "_" + (j++),
                createShapeMarker("", createShape(linePoly), noFill, lineCol, 2, ""));
        }
    }

    private static void collectInnerEdge(Set<String> processed, List<double[]> coords,
                                          Map<ChunkPos, String> map, String myTown,
                                          ChunkPos neighbor, double x1, double z1, double x2, double z2) {
        String other = map.get(neighbor);
        if (other != null && !other.equals(myTown)) {
            String townA = myTown.compareTo(other) < 0 ? myTown : other;
            String townB = myTown.compareTo(other) < 0 ? other : myTown;
            double minX = Math.min(x1, x2), minZ = Math.min(z1, z2);
            double maxX = Math.max(x1, x2), maxZ = Math.max(z1, z2);
            String edgeKey = townA + "|" + townB + "|" + minX + "," + minZ + "," + maxX + "," + maxZ;
            if (processed.add(edgeKey)) {
                coords.add(new double[]{x1, z1, x2, z2});
            }
        }
    }

    // ================================================================
    //                   ГОРОДА БЕЗ НАЦИИ
    // ================================================================

    private static void drawStandaloneTown(Town town, Map<String, Object> markers) throws Exception {
        if (town.getClaimedChunks().isEmpty()) return;
        Set<String> edges = calcEdges(town.getClaimedChunks());
        List<List<Point>> polygons = tracePolygons(edges);

        int cr = 150, cg = 150, cb = 150;
        float fillAlpha = 0.30f;
        if (town.isAtWar()) { cr = 255; cg = 50; cb = 50; fillAlpha = 0.4f; }
        else if (town.isCaptured()) { cr = 255; cg = 140; cb = 0; fillAlpha = 0.4f; }

        Object fill = cColor.newInstance(cr, cg, cb, fillAlpha);
        Object line = cColor.newInstance(cr, cg, cb, 1.0f);
        String popup = buildTownPopup(town, "Без нации", cr, cg, cb);

        int i = 0;
        for (List<Point> poly : polygons) {
            if (poly.size() < 3) continue;
            markers.put("standalone_" + town.getName() + "_" + (i++),
                createShapeMarker(town.getName(), createShape(poly), fill, line, 3, popup));
        }
    }

    // ================================================================
    //          ИКОНКИ ГОРОДОВ (PNG через Base64)
    //    Привязаны к town.getSpawnPos() — перемещаются при смене спавна
    // ================================================================

    private static void drawTownIcon(Town town, Map<String, Object> markers) throws Exception {
        // Иконка рисуется на позиции спавна города
        // При вызове /town setspawn спавн меняется → 
        // при следующем updateAllMarkers() иконка переедет
        if (town.getSpawnPos() == null) return;

        boolean isCapital = false;
        String nationName = "Без нации";
        String colorHex = "#888888";

        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                colorHex = String.format("#%06X", nation.getColor().getHex());
                nationName = nation.getName();
                isCapital = nation.isCapital(town.getName());
            }
        }

        // Координаты из spawnPos
        double px = town.getSpawnPos().getX() + 0.5;
        double py = town.getSpawnPos().getY() + 2.0;
        double pz = town.getSpawnPos().getZ() + 0.5;

        // Выбираем иконку и размер
        String base64 = isCapital ? CAPITAL_ICON_BASE64 : TOWN_ICON_BASE64;
        int iconSize = isCapital ? 32 : 20;

        // Статус-рамка
        String borderStyle = "";
        if (town.isAtWar()) {
            borderStyle = "border:2px solid #FF4444;border-radius:4px;";
        } else if (town.isCaptured()) {
            borderStyle = "border:2px solid #FF8C00;border-radius:4px;";
        }

        // Собираем HTML
        StringBuilder html = new StringBuilder();
        html.append("<div style=\"")
            .append("position:relative;")
            .append("transform:translate(-50%,-100%);")
            .append("display:inline-flex;flex-direction:column;align-items:center;")
            .append("cursor:pointer;")
            .append("\">");

        // Иконка
        html.append("<div style=\"")
            .append("width:").append(iconSize).append("px;")
            .append("height:").append(iconSize).append("px;")
            .append(borderStyle)
            .append("filter:drop-shadow(0 2px 4px rgba(0,0,0,0.8));")
            .append("\">");

        if (base64 != null) {
            html.append("<img src=\"data:image/png;base64,").append(base64).append("\" ")
                .append("width=\"").append(iconSize).append("\" ")
                .append("height=\"").append(iconSize).append("\" ")
                .append("style=\"display:block;image-rendering:pixelated;\" />");
        } else {
            // Fallback emoji
            String symbol = isCapital ? "👑" : "🏠";
            html.append("<span style=\"font-size:").append(iconSize - 4).append("px;\">")
                .append(symbol).append("</span>");
        }
        html.append("</div>");

        // Подпись
        html.append("<div style=\"")
            .append("margin-top:2px;")
            .append("padding:1px 5px;")
            .append("background:rgba(0,0,0,0.8);")
            .append("border-radius:3px;")
            .append("border:1px solid ").append(colorHex).append(";")
            .append("white-space:nowrap;")
            .append("font-family:'Segoe UI',Arial,sans-serif;")
            .append("font-size:").append(isCapital ? "11" : "9").append("px;")
            .append("color:#fff;")
            .append("font-weight:").append(isCapital ? "bold" : "normal").append(";")
            .append("text-shadow:0 1px 2px rgba(0,0,0,0.9);")
            .append("\">");
        if (isCapital) html.append("★ ");
        html.append(escapeHtml(town.getName()));
        html.append("</div>");

        // Дополнительная инфо-строка
        html.append("<div style=\"")
            .append("font-size:8px;color:#999;")
            .append("font-family:'Segoe UI',Arial,sans-serif;")
            .append("text-shadow:0 1px 1px rgba(0,0,0,0.8);")
            .append("\">");
        html.append(escapeHtml(nationName));
        html.append(" · 👥").append(town.getMembers().size());
        html.append("</div>");

        html.append("</div>");

        // Создаём маркер
        Object builder = mHtmlMarkerBuilder.invoke(null);
        mHtmlMarkerLabel.invoke(builder, town.getName() + (isCapital ? " ★ Столица" : ""));
        mHtmlMarkerHtml.invoke(builder, html.toString());

        if (htmlUseVec3) {
            mHtmlMarkerPosition.invoke(builder, cVector3d.newInstance(px, py, pz));
        } else {
            mHtmlMarkerPosition.invoke(builder, px, py, pz);
        }

        markers.put("icon_" + town.getName(), mHtmlMarkerBuild.invoke(builder));
    }

    // ================================================================
    //                    ГЕОМЕТРИЯ
    // ================================================================

    private static Set<String> calcEdges(Set<ChunkPos> chunks) {
        Set<String> edges = new HashSet<>();
        for (ChunkPos cp : chunks) {
            double x1 = cp.x * 16.0, z1 = cp.z * 16.0, x2 = x1 + 16, z2 = z1 + 16;
            toggleEdge(edges, x1, z1, x2, z1);
            toggleEdge(edges, x2, z1, x2, z2);
            toggleEdge(edges, x2, z2, x1, z2);
            toggleEdge(edges, x1, z2, x1, z1);
        }
        return edges;
    }

    private static void toggleEdge(Set<String> edgeSet, double x1, double z1, double x2, double z2) {
        String f = x1 + "," + z1 + ">" + x2 + "," + z2;
        String rev = x2 + "," + z2 + ">" + x1 + "," + z1;
        if (edgeSet.contains(rev)) edgeSet.remove(rev); else edgeSet.add(f);
    }

    private static List<List<Point>> tracePolygons(Set<String> edges) {
        List<List<Point>> polys = new ArrayList<>();
        Map<Point, Point> map = new HashMap<>();
        for (String edgeStr : edges) {
            String[] p = edgeStr.split(">");
            String[] pa = p[0].split(","), pb = p[1].split(",");
            map.put(new Point(Double.parseDouble(pa[0]), Double.parseDouble(pa[1])),
                    new Point(Double.parseDouble(pb[0]), Double.parseDouble(pb[1])));
        }
        while (!map.isEmpty()) {
            List<Point> poly = new ArrayList<>();
            Point start = map.keySet().iterator().next(), curr = start;
            int safety = 0;
            while (curr != null && safety++ < 200000) {
                poly.add(curr);
                Point next = map.remove(curr);
                if (next == null || next.equals(start)) break;
                curr = next;
            }
            if (poly.size() >= 3) polys.add(poly);
        }
        return polys;
    }

    // ================================================================
    //                    СОЗДАНИЕ МАРКЕРОВ
    // ================================================================

    private static Object createShape(List<Point> pts) throws Exception {
        Object arr = java.lang.reflect.Array.newInstance(clsVector2d, pts.size());
        for (int i = 0; i < pts.size(); i++)
            java.lang.reflect.Array.set(arr, i, cVector2d.newInstance(pts.get(i).x, pts.get(i).z));
        return cShape.newInstance(arr);
    }

    private static Object createShapeMarker(String label, Object shape, Object fill, Object line, int width, String detail) throws Exception {
        Object bd = mShapeMarkerBuilder.invoke(null);
        mShapeMarkerLabel.invoke(bd, label);
        mShapeMarkerShape.invoke(bd, shape, 64f);
        mShapeMarkerDepthTest.invoke(bd, false);
        mShapeMarkerFillColor.invoke(bd, fill);
        mShapeMarkerLineColor.invoke(bd, line);
        mShapeMarkerLineWidth.invoke(bd, width);
        mShapeMarkerDetail.invoke(bd, detail);
        return mShapeMarkerBuild.invoke(bd);
    }

    // ================================================================
    //                         ПОПАПЫ
    // ================================================================

    private static String buildNationPopup(Nation n, int r, int g, int b) {
        String c = "rgb(" + r + "," + g + "," + b + ")";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:'Segoe UI',sans-serif;padding:14px;color:#fff;")
          .append("background:rgba(10,10,15,0.94);border-radius:10px;border:2px solid ").append(c)
          .append(";min-width:180px;\">");

        sb.append("<div style=\"font-size:18px;font-weight:bold;color:").append(c)
          .append("\">🏛 ").append(escapeHtml(n.getName())).append("</div>");

        sb.append("<hr style=\"border:0;border-top:1px solid #333;margin:8px 0\">");

        sb.append("<div style=\"font-size:13px;line-height:1.6;\">");
        sb.append("<div>🏰 Городов: <b style=\"color:#FFD700\">").append(n.getTowns().size()).append("</b></div>");
        sb.append("<div>👥 Жителей: <b>").append(n.getTotalMembers()).append("</b></div>");
        sb.append("<div>📍 Территория: <b>").append(n.getTotalChunks()).append("</b> чанков</div>");
        sb.append("</div>");

        if (n.getCapitalTown() != null) {
            sb.append("<div style=\"margin-top:6px;padding:3px 8px;background:rgba(255,215,0,0.1);")
              .append("border-radius:4px;border-left:3px solid #FFD700;\">")
              .append("👑 Столица: <b style=\"color:#FFD700\">")
              .append(escapeHtml(n.getCapitalTown())).append("</b></div>");
        }

        // Список городов
        if (n.getTowns().size() > 1) {
            sb.append("<div style=\"margin-top:6px;font-size:11px;color:#888;\">Города: ");
            int count = 0;
            for (String tn : n.getTowns()) {
                if (count > 0) sb.append(", ");
                if (count >= 6) { sb.append("..."); break; }
                sb.append(escapeHtml(tn));
                count++;
            }
            sb.append("</div>");
        }

        if (!n.getWarTargets().isEmpty()) {
            sb.append("<div style=\"margin-top:6px;padding:3px 8px;background:rgba(255,0,0,0.15);")
              .append("border-radius:4px;color:#F44;border-left:3px solid #F44;\">")
              .append("⚔ Война: ").append(escapeHtml(String.join(", ", n.getWarTargets())))
              .append("</div>");
        }

        sb.append("</div>");
        return sb.toString();
    }

    private static String buildTownPopup(Town t, String nName, int r, int g, int b) {
        String c = "rgb(" + r + "," + g + "," + b + ")";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:'Segoe UI',sans-serif;padding:10px;color:#fff;")
          .append("background:rgba(10,10,15,0.94);border-radius:8px;border:1px solid ").append(c).append(";\">");

        sb.append("<div style=\"font-size:15px;font-weight:bold\">🏘 ").append(escapeHtml(t.getName())).append("</div>");
        sb.append("<div style=\"color:").append(c).append(";font-size:12px;\">").append(escapeHtml(nName)).append("</div>");
        sb.append("<div style=\"font-size:12px;color:#aaa;margin-top:4px;\">")
          .append("👥 ").append(t.getMembers().size())
          .append(" │ 📍 ").append(t.getClaimedChunks().size()).append(" чанков</div>");

        if (t.isAtWar()) sb.append("<div style=\"color:#F44;margin-top:3px;\">⚔ ВОЙНА</div>");
        if (t.isCaptured()) sb.append("<div style=\"color:#FA0;margin-top:3px;\">🏴 Захвачен: ")
            .append(escapeHtml(t.getCapturedBy())).append("</div>");

        sb.append("</div>");
        return sb.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;")
                   .replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ================================================================
    //                        POINT
    // ================================================================

    static class Point {
        double x, z;
        Point(double x, double z) { this.x = x; this.z = z; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return Double.compare(p.x, x) == 0 && Double.compare(p.z, z) == 0;
        }
        @Override public int hashCode() { return Objects.hash(x, z); }
    }
}
