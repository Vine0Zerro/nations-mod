package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
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
        NationsMod.LOGGER.info("DynMap обнаружен. Попытка подключения к API...");
    }

    private static void tryConnect() {
        if (enabled && markerAPI != null) return;

        try {
            // В Forge Dynmap API доступен через org.dynmap.DynmapCommonAPIListener.api
            Class<?> apiListenerClass = Class.forName("org.dynmap.DynmapCommonAPIListener");
            Field apiField = apiListenerClass.getDeclaredField("api");
            apiField.setAccessible(true);
            Object api = apiField.get(null);

            if (api != null) {
                Method getMarkerAPIMethod = api.getClass().getMethod("getMarkerAPI");
                markerAPI = getMarkerAPIMethod.invoke(api);
                
                if (markerAPI != null) {
                    setupMarkerSets();
                    enabled = true;
                    NationsMod.LOGGER.info("DynMap интеграция успешно активирована!");
                }
            }
        } catch (Exception e) {
            // Если через статический доступ не вышло, попробуем через Core
            try {
                Class<?> coreClass = Class.forName("org.dynmap.DynmapCore");
                Field field = coreClass.getDeclaredField("instance");
                field.setAccessible(true);
                Object core = field.get(null);
                if (core != null) {
                    Method getMarkerAPI = core.getClass().getMethod("getMarkerAPI");
                    markerAPI = getMarkerAPI.invoke(core);
                    if (markerAPI != null) {
                        setupMarkerSets();
                        enabled = true;
                        NationsMod.LOGGER.info("DynMap интеграция активирована через Core!");
                    }
                }
            } catch (Exception e2) {
                // Молчим, чтобы не спамить в лог каждую секунду
            }
        }
    }

    private static void setupMarkerSets() throws Exception {
        Method getSet = markerAPI.getClass().getMethod("getMarkerSet", String.class);
        Object existing = getSet.invoke(markerAPI, "nations.towns");
        if (existing != null) {
            Method deleteSet = existing.getClass().getMethod("deleteMarkerSet");
            deleteSet.invoke(existing);
        }

        Method createSet = markerAPI.getClass().getMethod("createMarkerSet", 
            String.class, String.class, Set.class, boolean.class);
        
        // nations.towns, Название в списке слоев, иконки (null), не скрывать по умолчанию
        townMarkerSet = createSet.invoke(markerAPI, "nations.towns", "Города и Нации", null, false);
    }

    public static void updateAllMarkers() {
        if (!enabled) {
            tryConnect();
        }
        if (!enabled || townMarkerSet == null) return;

        try {
            clearMarkers();
            for (Town town : NationsData.getAllTowns()) {
                drawTown(town);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.debug("Ошибка обновления DynMap: " + e.getMessage());
        }
    }

    private static void clearMarkers() throws Exception {
        Method getAreas = townMarkerSet.getClass().getMethod("getAreaMarkers");
        Set<?> areas = (Set<?>) getAreas.invoke(townMarkerSet);
        for (Object area : new HashSet<>(areas)) {
            Method delete = area.getClass().getMethod("deleteMarker");
            delete.invoke(area);
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

            Object area = createArea.invoke(townMarkerSet, markerId, buildLabel(town, nationName), 
                true, "world", new double[]{x1, x2, x2, x1}, new double[]{z1, z1, z2, z2}, false);

            if (area != null) {
                Method setFill = area.getClass().getMethod("setFillStyle", double.class, int.class);
                setFill.invoke(area, 0.35, color);
                Method setLine = area.getClass().getMethod("setLineStyle", int.class, double.class, int.class);
                setLine.invoke(area, 2, 0.8, color);
            }
        }
    }

    private static String buildLabel(Town town, String nationName) {
        String borderColor = town.isAtWar() ? "#F00" : "#FFD700";
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='padding:10px; background:rgba(0,0,0,0.9); border:2px solid ").append(borderColor).append("; border-radius:10px; color:white; font-family:sans-serif;'>");
        sb.append("<div style='font-size:16px; font-weight:bold; color:#FFD700; margin-bottom:5px;'>🏰 ").append(town.getName()).append("</div>");
        
        if (!nationName.isEmpty()) {
            sb.append("<div style='color:#55AAFF; font-weight:bold; margin-bottom:5px;'>🏛 Нация: ").append(nationName).append("</div>");
        }

        if (town.isAtWar()) sb.append("<div style='color:#FF4444; font-weight:bold;'>⚠️ СОСТОЯНИЕ ВОЙНЫ</div>");
        if (town.isCaptured()) sb.append("<div style='color:#FFAA00;'>🏴 Захвачен нацией: ").append(town.getCapturedBy()).append("</div>");

        sb.append("<hr style='border:0; border-top:1px solid #444; margin:8px 0;'>");
        sb.append("<div style='font-size:12px;'>");
        sb.append("👥 Жителей: <span style='color:#FFF;'>").append(town.getMembers().size()).append("</span><br>");
        sb.append("📍 Чанков: <span style='color:#FFF;'>").append(town.getClaimedChunks().size()).append("</span><br>");
        sb.append("⚔ PvP: ").append(town.isPvpEnabled() ? "<span style='color:#FF4444;'>ВКЛ</span>" : "<span style='color:#44FF44;'>ВЫКЛ</span>").append("<br>");
        
        // Правитель
        String mayorName = "неизвестно";
        if (NationsData.getServer() != null) {
            var p = NationsData.getServer().getPlayerList().getPlayer(town.getMayor());
            if (p != null) mayorName = p.getName().getString();
        }
        sb.append("👑 Правитель: <span style='color:#FFD700;'>").append(mayorName).append("</span>");
        sb.append("</div>");

        // Список жителей (укороченный)
        sb.append("<div style='font-size:10px; color:#AAA; margin-top:5px;'>Жители: ");
        int i = 0;
        for (UUID id : town.getMembers()) {
            if (i > 0) sb.append(", ");
            if (i > 5) { sb.append("и др."); break; }
            if (NationsData.getServer() != null) {
                var p = NationsData.getServer().getPlayerList().getPlayer(id);
                sb.append(p != null ? p.getName().getString() : "оффлайн");
            }
            i++;
        }
        sb.append("</div></div>");
        return sb.toString();
    }
}
