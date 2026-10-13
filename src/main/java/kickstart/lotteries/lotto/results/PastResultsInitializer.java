package kickstart.lotteries.lotto.results;

import org.salespointframework.core.DataInitializer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
/**
 * The {@code PastResultsInitializer} initializes ResultsRepository.
 */
@Component
@Order(20)
public class PastResultsInitializer implements DataInitializer {
	//initializes previous winning combinations of numbers and their dates
	private final PastResultsRepository repo;

	public PastResultsInitializer(PastResultsRepository repo) {
		this.repo = repo;
	}

	@Override
	@Transactional
	public void initialize() {
		repo.deleteAll(); //can be deleted later
		if (repo.count() > 0) {
			return;
		}
		Random random = new Random();
		LocalDateTime date = LocalDateTime.now().with(java.time.DayOfWeek.SUNDAY);

		if (date.isAfter(LocalDateTime.now())) {
			date = date.minusWeeks(1);
		}

		for (int i = 0; i < 7; i++) {
			Set<Integer> winningNumbers = generateRandomWinningNumbers(random);
			Integer superNumber = random.nextInt(10);

			Result result = new Result(date, winningNumbers, superNumber);
			repo.save(result);

			date = date.minusWeeks(1);
		}
	}



	private Set<Integer> generateRandomWinningNumbers(Random random) {
		Set<Integer> numbers = new HashSet<>();
		while (numbers.size() < 6) {
			int number = random.nextInt(49) + 1;
			numbers.add(number);
		}
		return numbers;
	}
}
