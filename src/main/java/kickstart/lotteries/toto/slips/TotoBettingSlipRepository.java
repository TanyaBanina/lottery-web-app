package kickstart.lotteries.toto.slips;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TotoBettingSlipRepository extends JpaRepository<TotoBettingSlip, Long> {

    List<TotoBettingSlip> findByStatus(TotoStatus status);

    List<TotoBettingSlip> findByStatusAndDrawDateLessThanEqual(TotoStatus status, LocalDateTime dateTime);
}

