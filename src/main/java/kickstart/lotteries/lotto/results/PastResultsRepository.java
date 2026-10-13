package kickstart.lotteries.lotto.results;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PastResultsRepository extends JpaRepository<Result, Long> {
	Result findByDrawDate(LocalDateTime drawDate);
	List<Result> findAllByOrderByDrawDateDesc();
	Result findTopByOrderByDrawDateDesc();
}

