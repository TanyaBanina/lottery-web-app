package kickstart.lotteries.toto.slips;

import kickstart.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TotoBettingSlipRepositoryTest {

    @Autowired
    TotoBettingSlipRepository repository;

    @Test
    void testSaveAndFind() {
        TotoBettingSlip slip = new TotoBettingSlip(
            LocalDateTime.now(),
            10.0,
            Map.of("Match 1", "Home"),
            new User.UserIdentifier(),
            null
        );
        slip.setWinningAmount(20.0);

        TotoBettingSlip saved = repository.save(slip);
        assertNotNull(saved.getId(), "Saved slip should have an ID");

        var found = repository.findById(saved.getId());
        assertTrue(found.isPresent(), "Should find the saved slip by ID");
        assertEquals(20.0, found.get().getWinningAmount(), "Winning amount should match");
    }
}

