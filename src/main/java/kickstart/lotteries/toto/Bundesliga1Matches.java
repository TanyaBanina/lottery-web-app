package kickstart.lotteries.toto;

import java.util.ArrayList;
import java.util.List;

public class Bundesliga1Matches {

    public static List<Match> getPredefinedMatches() {
        List<Match> matches = new ArrayList<>();
        matches.add(new Match(1, "SC Freiburg", "VfL Wolfsburg"));
        matches.add(new Match(2, "FC Augsburg", "Bayer 04 Leverkusen"));
        matches.add(new Match(3, "1. FSV Mainz 05", "FC Bayern München"));
        matches.add(new Match(4, "Borussia Mönchengladbach", "Holstein Kiel"));
        matches.add(new Match(5, "1. FC Union Berlin", "VfL Bochum 1848"));
        matches.add(new Match(6, "FC St. Pauli", "SV Werder Bremen"));
        matches.add(new Match(7, "1. FC Heidenheim 1846", "VfB Stuttgart"));
        matches.add(new Match(8, "Borussia Dortmund", "TSG Hoffenheim"));
        matches.add(new Match(9, "RB Leipzig", "Eintracht Frankfurt"));
        return matches;
    }
}

