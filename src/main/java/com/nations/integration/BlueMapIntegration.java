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

    // Оригинальные base64 иконки
    private static final String ICON_CROWN = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAABmJLR0QA/wD/AP+gvaeTAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH6gIQFR0z0/48VwAAAqVJREFUWMPtl89rE0EUx7+zs7vbFk0tVv+A4kERrJdePCh48CDiQfBisp68e/GsB717F/wD/AfEg3gQBC2ItyJ6Ew/WgxQramO32d3ZmRkP2zbZpGk3iXjwcWbe2/fNfN+ZefMGRF+W9X8D0C6V90d9LyPzQ4g0L0GZpwD4E+D6wN/16fN7I0Qh0FqZAtA+d+6o+30nhDYEASLhQGtlhghk/2ATADxpJigzTSa8IhqNQiMRCGBCeaSMjUZgCBCLte8SpV7o2M/JpA0AsrD+EwB0GGEALqb3A8gRsblo9yfZF0qy10w0fK21+9SLdWtO9cp4/b5QHTryHCdiawFk36oDAGSNMAD68JcA5OfDGV6p3b54r7FohfIZ8ypdz7a0nnpl8pr/1cajg9+b+A+vrfgA385dPfBYtPGlW15x6KwzOuVoBULs3BtJfbfZCO3sdwuvR4Ux/NfqCHdUedO+mhUvnymNVX+Q7Lnx+8ooM8e/wotnX33qiYbmLw8W5gdmVGnmsegXAPkAwCiK7bNng4o8n7LsL4o3f2h5yqdn8g2ZENbRdhE/HsI40CkTDLtFrrpzA5dWVIb6Mv7QgDPXP+IV6ZRJbIwD7SJ+vFZYRx6d/pv0lUL86GWTsvrCZlHzfHNNdM+OxtSiA/eem/Old8i53qt/rrvztNVVs2bVBTtlGGLsbTsRDQ/8K6xU7pBXdkf2hPMkpjFkIbTrkSOZrJCGfm3g9q5Iwj7dFxoibxn5M1K5GR7nlwfjdUHbbtlWba4orZdr/zIx+qeyaf5uwCwuAiw6EJcm2S49PegtXNyxNdG979HWkzccJ+Qnc3g1pcydTdX6bZ36mZ/eUPb50ZnY9l3tL/RubYj09jRGd/Z2vPCv29Oxn13X9vYO9UzPpkq9X1PK6GSB645jP74rc3L3zuSGfnf2QrS5ON0QCj2YgSklsKlQrajok/8B+le5c6l0OCgAAAAASUVORK5CYII=";
    private static final String ICON_TOWN = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAMAAAAoLQ9TAAAAA3NCSVQICAjb4U/gAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUH6gIQFR4s+v8+3QAAABl0RVh0Q29tbWVudABDcmVhdGVkIHdpdGggR0lNUFeBDhcAAABLUExURQAAAM/P0dDQ0svLzczMzvfz9vv7+/z7++Tm5tzd2/r5+fj49/n19/79/fX19P/+/v38/P39/f///+rq6cfHxt7e3qenpt/f3qiop9XU1KalpcC+vqKhocG/v8/PzquqqqGfn6OiocG7v8bFxMLAwNTU07q6uaWjpKqpqaaipa6qrLOusamlp6yoqv///9/h4dja2f////j49u3t7NLU1MbHxevr6vr6+erq6e3t7N3d3MbGxt7e3uDf37e3tqempt/f3t/f37a2tainp9TU09ra2bKxsaWlpL68vMvKydTT09XV1Lu7u7Kysainp5+enry6usHAv7Cwr5yam+np6NjY2NLS0ubm5eXl5d3d3MnJyN/f39vb27+/v7q6ud7e3tva2r+/vv///+VauVgAAABUdFJOUwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAKU1MITbP29rNNOe3tOUD29kBA9vZAOe3tOAhNs/f2s0wIClxcCsucjc0AAAABYktHRBJ7vGwAAAAAC4lEQVQY02NgIBIwMjGzIHFZ2dg5OLm4eWB8dl4+PX0DPT5+AaiAoKGRsYmpmbmhEFRA2MIyJDQs3MpaBCogamMbERIZZWcvBhUQd3CMjomNc3KWgApIurjGJyTGublLQQWkPTy9vH18/fxloAKy0nIBgUHB8gqKMIcopaioqqlraCK5VUtbR5dYbwIAoBMTO+1B/sUAAAAASUVORK5CYII=";

    private static Class<?> clsBlueMapAPI, clsBlueMapMap, clsMarkerSet;
    private static Class<?> clsShapeMarker, clsPOIMarker, clsShape, clsVector2d, clsVector3d, clsColor;
    private static Method mGetInstance, mGetMaps, mGetId, mGetMarkerSets;
    private static Method mMarkerSetBuilder, mMarkerSetLabel, mMarkerSetBuild, mMarkerSetGetMarkers;
    private static Method mShapeMarkerBuilder, mShapeMarkerLabel, mShapeMarkerShape;
    private static Method mShapeMarkerDepthTest, mShapeMarkerFillColor, mShapeMarkerLineColor;
    private static Method mShapeMarkerLineWidth, mShapeMarkerDetail, mShapeMarkerBuild;
    private static Method mPOIMarkerBuilder, mPOIMarkerLabel, mPOIMarkerPosition;
    private static Method mPOIMarkerDetail, mPOIMarkerIcon, mPOIMarkerBuild;
    private static Constructor<?> cVector2d, cVector3d, cShape, cColor;
    private static Method mOnEnable;
    private static boolean useVector3dPosition = false;
    private static boolean useVector2dAnchor = false;

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
        clsPOIMarker = Class.forName("de.bluecolored.bluemap.api.markers.POIMarker", true, cl);
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

        // POIMarker
        mPOIMarkerBuilder = clsPOIMarker.getMethod("builder");
        Class<?> pmb = mPOIMarkerBuilder.getReturnType();
        mPOIMarkerLabel = pmb.getMethod("label", String.class);
        mPOIMarkerDetail = pmb.getMethod("detail", String.class);
        mPOIMarkerBuild = pmb.getMethod("build");

        // position — пробуем разные варианты
        try {
            mPOIMarkerPosition = pmb.getMethod("position", double.class, double.class, double.class);
            useVector3dPosition = false;
        } catch (NoSuchMethodException e) {
            mPOIMarkerPosition = pmb.getMethod("position", clsVector3d);
            useVector3dPosition = true;
        }

        // icon — пробуем разные варианты
        try {
            mPOIMarkerIcon = pmb.getMethod("icon", String.class, int.class, int.class);
            useVector2dAnchor = false;
            NationsMod.LOGGER.info("BlueMap: using icon(String, int, int)");
        } catch (NoSuchMethodException e) {
            try {
                mPOIMarkerIcon = pmb.getMethod("icon", String.class, clsVector2d);
                useVector2dAnchor = true;
                NationsMod.LOGGER.info("BlueMap: using icon(String, Vector2d)");
            } catch (NoSuchMethodException e2) {
                mPOIMarkerIcon = null;
                NationsMod.LOGGER.warn("BlueMap: POI icon method not found, will use HtmlMarker fallback");
            }
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

            // Предварительно собираем все чанки всех наций для проверки внутренних границ
            Map<String, Set<ChunkPos>> nationChunksMap = new HashMap<>();
            for (Nation nation : NationsData.getAllNations()) {
                Set<ChunkPos> allChunks = new HashSet<>();
                for (String tn : nation.getTowns()) {
                    Town t = NationsData.getTown(tn);
                    if (t != null) allChunks.addAll(t.getClaimedChunks());
                }
                nationChunksMap.put(nation.getName(), allChunks);
            }

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

                // 1. Заливка + внешняя граница наций (толщина 4)
                for (Nation nation : NationsData.getAllNations()) {
                    try { drawNationFill(nation, markers, nationChunksMap.get(nation.getName())); }
                    catch (Exception e) { NationsMod.LOGGER.error("Nation fill error " + nation.getName() + ": " + e.getMessage()); }
                }

                // 2. Внутренние границы городов (толщина 2, только между городами одной нации)
                for (Nation nation : NationsData.getAllNations()) {
                    try { drawInnerTownBorders(nation, markers, nationChunksMap.get(nation.getName())); }
                    catch (Exception e) { NationsMod.LOGGER.error("Inner border error " + nation.getName() + ": " + e.getMessage()); }
                }

                // 3. Города без нации
                for (Town town : NationsData.getAllTowns()) {
                    if (town.getNationName() == null) {
                        try { drawStandaloneTown(town, markers); }
                        catch (Exception e) { NationsMod.LOGGER.error("Standalone error " + town.getName() + ": " + e.getMessage()); }
                    }
                }

                // 4. Иконки городов (POIMarker с base64)
                for (Town town : NationsData.getAllTowns()) {
                    try { drawTownIcon(town, markers); }
                    catch (Exception e) { NationsMod.LOGGER.error("Icon error " + town.getName() + ": " + e.getMessage()); }
                }

                NationsMod.LOGGER.info("BlueMap markers updated: " + markers.size() + " on " + mapId);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.error("BlueMap update error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== ОТРИСОВКА ====================

    private static void drawNationFill(Nation nation, Map<String, Object> markers, Set<ChunkPos> allChunks) throws Exception {
        if (allChunks == null || allChunks.isEmpty()) return;

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

    private static void drawInnerTownBorders(Nation nation, Map<String, Object> markers, Set<ChunkPos> allNationChunks) throws Exception {
        if (allNationChunks == null || allNationChunks.isEmpty()) return;

        int hex = nation.getColor().getHex();
        int r = (hex >> 16) & 0xFF, g = (hex >> 8) & 0xFF, b = hex & 0xFF;
        int dr = Math.max(0, r - 50), dg = Math.max(0, g - 50), db = Math.max(0, b - 50);
        Object lineColor = cColor.newInstance(dr, dg, db, 0.7f);
        Object noFill = cColor.newInstance(0, 0, 0, 0.0f);

        for (String townName : nation.getTowns()) {
            Town town = NationsData.getTown(townName);
            if (town == null || town.getClaimedChunks().isEmpty()) continue;

            // Вычисляем рёбра только МЕЖДУ этим городом и другими городами той же нации
            Set<String> innerEdges = new HashSet<>();
            for (ChunkPos cp : town.getClaimedChunks()) {
                double x1 = cp.x * 16.0, z1 = cp.z * 16.0;
                double x2 = x1 + 16.0, z2 = z1 + 16.0;

                // Для каждой стороны чанка проверяем: сосед принадлежит другому городу той же нации?
                checkBorderEdge(innerEdges, town, nation, new ChunkPos(cp.x, cp.z - 1), x1, z1, x2, z1); // Север
                checkBorderEdge(innerEdges, town, nation, new ChunkPos(cp.x, cp.z + 1), x1, z2, x2, z2); // Юг
                checkBorderEdge(innerEdges, town, nation, new ChunkPos(cp.x + 1, cp.z), x2, z1, x2, z2); // Восток
                checkBorderEdge(innerEdges, town, nation, new ChunkPos(cp.x - 1, cp.z), x1, z1, x1, z2); // Запад
            }

            if (innerEdges.isEmpty()) continue;

            // Собираем рёбра в полигоны и рисуем
            List<List<Point>> borderPolys = tracePolygonsFromEdgeStrings(innerEdges);
            int j = 0;
            for (List<Point> poly : borderPolys) {
                if (poly.size() < 2) continue;
                // Рисуем как тонкий полигон-линию
                for (int k = 0; k < poly.size() - 1; k++) {
                    Point p1 = poly.get(k), p2 = poly.get(k + 1);
                    List<Point> linePoly = makeLinePoly(p1, p2, 0.3);
                    if (linePoly.size() >= 3) {
                        Object shape = createShape(linePoly);
                        Object marker = createShapeMarker("", shape, noFill, lineColor, 2, "");
                        markers.put("inner_" + townName + "_" + (j++), marker);
                    }
                }
            }
        }
    }

    private static void checkBorderEdge(Set<String> edges, Town myTown, Nation nation, ChunkPos neighbor,
                                         double x1, double z1, double x2, double z2) {
        // Проверяем: сосед принадлежит другому городу той же нации?
        for (String otherTownName : nation.getTowns()) {
            if (otherTownName.equals(myTown.getName())) continue;
            Town otherTown = NationsData.getTown(otherTownName);
            if (otherTown != null && otherTown.ownsChunk(neighbor)) {
                // Это внутренняя граница между двумя городами одной нации
                String p1 = x1 + "," + z1;
                String p2 = x2 + "," + z2;
                if (p1.compareTo(p2) > 0) { String t = p1; p1 = p2; p2 = t; }
                edges.add(p1 + ">" + p2);
                return;
            }
        }
    }

    private static List<Point> makeLinePoly(Point a, Point b, double w) {
        List<Point> poly = new ArrayList<>();
        if (Math.abs(a.x - b.x) < 0.01) {
            // Вертикальная линия
            poly.add(new Point(a.x - w, Math.min(a.z, b.z)));
            poly.add(new Point(a.x + w, Math.min(a.z, b.z)));
            poly.add(new Point(b.x + w, Math.max(a.z, b.z)));
            poly.add(new Point(b.x - w, Math.max(a.z, b.z)));
        } else {
            // Горизонтальная линия
            poly.add(new Point(Math.min(a.x, b.x), a.z - w));
            poly.add(new Point(Math.max(a.x, b.x), a.z - w));
            poly.add(new Point(Math.max(a.x, b.x), a.z + w));
            poly.add(new Point(Math.min(a.x, b.x), a.z + w));
        }
        return poly;
    }

    private static List<List<Point>> tracePolygonsFromEdgeStrings(Set<String> edgeStrs) {
        List<List<Point>> result = new ArrayList<>();
        for (String e : edgeStrs) {
            String[] pts = e.split(">");
            String[] a = pts[0].split(","), b = pts[1].split(",");
            List<Point> line = new ArrayList<>();
            line.add(new Point(Double.parseDouble(a[0]), Double.parseDouble(a[1])));
            line.add(new Point(Double.parseDouble(b[0]), Double.parseDouble(b[1])));
            result.add(line);
        }
        return result;
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
        int cr = 136, cg = 136, cb = 136;

        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                int hex = nation.getColor().getHex();
                cr = (hex >> 16) & 0xFF; cg = (hex >> 8) & 0xFF; cb = hex & 0xFF;
                nationName = nation.getName();
                isCapital = nation.isCapital(town.getName());
            }
        }

        double px = town.getSpawnPos().getX() + 0.5;
        double py = town.getSpawnPos().getY() + 2.0;
        double pz = town.getSpawnPos().getZ() + 0.5;

        String popup = buildPopup(town, nationName, cr, cg, cb);

        // Пробуем POIMarker с base64 иконкой
        if (mPOIMarkerIcon != null) {
            try {
                Object builder = mPOIMarkerBuilder.invoke(null);
                mPOIMarkerLabel.invoke(builder, town.getName());

                if (useVector3dPosition) {
                    Object vec3 = cVector3d.newInstance(px, py, pz);
                    mPOIMarkerPosition.invoke(builder, vec3);
                } else {
                    mPOIMarkerPosition.invoke(builder, px, py, pz);
                }

                mPOIMarkerDetail.invoke(builder, popup);

                String icon = isCapital ? ICON_CROWN : ICON_TOWN;
                if (useVector2dAnchor) {
                    Object anchor = cVector2d.newInstance(16.0, 16.0);
                    mPOIMarkerIcon.invoke(builder, icon, anchor);
                } else {
                    mPOIMarkerIcon.invoke(builder, icon, 16, 16);
                }

                markers.put("icon_" + town.getName(), mPOIMarkerBuild.invoke(builder));
                return;
            } catch (Exception e) {
                NationsMod.LOGGER.warn("POIMarker icon failed for " + town.getName() + ", trying HtmlMarker: " + e.getMessage());
            }
        }

        // Фолбэк: HtmlMarker
        try {
            Class<?> clsHtmlMarker = Class.forName("de.bluecolored.bluemap.api.markers.HtmlMarker", true, BlueMapIntegration.class.getClassLoader());
            Method hmBuilder = clsHtmlMarker.getMethod("builder");
            Class<?> hmb = hmBuilder.getReturnType();
            Method hmLabel = hmb.getMethod("label", String.class);
            Method hmHtml = hmb.getMethod("html", String.class);
            Method hmBuild = hmb.getMethod("build");
            Method hmPos;
            boolean hmUseVec3;
            try {
                hmPos = hmb.getMethod("position", double.class, double.class, double.class);
                hmUseVec3 = false;
            } catch (NoSuchMethodException ex) {
                hmPos = hmb.getMethod("position", clsVector3d);
                hmUseVec3 = true;
            }

            String emoji = isCapital ? "👑" : "🏠";
            int size = isCapital ? 24 : 16;
            String html = "<div style=\"font-size:" + size + "px;transform:translate(-50%,-50%);text-shadow:0 0 3px #000;\">" + emoji + "</div>";

            Object b = hmBuilder.invoke(null);
            hmLabel.invoke(b, town.getName());
            hmHtml.invoke(b, html);
            if (hmUseVec3) {
                hmPos.invoke(b, cVector3d.newInstance(px, py, pz));
            } else {
                hmPos.invoke(b, px, py, pz);
            }
            markers.put("icon_" + town.getName(), hmBuild.invoke(b));
        } catch (Exception e2) {
            NationsMod.LOGGER.error("HtmlMarker fallback also failed for " + town.getName() + ": " + e2.getMessage());
        }
    }

    // ==================== УТИЛИТЫ ====================

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

    static class Point {
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
        sb.append("<div>🏰 Городов: <b style=\"color:#FFD700\">").append(nation.getTowns().size()).append("</b></div>");
        sb.append("<div>👥 Жителей: <b>").append(nation.getTotalMembers()).append("</b></div>");
        sb.append("<div>📍 Территория: <b>").append(nation.getTotalChunks()).append("</b> чанков</div>");
        if (nation.getCapitalTown() != null)
            sb.append("<div>👑 Столица: <b style=\"color:#FFD700\">").append(nation.getCapitalTown()).append("</b></div>");
        if (!nation.getWarTargets().isEmpty())
            sb.append("<div style=\"color:#F44\">⚔ Война: ").append(String.join(", ", nation.getWarTargets())).append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    private static String buildPopup(Town town, String nName, int r, int g, int b) {
        String c = "rgb(" + r + "," + g + "," + b + ")";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-family:'Segoe UI',sans-serif;padding:10px;color:#fff;background:rgba(10,10,15,0.92);border-radius:6px;border:1px solid ").append(c).append("\">");
        sb.append("<div style=\"font-size:15px;font-weight:bold\">🏰 ").append(town.getName()).append("</div>");
        sb.append("<div style=\"color:").append(c).append("\">").append(nName).append("</div>");
        sb.append("<div style=\"font-size:12px;color:#999\">👥 ").append(town.getMembers().size()).append(" | 📍 ").append(town.getClaimedChunks().size()).append(" чанков</div>");
        if (town.isAtWar()) sb.append("<div style=\"color:#F44\">⚔ ВОЙНА</div>");
        if (town.isCaptured()) sb.append("<div style=\"color:#FA0\">🏴 Захвачен: ").append(town.getCapturedBy()).append("</div>");
        sb.append("</div>");
        return sb.toString();
    }
}
