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

    public static class ChunkEntry {
        public int x, z;
        public String townName;
        public String nationName;
        public int color;
        public boolean isAtWar;
        public boolean isCaptured;
        public String capturedBy;

        public ChunkEntry(int x, int z, String townName, String nationName,
                          int color, boolean isAtWar, boolean isCaptured, String capturedBy) {
            this.x = x;
            this.z = z;
            this.townName = townName;
            this.nationName = nationName;
            this.color = color;
            this.isAtWar = isAtWar;
            this.isCaptured = isCaptured;
            this.capturedBy = capturedBy;
        }
    }

    public ClaimMapPacket(List<ChunkEntry> entries, int playerChunkX, int playerChunkZ,
                          String playerTown, String playerNation) {
        this.entries = entries;
        this.playerChunkX = playerChunkX;
        this.playerChunkZ = playerChunkZ;
        this.playerTown = playerTown;
        this.playerNation = playerNation;
    }

    public static ClaimMapPacket create(ServerPlayer player) {
        int pcx = player.blockPosition().getX() >> 4;
        int pcz = player.blockPosition().getZ() >> 4;
        int radius = 20;

        // Информация о игроке
        Town pTown = NationsData.getTownByPlayer(player.getUUID());
        String playerTownName = pTown != null ? pTown.getName() : "";
        String playerNationName = pTown != null && pTown.getNationName() != null ? pTown.getNationName() : "";

        List<ChunkEntry> entries = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = pcx + dx;
                int cz = pcz + dz;
                ChunkPos cp = new ChunkPos(cx, cz);
                Town town = NationsData.getTownByChunk(cp);
                if (town != null) {
                    String nationName = town.getNationName() != null ? town.getNationName() : "";
                    int color = 0xAAAAAA;
                    if (town.getNationName() != null) {
                        Nation nation = NationsData.getNation(town.getNationName());
                        if (nation != null) color = nation.getColor().getHex();
                    }
                    entries.add(new ChunkEntry(cx, cz, town.getName(), nationName,
                        color, town.isAtWar(), town.isCaptured(),
                        town.getCapturedBy() != null ? town.getCapturedBy() : ""));
                }
            }
        }
        return new ClaimMapPacket(entries, pcx, pcz, playerTownName, playerNationName);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerChunkX);
        buf.writeInt(playerChunkZ);
        buf.writeUtf(playerTown);
        buf.writeUtf(playerNation);
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
        }
    }

    public static ClaimMapPacket decode(FriendlyByteBuf buf) {
        int pcx = buf.readInt();
        int pcz = buf.readInt();
        String pTown = buf.readUtf();
        String pNation = buf.readUtf();
        int size = buf.readInt();
        List<ChunkEntry> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            entries.add(new ChunkEntry(
                buf.readInt(), buf.readInt(),
                buf.readUtf(), buf.readUtf(),
                buf.readInt(), buf.readBoolean(),
                buf.readBoolean(), buf.readUtf()
            ));
        }
        return new ClaimMapPacket(entries, pcx, pcz, pTown, pNation);
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

    public static class ClientHandler {
        public static void openMap(ClaimMapPacket packet) {
            Minecraft.getInstance().setScreen(new ClaimMapScreen(packet));
        }
    }
}
