package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.*;

public class DynmapIntegration {

    private static Object dynmapAPI = null;
    private static Object markerAPI = null;
    private static Object townMarkerSet = null;
    private static boolean enabled = false;
    private static boolean initialized = false;

    public static void init() {
        if (!ModList.get().isLoaded("dynmap")) {
            NationsMod.LOGGER.info("DynMap не установлен — интеграция отключена");
            return;
        }
        NationsMod.LOGGER.info("DynMap найден, ожидаем активации API...");
        // API будет получен при первом вызове updateAllMarkers
    }

    public static boolean isEnabled() {
        return enabled;
    }

    private static boolean tryConnect() {
        if (initialized) return enabled;

        try {
            // Получаем DynmapAPI через ForgeModHandler
            Class<?> pluginClass = Class.forName("org.dynmap.forge_1_20.DynmapMod");
            Object pluginInstance = null;

            // Попробуем через поле instance
            try {
                var field = pluginClass.getDeclaredField("instance");
                field.setAccessible(true);
                pluginInstance = field.get(null);
            } catch (Exception e) {
                // Попробуем другой способ
            }

            if (pluginInstance == null) {
                // Альтернативный способ — через DynmapCommonAPI
                Class<?> apiClass = Class.forName("org.dynmap.DynmapCommonAPI");

                // Ищем среди всех загруженных объектов
                // Используем другой подход — напрямую через marker API
                tryDirectMarkerAPI();
                return enabled;
            }

        } catch (Exception e) {
            NationsMod.LOGGER.debug("DynMap подключение (способ 1): " + e.getMessage());
        }

        // Способ 2: через Dynmap plugin напрямую
        try {
            tryDirectMarkerAPI();
        } catch (Exception e) {
            NationsMod.LOGGER.debug("DynMap подключение (способ 2): " + e.getMessage());
        }

        initialized = true;
        return enabled;
    }

    private static void tryDirectMarkerAPI() throws Exception {
        // Получаем API через статический метод
        Class<?> apiClass = Class.forName("org.dynmap.DynmapCommonAPI");

        // Ищем реализацию через DynmapPlugin
        Class<?> coreClass = Class.forName("org.dynmap.DynmapCore");

        // Пробуем через forge mod класс
        Class<?> forgeModClass = null;
        String[] classNames = {
            "org.dynmap.forge_1_20.DynmapMod",
            "org.dynmap.forge.DynmapMod",
            "org.dynmap.forge_1_20_1.DynmapMod"
        };

        for (String className : classNames) {
            try {
                forgeModClass = Class.forName(className);
                break;
            } catch (ClassNotFoundException ignored) {}
        }

        if (forgeModClass == null) {
            NationsMod.LOGGER.warn("DynMap: не удалось найти класс мода");
            return;
        }

        // Получаем instance
        Object modInstance = null;
        try {
            var field = forgeModClass.getDeclaredField("instance");
            field.setAccessible(true);
            modInstance = field.get(null);
        } catch (Exception e) {
            // Пробуем plugin
            try {
                var field = forgeModClass.getDeclaredField("plugin");
                field.setAccessible(true);
                modInstance = field.get(null);
            } catch (Exception e2) {
                NationsMod.LOGGER.warn("DynMap: не удалось получить instance");
                return;
            }
        }

        if (modInstance == null) {
            NationsMod.LOGGER.warn("DynMap: instance = null, API ещё не готов");
            return;
        }

        // Получаем MarkerAPI
        Method getMarkerAPI = null;
        for (Method m : modInstance.getClass().getMethods()) {
            if (m.getName().equals("getMarkerAPI") && m.getParameterCount() == 0) {
                getMarkerAPI = m;
                break;
            }
        }

        // Если не нашли в самом объекте, ищем core
        if (getMarkerAPI == null) {
            // Получаем core
            Object core = null;
            for (var field : modInstance.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object val = field.get(modInstance);
                if (val != null && val.getClass().getName().contains("DynmapCore")) {
                    core = val;
                    break;
                }
            }

            if (core != null) {
                for (Method m : core.getClass().getMethods()) {
                    if (m.getName().equals("getMarkerAPI") && m.getParameterCount() == 0) {
                        getMarkerAPI = m;
                        modInstance = core;
                        break;
                    }
                }
            }
        }

        if (getMarkerAPI == null) {
            NationsMod.LOGGER.warn("DynMap: метод getMarkerAPI не найден");
            return;
        }

        markerAPI = getMarkerAPI.invoke(modInstance);
        if (markerAPI == null) {
            NationsMod.LOGGER.warn("DynMap: MarkerAPI = null");
            return;
        }

        dynmapAPI = modInstance;
        enabled = true;
        setupMarkerSets();
        NationsMod.LOGGER.info("DynMap интеграция активирована!");
    }

