package kickstart.lotteries.toto;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Bundesliga1MatchesTest {

    @Test
    void testGetPredefinedMatches() {
        List<Match> matches = Bundesliga1Matches.getPredefinedMatches();

        assertNotNull(matches, "The returned match list should not be null.");
        assertFalse(matches.isEmpty(), "The returned match list should not be empty.");
        assertEquals(9, matches.size(), "There should be exactly 9 matches predefined.");

        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            assertEquals(i + 1, match.getMatchNumber(), "Match number should be consecutive starting at 1.");
            assertNotNull(match.getTeamA(), "Team A should not be null.");
            assertFalse(match.getTeamA().isBlank(), "Team A should not be blank.");
            assertNotNull(match.getTeamB(), "Team B should not be null.");
            assertFalse(match.getTeamB().isBlank(), "Team B should not be blank.");
        }
    }
}

