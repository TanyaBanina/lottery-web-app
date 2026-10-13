package kickstart.lotteries.lotto.results;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
/**
 * The {@code Result} class represents a lottery draw result.
 * It contains the winning combination of numbers for a specific draw, including the draw date,
 * the set of winning numbers, and the super number.
 */
@Entity
public class Result {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private LocalDateTime drawDate;
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> winningNumbers;//6 numbers
	private Integer superNumber;//1 number

	/**
	 * @param drawDate the date and time of the lottery draw
	 * @param winningNumbers the set of winning numbers for the draw
	 * @param superNumber the super number for the draw
	 */
	public Result(LocalDateTime drawDate, Set<Integer> winningNumbers, Integer superNumber) {
		this.drawDate = drawDate;
		this.winningNumbers = winningNumbers;
		this.superNumber = superNumber;
	}

	public Result() {

	}
	public void setId(Long id) {
		this.id = id;
	}

	public void setDrawDate(LocalDateTime drawDate) {
		this.drawDate = drawDate;
	}

	public void setWinningNumbers(Set<Integer> winningNumbers) {
		this.winningNumbers = winningNumbers;
	}

	public void setSuperNumber(Integer superNumber) {
		this.superNumber = superNumber;
	}

	public Long getId() {
		return id;
	}

	public LocalDateTime getDrawDate() {
		return drawDate;
	}

	public Set<Integer> getWinningNumbers() {
		return winningNumbers;
	}

	public Integer getSuperNumber() {
		return superNumber;
	}

	@Override
	public String toString() {
		return "Result{" +
			"drawDate=" + drawDate +
			", winningNumbers=" + winningNumbers +
			", superNumber=" + superNumber +
			'}';
	}
}

