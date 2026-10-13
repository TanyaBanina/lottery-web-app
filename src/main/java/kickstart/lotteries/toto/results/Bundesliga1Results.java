package kickstart.lotteries.toto.results;

import java.util.ArrayList;
import java.util.List;

public class Bundesliga1Results {
	public static List<MatchResult> getMatchdayResults() {
		List<MatchResult> matchresults = new ArrayList<>();
		matchresults.add(new MatchResult(1, 1));
		matchresults.add(new MatchResult(2, 2));
		matchresults.add(new MatchResult(3, 2));
		matchresults.add(new MatchResult(4, 1));
		matchresults.add(new MatchResult(5, 0));
		matchresults.add(new MatchResult(6, 2));
		matchresults.add(new MatchResult(7, 2));
		matchresults.add(new MatchResult(8, 0));
		matchresults.add(new MatchResult(9, 2));
		return matchresults;
	}
}
