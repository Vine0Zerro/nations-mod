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
        int total = 0;
        for (TownTemplate t : towns) total += t.getChunkCount();
        return total;
    }

    public static class TownTemplate {
        public final String name;
        public final List<int[]> chunks;

        public TownTemplate(String name, List<int[]> chunks) {
            this.name = name;
            this.chunks = chunks;
        }

        public int getChunkCount() { return chunks.size(); }

        public int[] getCenter() {
            if (chunks.isEmpty()) return new int[]{0, 0};
            int sumX = 0, sumZ = 0;
            for (int[] c : chunks) { sumX += c[0]; sumZ += c[1]; }
            return new int[]{sumX / chunks.size(), sumZ / chunks.size()};
        }
    }

    private static List<int[]> rect(int sx, int sz, int w, int h) {
        List<int[]> r = new ArrayList<>();
        for (int x = sx; x < sx + w; x++)
            for (int z = sz; z < sz + h; z++)
                r.add(new int[]{x, z});
        return r;
    }

    @SafeVarargs
    private static List<int[]> merge(List<int[]>... lists) {
        List<int[]> r = new ArrayList<>();
        for (List<int[]> l : lists) r.addAll(l);
        return r;
    }

    private static final Map<String, NationTemplate> TEMPLATES = new HashMap<>();

    static {
        // РОССИЯ ~490
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, Arrays.asList(
            new TownTemplate("Москва", merge(rect(0,0,7,6), rect(7,1,2,4), rect(-1,1,1,4))),
            new TownTemplate("Санкт-Петербург", merge(rect(-6,-3,5,5), rect(-7,-2,1,3), rect(-1,-3,1,3))),
            new TownTemplate("Нижний Новгород", merge(rect(9,0,5,4), rect(9,4,3,2), rect(9,-1,4,1))),
            new TownTemplate("Казань", merge(rect(14,-1,5,5), rect(13,0,1,3), rect(19,0,1,3))),
            new TownTemplate("Екатеринбург", merge(rect(20,-2,5,6), rect(20,4,3,2), rect(25,0,1,3))),
            new TownTemplate("Новосибирск", merge(rect(26,-3,6,7), rect(32,-1,2,4), rect(25,-1,1,4))),
            new TownTemplate("Красноярск", merge(rect(34,-3,5,7), rect(34,4,3,2), rect(39,-1,1,4))),
            new TownTemplate("Владивосток", merge(rect(40,-2,5,5), rect(40,3,3,2), rect(45,0,1,2))),
            new TownTemplate("Краснодар", merge(rect(-3,6,6,4), rect(-4,7,1,2), rect(3,6,2,3))),
            new TownTemplate("Мурманск", merge(rect(-4,-8,5,5), rect(-5,-7,1,3), rect(-3,-3,4,1))),
            new TownTemplate("Самара", merge(rect(9,6,5,4), rect(14,6,2,3), rect(8,7,1,2)))
        )));

        // США ~460
        TEMPLATES.put("usa", new NationTemplate("Соединённые Штаты Америки", NationColor.BLUE, Arrays.asList(
            new TownTemplate("Вашингтон", merge(rect(0,0,6,5), rect(6,1,2,3), rect(-1,1,1,3))),
            new TownTemplate("Нью-Йорк", merge(rect(8,-2,5,6), rect(13,-1,2,4), rect(7,0,1,3))),
            new TownTemplate("Чикаго", merge(rect(0,-7,6,5), rect(-1,-6,1,3), rect(6,-6,2,3), rect(0,-2,6,2))),
            new TownTemplate("Лос-Анджелес", merge(rect(-14,-4,6,7), rect(-15,-2,1,4), rect(-8,-3,2,5))),
            new TownTemplate("Денвер", merge(rect(-8,-7,5,6), rect(-8,-1,5,3), rect(-3,-7,3,5))),
            new TownTemplate("Хьюстон", merge(rect(-5,5,6,5), rect(-6,6,1,3), rect(1,5,2,4), rect(-5,2,5,3))),
            new TownTemplate("Майами", merge(rect(3,5,5,4), rect(5,9,3,3), rect(8,5,2,3), rect(3,4,5,1))),
            new TownTemplate("Сиэтл", merge(rect(-14,-11,5,7), rect(-15,-9,1,4), rect(-9,-10,1,3))),
            new TownTemplate("Миннеаполис", merge(rect(-3,-12,6,5), rect(-4,-11,1,3), rect(3,-11,2,3)))
        )));

        // КИТАЙ ~440
        TEMPLATES.put("china", new NationTemplate("Китайская Народная Республика", NationColor.DARK_RED, Arrays.asList(
            new TownTemplate("Пекин", merge(rect(0,0,6,5), rect(6,1,2,3), rect(-1,1,1,3))),
            new TownTemplate("Шанхай", merge(rect(8,1,5,5), rect(13,2,2,3), rect(7,2,1,3))),
            new TownTemplate("Гуанчжоу", merge(rect(5,6,5,5), rect(4,7,1,3), rect(10,7,2,3), rect(5,5,3,1))),
            new TownTemplate("Шэньчжэнь", merge(rect(7,11,5,4), rect(6,11,1,3), rect(12,12,1,2))),
            new TownTemplate("Чэнду", merge(rect(-7,2,6,5), rect(-8,3,1,3), rect(-1,2,1,4))),
            new TownTemplate("Ухань", merge(rect(0,5,5,5), rect(-1,6,1,3))),
            new TownTemplate("Харбин", merge(rect(2,-6,5,5), rect(1,-5,1,3), rect(7,-5,1,3), rect(2,-1,4,1))),
            new TownTemplate("Урумчи", merge(rect(-15,-2,6,6), rect(-16,0,1,3), rect(-9,0,2,4))),
            new TownTemplate("Лхаса", merge(rect(-15,4,6,5), rect(-16,5,1,3), rect(-9,5,2,3))),
            new TownTemplate("Куньмин", merge(rect(-7,7,5,5), rect(-8,8,1,3), rect(-2,7,2,4)))
        )));

        // ГЕРМАНИЯ ~240
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, Arrays.asList(
            new TownTemplate("Берлин", merge(rect(0,0,6,5), rect(-1,1,1,3))),
            new TownTemplate("Гамбург", merge(rect(-1,-6,7,6), rect(-2,-5,1,4))),
            new TownTemplate("Мюнхен", merge(rect(-1,5,7,5), rect(-2,6,1,3), rect(6,6,1,3))),
            new TownTemplate("Франкфурт", merge(rect(-7,0,6,5), rect(-8,1,1,3))),
            new TownTemplate("Кёльн", merge(rect(-7,-6,5,6), rect(-8,-5,1,4))),
            new TownTemplate("Штутгарт", merge(rect(-7,5,6,4), rect(-8,6,1,2))),
            new TownTemplate("Дрезден", merge(rect(6,0,4,5), rect(6,-2,3,2)))
        )));

        // ФРАНЦИЯ ~300
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, Arrays.asList(
            new TownTemplate("Париж", merge(rect(0,0,6,5), rect(-1,1,1,3), rect(6,1,1,3))),
            new TownTemplate("Лион", merge(rect(4,5,5,5), rect(3,6,1,3), rect(9,6,1,3))),
            new TownTemplate("Марсель", merge(rect(4,10,6,5), rect(3,11,1,3), rect(10,11,1,3))),
            new TownTemplate("Тулуза", merge(rect(-4,10,5,5), rect(-5,11,1,3), rect(1,10,3,4))),
            new TownTemplate("Бордо", merge(rect(-7,5,5,5), rect(-8,6,1,3), rect(-2,5,2,4))),
            new TownTemplate("Страсбург", merge(rect(7,0,4,5), rect(6,1,1,3))),
            new TownTemplate("Лилль", merge(rect(0,-5,5,5), rect(-1,-4,1,3), rect(5,-4,1,3))),
            new TownTemplate("Нант", merge(rect(-7,0,5,5), rect(-8,1,1,3), rect(-2,1,2,3)))
        )));

        // ЯПОНИЯ ~240
        TEMPLATES.put("japan", new NationTemplate("Японская Империя", NationColor.WHITE, Arrays.asList(
            new TownTemplate("Токио", merge(rect(0,0,4,6), rect(-1,1,1,4), rect(4,1,1,4))),
            new TownTemplate("Осака", merge(rect(-2,6,5,5), rect(-3,7,1,3), rect(3,7,1,3))),
            new TownTemplate("Нагоя", merge(rect(3,3,4,5), rect(2,4,1,3))),
            new TownTemplate("Саппоро", merge(rect(2,-8,5,6), rect(1,-7,1,4), rect(7,-7,1,4), rect(3,-2,3,2))),
            new TownTemplate("Фукуока", merge(rect(-5,11,5,5), rect(-6,12,1,3), rect(0,11,1,4))),
            new TownTemplate("Хиросима", merge(rect(-4,6,2,5), rect(-6,7,2,4))),
            new TownTemplate("Сэндай", merge(rect(0,-4,4,4), rect(-1,-3,1,2), rect(4,-3,1,2)))
        )));

        // UK ~250
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, Arrays.asList(
            new TownTemplate("Лондон", merge(rect(0,0,5,5), rect(-1,1,1,3), rect(5,1,2,3))),
            new TownTemplate("Бирмингем", merge(rect(-1,-5,6,5), rect(-2,-4,1,3), rect(5,-4,1,3))),
            new TownTemplate("Манчестер", merge(rect(-1,-10,5,5), rect(-2,-9,1,3), rect(4,-9,1,3))),
            new TownTemplate("Ливерпуль", merge(rect(-5,-10,4,5), rect(-6,-9,1,3))),
            new TownTemplate("Эдинбург", merge(rect(-2,-16,5,5), rect(-3,-15,1,3), rect(3,-15,1,3), rect(-1,-11,4,1))),
            new TownTemplate("Глазго", merge(rect(-6,-16,4,5), rect(-7,-15,1,3))),
            new TownTemplate("Бристоль", merge(rect(-4,-2,4,5), rect(-5,-1,1,3))),
            new TownTemplate("Кардифф", merge(rect(-6,0,5,4), rect(-7,1,1,2)))
        )));

        // РУМЫНИЯ ~200
        TEMPLATES.put("romania", new NationTemplate("Румыния", NationColor.YELLOW, Arrays.asList(
            new TownTemplate("Бухарест", merge(rect(0,0,5,5), rect(-1,1,1,3), rect(5,1,1,3))),
            new TownTemplate("Клуж-Напока", merge(rect(-6,-4,5,5), rect(-7,-3,1,3), rect(-1,-3,1,3))),
            new TownTemplate("Тимишоара", merge(rect(-8,1,5,5), rect(-9,2,1,3), rect(-3,2,2,3))),
            new TownTemplate("Яссы", merge(rect(3,-5,5,5), rect(2,-4,1,3), rect(8,-4,1,3), rect(3,0,3,1))),
            new TownTemplate("Констанца", merge(rect(5,2,4,5), rect(4,3,1,3), rect(5,0,3,2))),
            new TownTemplate("Брашов", merge(rect(-3,-1,3,5), rect(-4,0,1,3))),
            new TownTemplate("Крайова", merge(rect(-3,5,5,4), rect(-4,6,1,2), rect(2,5,1,3)))
        )));

        // ТУРЦИЯ ~340
        TEMPLATES.put("turkey", new NationTemplate("Турецкая Республика", NationColor.ORANGE, Arrays.asList(
            new TownTemplate("Анкара", merge(rect(0,0,6,5), rect(-1,1,1,3))),
            new TownTemplate("Стамбул", merge(rect(-8,0,7,5), rect(-9,1,1,3))),
            new TownTemplate("Измир", merge(rect(-9,5,5,5), rect(-10,6,1,3))),
            new TownTemplate("Анталья", merge(rect(-4,5,6,5), rect(-5,6,1,3))),
            new TownTemplate("Адана", merge(rect(2,5,6,5), rect(1,6,1,3))),
            new TownTemplate("Трабзон", merge(rect(6,0,6,5), rect(12,1,1,3))),
            new TownTemplate("Газиантеп", merge(rect(8,5,5,5), rect(7,6,1,3)))
        )));

        // БРАЗИЛИЯ ~400
        TEMPLATES.put("brazil", new NationTemplate("Федеративная Республика Бразилия", NationColor.GREEN, Arrays.asList(
            new TownTemplate("Бразилиа", merge(rect(0,0,6,5), rect(-1,1,1,3), rect(6,1,1,3))),
            new TownTemplate("Сан-Паулу", merge(rect(0,5,7,6), rect(-1,6,1,4), rect(7,6,1,4))),
            new TownTemplate("Рио-де-Жанейро", merge(rect(7,3,5,5), rect(6,4,1,3), rect(12,4,1,3))),
            new TownTemplate("Манаус", merge(rect(-8,-6,7,6), rect(-9,-5,1,4), rect(-1,-5,1,4))),
            new TownTemplate("Форталеза", merge(rect(4,-6,6,5), rect(3,-5,1,3), rect(10,-5,1,3))),
            new TownTemplate("Сальвадор", merge(rect(8,-1,5,5), rect(7,0,1,3), rect(13,0,1,3))),
            new TownTemplate("Белу-Оризонти", merge(rect(0,-3,4,3), rect(4,-3,1,2))),
            new TownTemplate("Куритиба", merge(rect(-3,5,3,5), rect(-4,6,1,3))),
            new TownTemplate("Порту-Алегри", merge(rect(-3,10,4,5), rect(-4,11,1,3), rect(1,10,2,4)))
        )));

        // ИНДИЯ ~380
        TEMPLATES.put("india", new NationTemplate("Республика Индия", NationColor.TEAL, Arrays.asList(
            new TownTemplate("Дели", merge(rect(0,0,6,5), rect(-1,1,1,3), rect(6,1,2,3))),
            new TownTemplate("Мумбаи", merge(rect(-5,5,5,6), rect(-6,6,1,4), rect(0,5,1,5))),
            new TownTemplate("Бангалор", merge(rect(0,10,5,6), rect(-1,11,1,4), rect(5,11,1,4))),
            new TownTemplate("Ченнаи", merge(rect(5,8,4,6), rect(4,9,1,4), rect(9,9,1,4))),
            new TownTemplate("Калькутта", merge(rect(8,0,5,5), rect(7,1,1,3), rect(13,1,1,3))),
            new TownTemplate("Хайдарабад", merge(rect(1,5,5,5), rect(0,6,1,3), rect(6,6,2,3))),
            new TownTemplate("Джайпур", merge(rect(-5,-2,5,5), rect(-6,-1,1,3), rect(0,-1,1,3))),
            new TownTemplate("Лакхнау", merge(rect(0,-4,5,4), rect(-1,-3,1,2), rect(5,-3,1,2)))
        )));
    }

    public static NationTemplate getTemplate(String name) {
        return TEMPLATES.get(name.toLowerCase());
    }

    public static Set<String> getAvailableTemplates() {
        return TEMPLATES.keySet();
    }
}
