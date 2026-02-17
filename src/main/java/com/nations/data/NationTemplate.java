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

    // === СИСТЕМА ОТРИСОВКИ ПО КАРТЕ ===
    
    private static List<TownTemplate> parseMap(String[] map, Map<Character, String> key) {
        Map<String, List<int[]>> tempMap = new HashMap<>();
        
        // Находим центр карты (примерно)
        int height = map.length;
        int width = 0;
        for(String s : map) if(s.length() > width) width = s.length();
        
        int centerX = width / 2;
        int centerZ = height / 2;

        for (int z = 0; z < height; z++) {
            String row = map[z];
            for (int x = 0; x < row.length(); x++) {
                char c = row.charAt(x);
                if (c == ' ' || c == '.') continue; // Пустота

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
        // =====================================================================
        // 1. РОССИЙСКАЯ ФЕДЕРАЦИЯ
        // =====================================================================
        Map<Character, String> rusKey = new HashMap<>();
        rusKey.put('M', "Москва");
        rusKey.put('S', "Санкт-Петербург");
        rusKey.put('K', "Казань");
        rusKey.put('N', "Нижний Новгород");
        rusKey.put('E', "Екатеринбург");
        rusKey.put('O', "Новосибирск"); // O - nOvosibirsk
        rusKey.put('R', "Красноярск");
        rusKey.put('V', "Владивосток");
        rusKey.put('D', "Краснодар"); // D - krasnoDar
        rusKey.put('Y', "Якутск");
        rusKey.put('U', "Мурманск");  // U - mUrmansk

        String[] rusMap = {
            "......UUUU....................................",
            "...SSSSUU.....................................",
            "..SSSSSNNNEEEEEOOOOO..........................",
            "..SSMMMNNNEEEEEOOOOORRRRRRYYYY................",
            ".DDDMMMKKKEEEEOOOOORRRRRRYYYYYY...............",
            ".DDDMMMKKK...OOOOORRRRRRYYYYYYYY..............",
            "..D............OOORRRRRRYYYYYYYY..............",
            "..........................VVVV................",
            "..........................VVVV................"
        };
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, parseMap(rusMap, rusKey)));

        // =====================================================================
        // 2. ГЕРМАНИЯ (Похожа на Германию, а не Испанию)
        // =====================================================================
        Map<Character, String> gerKey = new HashMap<>();
        gerKey.put('B', "Берлин");
        gerKey.put('H', "Гамбург");
        gerKey.put('M', "Мюнхен");
        gerKey.put('F', "Франкфурт");
        gerKey.put('K', "Кёльн");
        gerKey.put('S', "Штутгарт");
        gerKey.put('D', "Дрезден");

        String[] gerMap = {
            "...HHHBBB...",
            "..KHHHBBB...",
            ".KKFFDBBB...",
            ".KKFFDD.....",
            "..FFSS......",
            "..SSMMM.....",
            "...MMMM....."
        };
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, parseMap(gerMap, gerKey)));

        // =====================================================================
        // 3. ФРАНЦИЯ (Шестиугольник)
        // =====================================================================
        Map<Character, String> frKey = new HashMap<>();
        frKey.put('P', "Париж");
        frKey.put('L', "Лион");
        frKey.put('M', "Марсель");
        frKey.put('T', "Тулуза");
        frKey.put('B', "Бордо");
        frKey.put('S', "Страсбург");
        frKey.put('N', "Нант");

        String[] frMap = {
            "...NNNPPP.....",
            ".NNNNPPPPP.S..",
            ".NNBBPPPLLSS..",
            "..BBBBPLLLL...",
            "..BBBTTLLLL...",
            "...TTTTMMMM...",
            "....TTTMM....."
        };
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, parseMap(frMap, frKey)));

        // =====================================================================
        // 4. ВЕЛИКОБРИТАНИЯ (Вытянутая)
        // =====================================================================
        Map<Character, String> ukKey = new HashMap<>();
        ukKey.put('L', "Лондон");
        ukKey.put('B', "Бирмингем");
        ukKey.put('M', "Манчестер");
        ukKey.put('V', "Ливерпуль"); // liVerpool
        ukKey.put('E', "Эдинбург");
        ukKey.put('G', "Глазго");
        ukKey.put('R', "Бристоль"); // bRistol

        String[] ukMap = {
            "...E.....",
            "..GEE....",
            "..GGE....",
            "...M.....",
            "..VMM....",
            "..VBB....",
            ".RRBBL...",
            ".RRLLLL..",
            "..LLLL..."
        };
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, parseMap(ukMap, ukKey)));

        // =====================================================================
        // 5. США (Широкая + Флорида)
        // =====================================================================
        Map<Character, String> usaKey = new HashMap<>();
        usaKey.put('W', "Вашингтон");
        usaKey.put('N', "Нью-Йорк");
        usaKey.put('C', "Чикаго");
        usaKey.put('L', "Лос-Анджелес");
        usaKey.put('D', "Денвер");
        usaKey.put('H', "Хьюстон");
        usaKey.put('M', "Майами");
        usaKey.put('S', "Сиэтл");

        String[] usaMap = {
            "SS...CCCCCCNNN..",
            "SSDDCCCCCCNNNN..",
            "LLDDDWWWWNNNN...",
            "LLDDDWWWW.......",
            "LL...HHHHMMM....",
            ".....HHHHMMM....",
            ".........MM....."
        };
        TEMPLATES.put("usa", new NationTemplate("Соединённые Штаты Америки", NationColor.BLUE, parseMap(usaMap, usaKey)));

        // =====================================================================
        // 6. КИТАЙ (Огромный)
        // =====================================================================
        Map<Character, String> cnKey = new HashMap<>();
        cnKey.put('P', "Пекин");
        cnKey.put('S', "Шанхай");
        cnKey.put('G', "Гуанчжоу");
        cnKey.put('Z', "Шэньчжэнь"); // shenZhen
        cnKey.put('C', "Чэнду");
        cnKey.put('W', "Ухань");
        cnKey.put('H', "Харбин");
        cnKey.put('U', "Урумчи");

        String[] cnMap = {
            ".........HHHH...",
            "UUU......HHHH...",
            "UUU...PPPPPHH...",
            "UU....PPPPSS....",
            ".....CCWWSSS....",
            "....CCCWWSS.....",
            "....CCGGZZ......",
            ".....GGGZ......."
        };
        TEMPLATES.put("china", new NationTemplate("Китайская Народная Республика", NationColor.DARK_RED, parseMap(cnMap, cnKey)));

        // =====================================================================
        // 7. ЯПОНИЯ (Дуга)
        // =====================================================================
        Map<Character, String> jpKey = new HashMap<>();
        jpKey.put('T', "Токио");
        jpKey.put('O', "Осака");
        jpKey.put('N', "Нагоя");
        jpKey.put('S', "Саппоро");
        jpKey.put('F', "Фукуока");
        jpKey.put('H', "Хиросима");

        String[] jpMap = {
            "......SSSS.",
            ".....SSSS..",
            "....TTT....",
            "...NNTT....",
            "..OON......",
            ".HOO.......",
            "FHH........",
            "FF........."
        };
        TEMPLATES.put("japan", new NationTemplate("Японская Империя", NationColor.WHITE, parseMap(jpMap, jpKey)));

        // =====================================================================
        // 8. ТУРЦИЯ (Прямоугольник)
        // =====================================================================
        Map<Character, String> trKey = new HashMap<>();
        trKey.put('A', "Анкара");
        trKey.put('S', "Стамбул");
        trKey.put('I', "Измир");
        trKey.put('L', "Анталья"); // antaLya
        trKey.put('T', "Трабзон");
        trKey.put('G', "Газиантеп");

        String[] trMap = {
            ".SSSSTTTTT...",
            "SSAAATTTTTG..",
            "IIAAAAAAGGG..",
            "IILLLLLLGG...",
            "..LLLLL......"
        };
        TEMPLATES.put("turkey", new NationTemplate("Турецкая Республика", NationColor.ORANGE, parseMap(trMap, trKey)));

        // =====================================================================
        // 9. БРАЗИЛИЯ
        // =====================================================================
        Map<Character, String> brKey = new HashMap<>();
        brKey.put('B', "Бразилиа");
        brKey.put('S', "Сан-Паулу");
        brKey.put('R', "Рио-де-Жанейро");
        brKey.put('M', "Манаус");
        brKey.put('F', "Форталеза");
        brKey.put('V', "Сальвадор"); // salVador

        String[] brMap = {
            "MMMMMFFFFF...",
            "MMMMMBVVVV...",
            "..BBBBVVVV...",
            "..BBBBRRRR...",
            "...SSSRRR....",
            "...SSS......."
        };
        TEMPLATES.put("brazil", new NationTemplate("Федеративная Республика Бразилия", NationColor.GREEN, parseMap(brMap, brKey)));

        // =====================================================================
        // 10. ИНДИЯ (Треугольник)
        // =====================================================================
        Map<Character, String> inKey = new HashMap<>();
        inKey.put('D', "Дели");
        inKey.put('M', "Мумбаи");
        inKey.put('B', "Бангалор");
        inKey.put('C', "Ченнаи");
        inKey.put('K', "Калькутта");
        inKey.put('J', "Джайпур");

        String[] inMap = {
            "...JJDDDDKK..",
            "..JJJDDDDKKK.",
            ".MMMM...KKK..",
            ".MMMMBBCCC...",
            "..MMBBBCC....",
            "...BBBC......"
        };
        TEMPLATES.put("india", new NationTemplate("Республика Индия", NationColor.TEAL, parseMap(inMap, inKey)));

        // =====================================================================
        // 11. РУМЫНИЯ (Рыбка)
        // =====================================================================
        Map<Character, String> roKey = new HashMap<>();
        roKey.put('B', "Бухарест");
        roKey.put('C', "Клуж-Напока"); // Cluj
        roKey.put('T', "Тимишоара");
        roKey.put('I', "Яссы"); // Iasi
        roKey.put('O', "Констанца"); // cOnstanta

        String[] roMap = {
            "..CCCIII...",
            ".TCCCII....",
            ".TTTBBB....",
            "..BBBBBOO..",
            "....BBOOO.."
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
