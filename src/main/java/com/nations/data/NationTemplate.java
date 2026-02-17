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
        // =================================================================
        // РОССИЯ ~950 чанков, 11 городов
        // Контур: Кольский п-ов сверху, Европейская часть слева (трапеция),
        // Сибирь расширяется к середине, Дальний Восток справа,
        // Юг скошен, Владивосток примыкает к Якутску
        // =================================================================
        Map<Character, String> ru = new HashMap<>();
        ru.put('U', "Мурманск"); ru.put('S', "Санкт-Петербург"); ru.put('M', "Москва");
        ru.put('N', "Нижний Новгород"); ru.put('D', "Краснодар"); ru.put('K', "Казань");
        ru.put('E', "Екатеринбург"); ru.put('O', "Новосибирск"); ru.put('R', "Красноярск");
        ru.put('Y', "Якутск"); ru.put('V', "Владивосток");

        String[] ruMap = {
            "..............UUUU..............................................................................",//0
            "............UUUUUU..............................................................................",//1
            "...........UUUUUU...............................................................................",//2
            ".........SSSUUUUU...............................................................................",//3
            "........SSSSSU..................................................................................",//4
            ".......SSSSSM...................................................................................",//5
            "......SSSMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRYYYYYYYYY.........................................",//6
            "......SSSMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRYYYYYYYYYY.......................................",//7
            ".....SSSMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRYYYYYYYYYYY.....................................",//8
            ".....SSSMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRYYYYYYYYYYYY...................................",//9
            "....DDMMMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRRYYYYYYYYYYYYY.................................",//10
            "....DDDMMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRRRYYYYYYYYYYYY.................................",//11
            "....DDDMMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRRRRYYYYYYYYYYY.................................",//12
            "....DDDDMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRRRRYYYYYYYYYYYVVV..............................",//13
            "....DDDDMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRRRRYYYYYYYYYVVVVV..............................",//14
            ".....DDDMMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRRRRYYYYYYYVVVVVV...............................",//15
            ".....DDDDMMMMNNNKKKKEEEEEEOOOOOOOORRRRRRRRRRRRRRRYYYYYVVVVVV.................................",//16
            "......DDDMMMMNNNKKKKEEEEEEOOOOOOORRRRRRRRRRRRRRRYYYYYYVVVVV..................................",//17
            "......DDDDMMMNNNKKKKEEEEEEOOOOOORRRRRRRRRRRRRRRYYYYYYY.......................................",//18
            ".......DDDMMMNNNKKKKEEEEEEOOOOOORRRRRRRRRRRRRRYYYYYY.........................................",//19
            "........DDMMMNNNKKKKEEEEEEOOOOOORRRRRRRRRRRRRYYYYY...........................................",//20
            ".........DMMNNKKKKEEEEEEOOOOOORRRRRRRRRRRRRYYY...............................................",//21
            "..........DMNKKKKEEEEEEOOOOOORRRRRRRRRRRYYY..................................................",//22
            "...........DKKKKEEEEEEOOOOORRRRRRRRRRYYY.....................................................",//23
            "............KKKKEEEEEOOOORRRRRRRRYYY.........................................................",//24
            ".............KKKEEEEEOOORRRRRRRYYY...........................................................",//25
            "..............KKEEEEOORRRRRRYY...............................................................",//26
        };
        TEMPLATES.put("russia", new NationTemplate("Российская Федерация", NationColor.RED, parseMap(ruMap, ru)));

        // =================================================================
        // КИТАЙ ~560 чанков, 8 городов
        // Контур: Синьцзян/Тибет широкие слева, Маньчжурия вверху,
        // побережье справа, Гуанчжоу/Шэньчжэнь на юге
        // =================================================================
        Map<Character, String> cn = new HashMap<>();
        cn.put('U', "Урумчи"); cn.put('H', "Харбин"); cn.put('P', "Пекин");
        cn.put('W', "Ухань"); cn.put('S', "Шанхай"); cn.put('C', "Чэнду");
        cn.put('G', "Гуанчжоу"); cn.put('Z', "Шэньчжэнь");

        String[] cnMap = {
            "........................HHHHH........",//0
            ".......................HHHHHH........",//1
            "......................HHHHHHH........",//2
            "UUUUUUUU............PPPPPHHHH........",//3
            "UUUUUUUUUU.........PPPPPPHHHH........",//4
            "UUUUUUUUUUU.......PPPPPPPHH..........",//5
            "UUUUUUUUUUUU.....PPPPPPPPP...........",//6
            "UUUUUUUUUUUUUU..PPPPPPPPPP...........",//7
            "UUUUUUUUUUUUUUCCPPPPPPSSSS...........",//8
            "UUUUUUUUUUUUUCCCCPPPPSSSSSS..........",//9
            ".UUUUUUUUUUUCCCCCCWWWSSSSSS..........",//10
            "..UUUUUUUUUCCCCCCWWWWWSSSS...........",//11
            "...UUUUUUUCCCCCCWWWWWWSSS............",//12
            "....UUUUUUCCCCCCWWWWWWSS.............",//13
            ".....UUUUUCCCCCCWWWGGGSS.............",//14
            "......UUUUCCCCCWWGGGGGZZ.............",//15
            ".......UUUCCCCCCGGGGZZZ..............",//16
            "........UUCCCCGGGGGZZZZ..............",//17
            ".........UCCCCGGGGGZZZ...............",//18
            "..........CCCCGGGGZZZ................",//19
            "...........CCGGGGGZZ.................",//20
            "............CGGGGZZ..................",//21
            ".............GGGGZ...................",//22
        };
        TEMPLATES.put("china", new NationTemplate("Китайская Народная Республика", NationColor.DARK_RED, parseMap(cnMap, cn)));

        // =================================================================
        // США ~550 чанков, 8 городов
        // Контур: Сиэтл-ЛА запад, Чикаго-НЙ север, Флорида юго-восток
        // =================================================================
        Map<Character, String> us = new HashMap<>();
        us.put('T', "Сиэтл"); us.put('L', "Лос-Анджелес"); us.put('D', "Денвер");
        us.put('C', "Чикаго"); us.put('N', "Нью-Йорк"); us.put('W', "Вашингтон");
        us.put('H', "Хьюстон"); us.put('A', "Майами");

        String[] usMap = {
            "TTTTT..........CCCCCCCCCNNNNN............",//0
            "TTTTTT........CCCCCCCCCCNNNNNN...........",//1
            "TTTTTTT......CCCCCCCCCCCNNNNNNN..........",//2
            "TTTTTTDD....CCCCCCCCCCCCNNNNNNNN.........",//3
            "TTTLLLDDDDDCCCCCCCCCCCWWWWNNNNN.........",//4
            "TTLLLLDDDDDDDCCCCCCWWWWWWWWNNNN.........",//5
            "TLLLLLDDDDDDDDDCCCWWWWWWWWWNNN..........",//6
            "LLLLLLDDDDDDDDDDDDWWWWWWWWWNN...........",//7
            "LLLLLLDDDDDDDDDDDDWWWWWWWWWN............",//8
            "LLLLLLDDDDDDDDDDDDDWWWWWWWWN............",//9
            "LLLLLLDDDDDDDDDDDDDDWWWWWWW.............",//10
            ".LLLLLDDDDDDDDDDDDDDWWWWWWW.............",//11
            ".LLLLLDDDDDDDDDDDDHHHWWWWWW.............",//12
            "..LLLLDDDDDDDDDDDHHHHWWWWW..............",//13
            "...LLLDDDDDDDDDDHHHHHWWWW...............",//14
            "....LLDDDDDDDDDHHHHHHWWW................",//15
            ".....LDDDDDDDDDHHHHHH...................",//16
            "......DDDDDDDDDHHHHH....................",//17
            ".............HHHHHAAAA...................",//18
            ".............HHHHAAAAA...................",//19
            "..............HHHAAAAA...................",//20
            "...............HHAAAA....................",//21
            "................HAAAA....................",//22
            ".................AAAA....................",//23
            "..................AAA....................",//24
            "...................AA....................",//25
        };
        TEMPLATES.put("usa", new NationTemplate("Соединённые Штаты Америки", NationColor.BLUE, parseMap(usMap, us)));

        // =================================================================
        // БРАЗИЛИЯ ~500 чанков, 6 городов
        // Контур: выпуклый северо-восток, сужается к югу
        // =================================================================
        Map<Character, String> br = new HashMap<>();
        br.put('M', "Манаус"); br.put('F', "Форталеза"); br.put('V', "Сальвадор");
        br.put('B', "Бразилиа"); br.put('R', "Рио-де-Жанейро"); br.put('P', "Сан-Паулу");

        String[] brMap = {
            ".....MMMMMMMMMFFFFFF..................",//0
            "....MMMMMMMMMMFFFFFFF.................",//1
            "...MMMMMMMMMMMFFFFFFFF................",//2
            "...MMMMMMMMMMMBBBVVVVVVV..............",//3
            "....MMMMMMMMMBBBBVVVVVVVV.............",//4
            ".....MMMMMMMMBBBBVVVVVVVV.............",//5
            "......MMMMMMBBBBBVVVVVVV..............",//6
            ".......MMMMMBBBBBBVVVVVV..............",//7
            "........MMMMBBBBBBVVVVV...............",//8
            ".........MMMBBBBBBVVVVV...............",//9
            "..........MMBBBBBBBVVVV...............",//10
            "...........MBBBBBBBRRRRR..............",//11
            "............BBBBBBBRRRRR..............",//12
            ".............BBBBBBRRRRR..............",//13
            "..............BBBBBRRRRR..............",//14
            "...............BBBBBRRRR..............",//15
            "................BBBPPRRR..............",//16
            ".................BPPPPRRR.............",//17
            "..................PPPPPRR.............",//18
            "...................PPPPP..............",//19
            "....................PPPP..............",//20
            ".....................PPP..............",//21
            "......................PP..............",//22
        };
        TEMPLATES.put("brazil", new NationTemplate("Федеративная Республика Бразилия", NationColor.GREEN, parseMap(brMap, br)));

        // =================================================================
        // ИНДИЯ ~300 чанков, 6 городов
        // Контур: широкий Гималайский север, треугольник к югу
        // =================================================================
        Map<Character, String> in = new HashMap<>();
        in.put('J', "Джайпур"); in.put('D', "Дели"); in.put('K', "Калькутта");
        in.put('M', "Мумбаи"); in.put('B', "Бангалор"); in.put('C', "Ченнаи");

        String[] inMap = {
            "....JJJJJDDDDDDDD..............",//0
            "...JJJJJJDDDDDDDDKK............",//1
            "...JJJJJJDDDDDDDDDKKK..........",//2
            "..JJJJJJJDDDDDDDDDDKKKK........",//3
            "..JJJJJJJDDDDDDDDDDKKKKK.......",//4
            ".JJJJJJJDDDDDDDDDDDKKKKK.......",//5
            ".MMMMMMMDDDDDDDDDDDKKKKKK.......",//6
            ".MMMMMMMMMDDDDDDDDKKKKKKK.......",//7
            ".MMMMMMMMMMDDDDDDKKKKKK.........",//8
            "..MMMMMMMMMMMDDDKKKKKKK.........",//9
            "..MMMMMMMMMMMBBBBCCCCC..........",//10
            "...MMMMMMMMMBBBBBCCCCC..........",//11
            "...MMMMMMMMMBBBBCCCCC...........",//12
            "....MMMMMMMBBBBBCCCC............",//13
            ".....MMMMMMBBBBCCCC.............",//14
            "......MMMMBBBBBCCC..............",//15
            ".......MMMBBBBBCC...............",//16
            "........MMBBBBCC................",//17
            ".........BBBBBCC................",//18
            "..........BBBBC.................",//19
            "...........BBBC.................",//20
            "............BBB.................",//21
            ".............BB.................",//22
            "..............B.................",//23
        };
        TEMPLATES.put("india", new NationTemplate("Республика Индия", NationColor.TEAL, parseMap(inMap, in)));

        // =================================================================
        // ТУРЦИЯ ~200 чанков, 6 городов
        // Контур: длинный полуостров, Стамбул/Измир запад, Трабзон/Газиантеп восток
        // =================================================================
        Map<Character, String> tr = new HashMap<>();
        tr.put('S', "Стамбул"); tr.put('A', "Анкара"); tr.put('I', "Измир");
        tr.put('L', "Анталья"); tr.put('T', "Трабзон"); tr.put('G', "Газиантеп");

        String[] trMap = {
            "...SSSAAAAAAAAATTTTTTTTTT..........",//0
            "..SSSSAAAAAAAAATTTTTTTTTTT.........",//1
            ".SSSSSAAAAAAAAATTTTTTTTTTTTGG......",//2
            "SSSIIIAAAAAAAAATTTTTTTTTTTTGGG.....",//3
            "SSIIIIAAAAAAAAATTTTTTTTTTTTGGGG....",//4
            "SIIIIIAAAAAAAAATTTTTTTTTTTGGGGG....",//5
            ".IIIILLLLAAAAAATTTTTTTTTTTGGGG.....",//6
            "..IILLLLLLAAAAATTTTTTTTTTGGGG......",//7
            "...LLLLLLLLAAAA.TTTTTTTTGGG........",//8
            "....LLLLLLLLAA...TTTTTGGG..........",//9
            ".....LLLLLLL...........................",//10
            "......LLLLL............................",//11
        };
        TEMPLATES.put("turkey", new NationTemplate("Турецкая Республика", NationColor.ORANGE, parseMap(trMap, tr)));

        // =================================================================
        // ФРАНЦИЯ ~180 чанков, 7 городов
        // Контур: L'Hexagone — шестиугольник
        // =================================================================
        Map<Character, String> fr = new HashMap<>();
        fr.put('N', "Нант"); fr.put('P', "Париж"); fr.put('S', "Страсбург");
        fr.put('B', "Бордо"); fr.put('L', "Лион"); fr.put('T', "Тулуза"); fr.put('A', "Марсель");

        String[] frMap = {
            ".....NNNNPPPPPP..........",//0
            "....NNNNNPPPPPPP.........",//1
            "...NNNNNNPPPPPPPPSS......",//2
            "...NNNNNPPPPPPPPPSSS.....",//3
            "...NNNNBPPPPPPPLLSSS.....",//4
            "...NNNBBPPPPPPPLLLSS.....",//5
            "....NBBBBPPPPPLLLLS......",//6
            "....BBBBBPPPPLLLLLL......",//7
            "....BBBBBBPPLLLLLL.......",//8
            ".....BBBBBTTLLLLL........",//9
            ".....BBBBTTTTLLLL........",//10
            "......BBTTTTTAAAA........",//11
            "......TTTTTTAAAAA........",//12
            ".......TTTTTAAAAA........",//13
            "........TTTTTAAAA........",//14
            ".........TTTAAAA.........",//15
            "..........TTAAA..........",//16
        };
        TEMPLATES.put("france", new NationTemplate("Французская Республика", NationColor.NAVY, parseMap(frMap, fr)));

        // =================================================================
        // ЯПОНИЯ ~150 чанков, 6 городов
        // Контур: дуга островов Хоккайдо-Хонсю-Сикоку-Кюсю
        // Все острова соединены (для непрерывности территории)
        // =================================================================
        Map<Character, String> jp = new HashMap<>();
        jp.put('R', "Саппоро"); jp.put('T', "Токио"); jp.put('N', "Нагоя");
        jp.put('O', "Осака"); jp.put('H', "Хиросима"); jp.put('F', "Фукуока");

        String[] jpMap = {
            "..........RRRR..........",//0
            ".........RRRRR..........",//1
            ".........RRRRRR.........",//2
            ".........RRRRR..........",//3
            ".........RRRR...........",//4
            ".........RRR............",//5
            ".........TT.............",//6
            "........TTT.............",//7
            "........TTTT............",//8
            ".......TTTTT............",//9
            ".......TTTTT............",//10
            "......TTTTTT............",//11
            "......TTTTT.............",//12
            ".....NNTTT..............",//13
            ".....NNNT...............",//14
            "....NNNN................",//15
            "....NNN.................",//16
            "...OONN.................",//17
            "...OOON.................",//18
            "..OOOO..................",//19
            "..OOOO..................",//20
            ".HOOO...................",//21
            ".HHO....................",//22
            "HHH.....................",//23
            "HHH.....................",//24
            "FHH.....................",//25
            "FFF.....................",//26
            "FFF.....................",//27
            "FF......................",//28
        };
        TEMPLATES.put("japan", new NationTemplate("Японская Империя", NationColor.WHITE, parseMap(jpMap, jp)));

        // =================================================================
        // ГЕРМАНИЯ ~140 чанков, 7 городов
        // Контур: широкий север, сужается к Баварии
        // =================================================================
        Map<Character, String> de = new HashMap<>();
        de.put('H', "Гамбург"); de.put('B', "Берлин"); de.put('K', "Кёльн");
        de.put('F', "Франкфурт"); de.put('D', "Дрезден"); de.put('S', "Штутгарт"); de.put('Q', "Мюнхен");

        String[] deMap = {
            "....HHHHHBBBBBB..........",//0
            "...HHHHHHBBBBBBB.........",//1
            "...HHHHHHHBBBBBB.........",//2
            "..HHHHHHHHBBBBBBD........",//3
            "..KKKHHHHFFDDDDDD.......",//4
            ".KKKKHHHHFFFDDDDD........",//5
            ".KKKKKFFFFFFFF...........",//6
            "..KKKKFFFFFFFS..........",//7
            "...KKKFFFFFFSS..........",//8
            "....KKFFFFFSSSS.........",//9
            ".....FFFFFSSSSS.........",//10
            "......FFFSSSSSS.........",//11
            "......SSSSSQQQQQ........",//12
            ".......SSSSQQQQQ........",//13
            "........SQQQQQQQ........",//14
            ".........QQQQQ..........",//15
        };
        TEMPLATES.put("germany", new NationTemplate("Федеративная Республика Германия", NationColor.GOLD, parseMap(deMap, de)));

        // =================================================================
        // ВЕЛИКОБРИТАНИЯ ~110 чанков, 7 городов
        // Контур: Шотландия узкая вверху, Англия шире внизу
        // =================================================================
        Map<Character, String> uk = new HashMap<>();
        uk.put('E', "Эдинбург"); uk.put('G', "Глазго"); uk.put('M', "Манчестер");
        uk.put('W', "Ливерпуль"); uk.put('B', "Бирмингем"); uk.put('R', "Бристоль"); uk.put('L', "Лондон");

        String[] ukMap = {
            ".....EEE.................",//0
            "....GEEE.................",//1
            "....GGEE.................",//2
            "....GGEE.................",//3
            "....GGGE.................",//4
            ".....GMM.................",//5
            "....WMMM.................",//6
            "....WWMM.................",//7
            "....WWMM.................",//8
            "....WWBB.................",//9
            "....WWBB.................",//10
            "....RRBB.................",//11
            "....RRBBB................",//12
            "....RRBBL................",//13
            "....RRLLL................",//14
            ".....RLLLL...............",//15
            ".....LLLLL...............",//16
            ".....LLLLL...............",//17
            "......LLLL...............",//18
            "......LLLL...............",//19
            "......LLL................",//20
            ".......LL................",//21
        };
        TEMPLATES.put("uk", new NationTemplate("Соединённое Королевство", NationColor.PURPLE, parseMap(ukMap, uk)));

        // =================================================================
        // РУМЫНИЯ ~100 чанков, 5 городов
        // Контур: овал, Карпаты дугой, побережье справа
        // =================================================================
        Map<Character, String> ro = new HashMap<>();
        ro.put('C', "Клуж-Напока"); ro.put('I', "Яссы"); ro.put('T', "Тимишоара");
        ro.put('B', "Бухарест"); ro.put('O', "Констанца");

        String[] roMap = {
            "....CCCCCCIIIIIII.........",//0
            "...CCCCCCCIIIIIII.........",//1
            "..TTCCCCCCIIIIII..........",//2
            "..TTTCCCCCIIIII...........",//3
            "..TTTTCCCCBBBIII..........",//4
            "..TTTTTCBBBBBBII..........",//5
            "..TTTTBBBBBBBBOO..........",//6
            "...TTTBBBBBBBOOOO.........",//7
            "....TTBBBBBBOOOO..........",//8
            ".....TBBBBBBOOO...........",//9
            "......BBBBBOOO............",//10
            ".......BBBOOO.............",//11
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
