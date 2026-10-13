package kickstart.lotteries.lotto.slips;

import java.util.Set;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
/**
 * The {@code LottoNumbers} class represents a set of 6 lottery numbers for a single bet.
 */
@Embeddable
public class LottoNumbers {

	private Integer slotId;
    private Set<Integer> numbers;

	/**
	 * @param numbers the set of lottery numbers (6 numbers)
	 * @param slotId the identifier for the set of numbers
	 */
    public LottoNumbers(Set<Integer> numbers, Integer slotId) {
        this.numbers = numbers;
		this.slotId = slotId;
    }
	public LottoNumbers() {
	}
	@ElementCollection
    public Set<Integer> getNumbers() {
        return numbers;
    }

    public void setNumbers(Set<Integer> numbers) {
        this.numbers = numbers;
    }

	public Integer getSlotId() {
		return slotId;
	}

	public void setSlotId(Integer slotId) {
		this.slotId = slotId;
	}

	@Override
	public String toString() {
		return "LottoNumbers{" +
			"numbers=" + numbers +
			'}';
	}
}