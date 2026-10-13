package kickstart.communityTests;

import kickstart.community.JoinCommunityForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class JoinCommunityFormTest {

	private JoinCommunityForm sut;

	@BeforeEach
	void setUp() {
		sut = new JoinCommunityForm();
	}

	@Test
	void testGetNameAndSetName() {
		String name = "CommunityName";
		sut.setName(name);
		assertEquals(name, sut.getName());
	}

	@Test
	void testGetPasswordAndSetPassword() {
		String password = "password123";
		sut.setPassword(password);
		assertEquals(password, sut.getPassword());
	}

	@Test
	void testDefaultConstructor() {
		JoinCommunityForm sut = new JoinCommunityForm();
		assertNull(sut.getName());
		assertNull(sut.getPassword());
	}

}
