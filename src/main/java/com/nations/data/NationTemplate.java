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
            long sx = 0, sz = 0;
            for (int[] c : chunks) { sx += c[0]; sz += c[1]; }
            return new int[]{(int)(sx / chunks.size()), (int)(sz / chunks.size())};
        }
    }

    private static List<TownTemplate> parseMap(String[] map, Map<Character, String> key) {
        Map<String, List<int[]>> tempMap = new HashMap<>();

        int height = map.length;
        int width = 0;
        for (String s : map) if (s.length() > width) width = s.length();

        int centerX = width / 2;
        int centerZ = height / 2;

        for (int z = 0; z < height; z++) {
            String row = map[z];
            for (int x = 0; x < row.length(); x++) {
                char c = row.charAt(x);
                if (c == ' ' || c == '.') continue;
                String cityName = key.get(c);
                if (cityName != null) {
                    tempMap.computeIfAbsent(cityName, k -> new ArrayList<>())
                           .add(new int[]{x - centerX, z - centerZ});
                }
            }
        }

        List<TownTemplate> result = new ArrayList<>();
        for (Map.Entry<String, List<int[]>> entry : tempMap.entrySet()) {
            result.add(new TownTemplate(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private static final Map<String, NationTemplate> TEMPLATES = new HashMap<>();

    static {
        // =================================================================
        // 1. РОССИЯ (~450 чанков) — самая большая
        // Форма: широкая на востоке, сужается к западу,
        // выступ Кольского п-ова, Камчатка, Сахалин
        // =================================================================
        Map<Character, String> rusKey = new HashMap<>();
        rusKey.put('U', "Мурманск");
        rusKey.put('S', "Санкт-Петербург");
        rusKey.put('M', "Москва");
        rusKey.put('N', "Нижний Новгород");
        rusKey.put('D', "Краснодар");
        rusKey.put('K', "Казань");
        rusKey.put('E', "Екатеринбург");
        rusKey.put('O', "Новосибирск");
        rusKey.put('R', "Красноярск");
        rusKey.put('Y', "Якутск");
        rusKey.put('V', "Владивосток");

        String[] rusMap = {
            "..........UU............................................................",
            ".........UUU............................................................",
            "......SSSUU.............................................................",
            "......SSSS..............................................................",
            ".....SSMMMNN...KKKEEEEEOOOOOORRRRRRRRRYYYYYYYYY.........................",
            ".....SSMMMMNNNNKKKEEEEEOOOOOORRRRRRRRRYYYYYYYYYYY.......................",
            "....DDMMMMMNNNNKKKKEEEEOOOOOORRRRRRRRRRYYYYYYYYYY.......................",
            "....DDDMMMMNNNNKKKKEEEEOOOOOORRRRRRRRRRYYYYYYYYY...........VV...........",
            "....DDDMMMMNNNKKKKEEEEEOOOOORRRRRRRRRYYYYYYYYYYY..........VVV...........",
            ".....DDMMMMNNNNKKKEEEEEOOOOOORRRRRRRRRYYYYYYYYYYY.........VVV...........",
            ".....DDDMMMNNNKKKEEEEEOOOOORRRRRRRRRRRYYYYYYYYY...........VV............",
            "......DDDMMNNNKKKEEEEOOOOOORRRRRRRRRRRYYYYYYYYY.........................",
            "........DDDNNKKKEEEEEOOOOOORRRRRRRRRYYYYYYYYY...........................",
            "..........DKKKKEEEEOOOOORRRRRRRRRYYYYYYY................................",
            "...........KKKEEEEOOOORRRRRRRYYYYY......................................",
            "............KKEEEOOOORRRRRRYYY..........................................",
            ".............KEEOOOORRRRYYY.............................................",
            "..............EOOOORRRRYY...............................................",
        };
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, parseMap(rusMap, rusKey)));

        // =================================================================
        // 2. КИТАЙ (~280 чанков) — сплошная территория
        // Форма: широкий запад (Синьцзян/Тибет), сужается к востоку
        // =================================================================
        Map<Character, String> cnKey = new HashMap<>();
        cnKey.put('U', "Урумчи");
        cnKey.put('H', "Харбин");
        cnKey.put('P', "Пекин");
        cnKey.put('W', "Ухань");
        cnKey.put('S', "Шанхай");
        cnKey.put('C', "Чэнду");
        cnKey.put('G', "Гуанчжоу");
        cnKey.put('Z', "Шэньчжэнь");

        String[] cnMap = {
            "..............HHHHHH............",
            "..............HHHHHHH...........",
            "UUUUUU.....PPPPPHHHH...........",
            "UUUUUUU....PPPPPPHH............",
            "UUUUUUUU..PPPPPPPP.............",
            "UUUUUUUUCCPPPPSSSS..............",
            ".UUUUUUUCCCCWWWSSS..............",
            "..UUUUUCCCCCWWWSSSS.............",
            "...UUUUCCCCWWWWSSS..............",
            "....UUUCCCCWWGGGSS..............",
            ".....UUCCCGGGGZZZ...............",
            "......CCCCGGGZZZ................",
            ".......CCGGGGZZ.................",
            "........GGGGZZ..................",
        };
        TEMPLATES.put("china", new NationTemplate("Китайская Народная Республика", NationColor.DARK_RED, parseMap(cnMap, cnKey)));

        // =================================================================
        // 3. США (~270 чанков)
        // Форма: широкий прямоугольник, Флорида внизу справа
        // =================================================================
        Map<Character, String> usaKey = new HashMap<>();
        usaKey.put('S', "Сиэтл");
        usaKey.put('L', "Лос-Анджелес");
        usaKey.put('D', "Денвер");
        usaKey.put('C', "Чикаго");
        usaKey.put('N', "Нью-Йорк");
        usaKey.put('W', "Вашингтон");
        usaKey.put('H', "Хьюстон");
        usaKey.put('A', "Майами");

        String[] usaMap = {
            "SSSS.......CCCCCCCNNNN..........",
            "SSSSS.....CCCCCCCCNNNNN.........",
            "SSSSDD...CCCCCCCCNNNNNN.........",
            "SSLLDDDDDCCCCCCWWWWNNNN.........",
            "SLLLDDDDDDDCCWWWWWWNNN.........",
            "LLLLDDDDDDDDWWWWWWWNN..........",
            "LLLLDDDDDDDDWWWWWWNN...........",
            "LLLLDDDDDDHHHWWWWWN.............",
            ".LLLDDDDDHHHHWWWWW..............",
            "..LLDDDDDHHHHHWWW...............",
            "...LDDDDDHHHHHW.................",
            "....DDDDDHHHHH..................",
            ".....DDDDHHHH...................",
            "..........HHAAA.................",
            "...........HAAA.................",
            "............AAA.................",
            "............AA..................",
        };
        TEMPLATES.put("usa", new NationTemplate("Соединённые Штаты Америки", NationColor.BLUE, parseMap(usaMap, usaKey)));

        // =================================================================
        // 4. БРАЗИЛИЯ (~250 чанков)
        // Форма: выпуклая справа вверху, сужается внизу
        // =================================================================
        Map<Character, String> brKey = new HashMap<>();
        brKey.put('M', "Манаус");
        brKey.put('F', "Форталеза");
        brKey.put('V', "Сальвадор");
        brKey.put('B', "Бразилиа");
        brKey.put('R', "Рио-де-Жанейро");
        brKey.put('P', "Сан-Паулу");

        String[] brMap = {
            ".....MMMMMMFFFFF...........",
            "....MMMMMMMFFFFFF..........",
            "...MMMMMMMMFFFFFFF.........",
            "...MMMMMMBBBVVVVVV.........",
            "....MMMMMBBBBVVVVVV........",
            ".....MMMBBBBBVVVVV.........",
            "......MBBBBBBVVVV..........",
            ".......BBBBBBRRRR..........",
            "........BBBBBRRR...........",
            ".........BBBPRRR...........",
            "..........PPPPPR...........",
            "...........PPPP............",
            "............PPP............",
            ".............PP............",
        };
        TEMPLATES.put("brazil", new NationTemplate("Федеративная Республика Бразилия", NationColor.GREEN, parseMap(brMap, brKey)));

        // =================================================================
        // 5. ИНДИЯ (~160 чанков)
        // Форма: треугольник сужается к югу
        // =================================================================
        Map<Character, String> inKey = new HashMap<>();
        inKey.put('J', "Джайпур");
        inKey.put('D', "Дели");
        inKey.put('K', "Калькутта");
        inKey.put('M', "Мумбаи");
        inKey.put('B', "Бангалор");
        inKey.put('C', "Ченнаи");

        String[] inMap = {
            "....JJJDDDDDD...........",
            "...JJJJDDDDDDKK.........",
            "...JJJJDDDDDDKKK........",
            "..JJJJDDDDDDDKKK........",
            "..MMMMMDDDDDKKKK........",
            "..MMMMMMDDDKKKK.........",
            "..MMMMMMBBCCCCC..........",
            "...MMMMMBBCCCC..........",
            "....MMMBBBBCCC..........",
            ".....MMBBBCCC...........",
            "......BBBBCC............",
            ".......BBBC.............",
            "........BB..............",
            ".........B..............",
        };
        TEMPLATES.put("india", new NationTemplate("Республика Индия", NationColor.TEAL, parseMap(inMap, inKey)));

        // =================================================================
        // 6. ТУРЦИЯ (~100 чанков)
        // Форма: длинный прямоугольник, Стамбул слева
        // =================================================================
        Map<Character, String> trKey = new HashMap<>();
        trKey.put('S', "Стамбул");
        trKey.put('A', "Анкара");
        trKey.put('I', "Измир");
        trKey.put('L', "Анталья");
        trKey.put('T', "Трабзон");
        trKey.put('G', "Газиантеп");

        String[] trMap = {
            "..SSSAAAAAATTTTTT.......",
            ".SSSSAAAAAATTTTTTT......",
            "SSIIAAAAAAAATTTTTTGG....",
            "SSIIAAAAAAAAATTTTTGGG...",
            ".IIILLLAAAAATTTTTGGG....",
            "..IILLLLLAAATTTGGGG.....",
            "...LLLLLLAAATTGGG.......",
            "....LLLLL...............",
        };
        TEMPLATES.put("turkey", new NationTemplate("Турецкая Республика", NationColor.ORANGE, parseMap(trMap, trKey)));

        // =================================================================
        // 7. ФРАНЦИЯ (~90 чанков)
        // Форма: шестиугольник (Hexagone)
        // =================================================================
        Map<Character, String> frKey = new HashMap<>();
        frKey.put('N', "Нант");
        frKey.put('P', "Париж");
        frKey.put('S', "Страсбург");
        frKey.put('B', "Бордо");
        frKey.put('L', "Лион");
        frKey.put('T', "Тулуза");
        frKey.put('M', "Марсель");

        String[] frMap = {
            "...NNNPPPPP..........",
            "..NNNNPPPPPSS........",
            "..NNNPPPPPLSS........",
            "..NNBBPPPLLL.........",
            "...BBBBPLLLL.........",
            "...BBBTTLLLL.........",
            "....BTTTTMMM.........",
            "....TTTTMMMMM........",
            ".....TTTMMMM.........",
        };
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, parseMap(frMap, frKey)));

        // =================================================================
        // 8. ЯПОНИЯ (~70 чанков)
        // Форма: дуга из 4 островов, Хоккайдо вверху, Кюсю внизу
        // =================================================================
        Map<Character, String> jpKey = new HashMap<>();
        jpKey.put('R', "Саппоро");
        jpKey.put('T', "Токио");
        jpKey.put('N', "Нагоя");
        jpKey.put('O', "Осака");
        jpKey.put('H', "Хиросима");
        jpKey.put('F', "Фукуока");

        String[] jpMap = {
            "........RRRR.........",
            ".......RRRRR.........",
            "........RRR..........",
            "..........T..........",
            ".........TT..........",
            "........TTTT.........",
            "........TTTT.........",
            ".......NNTT..........",
            "......NNN............",
            ".....OONN............",
            "....OOOO.............",
            "...HOO...............",
            "..HHH................",
            ".FHH.................",
            "FFF..................",
            "FF...................",
        };
        TEMPLATES.put("japan", new NationTemplate("Японская Империя", NationColor.WHITE, parseMap(jpMap, jpKey)));

        // =================================================================
        // 9. ГЕРМАНИЯ (~65 чанков)
        // Форма: широкий север, сужается к югу
        // =================================================================
        Map<Character, String> gerKey = new HashMap<>();
        gerKey.put('H', "Гамбург");
        gerKey.put('B', "Берлин");
        gerKey.put('K', "Кёльн");
        gerKey.put('F', "Франкфурт");
        gerKey.put('D', "Дрезден");
        gerKey.put('S', "Штутгарт");
        gerKey.put('M', "Мюнхен");

        String[] gerMap = {
            "...HHHHHBBBB.........",
            "..HHHHHHBBBBB........",
            "..HHHHHBBBBBD........",
            ".KKKHHFFDDDDD........",
            ".KKKFFFFDDD..........",
            "..KKFFFSSS...........",
            "...FFFSSSSS..........",
            "...SSSSSMMM..........",
            "....SSMMMM...........",
            ".....MMMM............",
        };
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, parseMap(gerMap, gerKey)));

        // =================================================================
        // 10. ВЕЛИКОБРИТАНИЯ (~55 чанков)
        // Форма: вытянутая, Шотландия наверху, Англия внизу шире
        // =================================================================
        Map<Character, String> ukKey = new HashMap<>();
        ukKey.put('E', "Эдинбург");
        ukKey.put('G', "Глазго");
        ukKey.put('M', "Манчестер");
        ukKey.put('V', "Ливерпуль");
        ukKey.put('B', "Бирмингем");
        ukKey.put('R', "Бристоль");
        ukKey.put('L', "Лондон");

        String[] ukMap = {
            "...EE................",
            "..GEE................",
            "..GGE................",
            "..GGE................",
            "...MM................",
            "..VMM................",
            "..VVMM...............",
            "..VVBB...............",
            "..RRBB...............",
            "..RRBBL..............",
            "..RRLLL..............",
            "...LLLL..............",
            "...LLLL..............",
            "....LL...............",
        };
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, parseMap(ukMap, ukKey)));

        // =================================================================
        // 11. РУМЫНИЯ (~50 чанков)
        // Форма: овал с Карпатами в центре
        // =================================================================
        Map<Character, String> roKey = new HashMap<>();
        roKey.put('C', "Клуж-Напока");
        roKey.put('I', "Яссы");
        roKey.put('T', "Тимишоара");
        roKey.put('B', "Бухарест");
        roKey.put('O', "Констанца");

        String[] roMap = {
            "...CCCCIIIII.........",
            "..TCCCCIIIII.........",
            "..TTCCCIIII..........",
            "..TTTCBBBBI..........",
            "..TTBBBBBBOO.........",
            "...TBBBBBOOO.........",
            "....BBBBOOO..........",
            ".....BBOOO...........",
        };
        TEMPLATES.put("romania", new NationTemplate("Румыния", NationColor.YELLOW, parseMap(roMap, roKey)));
    }

    public static NationTemplate getTemplate(String name) {
        return TEMPLATES.get(name.toLowerCase());
    }

    public static Set<String> getAvailableTemplates() {
        return TEMPLATES.keySet();
    }
}
