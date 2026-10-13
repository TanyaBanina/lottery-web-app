package kickstart.lotteries.lotto.slips;

import kickstart.lotteries.lotto.Lotto;
import kickstart.lotteries.lotto.results.PastResultsRepository;

import kickstart.lotteries.lotto.results.Result;
import kickstart.user.User;
import kickstart.user.UserRepository;
import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * The {@code PastResultsInitializer} initializes LottoBettingSlipRepository.
 */
@Component
@Order(30)
public class LottoBettingSlipInitializer implements DataInitializer {

	private final LottoBettingSlipRepository bettingSlipRepo;
	private final UserRepository userRepo;
	private final PastResultsRepository pastResultsRepo;
	private final Lotto lotto;

	public LottoBettingSlipInitializer(Lotto lotto, LottoBettingSlipRepository bettingSlipRepo,
									   UserRepository userRepo, PastResultsRepository pastResultsRepo) {
		this.bettingSlipRepo = bettingSlipRepo;
		this.userRepo = userRepo;
		this.pastResultsRepo = pastResultsRepo;
		this.lotto = lotto;
	}

	@Override
	public void initialize() {
		bettingSlipRepo.deleteAll();//can be deleted later

		if (bettingSlipRepo.count() > 0) {
			return;
		}

		Result lastResult = pastResultsRepo.findTopByOrderByDrawDateDesc();

		LocalDateTime drawDate = lastResult.getDrawDate();
		LocalDateTime dateSubmitted = drawDate.minusDays(2);

		Optional<User> optionalUser = userRepo.findByUserAccount_Username("zocker");
		User.UserIdentifier userId = optionalUser.get().getId();

		Set<Integer> winningNumbers = lastResult.getWinningNumbers();
		int superNumber = lastResult.getSuperNumber();

		LottoNumbers lottoNumbers = new LottoNumbers(winningNumbers, 3);

		LottoBettingSlip bettingSlip = createWinningSlip(dateSubmitted, lottoNumbers,
			userId, superNumber);

		bettingSlip.setDrawDate(drawDate);
		lotto.processBettingSlip(bettingSlip);
	}


	public LottoBettingSlip  createWinningSlip(LocalDateTime dateSubmitted,
													 LottoNumbers lottoNumbers3, User.UserIdentifier userId, int superNumber){


		Set<Integer> numbersSet1 = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6));
		LottoNumbers lottoNumbers1 = new LottoNumbers(numbersSet1, 1);

		Set<Integer> numbersSet2 = new HashSet<>(Set.of(7, 8, 9, 10, 11, 12));
		LottoNumbers lottoNumbers2 = new LottoNumbers(numbersSet2, 2);

		List<LottoNumbers> numbersList = List.of(lottoNumbers1, lottoNumbers2, lottoNumbers3);

		double betAmount = 3.0;

		Long communityId = null;
		LottoBettingSlip example = new SingleLottoBettingSlip(
			dateSubmitted,
			userId,
			communityId,
			betAmount,
			numbersList,
			superNumber
		);


		return example;
	}
	}



