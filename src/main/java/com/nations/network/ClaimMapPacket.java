package com.nations.network;

import com.nations.data.*;
import com.nations.gui.ClaimMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class ClaimMapPacket {

    private final List<ChunkEntry> entries;
    private final int playerChunkX;
    private final int playerChunkZ;
    private final String playerTown;
    private final String playerNation;
    private final int maxChunks;
    private final int currentChunks;

    public static class ChunkEntry {
        public int x, z;
        public String townName;
        public String nationName;
        public int color;
        public boolean isAtWar;
        public boolean isCaptured;
        public String capturedBy;
        public boolean isPlayerTown;

        public ChunkEntry(int x, int z, String townName, String nationName,
                          int color, boolean isAtWar, boolean isCaptured,
                          String capturedBy, boolean isPlayerTown) {
            this.x = x;
            this.z = z;
            this.townName = townName;
            this.nationName = nationName;
            this.color = color;
            this.isAtWar = isAtWar;
            this.isCaptured = isCaptured;
            this.capturedBy = capturedBy;
            this.isPlayerTown = isPlayerTown;
        }
    }

    public ClaimMapPacket(List<ChunkEntry> entries, int playerChunkX, int playerChunkZ,
                          String playerTown, String playerNation, int maxChunks, int currentChunks) {
        this.entries = entries;
        this.playerChunkX = playerChunkX;
        this.playerChunkZ = playerChunkZ;
        this.playerTown = playerTown;
        this.playerNation = playerNation;
        this.maxChunks = maxChunks;
        this.currentChunks = currentChunks;
    }

    public static ClaimMapPacket create(ServerPlayer player) {
        int pcx = player.blockPosition().getX() >> 4;
        int pcz = player.blockPosition().getZ() >> 4;
        int radius = 20;

        Town pTown = NationsData.getTownByPlayer(player.getUUID());
        String playerTownName = pTown != null ? pTown.getName() : "";
        String playerNationName = pTown != null && pTown.getNationName() != null ? pTown.getNationName() : "";
        int maxChunks = pTown != null ? pTown.getMaxChunks() : 0;
        int currentChunks = pTown != null ? pTown.getClaimedChunks().size() : 0;

        List<ChunkEntry> entries = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = pcx + dx;
                int cz = pcz + dz;
                ChunkPos cp = new ChunkPos(cx, cz);
                Town town = NationsData.getTownByChunk(cp);
                if (town != null) {
                    String nationName = town.getNationName() != null ? town.getNationName() : "";
                    int color = 0x888888;
                    if (town.getNationName() != null) {
                        Nation nation = NationsData.getNation(town.getNationName());
                        if (nation != null) color = nation.getColor().getHex();
                    }
                    boolean isPlayerTown = pTown != null && town.getName().equalsIgnoreCase(pTown.getName());
                    entries.add(new ChunkEntry(cx, cz, town.getName(), nationName,
                        color, town.isAtWar(), town.isCaptured(),
                        town.getCapturedBy() != null ? town.getCapturedBy() : "",
                        isPlayerTown));
                }
            }
        }
        return new ClaimMapPacket(entries, pcx, pcz, playerTownName, playerNationName, maxChunks, currentChunks);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerChunkX);
        buf.writeInt(playerChunkZ);
        buf.writeUtf(playerTown);
        buf.writeUtf(playerNation);
        buf.writeInt(maxChunks);
        buf.writeInt(currentChunks);
        buf.writeInt(entries.size());
        for (ChunkEntry e : entries) {
            buf.writeInt(e.x);
            buf.writeInt(e.z);
            buf.writeUtf(e.townName);
            buf.writeUtf(e.nationName);
            buf.writeInt(e.color);
            buf.writeBoolean(e.isAtWar);
            buf.writeBoolean(e.isCaptured);
            buf.writeUtf(e.capturedBy);
            buf.writeBoolean(e.isPlayerTown);
        }
    }

    public static ClaimMapPacket decode(FriendlyByteBuf buf) {
        int pcx = buf.readInt();
        int pcz = buf.readInt();
        String pTown = buf.readUtf();
        String pNation = buf.readUtf();
        int maxC = buf.readInt();
        int curC = buf.readInt();
        int size = buf.readInt();
        List<ChunkEntry> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            entries.add(new ChunkEntry(
                buf.readInt(), buf.readInt(),
                buf.readUtf(), buf.readUtf(),
                buf.readInt(), buf.readBoolean(),
                buf.readBoolean(), buf.readUtf(),
                buf.readBoolean()
            ));
        }
        return new ClaimMapPacket(entries, pcx, pcz, pTown, pNation, maxC, curC);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientHandler.openMap(this);
            });
        });
        ctx.setPacketHandled(true);
    }

    public List<ChunkEntry> getEntries() { return entries; }
    public int getPlayerChunkX() { return playerChunkX; }
    public int getPlayerChunkZ() { return playerChunkZ; }
    public String getPlayerTown() { return playerTown; }
    public String getPlayerNation() { return playerNation; }
    public int getMaxChunks() { return maxChunks; }
    public int getCurrentChunks() { return currentChunks; }

    public static class ClientHandler {
        public static void openMap(ClaimMapPacket packet) {
            Minecraft.getInstance().setScreen(new ClaimMapScreen(packet));
        }
    }
}
