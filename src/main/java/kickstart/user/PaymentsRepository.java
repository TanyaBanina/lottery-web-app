package kickstart.user;

import org.springframework.data.jpa.repository.JpaRepository;
import kickstart.user.User.UserIdentifier;
import java.util.List;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {
	/**
	 * Interface, dass Payments als Liste zurück gibt
	 * abgerufen über UserID -> UserIdentifier userId
	 */
	List<Payments> findByUser_Id(UserIdentifier userId);
}