package kickstart.lotteries.toto;

import java.util.ArrayList;
import java.util.List;

public class Bundesliga2Matches {
    public static List<Match> getPredefinedMatches() {
        List<Match> matches = new ArrayList<>();
        matches.add(new Match(1, "Karlsruher SC", "SSV Jahn Regensburg"));
        matches.add(new Match(2, "Hertha BSC", "SC Preußen Münster"));
        matches.add(new Match(3, "FC Schalke 04", "Fortuna Düsseldorf"));
        matches.add(new Match(4, "1. FC Magdeburg", "SC Paderborn 07"));
        matches.add(new Match(5, "SSV Ulm 1846", "Hamburger SV"));
        matches.add(new Match(6, "SV Darmstadt 98", "1. FC Kaiserslautern"));
        matches.add(new Match(7, "1. FC Köln", "1. FC Nürnberg"));
        matches.add(new Match(8, "SpVgg Greuther Fürth", "Hannover 96"));
        matches.add(new Match(9, "Eintracht Braunschweig", "SV Elversberg"));
        return matches;
    }
}
