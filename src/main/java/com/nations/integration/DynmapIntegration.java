// FILE: src\main\java\com\nations\integration\DynmapIntegration.java
package com.nations.integration;

import com.nations.NationsMod;
import com.nations.data.Nation;
import com.nations.data.NationsData;
import com.nations.data.Town;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.HashSet;
import java.util.Set;

public class DynmapIntegration extends DynmapCommonAPIListener {

    private static MarkerAPI markerAPI = null;
    private static MarkerSet townMarkerSet = null;
    private static boolean registered = false;

    // Этот метод вызываем из NationsMod
    public static void init() {
        if (!ModList.get().isLoaded("dynmap")) {
            NationsMod.LOGGER.info("DynMap не найден — интеграция отключена.");
            return;
        }
        
        // Регистрируем слушателя только если мод загружен, 
        // чтобы избежать ошибок ClassNotFound, если Dynmap нет.
        try {
            new DynmapIntegration().register();
            registered = true;
            NationsMod.LOGGER.info("DynMap API Listener зарегистрирован.");
        } catch (Exception e) {
            NationsMod.LOGGER.error("Ошибка регистрации Dynmap listener: " + e.getMessage());
        }
    }

    private void register() {
        DynmapCommonAPIListener.register(this);
    }

    @Override
    public void apiEnabled(DynmapCommonAPI api) {
        // Этот метод вызовется сам, когда Dynmap будет готов
        markerAPI = api.getMarkerAPI();
        if (markerAPI != null) {
            setupMarkerSet();
            NationsMod.LOGGER.info("DynMap API успешно получен!");
        }
    }

    private static void setupMarkerSet() {
        if (markerAPI == null) return;
        
        // Создаем или получаем слой маркеров
        townMarkerSet = markerAPI.getMarkerSet("nations.towns");
        if (townMarkerSet == null) {
            townMarkerSet = markerAPI.createMarkerSet("nations.towns", "Города и Нации", null, false);
        } else {
            townMarkerSet.setMarkerSetLabel("Города и Нации");
        }
    }

    public static void updateAllMarkers() {
        if (markerAPI == null || townMarkerSet == null) return;

        try {
            // Очищаем старые маркеры, которых больше нет
            // (В простой реализации можно перерисовывать всё, но лучше удалять лишнее)
            // Здесь мы просто удаляем всё и рисуем заново для надежности
            Set<AreaMarker> oldMarkers = new HashSet<>(townMarkerSet.getAreaMarkers());
            for (AreaMarker marker : oldMarkers) {
                marker.deleteMarker();
            }

            for (Town town : NationsData.getAllTowns()) {
                drawTown(town);
            }
        } catch (Exception e) {
            NationsMod.LOGGER.debug("Ошибка обновления Dynmap: " + e.getMessage());
        }
    }

    private static void drawTown(Town town) {
        int color = 0x888888; // Серый по умолчанию
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

        // Рисуем каждый чанк как отдельный квадрат (самый простой способ без сложных алгоритмов объединения)
        for (ChunkPos cp : town.getClaimedChunks()) {
            double[] x = new double[4];
            double[] z = new double[4];
            
            // Координаты углов чанка
            x[0] = cp.x * 16.0; z[0] = cp.z * 16.0;
            x[1] = x[0] + 16.0; z[1] = z[0];
            x[2] = x[0] + 16.0; z[2] = z[0] + 16.0;
            x[3] = x[0];        z[3] = z[0] + 16.0;

            String markerId = "n_" + town.getName() + "_" + cp.x + "_" + cp.z;

            AreaMarker marker = townMarkerSet.createAreaMarker(markerId, buildLabel(town, nationName), false, "world", x, z, false);
            
            if (marker != null) {
                // Настройка стиля: 0.8 непрозрачность линии, 2 толщина, 0.35 непрозрачность заливки
                marker.setLineStyle(2, 0.8, color);
                marker.setFillStyle(0.35, color);
                // Включаем поддержку HTML в описании
                marker.setDescription(buildLabel(town, nationName)); 
            }
        }
    }

    private static String buildLabel(Town town, String nationName) {
        String borderColor = town.isAtWar() ? "#F00" : "#FFD700";
        // HTML разметка для всплывающего окна
        return "<div style='padding:10px; background:rgba(0,0,0,0.85); border:2px solid " + borderColor + "; border-radius:10px; color:white;'>" +
               "<b style='font-size:14px; color:#FFD700;'>🏰 " + town.getName() + "</b>" +
               (nationName.isEmpty() ? "" : "<br><span style='color:#5af;'>🏛 Нация: " + nationName + "</span>") +
               "<hr style='border:0; border-top:1px solid #444;'>" +
               "👥 Жителей: " + town.getMembers().size() + "<br>" +
               "⚔ PvP: " + (town.isPvpEnabled() ? "<span style='color:#f44;'>ВКЛ</span>" : "<span style='color:#4f4;'>ВЫКЛ</span>") +
               "</div>";
    }
}
