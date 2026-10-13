package kickstart.lotteries.toto;

public class Match {

    private int matchNumber;
    private String teamA;
    private String teamB;

    public Match(int matchNumber, String teamA, String teamB) {
        this.matchNumber = matchNumber;
        this.teamA = teamA;
        this.teamB = teamB;
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public String getTeamA() {
        return teamA;
    }

    public String getTeamB() {
        return teamB;
    }
}

