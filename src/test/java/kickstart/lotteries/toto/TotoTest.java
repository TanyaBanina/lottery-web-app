package kickstart.lotteries.toto;

import kickstart.community.Community;
import kickstart.community.CommunityManagement;
import kickstart.lotteries.toto.slips.TotoBettingSlip;
import kickstart.lotteries.toto.slips.TotoBettingSlipService;
import kickstart.user.BankAccount;
import kickstart.user.User;
import kickstart.user.UserRepository;
import kickstart.user.User.UserIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TotoTest {

    private Toto toto;
    private Toto.TotoDrawService drawService;
    private TotoBettingSlip slip;
    private UserRepository userRepository;
    private CommunityManagement communityManagement;
    private TotoBettingSlipService bettingSlipService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        communityManagement = Mockito.mock(CommunityManagement.class);
        bettingSlipService = Mockito.mock(TotoBettingSlipService.class);

        toto = new Toto();
        TotoTestHelper.injectField(toto, "userRepository", userRepository);
        TotoTestHelper.injectField(toto, "communityManagement", communityManagement);
        TotoTestHelper.injectField(toto, "bettingSlipService", bettingSlipService);

        drawService = toto.new TotoDrawService();

        slip = new TotoBettingSlip(
            LocalDateTime.now(),
            10.0,
            Map.of(
                "Match 1", "1",
                "Match 2", "0",
                "Match 3", "2",
                "Match 4", "1",
                "Match 5", "2",
                "Match 6", "0",
                "Match 7", "1",
                "Match 8", "0",
                "Match 9", "2"
            ),
            new UserIdentifier(),
            null
        );
    }

    @Test
    void testCalculateWinnings_ZeroMatchesCorrect() {
        List<String> actualResults = List.of("2","2","1","0","0","1","2","1","1");
        double winnings = toto.calculateWinnings(slip, actualResults);
        assertTrue(winnings >= 0);
    }

    @Test
    void testCalculateWinnings_SomeMatchesCorrect() {
        List<String> actualResults = List.of("1","0","2","1","2","0","1","0","2");
        double winnings = toto.calculateWinnings(slip, actualResults);
        assertTrue(winnings >= 0);
    }


    @Test
    void testProcessSingleBettingSlip_Community() {
        slip.setCommunityId(10L);
        Community community = new Community(null, "TestComm", "pass");
        when(communityManagement.findCommunityById(10L)).thenReturn(community);

        // community scenario does not need user deposit
        List<String> actualResults = List.of("1","1","1","1","1","1","1","1","1");
        toto.processSingleBettingSlip(slip, actualResults);
        verify(communityManagement, times(1)).evaluateCommunityTotoBettingSlip(slip);
    }

    @Test
    void testTotoDrawServicePerformDraw() {
        TotoBettingSlip dueSlip = new TotoBettingSlip(LocalDateTime.now().minusDays(1),5.0,
            Map.of("Match 1","1","Match 2","0","Match 3","2","Match 4","1","Match 5","2","Match 6","0","Match 7","1","Match 8","0","Match 9","2"),
            new UserIdentifier(),
            null
        );
        when(bettingSlipService.findDuePendingSlips()).thenReturn(List.of(dueSlip));

        // Provide a user so no "User nicht gefunden"
        UserIdentifier uid = new UserIdentifier();
        dueSlip.setUserId(uid);
        User user = new User(null, new BankAccount(100.0));
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));

        drawService.performDraw();
        verify(bettingSlipService, times(1)).findDuePendingSlips();
    }
}

