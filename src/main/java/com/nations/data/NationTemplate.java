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
        int t = 0; for (TownTemplate tt : towns) t += tt.getChunkCount(); return t;
    }

    public static class TownTemplate {
        public final String name;
        public final List<int[]> chunks;
        public TownTemplate(String name, List<int[]> chunks) { this.name = name; this.chunks = chunks; }
        public int getChunkCount() { return chunks.size(); }
        public int[] getCenter() {
            if (chunks.isEmpty()) return new int[]{0, 0};
            long sx = 0, sz = 0;
            for (int[] c : chunks) { sx += c[0]; sz += c[1]; }
            return new int[]{(int)(sx / chunks.size()), (int)(sz / chunks.size())};
        }
    }

    private static List<TownTemplate> parseMap(String[] map, Map<Character, String> key) {
        Map<String, List<int[]>> tmp = new HashMap<>();
        int h = map.length, w = 0;
        for (String s : map) if (s.length() > w) w = s.length();
        int cx = w / 2, cz = h / 2;
        for (int z = 0; z < h; z++) {
            String row = map[z];
            for (int x = 0; x < row.length(); x++) {
                char c = row.charAt(x);
                if (c == ' ' || c == '.') continue;
                String city = key.get(c);
                if (city != null) tmp.computeIfAbsent(city, k -> new ArrayList<>()).add(new int[]{x - cx, z - cz});
            }
        }
        List<TownTemplate> res = new ArrayList<>();
        for (var e : tmp.entrySet()) res.add(new TownTemplate(e.getKey(), e.getValue()));
        return res;
    }

    private static final Map<String, NationTemplate> TEMPLATES = new HashMap<>();

    static {

        // =====================================================================
        // РОССИЯ ~950 чанков — 11 городов
        // Форма: массивный запад (Европа), очень широкая Сибирь,
        // Кольский п-ов вверху, Камчатка/Владивосток справа внизу
        // Ширина ~95, высота ~28
        // =====================================================================
        Map<Character, String> ru = new HashMap<>();
        ru.put('U', "Мурманск");
        ru.put('S', "Санкт-Петербург");
        ru.put('M', "Москва");
        ru.put('N', "Нижний Новгород");
        ru.put('D', "Краснодар");
        ru.put('K', "Казань");
        ru.put('E', "Екатеринбург");
        ru.put('O', "Новосибирск");
        ru.put('R', "Красноярск");
        ru.put('Y', "Якутск");
        ru.put('V', "Владивосток");

        String[] ruMap = {
            "..............UUUU..............................................................................................",
            ".............UUUUUU.............................................................................................",
            "............UUUUUU..............................................................................................",
            "........SSSSUUUUU...............................................................................................",
            ".......SSSSSSU..................................................................................................",
            "......SSSSSSM...................................................................................................",
            ".....SSSSMMMMNNNKKKKEEEEEEEOOOOOOOORRRRRRRRRRYYYYYYYYYYY......................................................",
            ".....SSSSMMMMNNNKKKKEEEEEEEOOOOOOOORRRRRRRRRRRYYYYYYYYYYYY....................................................",
            "....SSSSMMMMMNNNKKKKEEEEEEEOOOOOOOORRRRRRRRRRRRYYYYYYYYYYYYY..................................................",
            "....SSSMMMMMMNNNNKKKEEEEEEEOOOOOOOORRRRRRRRRRRRYYYYYYYYYYYYYYY................................................",
            "...DDDMMMMMMNNNNKKKKEEEEEEEOOOOOOOORRRRRRRRRRRRRYYYYYYYYYYYYYYYY..............................................",
            "...DDDMMMMMMNNNNKKKKEEEEEEEOOOOOOOORRRRRRRRRRRRRRYYYYYYYYYYYYYYYYY............................................",
            "...DDDDMMMMMNNNNKKKKEEEEEEEOOOOOOOORRRRRRRRRRRRRRYYYYYYYYYYYYYYYYYYY..........................................",
            "...DDDDDMMMMNNNNKKKKEEEEEEEOOOOOOOORRRRRRRRRRRRRRRYYYYYYYYYYYYYYYYYY......................VVV.................",
            "...DDDDDMMMMNNNNKKKKEEEEEEEOOOOOOOORRRRRRRRRRRRRRYYYYYYYYYYYYYYYYY......................VVVVV.................",
            "....DDDDMMMMMNNNNKKKEEEEEEEOOOOOOOORRRRRRRRRRRRRRRYYYYYYYYYYYYYYYY.....................VVVVVV.................",
            "....DDDDDMMMMNNNKKKKEEEEEEEOOOOOOOORRRRRRRRRRRRRRYYYYYYYYYYYYYYY......................VVVVV..................",
            ".....DDDDMMMMNNNKKKKEEEEEEEOOOOOOOORRRRRRRRRRRRRRYYYYYYYYYYYYY.......................VVVV....................",
            ".....DDDDDMMMNNNKKKKEEEEEEEOOOOOOORRRRRRRRRRRRRYYYYYYYYYYYYY..................................................",
            "......DDDDMMMNNNKKKKEEEEEEEOOOOOOORRRRRRRRRRRRRYYYYYYYYYYYY...................................................",
            ".......DDDMMMNNNKKKKEEEEEEEOOOOOORRRRRRRRRRRRRYYYYYYYYYYY....................................................",
            "........DDDMMNNNKKKKEEEEEEEOOOOOORRRRRRRRRRRRYYYYYYYYYY......................................................",
            ".........DDDMNNKKKKEEEEEEEOOOOOORRRRRRRRRRRRYYYYYYY..........................................................",
            "..........DDDNKKKKEEEEEEEOOOOOORRRRRRRRRRRYYYYY.............................................................",
            "...........DDKKKKEEEEEEEOOOOOORRRRRRRRRRYYYY.................................................................",
            "............DKKKEEEEEEOOOOOORRRRRRRRRYYYY....................................................................",
            ".............KKKEEEEEOOOOORRRRRRRRYYYY.......................................................................",
            "..............KKEEEEEOOOORRRRRRYYY...........................................................................",
        };
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, parseMap(ruMap, ru)));

        // =====================================================================
        // КИТАЙ ~560 чанков — 8 городов
        // Форма: массивный запад (Синьцзян/Тибет), восточное побережье,
        // Маньчжурия вверху справа
        // Ширина ~40, высота ~28
        // =====================================================================
        Map<Character, String> cn = new HashMap<>();
        cn.put('U', "Урумчи");
        cn.put('H', "Харбин");
        cn.put('P', "Пекин");
        cn.put('W', "Ухань");
        cn.put('S', "Шанхай");
        cn.put('C', "Чэнду");
        cn.put('G', "Гуанчжоу");
        cn.put('Z', "Шэньчжэнь");

        String[] cnMap = {
            "..........................HHHHHH..............",
            ".........................HHHHHHH..............",
            "........................HHHHHHHH..............",
            "UUUUUUUU..............PPPPPHHHHH..............",
            "UUUUUUUUUU...........PPPPPPPHHHH..............",
            "UUUUUUUUUUU.........PPPPPPPPHH................",
            "UUUUUUUUUUUU.......PPPPPPPPPP.................",
            "UUUUUUUUUUUUU.....PPPPPPPPPPP.................",
            "UUUUUUUUUUUUUU...PPPPPPPPSSSS.................",
            "UUUUUUUUUUUUUUCCPPPPPPPSSSSS..................",
            ".UUUUUUUUUUUUCCCCPPPPPSSSSSS..................",
            "..UUUUUUUUUUCCCCCCWWWWSSSSS...................",
            "...UUUUUUUUUCCCCCCWWWWWSSSS...................",
            "....UUUUUUUUCCCCCWWWWWWSSS....................",
            ".....UUUUUUUCCCCCCWWWWWWSS....................",
            "......UUUUUCCCCCCWWWWGGGSSS...................",
            ".......UUUUCCCCCWWWGGGGGZZ....................",
            "........UUUCCCCCCGGGGGZZZ.....................",
            ".........UUCCCCGGGGGZZZZ......................",
            "..........UCCCCGGGGGZZZ.......................",
            "...........CCCCGGGGZZZ........................",
            "............CCGGGGGZZ.........................",
            ".............CGGGGZZ..........................",
            "..............GGGGZ...........................",
        };
        TEMPLATES.put("china", new NationTemplate("Китайская Народная Республика", NationColor.DARK_RED, parseMap(cnMap, cn)));

        // =====================================================================
        // США ~550 чанков — 8 городов
        // Форма: широкий прямоугольник, северо-запад (Сиэтл),
        // юго-запад (ЛА), Флорида выступает вниз
        // Ширина ~40, высота ~25
        // =====================================================================
        Map<Character, String> us = new HashMap<>();
        us.put('T', "Сиэтл");
        us.put('L', "Лос-Анджелес");
        us.put('D', "Денвер");
        us.put('C', "Чикаго");
        us.put('N', "Нью-Йорк");
        us.put('W', "Вашингтон");
        us.put('H', "Хьюстон");
        us.put('A', "Майами");

        String[] usMap = {
            "TTTTT...........CCCCCCCCCNNNNN...............",
            "TTTTTT.........CCCCCCCCCCNNNNNN...............",
            "TTTTTTT.......CCCCCCCCCCCNNNNNNN..............",
            "TTTTTTDD.....CCCCCCCCCCCCNNNNNNNN.............",
            "TTTLLLDDDD..CCCCCCCCCCCWWWWNNNNN.............",
            "TTTLLLDDDDDDDCCCCCCCCWWWWWWWNNNNN............",
            "TTLLLLDDDDDDDDDCCCCWWWWWWWWWNNNN.............",
            "TLLLLLDDDDDDDDDDDCWWWWWWWWWWNNN..............",
            "LLLLLLDDDDDDDDDDDDWWWWWWWWWWNN...............",
            "LLLLLLDDDDDDDDDDDDDWWWWWWWWWN................",
            "LLLLLLDDDDDDDDDDDDDDWWWWWWWWN................",
            "LLLLLLDDDDDDDDDDDDDDDWWWWWWW.................",
            ".LLLLLDDDDDDDDDDDDDDDDWWWWWW................",
            ".LLLLLDDDDDDDDDDDDHHHHHWWWWW.................",
            "..LLLLDDDDDDDDDDDHHHHHHWWWW..................",
            "...LLLDDDDDDDDDDHHHHHHHWWW...................",
            "....LLDDDDDDDDDDHHHHHHWWW....................",
            ".....LDDDDDDDDDDHHHHHH.......................",
            "......DDDDDDDDDHHHHH.........................",
            "..............HHHHHAAA........................",
            "..............HHHHHAAAA.......................",
            "...............HHHHAAAA.......................",
            "................HHHAAAA.......................",
            ".................HHAAA........................",
            "..................HAAA........................",
            "...................AA.........................",
        };
        TEMPLATES.put("usa", new NationTemplate("Соединённые Штаты Америки", NationColor.BLUE, parseMap(usMap, us)));

        // =====================================================================
        // БРАЗИЛИЯ ~500 чанков — 6 городов
        // Форма: массивный выступ на северо-востоке,
        // сужается к югу (Сан-Паулу)
        // Ширина ~35, высота ~28
        // =====================================================================
        Map<Character, String> br = new HashMap<>();
        br.put('M', "Манаус");
        br.put('F', "Форталеза");
        br.put('V', "Сальвадор");
        br.put('B', "Бразилиа");
        br.put('R', "Рио-де-Жанейро");
        br.put('P', "Сан-Паулу");

        String[] brMap = {
            "......MMMMMMMMFFFFFF......................",
            ".....MMMMMMMMMFFFFFFF.....................",
            "....MMMMMMMMMMFFFFFFFF....................",
            "...MMMMMMMMMMMFFFFFFFFF...................",
            "...MMMMMMMMMMMBBBVVVVVVVV.................",
            "....MMMMMMMMMBBBBVVVVVVVV.................",
            ".....MMMMMMMMBBBBVVVVVVVV.................",
            "......MMMMMMBBBBBVVVVVVV..................",
            ".......MMMMMBBBBBBVVVVVV..................",
            "........MMMMBBBBBBVVVVV...................",
            ".........MMMBBBBBBVVVVV...................",
            "..........MMBBBBBBBVVVV...................",
            "...........MBBBBBBBRRRRR..................",
            "............BBBBBBBRRRRR..................",
            ".............BBBBBBRRRRR..................",
            "..............BBBBBRRRRR..................",
            "...............BBBBBRRRR..................",
            "................BBBPPRRR..................",
            ".................BPPPPRRR.................",
            "..................PPPPPRRR................",
            "...................PPPPPRR................",
            "....................PPPPP.................",
            ".....................PPPP.................",
            "......................PPP.................",
            ".......................PP.................",
        };
        TEMPLATES.put("brazil", new NationTemplate("Федеративная Республика Бразилия", NationColor.GREEN, parseMap(brMap, br)));

        // =====================================================================
        // ИНДИЯ ~300 чанков — 6 городов
        // Форма: широкий север, классический треугольник на юг
        // Ширина ~25, высота ~24
        // =====================================================================
        Map<Character, String> in = new HashMap<>();
        in.put('J', "Джайпур");
        in.put('D', "Дели");
        in.put('K', "Калькутта");
        in.put('M', "Мумбаи");
        in.put('B', "Бангалор");
        in.put('C', "Ченнаи");

        String[] inMap = {
            "....JJJJJDDDDDDDD...............",
            "...JJJJJJDDDDDDDDKK.............",
            "...JJJJJJDDDDDDDDDKKK...........",
            "..JJJJJJJDDDDDDDDDDKKKK.........",
            "..JJJJJJJDDDDDDDDDDKKKKK........",
            ".JJJJJJJDDDDDDDDDDDKKKKK........",
            ".MMMMMMMDDDDDDDDDDDKKKKKK........",
            ".MMMMMMMMMDDDDDDDDKKKKKKK........",
            ".MMMMMMMMMMDDDDDDKKKKKK..........",
            "..MMMMMMMMMMMDDDKKKKKKK..........",
            "..MMMMMMMMMMMBBBBCCCCC...........",
            "...MMMMMMMMMBBBBBCCCCC...........",
            "...MMMMMMMMMBBBBCCCCC............",
            "....MMMMMMMBBBBBCCCC.............",
            ".....MMMMMMBBBBCCCC..............",
            "......MMMMBBBBBCCC...............",
            ".......MMMBBBBBCC................",
            "........MMBBBBCC.................",
            ".........BBBBBCC.................",
            "..........BBBBC..................",
            "...........BBBC..................",
            "............BBB..................",
            ".............BB..................",
            "..............B..................",
        };
        TEMPLATES.put("india", new NationTemplate("Республика Индия", NationColor.TEAL, parseMap(inMap, in)));

        // =====================================================================
        // ТУРЦИЯ ~200 чанков — 6 городов
        // Форма: длинный анатолийский полуостров,
        // Стамбул/пролив слева, восток массивнее
        // Ширина ~35, высота ~12
        // =====================================================================
        Map<Character, String> tr = new HashMap<>();
        tr.put('S', "Стамбул");
        tr.put('A', "Анкара");
        tr.put('I', "Измир");
        tr.put('L', "Анталья");
        tr.put('T', "Трабзон");
        tr.put('G', "Газиантеп");

        String[] trMap = {
            "...SSSAAAAAAAAATTTTTTTTTT...........",
            "..SSSSAAAAAAAAATTTTTTTTTTT..........",
            ".SSSSSAAAAAAAAATTTTTTTTTTTTGG.......",
            "SSSIIIAAAAAAAAATTTTTTTTTTTTGGG......",
            "SSIIIIAAAAAAAAATTTTTTTTTTTTGGGG.....",
            "SIIIIIAAAAAAAAATTTTTTTTTTTGGGGG.....",
            ".IIIILLLLAAAAAATTTTTTTTTTTGGGG......",
            "..IILLLLLLAAAAATTTTTTTTTTGGGG.......",
            "...LLLLLLLLAAAA.TTTTTTTTGGG.........",
            "....LLLLLLLLAA...TTTTTGGG...........",
            ".....LLLLLLL.............................",
            "......LLLLL..............................",
        };
        TEMPLATES.put("turkey", new NationTemplate("Турецкая Республика", NationColor.ORANGE, parseMap(trMap, tr)));

        // =====================================================================
        // ФРАНЦИЯ ~180 чанков — 7 городов
        // Форма: L'Hexagone — классический шестиугольник
        // Ширина ~18, высота ~18
        // =====================================================================
        Map<Character, String> fr = new HashMap<>();
        fr.put('N', "Нант");
        fr.put('P', "Париж");
        fr.put('S', "Страсбург");
        fr.put('B', "Бордо");
        fr.put('L', "Лион");
        fr.put('T', "Тулуза");
        fr.put('A', "Марсель");

        String[] frMap = {
            ".....NNNNPPPPPP..........",
            "....NNNNNPPPPPPP.........",
            "...NNNNNNPPPPPPPPSS......",
            "...NNNNNPPPPPPPPPSSS.....",
            "...NNNNBPPPPPPPLLSSS.....",
            "...NNNBBPPPPPPPLLLSS.....",
            "....NBBBBPPPPPLLLLS......",
            "....BBBBBPPPPLLLLLL......",
            "....BBBBBBPPLLLLLL.......",
            ".....BBBBBTTLLLLL........",
            ".....BBBBTTTTLLLL........",
            "......BBTTTTTAAAA........",
            "......TTTTTTAAAAA........",
            ".......TTTTTAAAAA........",
            "........TTTTTAAAA........",
            ".........TTTAAAA.........",
            "..........TTAAA..........",
        };
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, parseMap(frMap, fr)));

        // =====================================================================
        // ЯПОНИЯ ~150 чанков — 6 городов
        // Форма: дуга островов сверху вниз
        // Хоккайдо (Саппоро) — отдельный остров вверху
        // Хонсю — большой изогнутый (Токио, Нагоя, Осака)
        // Сикоку/Кюсю (Хиросима, Фукуока) внизу
        // Ширина ~18, высота ~30
        // =====================================================================
        Map<Character, String> jp = new HashMap<>();
        jp.put('R', "Саппоро");
        jp.put('T', "Токио");
        jp.put('N', "Нагоя");
        jp.put('O', "Осака");
        jp.put('H', "Хиросима");
        jp.put('F', "Фукуока");

        String[] jpMap = {
            "...........RRRR..........",
            "..........RRRRR..........",
            "..........RRRRRR.........",
            "..........RRRRR..........",
            ".........RRRRR...........",
            ".........RRRR............",
            "..........RR.............",
            "..........TT.............",
            ".........TTT.............",
            ".........TTTT............",
            "........TTTTT............",
            "........TTTTT............",
            ".......TTTTTT............",
            ".......TTTTT.............",
            "......NNTTT..............",
            "......NNNT...............",
            ".....NNNN................",
            ".....NNN.................",
            "....OONN.................",
            "....OOON.................",
            "...OOOO..................",
            "...OOOO..................",
            "..HOOO...................",
            "..HHO....................",
            ".HHH.....................",
            ".HHH.....................",
            "FHH......................",
            "FFF......................",
            "FFF......................",
            "FF.......................",
        };
        TEMPLATES.put("japan", new NationTemplate("Японская Империя", NationColor.WHITE, parseMap(jpMap, jp)));

        // =====================================================================
        // ГЕРМАНИЯ ~140 чанков — 7 городов
        // Форма: широкий север (побережье Балтики/Северного моря),
        // сужается к югу (Бавария)
        // Ширина ~16, высота ~16
        // =====================================================================
        Map<Character, String> de = new HashMap<>();
        de.put('H', "Гамбург");
        de.put('B', "Берлин");
        de.put('K', "Кёльн");
        de.put('F', "Франкфурт");
        de.put('D', "Дрезден");
        de.put('S', "Штутгарт");
        de.put('Q', "Мюнхен");

        String[] deMap = {
            "....HHHHHBBBBBB..........",
            "...HHHHHHBBBBBBB.........",
            "...HHHHHHHBBBBBB.........",
            "..HHHHHHHHBBBBBBD........",
            "..KKKHHHHFFDDDDDD.......",
            ".KKKKHHHHFFFDDDDD........",
            ".KKKKKFFFFFFFF...........",
            "..KKKKFFFFFFFS..........",
            "...KKKFFFFFFSS..........",
            "....KKFFFFFSSSS.........",
            ".....FFFFFSSSSS.........",
            "......FFFSSSSSS.........",
            "......SSSSSQQQQQ........",
            ".......SSSSQQQQQ........",
            "........SQQQQQQQ........",
            ".........QQQQQ..........",
        };
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, parseMap(deMap, de)));

        // =====================================================================
        // ВЕЛИКОБРИТАНИЯ ~110 чанков — 7 городов
        // Форма: Шотландия узкая вверху, Англия шире внизу,
        // Уэльс слева
        // Ширина ~10, высота ~22
        // =====================================================================
        Map<Character, String> uk = new HashMap<>();
        uk.put('E', "Эдинбург");
        uk.put('G', "Глазго");
        uk.put('M', "Манчестер");
        uk.put('W', "Ливерпуль");
        uk.put('B', "Бирмингем");
        uk.put('R', "Бристоль");
        uk.put('L', "Лондон");

        String[] ukMap = {
            ".....EEE.................",
            "....GEEE.................",
            "....GGEE.................",
            "....GGEE.................",
            "....GGGE.................",
            ".....GMM.................",
            "....WMMM.................",
            "....WWMM.................",
            "....WWMM.................",
            "....WWBB.................",
            "....WWBB.................",
            "....RRBB.................",
            "....RRBBB................",
            "....RRBBL................",
            "....RRLLL................",
            ".....RLLLL...............",
            ".....LLLLL...............",
            ".....LLLLL...............",
            "......LLLL...............",
            "......LLLL...............",
            "......LLL................",
            ".......LL................",
        };
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, parseMap(ukMap, uk)));

        // =====================================================================
        // РУМЫНИЯ ~100 чанков — 5 городов
        // Форма: овал, Карпаты по центру дугой,
        // побережье Чёрного моря справа
        // Ширина ~16, высота ~12
        // =====================================================================
        Map<Character, String> ro = new HashMap<>();
        ro.put('C', "Клуж-Напока");
        ro.put('I', "Яссы");
        ro.put('T', "Тимишоара");
        ro.put('B', "Бухарест");
        ro.put('O', "Констанца");

        String[] roMap = {
            "....CCCCCCIIIIIII.........",
            "...CCCCCCCIIIIIII.........",
            "..TTCCCCCCIIIIII..........",
            "..TTTCCCCCIIIII...........",
            "..TTTTCCCCBBBIII..........",
            "..TTTTTCBBBBBBII..........",
            "..TTTTBBBBBBBBOO..........",
            "...TTTBBBBBBBOOOO.........",
            "....TTBBBBBBOOOO..........",
            ".....TBBBBBBOOO...........",
            "......BBBBBOOO............",
            ".......BBBOOO.............",
        };
        TEMPLATES.put("romania", new NationTemplate("Румыния", NationColor.YELLOW, parseMap(roMap, ro)));
    }

    public static NationTemplate getTemplate(String name) {
        return TEMPLATES.get(name.toLowerCase());
    }

    public static Set<String> getAvailableTemplates() {
        return TEMPLATES.keySet();
    }
}
