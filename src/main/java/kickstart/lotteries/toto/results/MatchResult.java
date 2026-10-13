package kickstart.lotteries.toto.results;

public class MatchResult {
	private int matchNumber;
	private int matchResult;

	public MatchResult(int matchNumber, int matchResult) {
		this.matchNumber = matchNumber ;
		this.matchResult = matchResult ;
	}

	public int getMatchNumber() {
		return matchNumber ;
	}

	public int getMatchResult() {
		return matchResult;
	}
}
