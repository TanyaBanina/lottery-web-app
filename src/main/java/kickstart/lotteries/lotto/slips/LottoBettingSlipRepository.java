package kickstart.lotteries.lotto.slips;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kickstart.user.User.UserIdentifier;


public interface LottoBettingSlipRepository extends JpaRepository<LottoBettingSlip, Long> {
	List<LottoBettingSlip> findByUserId(UserIdentifier userId);
	List<LottoBettingSlip> findByDrawDate(LocalDateTime drawDate);
	List<LottoBettingSlip> findByCommunityId(Long communityId);
	List<LottoBettingSlip> findByStatus(Status status);
	List<PermanentLottoBettingSlip> findByIsActiveTrue();
	LottoBettingSlip findById(long id);


}