    private static void setupMarkerSets() {
        try {
            Method getSet = markerAPI.getClass().getMethod("getMarkerSet", String.class);
            Object existing = getSet.invoke(markerAPI, "nations.towns");
            if (existing != null) {
                Method deleteSet = existing.getClass().getMethod("deleteMarkerSet");
                deleteSet.invoke(existing);
            }

            Method createSet = markerAPI.getClass().getMethod("createMarkerSet",
                String.class, String.class, Set.class, boolean.class);
            townMarkerSet = createSet.invoke(markerAPI,
                "nations.towns", "Города и Нации", null, false);

            if (townMarkerSet != null) {
                Method setPriority = townMarkerSet.getClass().getMethod("setLayerPriority", int.class);
                setPriority.invoke(townMarkerSet, 10);

                Method setHide = townMarkerSet.getClass().getMethod("setHideByDefault", boolean.class);
                setHide.invoke(townMarkerSet, false);
            }

        } catch (Exception e) {
            NationsMod.LOGGER.warn("DynMap: ошибка создания маркеров: " + e.getMessage());
        }
    }

    public static void updateAllMarkers() {
        if (!initialized) {
            tryConnect();
        }
        if (!enabled || townMarkerSet == null) return;

        try {
            clearMarkers();

            for (Town town : NationsData.getAllTowns()) {
                drawTown(town);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.debug("DynMap: ошибка обновления: " + e.getMessage());
        }
    }

    private static void clearMarkers() {
        try {
            Method getAreas = townMarkerSet.getClass().getMethod("getAreaMarkers");
            Object areasResult = getAreas.invoke(townMarkerSet);
            if (areasResult instanceof Set<?>) {
                for (Object area : new HashSet<>((Set<?>) areasResult)) {
                    Method delete = area.getClass().getMethod("deleteMarker");
                    delete.invoke(area);
                }
            }

            Method getMarkers = townMarkerSet.getClass().getMethod("getMarkers");
            Object markersResult = getMarkers.invoke(townMarkerSet);
            if (markersResult instanceof Set<?>) {
                for (Object marker : new HashSet<>((Set<?>) markersResult)) {
                    Method delete = marker.getClass().getMethod("deleteMarker");
                    delete.invoke(marker);
                }
            }
        } catch (Exception e) {
            NationsMod.LOGGER.debug("DynMap: ошибка очистки: " + e.getMessage());
        }
    }

    private static void drawTown(Town town) {
        try {
            int color = 0x888888;
            int fillColor = 0x888888;
            String nationName = "";
            int strokeWeight = 2;

            if (town.getNationName() != null) {
                Nation nation = NationsData.getNation(town.getNationName());
                if (nation != null) {
                    color = nation.getColor().getHex();
                    fillColor = color;
                    nationName = nation.getName();
                }
            }

            if (town.isAtWar()) {
                color = 0xFF0000;
                strokeWeight = 3;
            }
            if (town.isCaptured()) {
                color = 0xFF6600;
                strokeWeight = 3;
            }

            String worldName = "world";

            for (ChunkPos cp : town.getClaimedChunks()) {
                double x1 = cp.x * 16;
                double z1 = cp.z * 16;
                double x2 = x1 + 16;
                double z2 = z1 + 16;

                String markerId = "nations_" + town.getName().toLowerCase()
                    + "_" + cp.x + "_" + cp.z;

                Method createArea = townMarkerSet.getClass().getMethod(
                    "createAreaMarker",
                    String.class, String.class, boolean.class,
                    String.class, double[].class, double[].class, boolean.class);

                Object area = createArea.invoke(townMarkerSet,
                    markerId,
                    buildLabel(town, nationName),
                    true,
                    worldName,
                    new double[]{x1, x2, x2, x1},
                    new double[]{z1, z1, z2, z2},
                    false);

                if (area != null) {
                    Method setFill = area.getClass().getMethod(
                        "setFillStyle", double.class, int.class);
                    setFill.invoke(area, 0.3, fillColor);

                    Method setLine = area.getClass().getMethod(
                        "setLineStyle", int.class, double.class, int.class);
                    setLine.invoke(area, strokeWeight, 0.8, color);
                }
            }
        } catch (Exception e) {
            NationsMod.LOGGER.debug("DynMap: ошибка отрисовки " +
                town.getName() + ": " + e.getMessage());
        }
    }

    private static String buildLabel(Town town, String nationName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='padding:8px;background:rgba(0,0,0,0.85);");
        sb.append("border-radius:8px;text-align:center;min-width:180px;");

        // Цвет рамки
        String borderColor = "#888";
        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                borderColor = String.format("#%06X", nation.getColor().getHex());
            }
        }
        if (town.isAtWar()) borderColor = "#FF0000";
        if (town.isCaptured()) borderColor = "#FF6600";

