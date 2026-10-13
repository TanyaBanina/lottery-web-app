package kickstart.lotteries.lotto;

import static org.junit.jupiter.api.Assertions.*;

import kickstart.lotteries.lotto.slips.LottoNumbers;
import kickstart.lotteries.lotto.slips.SingleLottoBettingSlip;
import kickstart.lotteries.lotto.slips.Status;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import kickstart.user.User.UserIdentifier;

class SingleLottoBettingSlipTest {

	@Test
	void testConstructorSetsActiveToFalse() {
		LocalDateTime dateSubmitted = LocalDateTime.now();
		UserIdentifier userId = new UserIdentifier(UUID.randomUUID());
		Long communityId = 123L;
		double betAmount = 10.0;
		List<LottoNumbers> numbersList = Collections.emptyList();
		int superNumber = 7;

		SingleLottoBettingSlip bettingSlip = new SingleLottoBettingSlip(dateSubmitted, userId, communityId, betAmount, numbersList, superNumber);

		assertFalse(bettingSlip.isActive(), "The active flag should be set to false in the constructor.");
	}

}

