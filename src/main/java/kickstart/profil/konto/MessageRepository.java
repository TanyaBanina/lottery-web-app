package kickstart.profil.konto;

import kickstart.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

	List<Message> findByUserId(User.UserIdentifier userId);
}

