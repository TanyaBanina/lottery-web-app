package kickstart.lotteries.lotto.slips;

import jakarta.persistence.*;

import kickstart.lotteries.BettingSlip;
import kickstart.lotteries.lotto.Lotto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import kickstart.user.User.UserIdentifier;

@Entity
public abstract class LottoBettingSlip extends BettingSlip {

	@ElementCollection(fetch = FetchType.EAGER)
	private List<LottoNumbers> numbersList = new ArrayList<>(); // allows up to 12 sets of numbers
	private Long communityId;
	private int superNumber;
	private LocalDateTime drawDate;
	private Status status; // pending or expired
	private boolean isActive;
	public static final double SLOT_PRICE = 1.0;
	public void setDrawDate(LocalDateTime drawDate) {
		this.drawDate = drawDate;
	}
	public LocalDateTime getDrawDate() {
		return drawDate;
	}

	public boolean isActive() {
		return isActive;
	}

	protected void setActive(boolean active) {
		isActive = active;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setCommunityId(Long communityId) {this.communityId = communityId;}
	public Long getCommunityId() {
		return communityId;
	}

	/**
	 * Initializes the slip with the provided details, including numbers, super number, and user information.
	 * Sets the draw date based on the next scheduled draw in the lottery system.
	 *
	 * @param dateSubmitted The date and time the betting slip was submitted.
	 * @param userId        The identifier for the user submitting the betting slip.
	 * @param communityId   The ID of the betting community associated with this slip, if applicable.
	 * @param betAmount     The amount of money wagered on this betting slip.
	 * @param numbersList   The list of Lotto numbers chosen by the user for this slip.
	 * @param superNumber   The "super number" selected by the user for additional winning conditions.
	 */
	public LottoBettingSlip(LocalDateTime dateSubmitted, UserIdentifier userId,
							Long communityId, double betAmount, List<LottoNumbers> numbersList, int superNumber) {
		super(null, dateSubmitted, userId, betAmount);
		this.numbersList = numbersList;
		this.superNumber = superNumber;
		this.drawDate = Lotto.getNextDrawDate();
		this.communityId = communityId;

	}

	public LottoBettingSlip() {
		super();
	}

	public boolean isPermanent() {
		return this instanceof PermanentLottoBettingSlip;
	}

	public String getDurationType() {
		return null;
	}
	public Duration getDuration() {
		return null;
	}

	public void setSuperNumber(int superNumber) {
		this.superNumber = superNumber;
	}

	public void setNumbersList(List<LottoNumbers> numbersList) {
		this.numbersList = numbersList;
	}

	public int getSuperNumber() {
		return superNumber;
	}

	public List<LottoNumbers> getNumbersList() {
		return numbersList;
	}

	public boolean isValid() {
		if (numbersList == null || numbersList.size() > 12) {
			return false;
		}
		for (LottoNumbers lottoNumbers : numbersList) {
			Set<Integer> numbers = lottoNumbers.getNumbers();
			if (numbers.size() != 6) {
				return false;
			}
			for (Integer number : numbers) {
				if (number < 1 || number > 49) {
					return false;
				}
			}
		}
		return true;
	}


	public Set<Integer> getNumbersForSlot(int slotIndex) {
		if (slotIndex < 0 || slotIndex >= numbersList.size()) {
			throw new IndexOutOfBoundsException("Slot index out of bounds");
		}

		LottoNumbers lottoNumbers = numbersList.get(slotIndex);
		return lottoNumbers.getNumbers();
	}


	public void setNumbersForSlot(int slotIndex, Set<Integer> numbers, boolean addSet) {

		if (numbers == null) {
			throw new IllegalArgumentException("Numbers set cannot be null");
		}

		if (numbers.size() != 6) {
			throw new IllegalArgumentException("Exactly 6 numbers must be provided. Provided: " + numbers.size());
		}

		if (slotIndex >= numbersList.size() && addSet) {
			if (numbersList.size() < 12) {
				LottoNumbers newSet = new LottoNumbers(numbers, numbersList.size() + 1);
				numbersList.add(newSet);
			} else {
				throw new IllegalStateException("Cannot add more than 12 slots.");
			}
			return;
		}

		if (slotIndex >= numbersList.size()) {
			throw new IndexOutOfBoundsException("Slot index out of bounds and addSet is false");
		}

		LottoNumbers lottoNumbers = numbersList.get(slotIndex);
		lottoNumbers.setNumbers(numbers);
	}

	@Override
	public String toString() {
		return "LottoBettingSlip{" +
			"numbersList=" + numbersList +
			", communityId=" + communityId +
			", superNumber=" + superNumber +
			", drawDate=" + drawDate +
			", status=" + status +
			'}';
	}
}
