package kickstart.profil.konto;

import kickstart.community.CommunityRepository;
import kickstart.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class MessageManagement {

	private final MessageRepository messageRepository;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");



	public MessageManagement(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	/**
	 * adds a Message to the Repository
	 * @param message
	 */
	public void addMessage(Message message) {
		String date = LocalDateTime.now().format(formatter);
		message.setDate(date);
		messageRepository.save(message);
	}

	/**
	 * returns the Messages for a user based on Userid
	 * @param UserId
	 * @return List of messages
	 */
	public List<Message> getMessagesByUserId(User.UserIdentifier UserId) {
		return messageRepository.findByUserId(UserId);

	}
}
