package com.nations.data;

import java.util.*;

public class NationTemplate {

    private final String nationName;
    private final NationColor color;
    private final List<TownTemplate> towns;

    public NationTemplate(String nationName, NationColor color, List<TownTemplate> towns) {
        this.nationName = nationName;
        this.color = color;
        this.towns = towns;
    }

    public String getNationName() { return nationName; }
    public NationColor getColor() { return color; }
    public List<TownTemplate> getTowns() { return towns; }

    public int getTotalChunks() {
        return towns.stream().mapToInt(t -> t.chunksX * t.chunksZ).sum();
    }

    public static class TownTemplate {
        public final String name;
        public final int offsetX;
        public final int offsetZ;
        public final int chunksX;
        public final int chunksZ;

        public TownTemplate(String name, int offsetX, int offsetZ, int chunksX, int chunksZ) {
            this.name = name;
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
            this.chunksX = chunksX;
            this.chunksZ = chunksZ;
        }
    }

    private static final Map<String, NationTemplate> TEMPLATES = new HashMap<>();

    static {
        TEMPLATES.put("российская федерация", new NationTemplate(
            "Российская Федерация", NationColor.RED, Arrays.asList(
                new TownTemplate("Москва",           0,  0,  5, 5),
                new TownTemplate("Санкт-Петербург",  -5,  0,  5, 3),
                new TownTemplate("Казань",            5,  0,  5, 2),
                new TownTemplate("Нижний Новгород",   0, -3,  4, 2),
                new TownTemplate("Екатеринбург",      5, -3,  4, 3),
                new TownTemplate("Краснодар",        -5,  3,  4, 2),
                new TownTemplate("Новосибирск",       5,  3,  4, 3),
                new TownTemplate("Владивосток",       9,  3,  5, 2)
            )
        ));

        TEMPLATES.put("сша", new NationTemplate(
            "США", NationColor.BLUE, Arrays.asList(
                new TownTemplate("Вашингтон",       0,  0,  5, 4),
                new TownTemplate("Нью-Йорк",        5,  0,  6, 3),
                new TownTemplate("Лос-Анджелес",    -5,  0,  4, 4),
                new TownTemplate("Чикаго",           0, -4,  4, 3),
                new TownTemplate("Хьюстон",          0,  4,  4, 3),
                new TownTemplate("Майами",           5,  4,  5, 2),
                new TownTemplate("Сиэтл",           -5, -4,  3, 2),
                new TownTemplate("Денвер",           -5,  4,  3, 2)
            )
        ));

        TEMPLATES.put("румыния", new NationTemplate(
            "Румыния", NationColor.YELLOW, Arrays.asList(
                new TownTemplate("Бухарест",        0,  0,  5, 5),
                new TownTemplate("Клуж-Напока",    -5,  0,  5, 3),
                new TownTemplate("Тимишоара",      -5, -3,  5, 3),
                new TownTemplate("Яссы",            5,  0,  5, 3),
                new TownTemplate("Констанца",       5,  3,  4, 3),
                new TownTemplate("Брашов",          0, -5,  5, 2),
                new TownTemplate("Крайова",         0,  5,  4, 2)
            )
        ));

        TEMPLATES.put("германия", new NationTemplate(
            "Германия", NationColor.GOLD, Arrays.asList(
                new TownTemplate("Берлин",          0,  0,  5, 5),
                new TownTemplate("Мюнхен",          0,  5,  5, 3),
                new TownTemplate("Гамбург",         0, -5,  5, 3),
                new TownTemplate("Франкфурт",      -5,  0,  5, 3),
                new TownTemplate("Кёльн",          -5, -3,  5, 2),
                new TownTemplate("Дрезден",         5,  0,  4, 3),
                new TownTemplate("Штутгарт",        5,  3,  4, 2)
            )
        ));

        TEMPLATES.put("китай", new NationTemplate(
            "Китай", NationColor.DARK_RED, Arrays.asList(
                new TownTemplate("Пекин",           0,  0,  5, 5),
                new TownTemplate("Шанхай",          5,  0,  5, 4),
                new TownTemplate("Гуанчжоу",        0,  5,  5, 3),
                new TownTemplate("Шэньчжэнь",       5,  5,  5, 2),
                new TownTemplate("Чэнду",          -5,  0,  5, 3),
                new TownTemplate("Ухань",            0, -5,  5, 3)
            )
        ));

        TEMPLATES.put("франция", new NationTemplate(
            "Франция", NationColor.NAVY, Arrays.asList(
                new TownTemplate("Париж",           0,  0,  5, 5),
                new TownTemplate("Марсель",          0,  5,  5, 3),
                new TownTemplate("Лион",            -5,  0,  5, 3),
                new TownTemplate("Тулуза",          -5,  5,  5, 3),
                new TownTemplate("Ницца",            5,  5,  5, 2),
                new TownTemplate("Страсбург",        5,  0,  4, 3),
                new TownTemplate("Бордо",           -5, -3,  4, 2)
            )
        ));

        TEMPLATES.put("великобритания", new NationTemplate(
            "Великобритания", NationColor.PURPLE, Arrays.asList(
                new TownTemplate("Лондон",          0,  0,  5, 5),
                new TownTemplate("Манчестер",        0, -5,  5, 3),
                new TownTemplate("Бирмингем",       -5,  0,  5, 3),
                new TownTemplate("Ливерпуль",       -5, -5,  5, 3),
                new TownTemplate("Эдинбург",         0, -8,  5, 2),
                new TownTemplate("Глазго",          -5, -8,  4, 3),
                new TownTemplate("Бристоль",         5,  0,  4, 2)
            )
        ));

        TEMPLATES.put("япония", new NationTemplate(
            "Япония", NationColor.WHITE, Arrays.asList(
                new TownTemplate("Токио",           0,  0,  5, 5),
                new TownTemplate("Осака",            0,  5,  5, 4),
                new TownTemplate("Нагоя",           -5,  0,  5, 3),
                new TownTemplate("Саппоро",          0, -5,  5, 3),
                new TownTemplate("Фукуока",          5,  5,  5, 2),
                new TownTemplate("Хиросима",         5,  0,  5, 3)
            )
        ));
    }

    public static NationTemplate getTemplate(String name) {
        return TEMPLATES.get(name.toLowerCase());
    }

    public static Set<String> getAvailableTemplates() {
        return TEMPLATES.keySet();
    }
}
