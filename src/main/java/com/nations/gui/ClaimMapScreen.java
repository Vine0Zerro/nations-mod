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
    private long openTime;
    private int animTick = 0;

    public ClaimMapScreen(ClaimMapPacket data) {
        super(Component.literal("Карта территорий"));
        this.data = data;
        this.openTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;
        mapStartX = (this.width - mapWidth) / 2;
        mapStartY = 40;

        int btnY = mapStartY + mapHeight + 6;
        int btnWidth = 110;
        int spacing = 6;
        int totalWidth = btnWidth * 3 + spacing * 2;
        int startX = (this.width - totalWidth) / 2;

        this.addRenderableWidget(Button.builder(
            Component.literal("Заприватить"),
            button -> claimSelected()
        ).bounds(startX, btnY, btnWidth, 18).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Очистить выбор"),
            button -> selectedKeys.clear()
        ).bounds(startX + btnWidth + spacing, btnY, btnWidth, 18).build());

        this.addRenderableWidget(Button.builder(
            Component.literal("Закрыть"),
            button -> this.onClose()
        ).bounds(startX + (btnWidth + spacing) * 2, btnY, btnWidth, 18).build());
    }

    @Override
    public void tick() {
        super.tick();
        animTick++;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);

        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;
        int pcx = data.getPlayerChunkX();
        int pcz = data.getPlayerChunkZ();

        // Заголовок
        drawHeader(g, mapWidth, mapHeight);

        // Рамка карты с градиентом
        g.fill(mapStartX - 2, mapStartY - 2,
            mapStartX + mapWidth + 2, mapStartY + mapHeight + 2, 0xFF333355);
        g.fill(mapStartX - 1, mapStartY - 1,
            mapStartX + mapWidth + 1, mapStartY + mapHeight + 1, 0xFF222244);
        g.fill(mapStartX, mapStartY,
            mapStartX + mapWidth, mapStartY + mapHeight, 0xFF0D0D1A);

        // Индекс чанков
        Map<String, ClaimMapPacket.ChunkEntry> claimedMap = new HashMap<>();
        for (var e : data.getEntries()) {
            claimedMap.put(e.x + "," + e.z, e);
        }

        // Рисуем чанки
        for (int dx = -mapRadius; dx <= mapRadius; dx++) {
            for (int dz = -mapRadius; dz <= mapRadius; dz++) {
                int cx = pcx + dx;
                int cz = pcz + dz;
                int px = mapStartX + (dx + mapRadius) * cellSize;
                int py = mapStartY + (dz + mapRadius) * cellSize;
                String key = cx + "," + cz;

                ClaimMapPacket.ChunkEntry entry = claimedMap.get(key);

                if (entry != null) {
                    drawClaimedChunk(g, px, py, entry);
                } else if (selectedKeys.contains(key)) {
                    drawSelectedChunk(g, px, py);
                } else {
                    g.fill(px, py, px + cellSize - 1, py + cellSize - 1, 0x11FFFFFF);
                }
            }
        }

        // Позиция игрока с анимацией
        drawPlayerMarker(g, mapStartX + mapRadius * cellSize, mapStartY + mapRadius * cellSize);

        // Компас
        drawCompass(g, mapWidth, mapHeight);

        // Информационная панель справа
        drawInfoPanel(g, mapWidth, mapHeight, mouseX, mouseY, pcx, pcz, claimedMap);

        // Нижняя панель
        drawBottomBar(g, mapWidth, mapHeight);

        // Тултип
        drawTooltip(g, mouseX, mouseY, pcx, pcz, claimedMap, mapWidth, mapHeight);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void drawHeader(GuiGraphics g, int mapWidth, int mapHeight) {
        int headerY = 5;
        g.fill(mapStartX - 2, headerY, mapStartX + mapWidth + 2, headerY + 30, 0xAA111133);
        g.fill(mapStartX - 2, headerY + 30, mapStartX + mapWidth + 2, headerY + 31, 0xFF333355);

        g.drawCenteredString(this.font, "КАРТА ТЕРРИТОРИЙ",
            this.width / 2, headerY + 4, 0xFFDD88);

        String info = "";
        if (!data.getPlayerTown().isEmpty()) {
            info += "Город: " + data.getPlayerTown();
            if (!data.getPlayerNation().isEmpty()) {
                info += "  |  Нация: " + data.getPlayerNation();
            }
        } else {
            info = "Вы не в городе";
        }
        g.drawCenteredString(this.font, info, this.width / 2, headerY + 17, 0x999999);
    }

    private void drawClaimedChunk(GuiGraphics g, int px, int py, ClaimMapPacket.ChunkEntry entry) {
        int s = cellSize - 1;
        int baseColor = entry.color;
        int alpha = entry.isPlayerTown ? 0xDD : 0xAA;
        int fillColor = (alpha << 24) | (baseColor & 0xFFFFFF);

        g.fill(px, py, px + s, py + s, fillColor);

        // Рамки для состояний
        if (entry.isPlayerTown) {
            // Своя территория — зелёная анимированная рамка
            int pulse = (int)(Math.sin(animTick * 0.15) * 30 + 225);
            int borderColor = (0xFF << 24) | (0 << 16) | (pulse << 8) | 0;
            drawBorder(g, px, py, s, borderColor);
        } else if (entry.isAtWar) {
            // Война — красная мигающая рамка
            if (animTick % 20 < 14) {
                drawBorder(g, px, py, s, 0xFFFF0000);
            }
        } else if (entry.isCaptured) {
            drawBorder(g, px, py, s, 0xFFFF6600);
        }
    }

    private void drawSelectedChunk(GuiGraphics g, int px, int py) {
        int s = cellSize - 1;
        int pulse = (int)(Math.sin(animTick * 0.2) * 40 + 180);
        int color = (pulse << 24) | 0x00FF88;
        g.fill(px, py, px + s, py + s, color);
        drawBorder(g, px, py, s, 0xFF00FF88);
    }

    private void drawPlayerMarker(GuiGraphics g, int px, int py) {
        int s = cellSize - 1;
        // Внешний пульсирующий круг
        int pulse = (int)(Math.sin(animTick * 0.1) * 2);
        g.fill(px - pulse, py - pulse, px + s + pulse, py + s + pulse, 0x44FF3333);
        // Основной маркер
        g.fill(px, py, px + s, py + s, 0xFFFF3333);
        // Центральная точка
        int cx = px + s / 2;
        int cy = py + s / 2;
        g.fill(cx, cy, cx + 1, cy + 1, 0xFFFFFFFF);
    }

    private void drawBorder(GuiGraphics g, int px, int py, int s, int color) {
        g.fill(px, py, px + s, py + 1, color);
        g.fill(px, py + s - 1, px + s, py + s, color);
        g.fill(px, py, px + 1, py + s, color);
        g.fill(px + s - 1, py, px + s, py + s, color);
    }

    private void drawCompass(GuiGraphics g, int mapWidth, int mapHeight) {
        int cx = mapStartX + mapWidth / 2;
        g.drawCenteredString(this.font, "N", cx, mapStartY - 10, 0x6688FF);
        g.drawCenteredString(this.font, "S", cx, mapStartY + mapHeight + 2, 0x6688FF);
        g.drawString(this.font, "W", mapStartX - 10, mapStartY + mapHeight / 2 - 4, 0x6688FF);
        g.drawString(this.font, "E", mapStartX + mapWidth + 4, mapStartY + mapHeight / 2 - 4, 0x6688FF);
    }

    private void drawInfoPanel(GuiGraphics g, int mapWidth, int mapHeight,
                                int mouseX, int mouseY, int pcx, int pcz,
                                Map<String, ClaimMapPacket.ChunkEntry> claimedMap) {
        int panelX = mapStartX + mapWidth + 12;
        int panelY = mapStartY;
        int panelW = 100;

        if (panelX + panelW > this.width) return;

        // Фон панели
        g.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + 160, 0xAA111133);
        g.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY - 1, 0xFF333355);

        int y = panelY + 4;

        g.drawString(this.font, "Легенда", panelX + 2, y, 0xFFDD88);
        y += 14;

        // Элементы легенды
        drawLegendItem(g, panelX, y, 0xFFFF3333, "Вы здесь"); y += 11;
        drawLegendItem(g, panelX, y, 0xFF00DD00, "Ваша земля"); y += 11;
        drawLegendItem(g, panelX, y, 0xAA00FF88, "Выбрано"); y += 11;
        drawLegendItem(g, panelX, y, 0xAAFF0000, "Зона войны"); y += 11;
        drawLegendItem(g, panelX, y, 0xAAFF6600, "Захвачено"); y += 11;
        drawLegendItem(g, panelX, y, 0x11FFFFFF, "Свободно"); y += 16;

        // Территория
        g.drawString(this.font, "Территория", panelX + 2, y, 0xFFDD88);
        y += 12;
        g.drawString(this.font, data.getCurrentChunks() + "/" + data.getMaxChunks(),
            panelX + 4, y, 0xCCCCCC);
        y += 16;

        // Управление
        g.drawString(this.font, "Управление", panelX + 2, y, 0xFFDD88);
        y += 12;
        g.drawString(this.font, "ЛКМ: Выбрать", panelX + 4, y, 0x999999);
        y += 10;
        g.drawString(this.font, "ПКМ: Отменить", panelX + 4, y, 0x999999);
        y += 10;
        g.drawString(this.font, "Зажмите для", panelX + 4, y, 0x999999);
        y += 10;
        g.drawString(this.font, "выделения зоны", panelX + 4, y, 0x999999);
    }

    private void drawLegendItem(GuiGraphics g, int x, int y, int color, String text) {
        g.fill(x + 4, y + 1, x + 12, y + 9, color);
        g.drawString(this.font, text, x + 16, y, 0xCCCCCC);
    }

    private void drawBottomBar(GuiGraphics g, int mapWidth, int mapHeight) {
        int barY = mapStartY + mapHeight + 28;
        String text = "Выбрано: " + selectedKeys.size() + " чанков";
        if (data.getMaxChunks() > 0) {
            int remaining = data.getMaxChunks() - data.getCurrentChunks();
            text += "  |  Доступно: " + remaining;
        }
        g.drawCenteredString(this.font, text, this.width / 2, barY, 0x888888);
    }

    private void drawTooltip(GuiGraphics g, int mouseX, int mouseY,
                              int pcx, int pcz,
                              Map<String, ClaimMapPacket.ChunkEntry> claimedMap,
                              int mapWidth, int mapHeight) {
        if (!isOnMap(mouseX, mouseY, mapWidth, mapHeight)) return;

        int dx = (mouseX - mapStartX) / cellSize - mapRadius;
        int dz = (mouseY - mapStartY) / cellSize - mapRadius;
        int chunkX = pcx + dx;
        int chunkZ = pcz + dz;
        String key = chunkX + "," + chunkZ;

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("Чанк [" + chunkX + ", " + chunkZ + "]"));

        ClaimMapPacket.ChunkEntry entry = claimedMap.get(key);
        if (entry != null) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Город: " + entry.townName));
            if (!entry.nationName.isEmpty()) {
                tooltip.add(Component.literal("Нация: " + entry.nationName));
            }
            if (entry.isAtWar) {
                tooltip.add(Component.literal("ЗОНА ВОЙНЫ"));
            }
            if (entry.isCaptured) {
                tooltip.add(Component.literal("Захвачен: " + entry.capturedBy));
            }
            if (entry.isPlayerTown) {
                tooltip.add(Component.literal("Ваша территория"));
            }
        } else {
            tooltip.add(Component.literal("Свободный чанк"));
            if (selectedKeys.contains(key)) {
                tooltip.add(Component.literal("Выбран для привата"));
            }
        }

        g.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;
        if (isOnMap(mouseX, mouseY, mapWidth, mapHeight)) {
            toggleChunk(mouseX, mouseY, button == 0);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                 double dragX, double dragY) {
        int mapWidth = (mapRadius * 2 + 1) * cellSize;
        int mapHeight = (mapRadius * 2 + 1) * cellSize;
        if (isOnMap(mouseX, mouseY, mapWidth, mapHeight)) {
            toggleChunk(mouseX, mouseY, button == 0);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private boolean isOnMap(double mouseX, double mouseY, int mapWidth, int mapHeight) {
        return mouseX >= mapStartX && mouseX < mapStartX + mapWidth &&
               mouseY >= mapStartY && mouseY < mapStartY + mapHeight;
    }

    private void toggleChunk(double mouseX, double mouseY, boolean add) {
        int dx = (int)((mouseX - mapStartX) / cellSize) - mapRadius;
        int dz = (int)((mouseY - mapStartY) / cellSize) - mapRadius;
        int cx = data.getPlayerChunkX() + dx;
        int cz = data.getPlayerChunkZ() + dz;
        String key = cx + "," + cz;

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
