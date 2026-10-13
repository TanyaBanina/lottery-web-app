package kickstart.lotteries;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import kickstart.user.User.UserIdentifier;


@Entity
public abstract class BettingSlip {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private LocalDateTime dateSubmitted;
	@Embedded
	private UserIdentifier userId;
	private double betAmount;
	private double winningAmount;

	/**
	 * Constructor to create a new betting slip with the specified attributes.
	 *
	 * @param id           Unique identifier for the betting slip.
	 * @param dateSubmitted The date and time when the slip was submitted.
	 * @param userId       The identifier for the user who submitted the slip.
	 * @param betAmount    The amount wagered on the betting slip.
	 */
	public BettingSlip(Long id, LocalDateTime dateSubmitted, UserIdentifier userId, double betAmount) {
		this.id = id;
		this.dateSubmitted = dateSubmitted;
		this.userId = userId;
		this.betAmount = betAmount;
		this.winningAmount = 0.0;
	}

	public BettingSlip() {}

	public void setDateSubmitted(LocalDateTime dateSubmitted) {this.dateSubmitted = dateSubmitted;}
	public void setBetAmount(double betAmount) {this.betAmount = betAmount;}
	public void setWinningAmount(double winningAmount) {this.winningAmount = winningAmount;}
	public void setUserId(UserIdentifier userId) {this.userId = userId;}
	public void setId(Long id) {this.id = id;}
	public LocalDateTime getDateSubmitted() {return dateSubmitted;}
	public double getBetAmount() {return betAmount;}
	public double getWinningAmount() {return winningAmount;}
	public UserIdentifier getUserId() {
		return userId;
	}
	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "BettingSlip{" +
			"dateSubmitted=" + dateSubmitted +
			", betAmount=" + betAmount +
			", winningAmount=" + winningAmount +
			'}';
	}
}
