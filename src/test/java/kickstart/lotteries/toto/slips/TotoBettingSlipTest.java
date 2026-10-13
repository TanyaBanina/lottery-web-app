package kickstart.lotteries.toto.slips;

import kickstart.user.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;

class TotoBettingSlipTest{

    @Test
    void testSetWinningAmount() {
        TotoBettingSlip slip = new TotoBettingSlip(
            LocalDateTime.now(),
            5.0,
            Map.of("Match 1", "Home", "Match 2", "Away"),
            new User.UserIdentifier(),
            123L
        );

        slip.setWinningAmount(50.0);
        assertEquals(50.0, slip.getWinningAmount(), "Winning amount should be set to 50.0");
    }

    @Test
    void testUserIdNullScenario() {
        TotoBettingSlip slip = new TotoBettingSlip(
            LocalDateTime.now(),
            2.0,
            Map.of("Match 1", "Draw"),
            null,
            null
        );

        assertNull(slip.getUserId(), "UserId should be null as given.");
    }
}


