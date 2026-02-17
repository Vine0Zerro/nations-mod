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
        public final int offsetX; // смещение в чанках от центра
        public final int offsetZ;
        public final int chunksX; // размер города в чанках
        public final int chunksZ;

        public TownTemplate(String name, int offsetX, int offsetZ, int chunksX, int chunksZ) {
            this.name = name;
            this.offsetX = offsetX;
            this.offsetZ = offsetZ;
            this.chunksX = chunksX;
            this.chunksZ = chunksZ;
        }
    }

    // ========== ШАБЛОНЫ НАЦИЙ ==========

    private static final Map<String, NationTemplate> TEMPLATES = new HashMap<>();

    static {
        // === РОССИЙСКАЯ ФЕДЕРАЦИЯ ===
        // 100 чанков: Москва(25) + СПб(15) + Новосибирск(12) + Екатеринбург(12) + 
        // Владивосток(10) + Казань(10) + Нижний Новгород(8) + Краснодар(8)
        TEMPLATES.put("российская федерация", new NationTemplate(
            "Российская Федерация", NationColor.RED, Arrays.asList(
                new TownTemplate("Москва",           0,  0,  5, 5),   // 25 центр
                new TownTemplate("Санкт-Петербург",  -5,  0,  5, 3),  // 15 запад
                new TownTemplate("Казань",            5,  0,  5, 2),  // 10 восток
                new TownTemplate("Нижний Новгород",   0, -3,  4, 2),  // 8 север
                new TownTemplate("Екатеринбург",      5, -3,  4, 3),  // 12 северо-восток
                new TownTemplate("Краснодар",        -5,  3,  4, 2),  // 8 юго-запад
                new TownTemplate("Новосибирск",       5,  3,  4, 3),  // 12 юго-восток
                new TownTemplate("Владивосток",       9,  3,  5, 2)   // 10 дальний восток
            )
        ));

        // === США ===
        // 100 чанков: Вашингтон(20) + Нью-Йорк(18) + Лос-Анджелес(16) + 
        // Чикаго(12) + Хьюстон(12) + Майами(10) + Сиэтл(6) + Денвер(6)
        TEMPLATES.put("сша", new NationTemplate(
            "США", NationColor.BLUE, Arrays.asList(
                new TownTemplate("Вашингтон",       0,  0,  5, 4),   // 20 центр
                new TownTemplate("Нью-Йорк",        5,  0,  6, 3),   // 18 восток
                new TownTemplate("Лос-Анджелес",    -5,  0,  4, 4),  // 16 запад
                new TownTemplate("Чикаго",           0, -4,  4, 3),   // 12 север
                new TownTemplate("Хьюстон",          0,  4,  4, 3),   // 12 юг
                new TownTemplate("Майами",           5,  4,  5, 2),   // 10 юго-восток
                new TownTemplate("Сиэтл",           -5, -4,  3, 2),   // 6 северо-запад
                new TownTemplate("Денвер",           -5,  4,  3, 2)    // 6 юго-запад
            )
        ));

        // === РУМЫНИЯ ===
        // 100 чанков: Бухарест(25) + Клуж-Напока(15) + Тимишоара(15) + 
        // Яссы(15) + Констанца(12) + Брашов(10) + Крайова(8)
        TEMPLATES.put("румыния", new NationTemplate(
            "Румыния", NationColor.YELLOW, Arrays.asList(
                new TownTemplate("Бухарест",        0,  0,  5, 5),   // 25 центр
                new TownTemplate("Клуж-Напока",    -5,  0,  5, 3),   // 15 запад
                new TownTemplate("Тимишоара",      -5, -3,  5, 3),   // 15 северо-запад
                new TownTemplate("Яссы",            5,  0,  5, 3),   // 15 восток
                new TownTemplate("Констанца",       5,  3,  4, 3),   // 12 юго-восток
                new TownTemplate("Брашов",          0, -5,  5, 2),   // 10 север
                new TownTemplate("Крайова",         0,  5,  4, 2)    //  8 юг
            )
        ));

        // === ГЕРМАНИЯ ===
        TEMPLATES.put("германия", new NationTemplate(
            "Германия", NationColor.GOLD, Arrays.asList(
                new TownTemplate("Берлин",          0,  0,  5, 5),   // 25
                new TownTemplate("Мюнхен",          0,  5,  5, 3),   // 15
                new TownTemplate("Гамбург",         0, -5,  5, 3),   // 15
                new TownTemplate("Франкфурт",      -5,  0,  5, 3),   // 15
                new TownTemplate("Кёльн",          -5, -3,  5, 2),   // 10
                new TownTemplate("Дрезден",         5,  0,  4, 3),   // 12
                new TownTemplate("Штутгарт",        5,  3,  4, 2)    //  8
            )
        ));

        // === КИТАЙ ===
        TEMPLATES.put("китай", new NationTemplate(
            "Китай", NationColor.DARK_RED, Arrays.asList(
                new TownTemplate("Пекин",           0,  0,  5, 5),   // 25
                new TownTemplate("Шанхай",          5,  0,  5, 4),   // 20
                new TownTemplate("Гуанчжоу",        0,  5,  5, 3),   // 15
                new TownTemplate("Шэньчжэнь",       5,  5,  5, 2),  // 10
                new TownTemplate("Чэнду",          -5,  0,  5, 3),   // 15
                new TownTemplate("Ухань",            0, -5,  5, 3)    // 15
            )
        ));

        // === ФРАНЦИЯ ===
        TEMPLATES.put("франция", new NationTemplate(
            "Франция", NationColor.NAVY, Arrays.asList(
                new TownTemplate("Париж",           0,  0,  5, 5),   // 25
                new TownTemplate("Марсель",          0,  5,  5, 3),  // 15
                new TownTemplate("Лион",            -5,  0,  5, 3),  // 15
                new TownTemplate("Тулуза",          -5,  5,  5, 3),  // 15
                new TownTemplate("Ницца",            5,  5,  5, 2),  // 10
                new TownTemplate("Страсбург",        5,  0,  4, 3),  // 12
                new TownTemplate("Бордо",           -5, -3,  4, 2)   //  8
            )
        ));

        // === ВЕЛИКОБРИТАНИЯ ===
        TEMPLATES.put("великобритания", new NationTemplate(
            "Великобритания", NationColor.PURPLE, Arrays.asList(
                new TownTemplate("Лондон",          0,  0,  5, 5),   // 25
                new TownTemplate("Манчестер",        0, -5,  5, 3),  // 15
                new TownTemplate("Бирмингем",       -5,  0,  5, 3),  // 15
                new TownTemplate("Ливерпуль",       -5, -5,  5, 3),  // 15
                new TownTemplate("Эдинбург",         0, -8,  5, 2),  // 10
                new TownTemplate("Глазго",          -5, -8,  4, 3),  // 12
                new TownTemplate("Бристоль",         5,  0,  4, 2)   //  8
            )
        ));

        // === ЯПОНИЯ ===
        TEMPLATES.put("япония", new NationTemplate(
            "Япония", NationColor.WHITE, Arrays.asList(
                new TownTemplate("Токио",           0,  0,  5, 5),   // 25
                new TownTemplate("Осака",            0,  5,  5, 4),  // 20
                new TownTemplate("Нагоя",           -5,  0,  5, 3),  // 15
                new TownTemplate("Саппоро",          0, -5,  5, 3),  // 15
                new TownTemplate("Фукуока",          5,  5,  5, 2),  // 10
                new TownTemplate("Хиросима",         5,  0,  5, 3)   // 15
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
