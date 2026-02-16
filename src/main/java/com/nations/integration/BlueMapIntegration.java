package com.nations.integration;

import com.flowpowered.math.vector.Vector2d;
import com.nations.NationsMod;
import com.nations.data.*;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.ModList;

import java.util.UUID;

public class BlueMapIntegration {

    private static boolean enabled = false;
    private static final String MARKER_SET_ID = "nations_towns";

    public static void init() {
        if (!ModList.get().isLoaded("bluemap")) {
            return;
        }

        // Подписываемся на активацию API
        BlueMapAPI.onEnable(api -> {
            enabled = true;
            NationsMod.LOGGER.info("BlueMap API активирован!");
            updateAllMarkers();
        });
        
        BlueMapAPI.onDisable(api -> enabled = false);
    }

    public static void updateAllMarkers() {
        if (!enabled) return;

        BlueMapAPI.getInstance().ifPresent(api -> {
            // Проходимся по всем картам (world, nether, end...)
            for (BlueMapMap map : api.getMaps()) {
                // Создаем или получаем набор маркеров
                MarkerSet markerSet = map.getMarkerSets().computeIfAbsent(MARKER_SET_ID, 
                    id -> MarkerSet.builder()
                        .label("Города и Нации")
                        .defaultHidden(false)
                        .build()
                );

                // Очищаем старые маркеры
                markerSet.getMarkers().clear();

                // Рисуем города
                for (Town town : NationsData.getAllTowns()) {
                    drawTown(town, markerSet, map);
                }
            }
        });
    }

    private static void drawTown(Town town, MarkerSet markerSet, BlueMapMap map) {
        // Пропускаем, если город не в этом мире (пока считаем, что все в overworld)
        // Если у тебя мультимир, нужно добавить проверку измерения в Town.java
        if (!map.getId().toLowerCase().contains("overworld") && !map.getId().equals("world")) return;

        // Цвет
        int r = 136, g = 136, b = 136; // Серый
        String nationName = "";

        if (town.getNationName() != null) {
            Nation nation = NationsData.getNation(town.getNationName());
            if (nation != null) {
                int hex = nation.getColor().getHex();
                r = (hex >> 16) & 0xFF;
                g = (hex >> 8) & 0xFF;
                b = (hex) & 0xFF;
                nationName = nation.getName();
            }
        }

        Color fillColor = new Color(r, g, b, 0.4f);
        Color lineColor = new Color(r, g, b, 0.9f);

        if (town.isAtWar()) {
            lineColor = new Color(255, 0, 0, 1.0f); // Красная обводка при войне
        } else if (town.isCaptured()) {
            lineColor = new Color(255, 100, 0, 1.0f); // Оранжевая при захвате
        }

        // Рисуем чанки
        for (ChunkPos cp : town.getClaimedChunks()) {
            double x1 = cp.x * 16;
            double z1 = cp.z * 16;
            double x2 = x1 + 16;
            double z2 = z1 + 16;

            // Создаем квадрат
            Shape shape = new Shape(
                new Vector2d(x1, z1),
                new Vector2d(x2, z1),
                new Vector2d(x2, z2),
                new Vector2d(x1, z2)
            );

            String markerId = "town_" + town.getName() + "_" + cp.x + "_" + cp.z;
            
            ShapeMarker chunkMarker = ShapeMarker.builder()
                .label(town.getName())
                .shape(shape, 64f) // Высота 64 блока
                .depthTestEnabled(false) // Видно сквозь стены
                .fillColor(fillColor)
                .lineColor(lineColor)
                .lineWidth(2)
                .detail(buildPopup(town, nationName)) // HTML описание
                .build();

            markerSet.put(markerId, chunkMarker);
        }

        // Маркер спавна (иконка)
        if (town.getSpawnPos() != null) {
            String spawnId = "spawn_" + town.getName();
            POIMarker spawnMarker = POIMarker.toBuilder()
                .label(town.getName())
                .position(town.getSpawnPos().getX(), town.getSpawnPos().getY() + 2, town.getSpawnPos().getZ())
                .detail(buildPopup(town, nationName))
                .build();
            
            markerSet.put(spawnId, spawnMarker);
        }
    }

    private static String buildPopup(Town town, String nationName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='text-align:center; font-family: Minecraft, sans-serif;'>");
        sb.append("<h3 style='color:#FFD700; margin:0;'>🏰 ").append(town.getName()).append("</h3>");
        
        if (!nationName.isEmpty()) {
            sb.append("<div style='color:#55AAFF; font-weight:bold;'>🏛 ").append(nationName).append("</div>");
        }

        if (town.isAtWar()) sb.append("<div style='color:#FF0000; font-weight:bold;'>⚔ ВОЙНА</div>");
        if (town.isCaptured()) sb.append("<div style='color:#FFAA00; font-weight:bold;'>🏴 ЗАХВАЧЕН</div>");

        sb.append("<hr>");
        sb.append("<div>👥 Жителей: <b>").append(town.getMembers().size()).append("</b></div>");
        sb.append("<div>📍 Чанков: <b>").append(town.getClaimedChunks().size()).append("</b></div>");
        sb.append("<div>⚔ PvP: <b>").append(town.isPvpEnabled() ? "<span style='color:red'>ON</span>" : "<span style='color:green'>OFF</span>").append("</b></div>");
        
        String mayorName = "Неизвестно";
        if (NationsData.getServer() != null) {
            var p = NationsData.getServer().getPlayerList().getPlayer(town.getMayor());
            if (p != null) mayorName = p.getName().getString();
        }
        sb.append("<div style='margin-top:5px;'>👑 Правитель: <span style='color:gold;'>").append(mayorName).append("</span></div>");
        sb.append("</div>");
        
        return sb.toString();
    }
}
