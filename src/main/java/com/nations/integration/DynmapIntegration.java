package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.DynmapCore;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.HashSet;
import java.util.Set;

public class DynmapIntegration {
    private static MarkerAPI markerAPI = null;
    private static MarkerSet townMarkerSet = null;
    private static boolean enabled = false;

    public static void init() {
        if (!ModList.get().isLoaded("dynmap")) {
            NationsMod.LOGGER.info("DynMap не найден — интеграция отключена");
            return;
        }
        NationsMod.LOGGER.info("DynMap обнаружен. Подключение через API...");
    }

    private static void tryConnect() {
        if (enabled) return;

        DynmapCommonAPI api = null;
        try {
            api = DynmapCommonAPIListener.getApi();
        } catch (Exception ignored) {}

        if (api == null) {
            try {
                api = DynmapCore.instance;
            } catch (Exception ignored) {}
        }

        if (api == null) return;

        try {
            markerAPI = api.getMarkerAPI();
            if (markerAPI != null) {
                setupMarkerSets();
                enabled = true;
                NationsMod.LOGGER.info("DynMap интеграция успешно запущена!");
            }
        } catch (Exception ignored) {}
    }

    private static void setupMarkerSets() {
        MarkerSet existing = markerAPI.getMarkerSet("nations.towns");
        if (existing != null) {
            existing.deleteMarkerSet();
        }
        townMarkerSet = markerAPI.createMarkerSet("nations.towns", "Города и Нации", null, false);
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

    private static void clearMarkers() {
        Set<AreaMarker> areas = townMarkerSet.getAreaMarkers();
        if (areas != null) {
            for (AreaMarker area : new HashSet<>(areas)) {
                area.deleteMarker();
            }
        }
    }

    private static void drawTown(Town town) {
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

            AreaMarker area = townMarkerSet.createAreaMarker(markerId, buildLabel(town, nationName), true, "world",
                    new double[]{x1, x2, x2, x1}, new double[]{z1, z1, z2, z2}, false);
            if (area != null) {
                area.setFillStyle(0.35, color);
                area.setLineStyle(2, 0.8, color);
            }
        }
    }

    private static String buildLabel(Town town, String nationName) {
        String borderColor = town.isAtWar() ? "#F00" : "#FFD700";
        return "<span style='font-weight:bold; color: #FFD700; border: 1px solid " + borderColor + "; padding: 2px;'>" +
                "🏰 " + town.getName() + "</span>" +
                (nationName.isEmpty() ? "" : "<br>🏛 Нация: " + nationName) +
                "<br>👥 Жителей: " + town.getMembers().size() +
                "<br>⚔ PvP: " + (town.isPvpEnabled() ? "ВКЛ" : "ВЫКЛ");
    }
}
