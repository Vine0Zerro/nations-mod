package com.nations.gui;

import com.nations.network.ClaimChunksPacket;
import com.nations.network.ClaimMapPacket;
import com.nations.network.NetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ClaimMapScreen extends Screen {

    private final ClaimMapPacket data;
    private final int mapRadius = 20;
    private final int cellSize = 7;
    private final Set<String> selectedKeys = new HashSet<>();
    private int mapStartX, mapStartY;
    private int zoom = 1;
    private float scrollOffsetX = 0;
    private float scrollOffsetY = 0;

    // Цвета интерфейса
    private static final int BG_COLOR = 0xFF0A0A1A;
    private static final int BORDER_COLOR = 0xFF2A2A4A;
    private static final int GRID_COLOR = 0x22FFFFFF;
    private static final int SELECTED_COLOR = 0xAA00FF88;
    private static final int PLAYER_COLOR = 0xFFFF3333;
    private static final int EMPTY_COLOR = 0x18FFFFFF;
    private static final int HEADER_COLOR = 0xFF1A1A3A;
    private static final int WAR_BORDER = 0xFFFF0000;
    private static final int CAPTURED_BORDER = 0xFFFF6600;
    private static final int ALLY_BORDER = 0xFF00CCFF;

    public ClaimMapScreen(ClaimMapPacket data) {
        super(Component.literal("Карта территорий"));
        this.data = data;
    }

    @Override
    protected void init() {
        super.init();
        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;
        mapStartX = (this.width - mapWidth) / 2;
        mapStartY = (this.height - mapHeight) / 2 + 5;

        int btnY = mapStartY + mapHeight + 8;
        int btnWidth = 100;
        int spacing = 8;
        int totalWidth = btnWidth * 3 + spacing * 2;
        int startX = (this.width - totalWidth) / 2;

        // Кнопка привата
        this.addRenderableWidget(Button.builder(
            Component.literal("§a✔ Запривачить"),
            button -> claimSelected()
        ).bounds(startX, btnY, btnWidth, 20).build());

        // Кнопка очистки
        this.addRenderableWidget(Button.builder(
            Component.literal("§c✘ Очистить"),
            button -> selectedKeys.clear()
        ).bounds(startX + btnWidth + spacing, btnY, btnWidth, 20).build());

        // Кнопка закрытия
        this.addRenderableWidget(Button.builder(
            Component.literal("§7Закрыть"),
            button -> this.onClose()
        ).bounds(startX + (btnWidth + spacing) * 2, btnY, btnWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;
        int pcx = data.getPlayerChunkX();
        int pcz = data.getPlayerChunkZ();

        // === Заголовок ===
        drawHeader(g, mapWidth);

        // === Фон карты ===
        // Внешняя рамка
        g.fill(mapStartX - 3, mapStartY - 3,
            mapStartX + mapWidth + 3, mapStartY + mapHeight + 3, BORDER_COLOR);
        // Внутренний фон
        g.fill(mapStartX - 1, mapStartY - 1,
            mapStartX + mapWidth + 1, mapStartY + mapHeight + 1, 0xFF111122);
        g.fill(mapStartX, mapStartY,
            mapStartX + mapWidth, mapStartY + mapHeight, BG_COLOR);

        // === Индекс чанков ===
        Map<String, ClaimMapPacket.ChunkEntry> claimedMap = new HashMap<>();
        for (var e : data.getEntries()) {
            claimedMap.put(e.x + "," + e.z, e);
        }

        // === Рисуем чанки ===
        for (int dx = -mapRadius; dx <= mapRadius; dx++) {
            for (int dz = -mapRadius; dz <= mapRadius; dz++) {
                int cx = pcx + dx;
                int cz = pcz + dz;
                int px = mapStartX + (dx + mapRadius) * cellSize;
                int py = mapStartY + (dz + mapRadius) * cellSize;
                String key = cx + "," + cz;

                ClaimMapPacket.ChunkEntry entry = claimedMap.get(key);

                if (entry != null) {
                    // Занятый чанк
                    int baseColor = entry.color;
                    int alpha = 0xBB;
                    int fillColor = (alpha << 24) | (baseColor & 0xFFFFFF);
                    g.fill(px, py, px + cellSize - 1, py + cellSize - 1, fillColor);

                    // Рамка для особых состояний
                    if (entry.isAtWar) {
                        drawChunkBorder(g, px, py, WAR_BORDER);
                    } else if (entry.isCaptured) {
                        drawChunkBorder(g, px, py, CAPTURED_BORDER);
                    }

                    // Своя территория — яркая рамка
                    if (!data.getPlayerTown().isEmpty() && entry.townName.equals(data.getPlayerTown())) {
                        drawChunkBorder(g, px, py, 0xFF00FF00);
                    }
                } else if (selectedKeys.contains(key)) {
                    // Выбранный чанк
                    g.fill(px, py, px + cellSize - 1, py + cellSize - 1, SELECTED_COLOR);
                    // Пунктирная рамка
                    drawChunkBorder(g, px, py, 0xFF00FF88);
                } else {
                    // Пустой чанк — лёгкая сетка
                    g.fill(px, py, px + cellSize - 1, py + cellSize - 1, EMPTY_COLOR);
                }
            }
        }

        // === Позиция игрока ===
        int playerPx = mapStartX + mapRadius * cellSize;
        int playerPy = mapStartY + mapRadius * cellSize;
        // Красный маркер
        g.fill(playerPx + 1, playerPy + 1, playerPx + cellSize - 2, playerPy + cellSize - 2, PLAYER_COLOR);
        // Белая точка в центре
        int cx2 = playerPx + cellSize / 2;
        int cy2 = playerPy + cellSize / 2;
        g.fill(cx2, cy2, cx2 + 1, cy2 + 1, 0xFFFFFFFF);

        // === Координатные оси ===
        drawAxes(g, pcx, pcz, mapWidth, mapHeight);

        // === Легенда ===
        drawLegend(g, mapWidth, mapHeight);

        // === Тултип при наведении ===
        drawTooltip(g, mouseX, mouseY, pcx, pcz, claimedMap, mapWidth, mapHeight);

        // === Нижняя панель инфо ===
        drawBottomInfo(g, mapWidth, mapHeight);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawHeader(GuiGraphics g, int mapWidth) {
        int headerY = mapStartY - 28;
        g.fill(mapStartX - 3, headerY, mapStartX + mapWidth + 3, mapStartY - 4, HEADER_COLOR);
        g.fill(mapStartX - 3, headerY, mapStartX + mapWidth + 3, headerY + 1, BORDER_COLOR);

        g.drawCenteredString(this.font, "§6§l🗺 КАРТА ТЕРРИТОРИЙ",
            this.width / 2, headerY + 4, 0xFFFFFF);

        String info = "";
        if (!data.getPlayerTown().isEmpty()) {
            info += "§7Город: §e" + data.getPlayerTown() + "  ";
        }
        if (!data.getPlayerNation().isEmpty()) {
            info += "§7Нация: §9" + data.getPlayerNation();
        }
        if (!info.isEmpty()) {
            g.drawCenteredString(this.font, info, this.width / 2, headerY + 15, 0xAAAAAA);
        }
    }

    private void drawChunkBorder(GuiGraphics g, int px, int py, int color) {
        int s = cellSize - 1;
        g.fill(px, py, px + s, py + 1, color);           // верх
        g.fill(px, py + s - 1, px + s, py + s, color);   // низ
        g.fill(px, py, px + 1, py + s, color);           // лево
        g.fill(px + s - 1, py, px + s, py + s, color);   // право
    }

    private void drawAxes(GuiGraphics g, int pcx, int pcz, int mapWidth, int mapHeight) {
        // Координаты по краям
        String west = String.valueOf(pcx - mapRadius);
        String east = String.valueOf(pcx + mapRadius);
        String north = String.valueOf(pcz - mapRadius);
        String south = String.valueOf(pcz + mapRadius);

        g.drawString(this.font, west, mapStartX, mapStartY + mapHeight + 2, 0x666666);
        g.drawString(this.font, east,
            mapStartX + mapWidth - this.font.width(east), mapStartY + mapHeight + 2, 0x666666);
        g.drawString(this.font, "N", mapStartX + mapWidth / 2 - 2, mapStartY - 12, 0x88AAFF);
        g.drawString(this.font, "S", mapStartX + mapWidth / 2 - 2,
            mapStartY + mapHeight + 2, 0x88AAFF);
    }

    private void drawLegend(GuiGraphics g, int mapWidth, int mapHeight) {
        int legendX = mapStartX + mapWidth + 8;
        int legendY = mapStartY;

        if (legendX + 80 > this.width) return; // Не хватает места

        g.drawString(this.font, "§6§lЛегенда:", legendX, legendY, 0xFFFFFF);
        legendY += 14;

        // Игрок
        g.fill(legendX, legendY + 1, legendX + 8, legendY + 9, PLAYER_COLOR);
        g.drawString(this.font, "§f Вы", legendX + 10, legendY, 0xFFFFFF);
        legendY += 12;

        // Свой город
        g.fill(legendX, legendY + 1, legendX + 8, legendY + 9, 0xFF00FF00);
        g.drawString(this.font, "§a Свои", legendX + 10, legendY, 0xFFFFFF);
        legendY += 12;

        // Выбранные
        g.fill(legendX, legendY + 1, legendX + 8, legendY + 9, SELECTED_COLOR);
        g.drawString(this.font, "§a Выбран", legendX + 10, legendY, 0xFFFFFF);
        legendY += 12;

        // Война
        g.fill(legendX, legendY + 1, legendX + 8, legendY + 9, WAR_BORDER);
        g.drawString(this.font, "§c Война", legendX + 10, legendY, 0xFFFFFF);
        legendY += 12;

        // Захват
        g.fill(legendX, legendY + 1, legendX + 8, legendY + 9, CAPTURED_BORDER);
        g.drawString(this.font, "§6 Захват", legendX + 10, legendY, 0xFFFFFF);
        legendY += 12;

        // Пустой
        g.fill(legendX, legendY + 1, legendX + 8, legendY + 9, EMPTY_COLOR);
        g.drawString(this.font, "§7 Пусто", legendX + 10, legendY, 0xFFFFFF);
        legendY += 18;

        // Управление
        g.drawString(this.font, "§6§lУправление:", legendX, legendY, 0xFFFFFF);
        legendY += 12;
        g.drawString(this.font, "§7ЛКМ §fВыбрать", legendX, legendY, 0xFFFFFF);
        legendY += 10;
        g.drawString(this.font, "§7ПКМ §fУбрать", legendX, legendY, 0xFFFFFF);
        legendY += 10;
        g.drawString(this.font, "§7Зажать §fОбласть", legendX, legendY, 0xFFFFFF);
    }

    private void drawTooltip(GuiGraphics g, int mouseX, int mouseY,
                              int pcx, int pcz,
                              Map<String, ClaimMapPacket.ChunkEntry> claimedMap,
                              int mapWidth, int mapHeight) {
        if (mouseX < mapStartX || mouseX >= mapStartX + mapWidth ||
            mouseY < mapStartY || mouseY >= mapStartY + mapHeight) return;

        int dx = (mouseX - mapStartX) / cellSize - mapRadius;
        int dz = (mouseY - mapStartY) / cellSize - mapRadius;
        int chunkX = pcx + dx;
        int chunkZ = pcz + dz;
        String key = chunkX + "," + chunkZ;

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("§6Чанк §f[" + chunkX + ", " + chunkZ + "]"));

        ClaimMapPacket.ChunkEntry entry = claimedMap.get(key);
        if (entry != null) {
            tooltip.add(Component.literal("§7────────────────"));
            tooltip.add(Component.literal("§e🏰 Город: §f" + entry.townName));
            if (!entry.nationName.isEmpty()) {
                tooltip.add(Component.literal("§9🏛 Нация: §f" + entry.nationName));
            }
            if (entry.isAtWar) {
                tooltip.add(Component.literal("§c⚔ В СОСТОЯНИИ ВОЙНЫ"));
            }
            if (entry.isCaptured) {
                tooltip.add(Component.literal("§6🏴 Захвачен: §f" + entry.capturedBy));
            }
            if (!data.getPlayerTown().isEmpty() && entry.townName.equals(data.getPlayerTown())) {
                tooltip.add(Component.literal("§a✔ Ваша территория"));
            }
        } else {
            tooltip.add(Component.literal("§a✔ Свободен"));
            if (selectedKeys.contains(key)) {
                tooltip.add(Component.literal("§2☑ Выбран для привата"));
            } else {
                tooltip.add(Component.literal("§7ЛКМ — выбрать"));
            }
        }

        g.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    private void drawBottomInfo(GuiGraphics g, int mapWidth, int mapHeight) {
        int infoY = mapStartY + mapHeight + 32;
        String selText = "§7Выбрано чанков: §e" + selectedKeys.size() + " §7(макс 5/мин)";
        g.drawCenteredString(this.font, selText, this.width / 2, infoY, 0xAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isOnMap(mouseX, mouseY)) {
            toggleChunk(mouseX, mouseY, button == 0);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                 double dragX, double dragY) {
        if (isOnMap(mouseX, mouseY)) {
            toggleChunk(mouseX, mouseY, button == 0);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean isOnMap(double mouseX, double mouseY) {
        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;
        return mouseX >= mapStartX && mouseX < mapStartX + mapWidth &&
               mouseY >= mapStartY && mouseY < mapStartY + mapHeight;
    }

    private void toggleChunk(double mouseX, double mouseY, boolean add) {
        int dx = (int)((mouseX - mapStartX) / cellSize) - mapRadius;
        int dz = (int)((mouseY - mapStartY) / cellSize) - mapRadius;
        int cx = data.getPlayerChunkX() + dx;
        int cz = data.getPlayerChunkZ() + dz;
        String key = cx + "," + cz;

        // Проверка что не занято
        boolean occupied = false;
        for (var e : data.getEntries()) {
            if (e.x == cx && e.z == cz) {
                occupied = true;
                break;
            }
        }

        if (!occupied) {
            if (add) selectedKeys.add(key);
            else selectedKeys.remove(key);
        }
    }

    private void claimSelected() {
        if (selectedKeys.isEmpty()) return;

        List<int[]> chunks = new ArrayList<>();
        for (String key : selectedKeys) {
            String[] parts = key.split(",");
            chunks.add(new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
        }

        NetworkHandler.sendToServer(new ClaimChunksPacket(chunks));
        selectedKeys.clear();
        this.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
