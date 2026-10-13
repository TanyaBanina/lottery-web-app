package kickstart.lotteries.lotto;

import kickstart.community.CommunityManagement;
import kickstart.lotteries.lotto.results.PastResultsRepository;
import kickstart.lotteries.lotto.slips.LottoBettingSlip;
import kickstart.lotteries.lotto.slips.LottoBettingSlipRepository;
import kickstart.lotteries.lotto.results.Result;
import kickstart.profil.konto.MessageManagement;
import kickstart.user.UserManagement;
import kickstart.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LottoTest {

	private Lotto lotto;
	private LottoBettingSlipRepository bettingSlipRepository;
	private PastResultsRepository pastResultsRepository;
	private CommunityManagement communityManagement;
	private UserManagement userManagement;
	private MessageManagement messageManagement;


	@BeforeEach
	void setUp() {
		bettingSlipRepository = mock(LottoBettingSlipRepository.class);
		pastResultsRepository = mock(PastResultsRepository.class);
		communityManagement = mock(CommunityManagement.class);
		messageManagement = mock(MessageManagement.class);
		lotto = new Lotto(bettingSlipRepository, pastResultsRepository, communityManagement, userManagement, messageManagement);
	}

	@Test
	void testGenerateLotteryNumbers() {
		Set<Integer> numbers = lotto.generateLotteryNumbers();
		assertNotNull(numbers);
		assertEquals(6, numbers.size());
		assertTrue(numbers.stream().allMatch(num -> num >= 1 && num <= 49));
	}

	@Test
	void testGenerateSuperNumber() {
		int superNumber = lotto.generateSuperNumber();
		assertTrue(superNumber >= 1 && superNumber <= 10);
	}

	@Test
	void testSavePastResult() {
		LocalDateTime date = LocalDateTime.now();
		Set<Integer> winningNumbers = Set.of(1, 2, 3, 4, 5, 6);
		int superNumber = 7;

		lotto.savePastResult(date, winningNumbers, superNumber);
		verify(pastResultsRepository, times(1)).save(Mockito.any(Result.class));
	}

	@Test
	void testSaveSlip() {
		LottoBettingSlip slip = mock(LottoBettingSlip.class);

		lotto.saveSlip(slip);
		verify(bettingSlipRepository, times(1)).save(slip);
	}

	@Test
	void testGetPastResults() {
		List<Result> mockResults = List.of(new Result(LocalDateTime.now(), Set.of(1, 2, 3, 4, 5, 6), 7));
		when(pastResultsRepository.findAll()).thenReturn(mockResults);

		List<Result> results = lotto.getPastResults();
		assertNotNull(results);
		assertEquals(1, results.size());
	}

	@Test
	void testGetNextDrawDate() {
		assertNotNull(Lotto.getNextDrawDate());
	}


	@Test
	void testGetFormattedNextDrawDate() {
		String formattedDate = lotto.getFormattedNextDrawDate();
		assertNotNull(formattedDate);
	}

	@Test
	void testCalculateNextDrawDate() {
		LocalDateTime nextDrawDate = lotto.calculateNextDrawDate();
		assertNotNull(nextDrawDate);
		assertEquals(nextDrawDate.getDayOfWeek(), java.time.DayOfWeek.SUNDAY);
	}

	@Test
	void testCountMatchingMainNumbers() {
		Set<Integer> ticketNumbers = Set.of(1, 2, 3, 4, 5, 6);
		Set<Integer> winningNumbers = Set.of(3, 4, 5, 6, 7, 8);

		int matches = lotto.countMatchingMainNumbers(ticketNumbers, winningNumbers);
		assertEquals(4, matches);
	}

	@Test
	void testGetPrizeAmount() {
		assertEquals(1000000.0, lotto.getPrizeAmount(Lotto.WinningCategory.JACKPOT));
		assertEquals(0.0, lotto.getPrizeAmount(Lotto.WinningCategory.NO_WIN));
	}
}

