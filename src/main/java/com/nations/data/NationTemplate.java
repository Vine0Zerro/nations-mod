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
        // РОССИЙСКАЯ ФЕДЕРАЦИЯ — 100 чанков (крупная, горизонтальная)
        // Москва(0..6,0..4)=24, СПб(-4..0,0..3)=12, НижНовг(0..6,-2..0)=12
        // Краснодар(-4..0,3..5)=8, Казань(6..10,0..4)=16, Екат(6..10,-2..0)=8
        // Новосибирск(10..14,-2..2)=16, Владивосток(10..14,2..3)=4
        TEMPLATES.put("russia", new NationTemplate(
            "Российская Федерация", NationColor.RED, Arrays.asList(
                new TownTemplate("Москва",            0,  0,  6, 4),
                new TownTemplate("Санкт-Петербург",  -4,  0,  4, 3),
                new TownTemplate("Нижний Новгород",   0, -2,  6, 2),
                new TownTemplate("Краснодар",        -4,  3,  4, 2),
                new TownTemplate("Казань",            6,  0,  4, 4),
                new TownTemplate("Екатеринбург",      6, -2,  4, 2),
                new TownTemplate("Новосибирск",      10, -2,  4, 4),
                new TownTemplate("Владивосток",      10,  2,  4, 1)
            )
        ));

        // США — 95 чанков (крупная, горизонтальная)
        // ЛА(-5..0,-3..3)=30, Вашингтон(0..5,0..3)=15, НьюЙорк(5..10,0..3)=15
        // Чикаго(0..5,-3..0)=15, Хьюстон(0..5,3..5)=10, Майами(5..10,3..5)=10
        TEMPLATES.put("usa", new NationTemplate(
            "Соединённые Штаты Америки", NationColor.BLUE, Arrays.asList(
                new TownTemplate("Вашингтон",        0,  0,  5, 3),
                new TownTemplate("Нью-Йорк",         5,  0,  5, 3),
                new TownTemplate("Чикаго",            0, -3,  5, 3),
                new TownTemplate("Лос-Анджелес",     -5, -3,  5, 6),
                new TownTemplate("Хьюстон",           0,  3,  5, 2),
                new TownTemplate("Майами",            5,  3,  5, 2)
            )
        ));

        // КИТАЙ — 93 чанка (крупная)
        // Пекин(0..5,0..4)=20, Шанхай(5..9,0..4)=16, Гуанчжоу(0..5,4..7)=15
        // Шэньчжэнь(5..9,4..7)=12, Чэнду(-5..0,0..4)=20, Ухань(-5..0,4..6)=10
        TEMPLATES.put("china", new NationTemplate(
            "Китайская Народная Республика", NationColor.DARK_RED, Arrays.asList(
                new TownTemplate("Пекин",            0,  0,  5, 4),
                new TownTemplate("Шанхай",           5,  0,  4, 4),
                new TownTemplate("Гуанчжоу",         0,  4,  5, 3),
                new TownTemplate("Шэньчжэнь",        5,  4,  4, 3),
                new TownTemplate("Чэнду",           -5,  0,  5, 4),
                new TownTemplate("Ухань",            -5,  4,  5, 2)
            )
        ));

        // БРАЗИЛИЯ — 88 чанков (крупная, вертикальная)
        // Бразилиа(0..5,0..4)=20, Манаус(0..5,-4..0)=20, Форталеза(5..9,-4..0)=16
        // Рио(5..9,0..4)=16, СанПаулу(0..5,4..7)=15, Куритиба(5..6,4..5)=1
        TEMPLATES.put("brazil", new NationTemplate(
            "Федеративная Республика Бразилия", NationColor.GREEN, Arrays.asList(
                new TownTemplate("Бразилиа",         0,  0,  5, 4),
                new TownTemplate("Манаус",           0, -4,  5, 4),
                new TownTemplate("Форталеза",        5, -4,  4, 4),
                new TownTemplate("Рио-де-Жанейро",   5,  0,  4, 4),
                new TownTemplate("Сан-Паулу",        0,  4,  5, 3),
                new TownTemplate("Куритиба",         5,  4,  1, 1)
            )
        ));

        // ИНДИЯ — 87 чанков (крупная, треугольная вниз)
        // Дели(0..6,0..4)=24, Джайпур(-6..0,0..3)=18, Калькутта(6..9,0..3)=9
        // Мумбаи(-6..0,3..6)=18, Бангалор(0..6,4..6)=12, Ченнаи(6..9,3..5)=6
        TEMPLATES.put("india", new NationTemplate(
            "Республика Индия", NationColor.TEAL, Arrays.asList(
                new TownTemplate("Дели",             0,  0,  6, 4),
                new TownTemplate("Джайпур",         -6,  0,  6, 3),
                new TownTemplate("Калькутта",        6,  0,  3, 3),
                new TownTemplate("Мумбаи",          -6,  3,  6, 3),
                new TownTemplate("Бангалор",         0,  4,  6, 2),
                new TownTemplate("Ченнаи",           6,  3,  3, 2)
            )
        ));

        // ТУРЦИЯ — 76 чанков (средняя, горизонтальная)
        // Анкара(0..5,0..4)=20, Стамбул(-5..0,0..4)=20, Измир(-5..0,4..6)=10
        // Анталья(0..5,4..6)=10, Трабзон(5..9,0..2)=8, Бурса(5..9,2..4)=8
        TEMPLATES.put("turkey", new NationTemplate(
            "Турецкая Республика", NationColor.ORANGE, Arrays.asList(
                new TownTemplate("Анкара",           0,  0,  5, 4),
                new TownTemplate("Стамбул",         -5,  0,  5, 4),
                new TownTemplate("Измир",           -5,  4,  5, 2),
                new TownTemplate("Анталья",          0,  4,  5, 2),
                new TownTemplate("Трабзон",          5,  0,  4, 2),
                new TownTemplate("Бурса",            5,  2,  4, 2)
            )
        ));

        // ФРАНЦИЯ — 74 чанка (средняя)
        // Париж(0..5,0..4)=20, Лион(5..9,0..4)=16, Марсель(5..9,4..6)=8
        // Тулуза(0..5,4..6)=10, Бордо(-5..0,0..3)=15, Страсбург(-5..0,3..4)=5
        TEMPLATES.put("france", new NationTemplate(
            "Французская Республика", NationColor.NAVY, Arrays.asList(
                new TownTemplate("Париж",            0,  0,  5, 4),
                new TownTemplate("Лион",             5,  0,  4, 4),
                new TownTemplate("Марсель",          5,  4,  4, 2),
                new TownTemplate("Тулуза",           0,  4,  5, 2),
                new TownTemplate("Бордо",           -5,  0,  5, 3),
                new TownTemplate("Страсбург",       -5,  3,  5, 1)
            )
        ));

        // ГЕРМАНИЯ — 72 чанка (средняя, вертикальная)
        // Берлин(0..6,0..4)=24, Гамбург(0..6,-4..0)=24, Мюнхен(0..6,4..7)=18
        // Франкфурт(-6..0,0..1)=6
        TEMPLATES.put("germany", new NationTemplate(
            "Федеративная Республика Германия", NationColor.GOLD, Arrays.asList(
                new TownTemplate("Берлин",           0,  0,  6, 4),
                new TownTemplate("Гамбург",          0, -4,  6, 4),
                new TownTemplate("Мюнхен",           0,  4,  6, 3),
                new TownTemplate("Франкфурт",       -6,  0,  6, 1)
            )
        ));

        // ЯПОНИЯ — 70 чанков (средняя, вертикальная цепочка)
        // Токио(0..4,0..5)=20, Осака(0..4,5..8)=12, Нагоя(4..7,0..5)=15
        // Саппоро(0..4,-4..0)=16, Хиросима(0..4,8..9)=4, Фукуока(4..7,5..6)=3
        TEMPLATES.put("japan", new NationTemplate(
            "Японская Империя", NationColor.WHITE, Arrays.asList(
                new TownTemplate("Токио",            0,  0,  4, 5),
                new TownTemplate("Осака",            0,  5,  4, 3),
                new TownTemplate("Нагоя",            4,  0,  3, 5),
                new TownTemplate("Саппоро",          0, -4,  4, 4),
                new TownTemplate("Хиросима",         0,  8,  4, 1),
                new TownTemplate("Фукуока",          4,  5,  3, 1)
            )
        ));

        // ВЕЛИКОБРИТАНИЯ — 68 чанков (средняя, вертикальная)
        // Лондон(0..4,0..4)=16, Бирмингем(0..4,-4..0)=16
        // Манчестер(0..4,-8..-4)=16, Эдинбург(0..4,-12..-8)=16, Бристоль(4..5,0..4)=4
        TEMPLATES.put("uk", new NationTemplate(
            "Соединённое Королевство", NationColor.PURPLE, Arrays.asList(
                new TownTemplate("Лондон",           0,  0,  4, 4),
                new TownTemplate("Бирмингем",        0, -4,  4, 4),
                new TownTemplate("Манчестер",        0, -8,  4, 4),
                new TownTemplate("Эдинбург",         0,-12,  4, 4),
                new TownTemplate("Бристоль",         4,  0,  1, 4)
            )
        ));

        // РУМЫНИЯ — 46 чанков (малая, компактная)
        // Бухарест(0..4,0..4)=16, Клуж(-4..0,0..3)=12, Тимишоара(-4..0,3..5)=8
        // Яссы(4..6,0..3)=6, Констанца(0..4,4..5)=4
        TEMPLATES.put("romania", new NationTemplate(
            "Румыния", NationColor.YELLOW, Arrays.asList(
                new TownTemplate("Бухарест",         0,  0,  4, 4),
                new TownTemplate("Клуж-Напока",     -4,  0,  4, 3),
                new TownTemplate("Тимишоара",       -4,  3,  4, 2),
                new TownTemplate("Яссы",             4,  0,  2, 3),
                new TownTemplate("Констанца",        0,  4,  4, 1)
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
