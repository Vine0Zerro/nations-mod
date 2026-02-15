package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nations.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public class TownCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("town")
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> createTown(ctx.getSource(),
                        StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("delete")
                .executes(ctx -> deleteTown(ctx.getSource())))
            .then(Commands.literal("invite")
                .then(Commands.argument("player", StringArgumentType.word())
                    .executes(ctx -> invitePlayer(ctx.getSource(),
                        StringArgumentType.getString(ctx, "player")))))
            .then(Commands.literal("join")
                .then(Commands.argument("town", StringArgumentType.word())
                    .executes(ctx -> joinTown(ctx.getSource(),
                        StringArgumentType.getString(ctx, "town")))))
            .then(Commands.literal("leave")
                .executes(ctx -> leaveTown(ctx.getSource())))
            .then(Commands.literal("kick")
                .then(Commands.argument("player", StringArgumentType.word())
                    .executes(ctx -> kickPlayer(ctx.getSource(),
                        StringArgumentType.getString(ctx, "player")))))
            .then(Commands.literal("info")
                .executes(ctx -> townInfo(ctx.getSource()))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> townInfoByName(ctx.getSource(),
                        StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("list")
                .executes(ctx -> listTowns(ctx.getSource())))
            .then(Commands.literal("pvp")
                .then(Commands.literal("on")
                    .executes(ctx -> setPvp(ctx.getSource(), true)))
                .then(Commands.literal("off")
                    .executes(ctx -> setPvp(ctx.getSource(), false))))
            .then(Commands.literal("destruction")
                .then(Commands.literal("on")
                    .executes(ctx -> setDestruction(ctx.getSource(), true)))
                .then(Commands.literal("off")
                    .executes(ctx -> setDestruction(ctx.getSource(), false))))
            // Новые команды ролей
            .then(Commands.literal("role")
                .then(Commands.literal("set")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .then(Commands.argument("role", StringArgumentType.word())
                            .executes(ctx -> setRole(ctx.getSource(),
                                StringArgumentType.getString(ctx, "player"),
                                StringArgumentType.getString(ctx, "role")))))))
            .then(Commands.literal("roles")
                .executes(ctx -> listRoles(ctx.getSource())))
            .then(Commands.literal("members")
                .executes(ctx -> listMembers(ctx.getSource())))
        );
    }

    private static int createTown(CommandSourceStack source, String name) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            UUID uuid = player.getUUID();

            if (NationsData.getTownByPlayer(uuid) != null) {
                source.sendFailure(Component.literal("§cВы уже состоите в городе!"));
                return 0;
            }
            if (NationsData.townExists(name)) {
                source.sendFailure(Component.literal("§cГород с таким именем уже существует!"));
                return 0;
            }

            Town town = new Town(name, uuid);
            ChunkPos cp = new ChunkPos(player.blockPosition());
            if (NationsData.getTownByChunk(cp) != null) {
                source.sendFailure(Component.literal("§cЭтот чанк уже занят другим городом!"));
                return 0;
            }
            town.claimChunk(cp);
            NationsData.addTown(town);
            Economy.deposit(uuid, 0); // инициализация баланса
            source.sendSuccess(() -> Component.literal(
                "§a🏰 Город §e" + name + "§a успешно создан! Вы — Правитель."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int deleteTown(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не состоите в городе!"));
                return 0;
            }
            if (!town.hasPermission(player.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal("§cТолько Правитель может удалить город!"));
                return 0;
            }
            if (town.getNationName() != null) {
                var nation = NationsData.getNation(town.getNationName());
                if (nation != null) {
                    nation.removeTown(town.getName());
                    NationsData.save();
                }
            }
            NationsData.removeTown(town.getName());
            source.sendSuccess(() -> Component.literal(
                "§a🏰 Город §e" + town.getName() + "§a удалён!"), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int invitePlayer(CommandSourceStack source, String playerName) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.hasPermission(sender.getUUID(), TownRole.OFFICER)) {
                source.sendFailure(Component.literal("§cНужна роль Офицер или выше!"));
                return 0;
            }
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
            if (target == null) {
                source.sendFailure(Component.literal("§cИгрок не найден!"));
                return 0;
            }
            if (NationsData.getTownByPlayer(target.getUUID()) != null) {
                source.sendFailure(Component.literal("§cИгрок уже в городе!"));
                return 0;
            }
            target.sendSystemMessage(Component.literal(
                "§a🏰 Вас пригласили в город §e" + town.getName() +
                "§a! Напишите §e/town join " + town.getName()));
            source.sendSuccess(() -> Component.literal(
                "§aПриглашение отправлено игроку §e" + playerName), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int joinTown(CommandSourceStack source, String townName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            if (NationsData.getTownByPlayer(player.getUUID()) != null) {
                source.sendFailure(Component.literal("§cВы уже в городе!"));
                return 0;
            }
            Town town = NationsData.getTown(townName);
            if (town == null) {
                source.sendFailure(Component.literal("§cГород не найден!"));
                return 0;
            }
            town.addMember(player.getUUID());
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aВы присоединились к городу §e" + town.getName() +
                " §aкак §f" + town.getRole(player.getUUID()).getDisplayName()), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int leaveTown(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не в городе!"));
                return 0;
            }
            if (town.hasPermission(player.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal(
                    "§cПравитель не может покинуть город! Удалите: /town delete"));
                return 0;
            }
            town.removeMember(player.getUUID());
            NationsData.save();
            source.sendSuccess(() -> Component.literal("§aВы покинули город."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int kickPlayer(CommandSourceStack source, String playerName) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.hasPermission(sender.getUUID(), TownRole.GENERAL)) {
                source.sendFailure(Component.literal("§cНужна роль Генерал или выше!"));
                return 0;
            }
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
            if (target == null) {
                source.sendFailure(Component.literal("§cИгрок не найден!"));
                return 0;
            }
            if (!town.isMember(target.getUUID())) {
                source.sendFailure(Component.literal("§cИгрок не в вашем городе!"));
                return 0;
            }
            if (town.getRole(target.getUUID()).getPower() >= town.getRole(sender.getUUID()).getPower()) {
                source.sendFailure(Component.literal("§cНельзя выгнать того, чья роль равна или выше вашей!"));
                return 0;
            }
            town.removeMember(target.getUUID());
            NationsData.save();
            target.sendSystemMessage(Component.literal("§cВас выгнали из города " + town.getName()));
            source.sendSuccess(() -> Component.literal(
                "§aИгрок §e" + playerName + "§a выгнан из города."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int townInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не в городе!"));
                return 0;
            }
            sendTownInfo(source, town);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int townInfoByName(CommandSourceStack source, String name) {
        Town town = NationsData.getTown(name);
        if (town == null) {
            source.sendFailure(Component.literal("§cГород не найден!"));
            return 0;
        }
        sendTownInfo(source, town);
        return 1;
    }

    private static void sendTownInfo(CommandSourceStack source, Town town) {
        StringBuilder sb = new StringBuilder();
        sb.append("§6§l══════════════════════════\n");
        sb.append("§6§l  🏰 ").append(town.getName()).append("\n");
        sb.append("§6§l══════════════════════════\n");
        sb.append("§7Нация: §f").append(town.getNationName() != null ? town.getNationName() : "нет").append("\n");
        sb.append("§7Участников: §f").append(town.getMembers().size()).append("\n");
        sb.append("§7Чанков: §f").append(town.getClaimedChunks().size()).append("\n");
        sb.append("§7Сила: §f").append(town.getPower()).append("\n");
        sb.append("§7Налог: §f").append(String.format("%.1f%%", town.getTaxRate() * 100)).append("\n");
        sb.append("§7Казна: §e").append(Economy.format(Economy.getTownBalance(town.getName()))).append("\n");
        sb.append("§7PvP: ").append(town.isPvpEnabled() ? "§aВКЛ" : "§cВЫКЛ").append("\n");
        sb.append("§7Разрушение: ").append(town.isDestructionEnabled() ? "§aВКЛ" : "§cВЫКЛ").append("\n");
        sb.append("§7Война: ").append(town.isAtWar() ? "§cДА" : "§aНЕТ").append("\n");
        if (town.isCaptured()) {
            sb.append("§c§lЗАХВАЧЕН нацией: §e").append(town.getCapturedBy()).append("\n");
        }
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
    }

    private static int listTowns(CommandSourceStack source) {
        var allTowns = NationsData.getAllTowns();
        if (allTowns.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Городов пока нет."), false);
            return 1;
        }
        StringBuilder sb = new StringBuilder("§6=== 🏰 Города ===\n");
        for (Town t : allTowns) {
            sb.append("§e").append(t.getName())
              .append(" §7[").append(t.getMembers().size()).append(" чел.] ")
              .append(t.getNationName() != null ? "§9" + t.getNationName() : "§8без нации");
            if (t.isCaptured()) sb.append(" §c[ЗАХВАЧЕН]");
            sb.append("\n");
        }
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int setPvp(CommandSourceStack source, boolean enabled) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§cНужна роль Зам. Правителя или выше!"));
                return 0;
            }
            town.setPvpEnabled(enabled);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aPvP " + (enabled ? "§aвключён" : "§cвыключен")), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setDestruction(CommandSourceStack source, boolean enabled) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null || !town.hasPermission(player.getUUID(), TownRole.VICE_RULER)) {
                source.sendFailure(Component.literal("§cНужна роль Зам. Правителя или выше!"));
                return 0;
            }
            town.setDestructionEnabled(enabled);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aРазрушение " + (enabled ? "§aвключено" : "§cвыключено")), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int setRole(CommandSourceStack source, String playerName, String roleId) {
        try {
            ServerPlayer sender = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(sender.getUUID());
            if (town == null || !town.hasPermission(sender.getUUID(), TownRole.RULER)) {
                source.sendFailure(Component.literal("§cТолько Правитель может назначать роли!"));
                return 0;
            }
            ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
            if (target == null) {
                source.sendFailure(Component.literal("§cИгрок не найден!"));
                return 0;
            }
            if (!town.isMember(target.getUUID())) {
                source.sendFailure(Component.literal("§cИгрок не в вашем городе!"));
                return 0;
            }
            TownRole role = TownRole.fromId(roleId);
            if (role == null) {
                source.sendFailure(Component.literal(
                    "§cНеизвестная роль! Используйте /town roles для списка."));
                return 0;
            }
            if (role == TownRole.RULER) {
                source.sendFailure(Component.literal("§cНельзя назначить второго Правителя!"));
                return 0;
            }
            town.setRole(target.getUUID(), role);
            NationsData.save();
            target.sendSystemMessage(Component.literal(
                "§aВам назначена роль: §e" + role.getDisplayName() + " §aв городе §e" + town.getName()));
            source.sendSuccess(() -> Component.literal(
                "§aИгроку §e" + playerName + "§a назначена роль: §e" + role.getDisplayName()), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int listRoles(CommandSourceStack source) {
        StringBuilder sb = new StringBuilder("§6=== 👑 Роли ===\n");
        for (TownRole r : TownRole.values()) {
            sb.append("§e").append(r.getId()).append(" §7- §f").append(r.getDisplayName());
            sb.append(" §7(сила: ").append(r.getPower()).append(")\n");
        }
        sb.append("\n§7Права:\n");
        sb.append("§7• §fСтроитель§7+ — приватить чанки\n");
        sb.append("§7• §fОфицер§7+ — приглашать игроков\n");
        sb.append("§7• §fГенерал§7+ — выгонять игроков\n");
        sb.append("§7• §fЗам. Правителя§7+ — налоги, PvP, казна\n");
        sb.append("§7• §fПравитель§7 — всё + назначение ролей\n");
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int listMembers(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Town town = NationsData.getTownByPlayer(player.getUUID());
            if (town == null) {
                source.sendFailure(Component.literal("§cВы не в городе!"));
                return 0;
            }
            StringBuilder sb = new StringBuilder("§6=== 👥 Жители " + town.getName() + " ===\n");
            for (UUID memberId : town.getMembers()) {
                TownRole role = town.getRole(memberId);
                String name = source.getServer().getPlayerList().getPlayer(memberId) != null ?
                    source.getServer().getPlayerList().getPlayer(memberId).getName().getString() :
                    memberId.toString().substring(0, 8) + "...";
                sb.append("§e").append(name).append(" §7- §f").append(role.getDisplayName()).append("\n");
            }
            source.sendSuccess(() -> Component.literal(sb.toString()), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }
}
