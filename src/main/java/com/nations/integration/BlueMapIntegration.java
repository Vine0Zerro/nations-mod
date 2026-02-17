package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class BlueMapIntegration {

    private static boolean enabled = false;
    private static Object blueMapAPI = null;
    private static final String MARKER_SET_ID = "nations_towns";

    private static Class<?> clsBlueMapAPI, clsBlueMapMap, clsMarkerSet;
    private static Class<?> clsShapeMarker, clsHtmlMarker, clsShape, clsVector2d, clsVector3d, clsColor;
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets;
    private static Method mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape;
    private static Method mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor;
    private static Method mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mHtmlMarkerBuilder, mHtmlMarkerLabel, mHtmlMarkerPosition, mHtmlMarkerHtml, mHtmlMarkerBuild;
    private static Constructor<?> cVector2d, cVector3d, cShape, cColor;
    private static Method mOnEnable;

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) {
            NationsMod.LOGGER.warn("BlueMap not found, integration disabled.");
            return;
        }
        try {
            loadClasses();
            registerOnEnable();
            NationsMod.LOGGER.info("BlueMap integration registered — waiting for API...");
        } catch (Exception e) {
            NationsMod.LOGGER.error("Failed to init BlueMap integration: " + e.getMessage());
            e.printStackTrace();
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

        // HtmlMarker для иконок
        mHtmlMarkerBuilder = clsHtmlMarker.getMethod("builder");
        Class<?> hmb = mHtmlMarkerBuilder.getReturnType();
        mHtmlMarkerLabel = hmb.getMethod("label", String.class);
        mHtmlMarkerHtml = hmb.getMethod("html", String.class);
        mHtmlMarkerBuild = hmb.getMethod("build");

        // position — пробуем Vector3d, потом 3 double
        try {
            mHtmlMarkerPosition = hmb.getMethod("position", clsVector3d);
        } catch (NoSuchMethodException e) {
            mHtmlMarkerPosition = hmb.getMethod("position", double.class, double.class, double.class);
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
            NationsMod.LOGGER.info("BlueMap API available! Rendering markers...");
            try { updateAllMarkers(); } catch (Exception e) {
                NationsMod.LOGGER.error("Initial render error: " + e.getMessage());
                e.printStackTrace();
            }
        };
        mOnEnable.invoke(null, callback);
        try {
            Optional<?> opt = (Optional<?>) mGetInstance.invoke(null);
            if (opt.isPresent()) { blueMapAPI = opt.get(); enabled = true; }
        } catch (Exception ignored) {}
    }

    public static boolean isEnabled() { return enabled && blueMapAPI != null; }

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
                if (!mapId.toLowerCase().contains("overworld") && !mapId.equals("world")
                    && !mapId.toLowerCase().contains("мир")) continue;

                Map<String, Object> markerSets = (Map<String, Object>) mGetMarkerSets.invoke(map);
                Object markerSet = markerSets.get(MARKER_SET_ID);

                if (markerSet == null) {
                    Object b = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(b, "Нации и Города");
                    markerSet = mMarkerSetBuild.invoke(b);
                    markerSets.put(MARKER_SET_ID, markerSet);
                }

                Map<String, Object> markers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(markerSet);
                markers.clear();

                for (Nation nation : NationsData.getAllNations()) {
                    try { drawNationFill(nation, markers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Nation fill error " + nation.getName() + ": " + e.getMessage());
                    }
                }

                for (Nation nation : NationsData.getAllNations()) {
                    try { drawInnerTownBorders(nation, markers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Inner border error " + nation.getName() + ": " + e.getMessage());
                    }
                }

                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null) {
                        try { drawStandaloneTown(town, markers); } catch (Exception e) {
                            NationsMod.LOGGER.error("Standalone town error " + town.getName() + ": " + e.getMessage());
                        }
                    }
                }

                for (Town town : NationsData.getAllTowns()) {
                    try { drawTownIcon(town, markers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Icon error " + town.getName() + ": " + e.getMessage());
                    }
                }

                NationsMod.LOGGER.info("BlueMap markers updated: " + markers.size() + " on " + mapId);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.error("BlueMap update error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== ОТРИСОВКА ==========

    private static void drawNationFill(Nation nation, Map<String, Object> markers) throws Exception {
        Set<ChunkPos> allChunks = new HashSet<>();
        for (String tn : nation.getTowns()) {
            Town t = NationsData.getTown(tn);
            if (t != null) allChunks.addAll(t.getClaimedChunks());
        }
        if (allChunks.isEmpty()) return;

        Set<String> outerEdges = calculateOuterEdges(allChunks);
        List<List<Point>> polygons = tracePolygons(outerEdges);

        int hex = nation.getColor().getHex();
        int r = (hex >> 16) & 0xFF, g = (hex >> 8) & 0xFF, b = hex & 0xFF;

        Object fillColor = cColor.newInstance(r, g, b, 0.25f);
        Object lineColor = cColor.newInstance(r, g, b, 1.0f);
        String popup = buildNationPopup(nation, r, g, b);

        int i = 0;
        for (List<Point> poly : polygons) {
            if (poly.size() < 3) continue;
            Object shape = createShape(poly);
            Object marker = createShapeMarker(nation.getName(), shape, fillColor, lineColor, 4, popup);
            markers.put("nation_" + nation.getName() + "_" + (i++), marker);
        }
    }

    private static void drawInnerTownBorders(Nation nation, Map<String, Object> markers) throws Exception {
        // Для каждого города внутри нации рисуем его собственную границу тонкой линией
        int hex = nation.getColor().getHex();
        int r = (hex >> 16) & 0xFF, g = (hex >> 8) & 0xFF, b = hex & 0xFF;

        // Чуть темнее/светлее чем основной цвет для различимости
        int dr = Math.max(0, r - 40), dg = Math.max(0, g - 40), db = Math.max(0, b - 40);
        Object lineColor = cColor.newInstance(dr, dg, db, 0.8f);
        Object noFill = cColor.newInstance(0, 0, 0, 0.0f);

        for (String townName : nation.getTowns()) {
            Town town = NationsData.getTown(townName);
            if (town == null || town.getClaimedChunks().isEmpty()) continue;

            Set<String> edges = calculateOuterEdges(town.getClaimedChunks());
            List<List<Point>> polygons = tracePolygons(edges);

            int j = 0;
            for (List<Point> poly : polygons) {
                if (poly.size() < 3) continue;
                Object shape = createShape(poly);
                // Толщина 2 (в 2 раза меньше чем у нации = 4), без заливки
                Object marker = createShapeMarker(townName, shape, noFill, lineColor, 2, "");
                markers.put("townborder_" + townName + "_" + (j++), marker);
            }
        }
    }

    private static void drawStandaloneTown(Town town, Map<String, Object> markers) throws Exception {
        if (town.getClaimedChunks().isEmpty()) return;

        Set<String> edges = calculateOuterEdges(town.getClaimedChunks());
        List<List<Point>> polygons = tracePolygons(edges);

        int r = 150, g = 150, b = 150;
        Object fillColor = cColor.newInstance(r, g, b, 0.30f);
        Object lineColor = cColor.newInstance(r, g, b, 1.0f);

        if (town.isAtWar()) {
            fillColor = cColor.newInstance(255, 50, 50, 0.4f);
            lineColor = cColor.newInstance(255, 50, 50, 1.0f);
        } else if (town.isCaptured()) {
            fillColor = cColor.newInstance(255, 140, 0, 0.4f);
            lineColor = cColor.newInstance(255, 140, 0, 1.0f);
        }

        String popup = buildPopup(town, "Без нации", r, g, b);
        int i = 0;
        for (List<Point> poly : polygons) {
            if (poly.size() < 3) continue;
            Object marker = createShapeMarker(town.getName(), createShape(poly), fillColor, lineColor, 3, popup);
            markers.put("standalone_" + town.getName() + "_" + (i++), marker);
        }
    }

    private static void drawTownIcon(Town town, Map<String, Object> markers) throws Exception {
        if (town.getSpawnPos() == null) return;

        boolean isCapital = false;
        String nationName = "Без нации";
        String colorHex = "#888888";

        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                int hex = nation.getColor().getHex();
                colorHex = String.format("#%06X", hex);
                nationName = nation.getName();
                isCapital = nation.isCapital(town.getName());
            }
        }

        double px = town.getSpawnPos().getX() + 0.5;
        double py = town.getSpawnPos().getY() + 2.0;
        double pz = town.getSpawnPos().getZ() + 0.5;

        // HTML иконка с эмодзи
        String emoji = isCapital ? "👑" : "🏰";
        int fontSize = isCapital ? 28 : 20;
        String label = town.getName();

        String html = "<div style=\"" +
            "display:flex;flex-direction:column;align-items:center;transform:translate(-50%,-100%);" +
            "pointer-events:auto;cursor:pointer;" +
            "\">" +
            "<div style=\"font-size:" + fontSize + "px;text-shadow:0 0 4px rgba(0,0,0,0.8);\">" + emoji + "</div>" +
            "<div style=\"" +
            "background:rgba(0,0,0,0.75);color:white;padding:2px 6px;border-radius:4px;" +
            "font-size:11px;font-family:'Segoe UI',sans-serif;white-space:nowrap;" +
            "border:1px solid " + colorHex + ";margin-top:2px;" +
            "\">" + label + "</div>" +
            "</div>";

        Object builder = mHtmlMarkerBuilder.invoke(null);
        mHtmlMarkerLabel.invoke(builder, label);
        mHtmlMarkerHtml.invoke(builder, html);

        if (mHtmlMarkerPosition.getParameterCount() == 1) {
            Object vec3 = cVector3d.newInstance(px, py, pz);
            mHtmlMarkerPosition.invoke(builder, vec3);
        } else {
            mHtmlMarkerPosition.invoke(builder, px, py, pz);
        }

        markers.put("icon_" + town.getName(), mHtmlMarkerBuild.invoke(builder));
    }

    // ========== УТИЛИТЫ ==========

    private static Set<String> calculateOuterEdges(Set<ChunkPos> chunks) {
        Set<String> edges = new HashSet<>();
        for (ChunkPos cp : chunks) {
            double x1 = cp.x * 16.0, z1 = cp.z * 16.0;
            double x2 = x1 + 16.0, z2 = z1 + 16.0;
            toggleEdge(edges, x1, z1, x2, z1);
            toggleEdge(edges, x2, z1, x2, z2);
            toggleEdge(edges, x2, z2, x1, z2);
            toggleEdge(edges, x1, z2, x1, z1);
        }
        return edges;
    }

    private static void toggleEdge(Set<String> edges, double x1, double z1, double x2, double z2) {
        String f = x1 + "," + z1 + ">" + x2 + "," + z2;
        String b = x2 + "," + z2 + ">" + x1 + "," + z1;
        if (edges.contains(b)) edges.remove(b); else edges.add(f);
    }

    private static List<List<Point>> tracePolygons(Set<String> edges) {
        List<List<Point>> polygons = new ArrayList<>();
        Map<Point, Point> map = new HashMap<>();
        for (String e : edges) {
            String[] p = e.split(">");
            String[] a = p[0].split(","), b = p[1].split(",");
            map.put(new Point(Double.parseDouble(a[0]), Double.parseDouble(a[1])),
                    new Point(Double.parseDouble(b[0]), Double.parseDouble(b[1])));
        }
        while (!map.isEmpty()) {
            List<Point> poly = new ArrayList<>();
            Point start = map.keySet().iterator().next();
            Point curr = start;
            int safety = 0;
            while (curr != null && safety++ < 200000) {
                poly.add(curr);
                Point next = map.remove(curr);
                if (next == null || next.equals(start)) break;
                curr = next;
            }
            if (poly.size() >= 3) polygons.add(poly);
        }
        return polygons;
    }

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

    private static class Point {
        double x, z;
        Point(double x, double z) { this.x = x; this.z = z; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point p = (Point) o;
            return Double.compare(p.x, x) == 0 && Double.compare(p.z, z) == 0;
        }
        @Override public int hashCode() { return Objects.hash(x, z); }
    }

    private static String buildNationPopup(Nation nation, int r, int g, int b) {
        String c = "rgb(" + r + "," + g + "," + b + ")";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:'Segoe UI',sans-serif;padding:12px;color:#fff;background:rgba(10,10,15,0.92);border-radius:8px;border:2px solid ").append(c).append("\">");
        sb.append("<div style=\"font-size:18px;font-weight:bold;color:").append(c).append(";margin-bottom:6px\">🏛 ").append(nation.getName()).append("</div>");
        sb.append("<hr style=\"border:0;border-top:1px solid #444;margin:6px 0\">");
        sb.append("<div style=\"margin:3px 0\">🏰 Городов: <b style=\"color:#FFD700\">").append(nation.getTowns().size()).append("</b></div>");
        sb.append("<div style=\"margin:3px 0\">👥 Жителей: <b>").append(nation.getTotalMembers()).append("</b></div>");
        sb.append("<div style=\"margin:3px 0\">📍 Территория: <b>").append(nation.getTotalChunks()).append("</b> чанков</div>");
        if (nation.getCapitalTown() != null)
            sb.append("<div style=\"margin:3px 0\">👑 Столица: <b style=\"color:#FFD700\">").append(nation.getCapitalTown()).append("</b></div>");
        if (!nation.getWarTargets().isEmpty())
            sb.append("<div style=\"margin:3px 0;color:#F44\">⚔ Война: ").append(String.join(", ", nation.getWarTargets())).append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    private static String buildPopup(Town town, String nName, int r, int g, int b) {
        String c = "rgb(" + r + "," + g + "," + b + ")";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:'Segoe UI',sans-serif;padding:10px;color:#fff;background:rgba(10,10,15,0.92);border-radius:6px;border:1px solid ").append(c).append("\">");
        sb.append("<div style=\"font-size:15px;font-weight:bold;margin-bottom:4px\">🏰 ").append(town.getName()).append("</div>");
        sb.append("<div style=\"color:").append(c).append(";margin-bottom:4px\">").append(nName).append("</div>");
        sb.append("<div style=\"font-size:12px;color:#999\">👥 ").append(town.getMembers().size()).append(" | 📍 ").append(town.getClaimedChunks().size()).append(" чанков</div>");
        if (town.isAtWar()) sb.append("<div style=\"color:#F44;margin-top:4px\">⚔ ВОЙНА</div>");
        if (town.isCaptured()) sb.append("<div style=\"color:#FA0;margin-top:4px\">🏴 Захвачен: ").append(town.getCapturedBy()).append("</div>");
        sb.append("</div>");
        return sb.toString();
    }
}
