package com.nations.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.nations.data.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

public class RankingCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ranking")
            .executes(ctx -> showRanking(ctx.getSource()))
            .then(Commands.literal("nations")
                .executes(ctx -> showRanking(ctx.getSource())))
            .then(Commands.literal("towns")
                .executes(ctx -> showTownRanking(ctx.getSource())))
            .then(Commands.literal("wars")
                .executes(ctx -> showWarStats(ctx.getSource())))
            .then(Commands.literal("wealth")
                .executes(ctx -> showWealthRanking(ctx.getSource())))
        );
    }

    private static int showRanking(CommandSourceStack source) {
        List<Nation> ranking = NationsData.getNationRanking();
        if (ranking.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Наций пока нет."), false);
            return 1;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§6§l══════════════════════════\n");
        sb.append("§6§l    🏆 РЕЙТИНГ НАЦИЙ    \n");
        sb.append("§6§l══════════════════════════\n\n");

        int i = 1;
        for (Nation n : ranking) {
            String medal;
            switch (i) {
                case 1: medal = "§6🥇"; break;
                case 2: medal = "§f🥈"; break;
                case 3: medal = "§c🥉"; break;
                default: medal = "§7#" + i; break;
            }

            sb.append(medal).append(" §e").append(n.getName()).append("\n");
            sb.append("   §7Рейтинг: §f").append(n.getRating());
            sb.append(" §7| Городов: §f").append(n.getTowns().size());
            sb.append(" §7| Людей: §f").append(n.getTotalMembers());
            sb.append(" §7| Чанков: §f").append(n.getTotalChunks()).append("\n");
            sb.append("   §7Побед: §a").append(n.getWarsWon());
            sb.append(" §7| Поражений: §c").append(n.getWarsLost());
            sb.append(" §7| Захватов: §e").append(n.getTownsCaptured()).append("\n\n");

            i++;
            if (i > 10) break;
        }

        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int showTownRanking(CommandSourceStack source) {
        var allTowns = NationsData.getAllTowns();
        if (allTowns.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Городов пока нет."), false);
            return 1;
        }

        List<Town> sorted = allTowns.stream()
            .sorted((a, b) -> Integer.compare(b.getPower(), a.getPower()))
            .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("§6§l══════════════════════════\n");
        sb.append("§6§l    🏰 РЕЙТИНГ ГОРОДОВ    \n");
        sb.append("§6§l══════════════════════════\n\n");

        int i = 1;
        for (Town t : sorted) {
            String medal;
            switch (i) {
                case 1: medal = "§6🥇"; break;
                case 2: medal = "§f🥈"; break;
                case 3: medal = "§c🥉"; break;
                default: medal = "§7#" + i; break;
            }

            sb.append(medal).append(" §e").append(t.getName());
            if (t.getNationName() != null) sb.append(" §7[§9").append(t.getNationName()).append("§7]");
            sb.append("\n");
            sb.append("   §7Сила: §f").append(t.getPower());
            sb.append(" §7| Людей: §f").append(t.getMembers().size());
            sb.append(" §7| Чанков: §f").append(t.getClaimedChunks().size());
            if (t.isCaptured()) sb.append(" §c[ЗАХВАЧЕН]");
            sb.append("\n\n");

            i++;
            if (i > 10) break;
        }

        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int showWarStats(CommandSourceStack source) {
        List<Nation> ranking = NationsData.getNationRanking();
        if (ranking.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Наций пока нет."), false);
            return 1;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§4§l══════════════════════════\n");
        sb.append("§4§l    ⚔ ВОЕННАЯ СТАТИСТИКА    \n");
        sb.append("§4§l══════════════════════════\n\n");

        // Текущие войны
        boolean hasWars = false;
        for (Nation n : ranking) {
            if (!n.getWarTargets().isEmpty()) {
                if (!hasWars) {
                    sb.append("§c§lАктивные войны:\n");
                    hasWars = true;
                }
                for (String target : n.getWarTargets()) {
                    sb.append("   §c⚔ §e").append(n.getName()).append(" §cvs §e").append(target).append("\n");
                }
            }
        }
        if (!hasWars) sb.append("§a☮ Сейчас войн нет\n");

        sb.append("\n§6§lИстория побед:\n");
        List<Nation> byWins = ranking.stream()
            .sorted((a, b) -> Integer.compare(b.getWarsWon(), a.getWarsWon()))
            .toList();

        int i = 1;
        for (Nation n : byWins) {
            if (n.getWarsWon() == 0 && n.getWarsLost() == 0) continue;
            sb.append("   §e").append(i).append(". §f").append(n.getName());
            sb.append(" §7- §aПобед: ").append(n.getWarsWon());
            sb.append(" §7| §cПоражений: ").append(n.getWarsLost());
            sb.append(" §7| §eЗахватов: ").append(n.getTownsCaptured()).append("\n");
            i++;
            if (i > 10) break;
        }

        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }

    private static int showWealthRanking(CommandSourceStack source) {
        List<Nation> ranking = NationsData.getNationRanking();
        if (ranking.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7Наций пока нет."), false);
            return 1;
        }

        List<Nation> byWealth = ranking.stream()
            .sorted((a, b) -> Double.compare(
                Economy.getNationBalance(b.getName()),
                Economy.getNationBalance(a.getName())))
            .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("§6§l══════════════════════════\n");
        sb.append("§6§l    💰 БОГАТСТВО НАЦИЙ    \n");
        sb.append("§6§l══════════════════════════\n\n");

        int i = 1;
        for (Nation n : byWealth) {
            String medal;
            switch (i) {
                case 1: medal = "§6🥇"; break;
                case 2: medal = "§f🥈"; break;
                case 3: medal = "§c🥉"; break;
                default: medal = "§7#" + i; break;
            }

            double nationBal = Economy.getNationBalance(n.getName());
            double totalTownBal = 0;
            for (String townName : n.getTowns()) {
                totalTownBal += Economy.getTownBalance(townName);
            }

            sb.append(medal).append(" §e").append(n.getName()).append("\n");
            sb.append("   §7Казна нации: §e").append(Economy.format(nationBal)).append("\n");
            sb.append("   §7Казна городов: §e").append(Economy.format(totalTownBal)).append("\n\n");

            i++;
            if (i > 10) break;
        }

        source.sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }
}
