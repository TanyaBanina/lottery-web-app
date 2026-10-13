package kickstart.communityTests;

import kickstart.community.CommunityForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class CommunityFormTest {

	private CommunityForm sut;

	@BeforeEach
	void setUp() {
		sut = new CommunityForm();
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
		CommunityForm newCommunityForm = new CommunityForm();
		assertNull(newCommunityForm.getName());
		assertNull(newCommunityForm.getPassword());
	}
}
