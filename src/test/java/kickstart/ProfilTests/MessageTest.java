package kickstart.ProfilTests;

import kickstart.profil.konto.Message;
import kickstart.user.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;


public class MessageTest {
	@Test
	void testMessageConstructor() {
		User.UserIdentifier userId = mock(User.UserIdentifier.class);
		String content = "This is a test message";

		Message message = new Message(content, userId);

		assertEquals(content, message.getContent());
		assertEquals(userId, message.getUserId());
		assertNull(message.getDate());
	}

	@Test
	void testSettersAndGetters() {
		Message message = new Message();

		String content = "Another test message";
		String date = "2025-01-13";

		message.setContent(content);
		message.setDate(date);

		assertEquals(content, message.getContent());
		assertEquals(date, message.getDate());
		assertNull(message.getUserId());

		message.setDate("2025-02-01");
		assertEquals("2025-02-01", message.getDate());

		message.setContent("Updated content");
		assertEquals("Updated content", message.getContent());
	}

	@Test
	void testDefaultConstructor() {
		Message message = new Message();
		assertNull(message.getContent());
		assertNull(message.getUserId());
		assertNull(message.getDate());
	}
}
