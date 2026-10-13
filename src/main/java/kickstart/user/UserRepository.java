package kickstart.user;

import kickstart.user.User.UserIdentifier;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, UserIdentifier> {

	@Override
	Streamable<User> findAll();
	/**
	 * Suchen basierend auf dem Benutzernamen des UserAccount
	 * @param username als String
	 */

	Optional<User> findByUserAccount_Username(String username);
	/**
	 * Suchen basierend auf der UserIdentifier
	 * @param userIdentifier als UserIdentifier
	 */
	Optional<User> findById(UserIdentifier userIdentifier);


}