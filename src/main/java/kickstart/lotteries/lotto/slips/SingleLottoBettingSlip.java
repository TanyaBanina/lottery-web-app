package kickstart.lotteries.lotto.slips;


import jakarta.persistence.Entity;

import kickstart.user.User.UserIdentifier;
import java.time.LocalDateTime;
import java.util.List;
/**
 * The {@code SingleLottoBettingSlip} class represents a betting slip for a single lottery draw.
 * Unlike permanent betting slips, it has no duration and is valid for a single instance.
 */
@Entity
public class SingleLottoBettingSlip extends LottoBettingSlip {

    public SingleLottoBettingSlip(LocalDateTime dateSubmitted, UserIdentifier userId,
								  Long communityId, double betAmount, List<LottoNumbers> numbersList, int superNumber) {
		super(dateSubmitted, userId, communityId, betAmount, numbersList, superNumber);
		setStatus(Status.PENDING);
		setActive(false);
    }

	public SingleLottoBettingSlip() {

	}
}


