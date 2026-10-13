package kickstart.lotteries.toto.results;

import java.util.ArrayList;
import java.util.List;

public class Bundesliga2Results {
	public static List<MatchResult> getMatchdayResults() {
		List<MatchResult> matchresults = new ArrayList<>();
		matchresults.add(new MatchResult(1, 1));
		matchresults.add(new MatchResult(2, 2));
		matchresults.add(new MatchResult(3, 0));
		matchresults.add(new MatchResult(4, 0));
		matchresults.add(new MatchResult(5, 0));
		matchresults.add(new MatchResult(6, 1));
		matchresults.add(new MatchResult(7, 1));
		matchresults.add(new MatchResult(8, 1));
		matchresults.add(new MatchResult(9, 2));
		return matchresults;
	}
}
