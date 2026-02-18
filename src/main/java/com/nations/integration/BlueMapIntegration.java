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
    private static Class<?> clsShapeMarker, clsShape, clsVector2d, clsVector3d, clsColor;
    private static Class<?> clsHtmlMarker;
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets;
    private static Method mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape;
    private static Method mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor;
    private static Method mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mHtmlMarkerBuilder, mHtmlMarkerLabel, mHtmlMarkerPosition, mHtmlMarkerHtml, mHtmlMarkerBuild;
    private static boolean htmlUseVec3 = false;
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
            NationsMod.LOGGER.info("BlueMap integration registered.");
        } catch (Exception e) {
            NationsMod.LOGGER.error("Failed to init BlueMap: " + e.getMessage());
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
                if (!lower.contains("overworld") && !lower.equals("world") && !lower.contains("мир")) continue;

                Map<String, Object> markerSets = (Map<String, Object>) mGetMarkerSets.invoke(map);
                Object markerSet = markerSets.get(MARKER_SET_ID);
                if (markerSet == null) {
                    Object msBuilder = mMarkerSetBuilder.invoke(null);
                    mMarkerSetLabel.invoke(msBuilder, "Нации и Города");
                    markerSet = mMarkerSetBuild.invoke(msBuilder);
                    markerSets.put(MARKER_SET_ID, markerSet);
                }

                Map<String, Object> markers = (Map<String, Object>) mMarkerSetGetMarkers.invoke(markerSet);
                markers.clear();

                // 1. Заливка + внешняя граница наций (толщина 4)
                for (Nation nation : NationsData.getAllNations()) {
                    try { drawNationFill(nation, markers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Nation error " + nation.getName() + ": " + e.getMessage());
                    }
                }

                // 2. Внутренние границы между городами (толщина 2)
                for (Nation nation : NationsData.getAllNations()) {
                    try { drawInnerBorders(nation, markers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Inner border error " + nation.getName() + ": " + e.getMessage());
                    }
                }

                // 3. Города без нации
                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null) {
                        try { drawStandaloneTown(town, markers); } catch (Exception e) {
                            NationsMod.LOGGER.error("Standalone error " + town.getName() + ": " + e.getMessage());
                        }
                    }
                }

                // 4. Иконки городов
                for (Town town : NationsData.getAllTowns()) {
                    try { drawTownIcon(town, markers); } catch (Exception e) {
                        NationsMod.LOGGER.error("Icon error " + town.getName() + ": " + e.getMessage());
                    }
                }

                NationsMod.LOGGER.info("BlueMap markers updated: " + markers.size() + " on " + mapId);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.error("BlueMap update error: " + e.getMessage());
        }
    }

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
        Object fill = cColor.newInstance(cr, cg, cb, 0.25f);
        Object line = cColor.newInstance(cr, cg, cb, 1.0f);
        String popup = buildNationPopup(nation, cr, cg, cb);

        int i = 0;
        for (List<Point> poly : polygons) {
            if (poly.size() < 3) continue;
            markers.put("nation_" + nation.getName() + "_" + (i++),
                createShapeMarker(nation.getName(), createShape(poly), fill, line, 4, popup));
        }
    }

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

        // Собираем уникальные рёбра между разными городами
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

        // Также убираем рёбра которые совпадают с внешней границей нации
        Set<ChunkPos> allNationChunks = new HashSet<>(chunkToTown.keySet());

        int j = 0;
        for (double[] ec : edgeCoords) {
            double ax = ec[0], az = ec[1], bx = ec[2], bz = ec[3];

            // Проверяем: это ребро НЕ должно быть на внешней границе нации
            // Внешняя граница = ребро где с одной стороны нация, с другой — пустота
            // Внутреннее ребро = с обеих сторон есть чанки нации, но разных городов
            // Так как мы уже фильтруем по "другой город той же нации", это автоматически внутреннее

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
                                          Map<ChunkPos, String> map, String myTown, ChunkPos neighbor,
                                          double x1, double z1, double x2, double z2) {
        String other = map.get(neighbor);
        if (other != null && !other.equals(myTown)) {
            // Создаём уникальный ключ для ребра (сортировка координат + имён городов)
            String edgeKey;
            String townA = myTown.compareTo(other) < 0 ? myTown : other;
            String townB = myTown.compareTo(other) < 0 ? other : myTown;
            double minX = Math.min(x1, x2), minZ = Math.min(z1, z2);
            double maxX = Math.max(x1, x2), maxZ = Math.max(z1, z2);
            edgeKey = townA + "|" + townB + "|" + minX + "," + minZ + "," + maxX + "," + maxZ;

            if (!processed.contains(edgeKey)) {
                processed.add(edgeKey);
                coords.add(new double[]{x1, z1, x2, z2});
            }
        }
    }

    private static void drawStandaloneTown(Town town, Map<String, Object> markers) throws Exception {
        if (town.getClaimedChunks().isEmpty()) return;
        Set<String> edges = calcEdges(town.getClaimedChunks());
        List<List<Point>> polygons = tracePolygons(edges);
        int cr = 150, cg = 150, cb = 150;
        Object fill = cColor.newInstance(cr, cg, cb, 0.30f);
        Object line = cColor.newInstance(cr, cg, cb, 1.0f);
        if (town.isAtWar()) { fill = cColor.newInstance(255, 50, 50, 0.4f); line = cColor.newInstance(255, 50, 50, 1.0f); }
        else if (town.isCaptured()) { fill = cColor.newInstance(255, 140, 0, 0.4f); line = cColor.newInstance(255, 140, 0, 1.0f); }
        String popup = buildPopup(town, "Без нации", cr, cg, cb);
        int i = 0;
        for (List<Point> poly : polygons) {
            if (poly.size() < 3) continue;
            markers.put("standalone_" + town.getName() + "_" + (i++),
                createShapeMarker(town.getName(), createShape(poly), fill, line, 3, popup));
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
                colorHex = String.format("#%06X", nation.getColor().getHex());
                nationName = nation.getName();
                isCapital = nation.isCapital(town.getName());
            }
        }

        double px = town.getSpawnPos().getX() + 0.5;
        double py = town.getSpawnPos().getY() + 2.0;
        double pz = town.getSpawnPos().getZ() + 0.5;

        // Используем CSS background-image с base64 вместо <img>
        String symbol = isCapital ? "&#x1F451;" : "&#x1F3E0;";
        int size = isCapital ? 28 : 18;

        String html = "<div style=\"" +
            "position:relative;" +
            "transform:translate(-50%,-50%);" +
            "width:" + size + "px;" +
            "height:" + size + "px;" +
            "font-size:" + size + "px;" +
            "line-height:" + size + "px;" +
            "text-align:center;" +
            "cursor:pointer;" +
            "filter:drop-shadow(0 0 2px rgba(0,0,0,0.8));" +
            "\">" + symbol + "</div>";

        Object builder = mHtmlMarkerBuilder.invoke(null);
        mHtmlMarkerLabel.invoke(builder, town.getName());
        mHtmlMarkerHtml.invoke(builder, html);

        if (htmlUseVec3) {
            mHtmlMarkerPosition.invoke(builder, cVector3d.newInstance(px, py, pz));
        } else {
            mHtmlMarkerPosition.invoke(builder, px, py, pz);
        }

        markers.put("icon_" + town.getName(), mHtmlMarkerBuild.invoke(builder));
    }

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
        String f = x1+","+z1+">"+x2+","+z2, rev = x2+","+z2+">"+x1+","+z1;
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

    private static String buildNationPopup(Nation n, int r, int g, int b) {
        String c = "rgb("+r+","+g+","+b+")";
        return "<div style=\"font-family:'Segoe UI',sans-serif;padding:12px;color:#fff;background:rgba(10,10,15,0.92);border-radius:8px;border:2px solid "+c+"\">" +
            "<div style=\"font-size:18px;font-weight:bold;color:"+c+"\">🏛 "+n.getName()+"</div>" +
            "<hr style=\"border:0;border-top:1px solid #444;margin:6px 0\">" +
            "<div>🏰 Городов: <b style=\"color:#FFD700\">"+n.getTowns().size()+"</b></div>" +
            "<div>👥 Жителей: <b>"+n.getTotalMembers()+"</b></div>" +
            "<div>📍 Территория: <b>"+n.getTotalChunks()+"</b> чанков</div>" +
            (n.getCapitalTown()!=null?"<div>👑 Столица: <b style=\"color:#FFD700\">"+n.getCapitalTown()+"</b></div>":"") +
            (!n.getWarTargets().isEmpty()?"<div style=\"color:#F44\">⚔ Война: "+String.join(", ",n.getWarTargets())+"</div>":"") +
            "</div>";
    }

    private static String buildPopup(Town t, String nName, int r, int g, int b) {
        String c = "rgb("+r+","+g+","+b+")";
        return "<div style=\"font-family:'Segoe UI',sans-serif;padding:10px;color:#fff;background:rgba(10,10,15,0.92);border-radius:6px;border:1px solid "+c+"\">" +
            "<div style=\"font-size:15px;font-weight:bold\">🏰 "+t.getName()+"</div>" +
            "<div style=\"color:"+c+"\">"+nName+"</div>" +
            "<div style=\"font-size:12px;color:#999\">👥 "+t.getMembers().size()+" | 📍 "+t.getClaimedChunks().size()+" чанков</div>" +
            (t.isAtWar()?"<div style=\"color:#F44\">⚔ ВОЙНА</div>":"") +
            (t.isCaptured()?"<div style=\"color:#FA0\">🏴 Захвачен: "+t.getCapturedBy()+"</div>":"") +
            "</div>";
    }
}
