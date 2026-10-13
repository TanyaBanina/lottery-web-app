package kickstart.lotteries.lotto.slips;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
/**
 * The {@code PermanentSlipScheduler} service is responsible for periodically checking all active permanent betting slips
 * and deactivating those whose end date has passed. This task runs automatically every day at 11:59 PM.
 */
@Service
public class PermanentSlipScheduler {

	private final LottoBettingSlipRepository bettingSlipRepository;

	public PermanentSlipScheduler(LottoBettingSlipRepository bettingSlipRepository) {
		this.bettingSlipRepository = bettingSlipRepository;
	}

	@Scheduled(cron = "0 59 23 * * ?")
	public void updateActiveStatus() {
		LocalDateTime now = LocalDateTime.now();
		List<PermanentLottoBettingSlip> activePermanentSlips = bettingSlipRepository.findByIsActiveTrue();

		for (PermanentLottoBettingSlip slip : activePermanentSlips) {
			if (slip.getEndDate().isBefore(now)) {
				slip.setActive(false);
				bettingSlipRepository.save(slip);
			}
		}
	}
}
