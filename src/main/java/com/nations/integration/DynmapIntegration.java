package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.*;

public class DynmapIntegration {

    private static Object markerAPI = null;
    private static Object townMarkerSet = null;
    private static boolean enabled = false;

    public static void init() {
        if (!ModList.get().isLoaded("dynmap")) {
            NationsMod.LOGGER.info("DynMap не найден — интеграция отключена");
            return;
        }
        NationsMod.LOGGER.info("DynMap обнаружен. Подключение через API Bridge...");
    }

    private static void tryConnect() {
        if (enabled) return;

        try {
            // Пытаемся получить API через официальный листенер Dynmap
            Class<?> apiListenerClass = Class.forName("org.dynmap.DynmapCommonAPIListener");
            Method getApiMethod = apiListenerClass.getMethod("getApi");
            Object api = getApiMethod.invoke(null);

            if (api != null) {
                Method getMarkerAPI = api.getClass().getMethod("getMarkerAPI");
                markerAPI = getMarkerAPI.invoke(api);
                
                if (markerAPI != null) {
                    setupMarkerSets();
                    enabled = true;
                    NationsMod.LOGGER.info("DynMap интеграция успешно запущена через API Bridge!");
                }
            }
        } catch (Exception e) {
            // Если не вышло, пробуем через Core напрямую (для версии 3.7+)
            try {
                Class<?> coreClass = Class.forName("org.dynmap.DynmapCore");
                Object core = coreClass.getField("instance").get(null);
                if (core != null) {
                    markerAPI = core.getClass().getMethod("getMarkerAPI").invoke(core);
                    if (markerAPI != null) {
                        setupMarkerSets();
                        enabled = true;
                        NationsMod.LOGGER.info("DynMap интеграция запущена через Core Instance!");
                    }
                }
            } catch (Exception e2) {
                // Ждем следующего тика
            }
        }
    }

    private static void setupMarkerSets() throws Exception {
        Method getSet = markerAPI.getClass().getMethod("getMarkerSet", String.class);
        Object existing = getSet.invoke(markerAPI, "nations.towns");
        if (existing != null) {
            existing.getClass().getMethod("deleteMarkerSet").invoke(existing);
        }

        Method createSet = markerAPI.getClass().getMethod("createMarkerSet", 
            String.class, String.class, Set.class, boolean.class);
        
        townMarkerSet = createSet.invoke(markerAPI, "nations.towns", "Города и Нации", null, false);
    }

    public static void updateAllMarkers() {
        if (!enabled) tryConnect();
        if (!enabled || townMarkerSet == null) return;

        try {
            clearMarkers();
            for (Town town : NationsData.getAllTowns()) {
                drawTown(town);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.debug("Dynmap Update Error: " + e.getMessage());
        }
    }

    private static void clearMarkers() throws Exception {
        Method getAreas = townMarkerSet.getClass().getMethod("getAreaMarkers");
        Set<?> areas = (Set<?>) getAreas.invoke(townMarkerSet);
        if (areas != null) {
            for (Object area : new HashSet<>(areas)) {
                area.getClass().getMethod("deleteMarker").invoke(area);
            }
        }
    }

    private static void drawTown(Town town) throws Exception {
        int color = 0x888888;
        String nationName = "";
        
        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                color = nation.getColor().getHex();
                nationName = nation.getName();
            }
        }

        if (town.isAtWar()) color = 0xFF0000;
        if (town.isCaptured()) color = 0xFF6600;

        for (ChunkPos cp : town.getClaimedChunks()) {
            double x1 = cp.x * 16;
            double z1 = cp.z * 16;
            double x2 = x1 + 16;
            double z2 = z1 + 16;

            String markerId = "n_" + town.getName() + "_" + cp.x + "_" + cp.z;
            
            Method createArea = townMarkerSet.getClass().getMethod("createAreaMarker",
                String.class, String.class, boolean.class, String.class, 
                double[].class, double[].class, boolean.class);

            // Параметры: ID, Label, isHTML, World, X[], Z[], persistent
            Object area = createArea.invoke(townMarkerSet, markerId, buildLabel(town, nationName), 
                true, "world", new double[]{x1, x2, x2, x1}, new double[]{z1, z1, z2, z2}, false);

            if (area != null) {
                area.getClass().getMethod("setFillStyle", double.class, int.class).invoke(area, 0.35, color);
                area.getClass().getMethod("setLineStyle", int.class, double.class, int.class).invoke(area, 2, 0.8, color);
            }
        }
    }

    private static String buildLabel(Town town, String nationName) {
        String borderColor = town.isAtWar() ? "#F00" : "#FFD700";
        return "<div style='padding:10px; background:rgba(0,0,0,0.85); border:2px solid " + borderColor + "; border-radius:10px; color:white;'>" +
               "<b style='font-size:14px; color:#FFD700;'>🏰 " + town.getName() + "</b>" +
               (nationName.isEmpty() ? "" : "<br><span style='color:#5af;'>🏛 Нация: " + nationName + "</span>") +
               "<hr style='border:0; border-top:1px solid #444;'>" +
               "👥 Жителей: " + town.getMembers().size() + "<br>" +
               "⚔ PvP: " + (town.isPvpEnabled() ? "<span style='color:#f44;'>ВКЛ</span>" : "<span style='color:#4f4;'>ВЫКЛ</span>") +
               "</div>";
    }
}
