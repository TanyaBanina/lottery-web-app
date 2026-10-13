package kickstart.lotteries.lotto.slips;


import jakarta.persistence.Entity;

import java.time.LocalDateTime;
import java.util.List;
import kickstart.user.User.UserIdentifier;
/**
 * The {@code PermanentLottoBettingSlip} class represents a permanent betting slip for a lottery draw
 * with a specified duration. The betting slip remains active until its end date is reached.
 */
@Entity
public class PermanentLottoBettingSlip extends LottoBettingSlip {
    private Duration duration;
	private LocalDateTime endDate;

	public LocalDateTime getEndDate() {
		return endDate;
	}

	/**
	 * @param duration the duration for which the betting slip remains active
	 */
	public PermanentLottoBettingSlip(LocalDateTime dateSubmitted, UserIdentifier userId,
									 Long communityId, double betAmount, List<LottoNumbers> numbersList, int superNumber, Duration duration) {
		super(dateSubmitted, userId, communityId, betAmount, numbersList, superNumber);
		if (duration == null) {
			throw new IllegalArgumentException("Duration cannot be null");
		}
        this.duration = duration;
		this.endDate = calculateEndDate(dateSubmitted, duration);
		setStatus(Status.PENDING);
		setActive(true);
	}

	private LocalDateTime calculateEndDate(LocalDateTime dateSubmitted, Duration duration) {
        return switch (duration) {
            case MONTHLY -> dateSubmitted.plusMonths(1);
            case SEMIANNUAL -> dateSubmitted.plusMonths(6);
            case ANNUALY -> dateSubmitted.plusYears(1);
        };
	}

	public PermanentLottoBettingSlip() {

	}

	@Override
	public String getDurationType() {
		return duration != null ? duration.name() : "Unbekannt";
	}

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}

