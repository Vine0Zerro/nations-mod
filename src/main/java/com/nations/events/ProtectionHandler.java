package com.nations.events;

import com.nations.data.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ProtectionHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ChunkPos cp = new ChunkPos(event.getPos());
        Town town = NationsData.getTownByChunk(cp);
        if (town == null) return;
        if (town.isMember(player.getUUID())) return;
        if (canInteractDuringWar(player, town)) return;

        event.setCanceled(true);
        player.sendSystemMessage(Component.literal(
            "§c🛡 Территория города §e" + town.getName() + "§c!"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ChunkPos cp = new ChunkPos(event.getPos());
        Town town = NationsData.getTownByChunk(cp);
        if (town == null) return;
        if (town.isMember(player.getUUID())) return;
        if (canInteractDuringWar(player, town)) return;

        event.setCanceled(true);
        player.sendSystemMessage(Component.literal(
            "§c🛡 Территория города §e" + town.getName() + "§c!"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ChunkPos cp = new ChunkPos(event.getPos());
        Town town = NationsData.getTownByChunk(cp);
        if (town == null) return;
        if (town.isMember(player.getUUID())) return;

        // Союзники могут взаимодействовать
        if (isAlly(player, town)) return;
        if (canInteractDuringWar(player, town)) return;

        event.setCanceled(true);
        player.sendSystemMessage(Component.literal(
            "§c🛡 Территория города §e" + town.getName() + "§c!"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer attacker)) return;
        if (!(event.getTarget() instanceof ServerPlayer victim)) return;

        ChunkPos cp = new ChunkPos(victim.blockPosition());
        Town town = NationsData.getTownByChunk(cp);
        if (town == null) return;

        if (!town.isPvpEnabled()) {
            event.setCanceled(true);
            attacker.sendSystemMessage(Component.literal(
                "§c⚔ PvP выключен на территории §e" + town.getName() + "§c!"));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos -> {
            ChunkPos cp = new ChunkPos(pos);
            Town town = NationsData.getTownByChunk(cp);
            if (town == null) return false;
            return !(town.isAtWar() && town.isDestructionEnabled());
        });
    }

    private boolean canInteractDuringWar(ServerPlayer player, Town targetTown) {
        if (!targetTown.isAtWar() || !targetTown.isDestructionEnabled()) return false;
        if (targetTown.getNationName() == null) return false;

        Nation targetNation = NationsData.getNation(targetTown.getNationName());
        if (targetNation == null) return false;

        Nation playerNation = NationsData.getNationByPlayer(player.getUUID());
        if (playerNation == null) return false;

        return targetNation.isAtWarWith(playerNation.getName());
    }

    private boolean isAlly(ServerPlayer player, Town targetTown) {
        if (targetTown.getNationName() == null) return false;

        Nation playerNation = NationsData.getNationByPlayer(player.getUUID());
        if (playerNation == null) return false;

        return NationsData.areAllied(playerNation.getName(), targetTown.getNationName());
    }
}
