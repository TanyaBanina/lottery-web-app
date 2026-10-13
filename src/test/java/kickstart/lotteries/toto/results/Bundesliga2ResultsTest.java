package kickstart.lotteries.toto.results;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class Bundesliga2ResultsTest {

    @Test
    void testGetMatchdayResultsNotEmpty() {
        List<MatchResult> results = Bundesliga2Results.getMatchdayResults();
        assertNotNull(results, "Matchday results should not be null.");
        assertFalse(results.isEmpty(), "Matchday results should not be empty.");
        
        // additional check
        MatchResult first = results.get(0);
        assertNotNull(first, "The first match result should not be null.");
    }
}