        sb.append("border:2px solid ").append(borderColor).append(";'>");

        // Название города
        sb.append("<b style='font-size:14px;color:#FFD700;'>&#127984; ")
          .append(town.getName()).append("</b>");

        // Нация
        if (!nationName.isEmpty()) {
            sb.append("<br><span style='color:#6688FF;'>&#127963; ")
              .append(nationName).append("</span>");
        }

        // Статусы
        if (town.isAtWar()) {
            sb.append("<br><b style='color:#FF4444;'>&#9876; ЗОНА ВОЙНЫ</b>");
        }
        if (town.isCaptured()) {
            sb.append("<br><span style='color:#FF6600;'>&#127988; Захвачен: ")
              .append(town.getCapturedBy()).append("</span>");
        }

        // Статистика
        sb.append("<br><span style='color:#AAAAAA;'>Жителей: ")
          .append(town.getMembers().size())
          .append(" &bull; Чанков: ")
          .append(town.getClaimedChunks().size())
          .append("</span>");

        // PvP
        sb.append("<br><span style='color:")
          .append(town.isPvpEnabled() ? "#FF4444" : "#44FF44").append(";'>")
          .append(town.isPvpEnabled() ? "&#9876; PvP: ВКЛ" : "&#128737; PvP: ВЫКЛ")
          .append("</span>");

        // Правитель
        String mayorName = "оффлайн";
        if (NationsData.getServer() != null) {
            var p = NationsData.getServer().getPlayerList().getPlayer(town.getMayor());
            if (p != null) mayorName = p.getName().getString();
        }
        sb.append("<br><span style='color:#FFD700;'>&#128081; ")
          .append(mayorName).append("</span>");

        // Жители
        sb.append("<br><span style='color:#888;font-size:11px;'>Жители: ");
        int count = 0;
        for (UUID memberId : town.getMembers()) {
            if (count > 0) sb.append(", ");
            if (count >= 8) {
                sb.append("+" + (town.getMembers().size() - 8));
                break;
            }
            if (NationsData.getServer() != null) {
                var p = NationsData.getServer().getPlayerList().getPlayer(memberId);
                if (p != null) {
                    sb.append("<span style='color:#4F4;'>")
                      .append(p.getName().getString()).append("</span>");
                } else {
                    sb.append("<span style='color:#888;'>")
                      .append(memberId.toString().substring(0, 6)).append("</span>");
                }
            }
            count++;
        }
        sb.append("</span>");

        sb.append("</div>");
        return sb.toString();
    }
}
