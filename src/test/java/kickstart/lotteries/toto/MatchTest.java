package kickstart.lotteries.toto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchTest {

    @Test
    void testMatchConstructorAndGetters() {
        int matchNumber = 1;
        String teamA = "Team A";
        String teamB = "Team B";

        Match match = new Match(matchNumber, teamA, teamB);

        assertEquals(matchNumber, match.getMatchNumber(), "Match number same as constructor input.");
        assertEquals(teamA, match.getTeamA(), "Team A match with constructor input.");
        assertEquals(teamB, match.getTeamB(), "Team B match with constructor input.");
    }
}

