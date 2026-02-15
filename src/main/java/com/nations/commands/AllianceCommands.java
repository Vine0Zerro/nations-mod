package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.nations.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AllianceCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("alliance")
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> createAlliance(ctx.getSource(),
                        StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("delete")
                .executes(ctx -> deleteAlliance(ctx.getSource())))
            .then(Commands.literal("invite")
                .then(Commands.argument("nation", StringArgumentType.word())
                    .executes(ctx -> inviteNation(ctx.getSource(),
                        StringArgumentType.getString(ctx, "nation")))))
            .then(Commands.literal("accept")
                .then(Commands.argument("alliance", StringArgumentType.word())
                    .executes(ctx -> acceptInvite(ctx.getSource(),
                        StringArgumentType.getString(ctx, "alliance")))))
            .then(Commands.literal("leave")
                .executes(ctx -> leaveAlliance(ctx.getSource())))
            .then(Commands.literal("kick")
                .then(Commands.argument("nation", StringArgumentType.word())
                    .executes(ctx -> kickNation(ctx.getSource(),
                        StringArgumentType.getString(ctx, "nation")))))
            .then(Commands.literal("info")
                .executes(ctx -> allianceInfo(ctx.getSource()))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> allianceInfoByName(ctx.getSource(),
                        StringArgumentType.getString(ctx, "name")))))
            .then(Commands.literal("list")
                .executes(ctx -> listAlliances(ctx.getSource())))
        );
    }

    private static int createAlliance(CommandSourceStack source, String name) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            if (nation.getAllianceName() != null) {
                source.sendFailure(Component.literal("§cВаша нация уже в альянсе!"));
                return 0;
            }
            if (NationsData.allianceExists(name)) {
                source.sendFailure(Component.literal("§cАльянс с таким именем уже существует!"));
                return 0;
            }
            Alliance alliance = new Alliance(name, nation.getName());
            nation.setAllianceName(name);
            NationsData.addAlliance(alliance);
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§6🤝 Создан альянс §e" + name +
                    " §6во главе с нацией §e" + nation.getName()), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int deleteAlliance(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            if (nation.getAllianceName() == null) {
                source.sendFailure(Component.literal("§cВаша нация не в альянсе!"));
                return 0;
            }
            Alliance alliance = NationsData.getAlliance(nation.getAllianceName());
            if (alliance == null || !alliance.getLeaderNation().equalsIgnoreCase(nation.getName())) {
                source.sendFailure(Component.literal("§cТолько глава альянса может его удалить!"));
                return 0;
            }
            String allianceName = alliance.getName();
            for (String memberName : alliance.getMembers()) {
                Nation member = NationsData.getNation(memberName);
                if (member != null) member.setAllianceName(null);
            }
            NationsData.removeAlliance(allianceName);
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§cАльянс §e" + allianceName + " §cбыл распущен!"), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int inviteNation(CommandSourceStack source, String nationName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            Alliance alliance = NationsData.getAlliance(nation.getAllianceName());
            if (alliance == null || !alliance.getLeaderNation().equalsIgnoreCase(nation.getName())) {
                source.sendFailure(Component.literal("§cТолько глава альянса может приглашать!"));
                return 0;
            }
            Nation target = NationsData.getNation(nationName);
            if (target == null) {
                source.sendFailure(Component.literal("§cНация не найдена!"));
                return 0;
            }
            if (target.getAllianceName() != null) {
                source.sendFailure(Component.literal("§cЭта нация уже в альянсе!"));
                return 0;
            }
            alliance.invite(target.getName());
            NationsData.save();

            ServerPlayer targetLeader = source.getServer().getPlayerList().getPlayer(target.getLeader());
            if (targetLeader != null) {
                targetLeader.sendSystemMessage(Component.literal(
                    "§aВашу нацию пригласили в альянс §e" + alliance.getName() +
                    "§a! Напишите §e/alliance accept " + alliance.getName()));
            }
            source.sendSuccess(() -> Component.literal(
                "§aПриглашение отправлено нации §e" + nationName), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int acceptInvite(CommandSourceStack source, String allianceName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            Alliance alliance = NationsData.getAlliance(allianceName);
            if (alliance == null) {
                source.sendFailure(Component.literal("§cАльянс не найден!"));
                return 0;
            }
            if (!alliance.hasInvite(nation.getName())) {
                source.sendFailure(Component.literal("§cУ вас нет приглашения!"));
                return 0;
            }
            alliance.removeInvite(nation.getName());
            alliance.addMember(nation.getName());
            nation.setAllianceName(alliance.getName());
            NationsData.save();

            source.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§a🤝 Нация §e" + nation.getName() +
                    " §aвступила в альянс §e" + alliance.getName()), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int leaveAlliance(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            if (nation.getAllianceName() == null) {
                source.sendFailure(Component.literal("§cВаша нация не в альянсе!"));
                return 0;
            }
            Alliance alliance = NationsData.getAlliance(nation.getAllianceName());
            if (alliance != null) {
                if (alliance.getLeaderNation().equalsIgnoreCase(nation.getName())) {
                    source.sendFailure(Component.literal(
                        "§cГлава альянса не может выйти! Удалите альянс: /alliance delete"));
                    return 0;
                }
                alliance.removeMember(nation.getName());
            }
            nation.setAllianceName(null);
            NationsData.save();
            source.sendSuccess(() -> Component.literal("§aВаша нация покинула альянс."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int kickNation(CommandSourceStack source, String nationName) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || !nation.getLeader().equals(player.getUUID())) {
                source.sendFailure(Component.literal("§cВы не лидер нации!"));
                return 0;
            }
            Alliance alliance = NationsData.getAlliance(nation.getAllianceName());
            if (alliance == null || !alliance.getLeaderNation().equalsIgnoreCase(nation.getName())) {
                source.sendFailure(Component.literal("§cТолько глава альянса может исключать!"));
                return 0;
            }
            Nation target = NationsData.getNation(nationName);
            if (target == null || !alliance.hasMember(target.getName())) {
                source.sendFailure(Component.literal("§cНация не в альянсе!"));
                return 0;
            }
            alliance.removeMember(target.getName());
            target.setAllianceName(null);
            NationsData.save();
            source.sendSuccess(() -> Component.literal(
                "§aНация §e" + nationName + " §aисключена из альянса."), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int allianceInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            Nation nation = NationsData.getNationByPlayer(player.getUUID());
            if (nation == null || nation.getAllianceName() == null) {
                source.sendFailure(Component.literal("§cВы не в альянсе!"));
                return 0;
            }
            Alliance alliance = NationsData.getAlliance(nation.getAllianceName());
            if (alliance == null) {
                source.sendFailure(Component.literal("§cАльянс не найден!"));
                return 0;
            }
            sendAllianceInfo(source, alliance);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cОшибка: " + e.getMessage()));
            return 0;
        }
    }

    private static int allianceInfoByName(CommandSourceStack source, String name) {
        Alliance alliance = NationsData.getAlliance(name);
        if (alliance == null) {
            source.sendFailure(Component.literal("§cАльянс не найден!"));
            return 0;
        }
        sendAllianceInfo(source, alliance);
        return 1;
    }

    private static void sendAllianceInfo(CommandSourceStack source, Alliance alliance) {
        StringBuilder sb = new StringBuilder();
        sb.append("§6=== 🤝 Альянс: §e").append(alliance.getName()).append(" §6===\n");
        sb.append("§7Глава: §f").append(alliance.getLeaderNation()).append("\n");
        sb.append("§7Нации: §f").append(String.join(", ", alliance.getMembers())).append("\n");
        sb.append("§7Всего наций: §f").append(alliance.getMembers().size());
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
    }

    private static int listAlliances(CommandSourceStack source) {
        var all = NationsData.getAllAlliances();
        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Альянсов пока нет."), false);
            return 1;
        }
        StringBuilder sb = new StringBuilder("§6=== 🤝 Альянсы ===\n");
        for (Alliance a : all) {
            sb.append("§e").append(a.getName())
              .append(" §7[глава: ").append(a.getLeaderNation())
              .append(", наций: ").append(a.getMembers().size()).append("]\n");
        }
        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }
}
