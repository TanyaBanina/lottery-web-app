package kickstart.communityTests;

import jakarta.transaction.Transactional;
import kickstart.community.CommunityBettingInfo;
import kickstart.user.RegistrationForm;
import kickstart.user.User;


import kickstart.user.UserManagement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class CommunityBettingInfoTest {

	private CommunityBettingInfo sut;
	private User user1;
	@Autowired
	private UserManagement userManagement;


	@BeforeEach
	void setUp() {
		RegistrationForm form = new RegistrationForm("user1", "1", 100.00, null);
		user1 = userManagement.createUser(form);
		form = new RegistrationForm("user2", "1", 100.00, null);
		User user2 = userManagement.createUser(form);


		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		when(authentication.isAuthenticated()).thenReturn(true);
		when(authentication.getName()).thenReturn("testUser");
		when(securityContext.getAuthentication()).thenReturn(authentication);

		SecurityContextHolder.setContext(securityContext);

		sut = new CommunityBettingInfo(
			50.0,
			List.of(user1, user2),
			123L,
			"Soccer Game",
			"01/12/2024",
			user2,
			0.0,
			"08/12/2024"
		);
	}


	@Test
	void testAddNewCommunityMember() {
		RegistrationForm form = new RegistrationForm("user3", "1", 100.00, null);
		User user3 = userManagement.createUser(form);
		sut.addNewCommunityMember(user3);


		assertEquals(0, sut.getParticipants());
		assertTrue(sut.getUserBets().containsKey("user3"));
		assertEquals(0.0, sut.getUserBets().get("user3"));
	}

	@Test
	void testAddUserBet() {
		sut.addUserBet("user1", 100.0);
		Map<String, Double> userBets = sut.getUserBets();

		assertEquals(100.0, userBets.get("user1"));
		assertEquals(100.0, sut.getBetAmount());
	}

	@Test
	void testSetAndGetBasePrice() {
		sut.setBasePrice(75.0);
		assertEquals(75.0, sut.getBasePrice());
	}

	@Test
	void testSetAndGetDrawDate() {
		sut.setDrawDate("10/12/2024");
		assertEquals("10/12/2024", sut.getDrawDate());
	}

	@Test
	void testSetAndGetGameName() {
		sut.setGameName("Lotto");
		assertEquals("Lotto", sut.getGameName());
	}

	@Test
	void GetParticipantsEmpty() {
		assertEquals(0, sut.getParticipants());
	}

	@Test
	void getParticipantsAfterBetAdded() {
		sut.addUserBet("user1", 100.0);
		sut.addUserBet("user2", 0.0);

		assertEquals(1, sut.getParticipants());
	}

	@Test
	void testSetAndGetUserBets() {
		Map<String, Double> newUserBets = Map.of(
			"user1", 200.0,
			"user2", 300.0
		);
		sut.setUserBets(newUserBets);
		assertEquals(newUserBets, sut.getUserBets());
	}

	@Test
	void testSetAndGetBetAmount() {
		sut.setBetAmount(500.0);
		assertEquals(500.0, sut.getBetAmount());
	}

	@Test
	void testConstructorInitialValues() {
		assertEquals(0, sut.getParticipants());
		assertEquals("Soccer Game", sut.getGameName());
		assertEquals(50.0, sut.getBasePrice());
		assertEquals("01/12/2024", sut.getSubmittedDate());
		assertEquals(0.0, sut.getBetAmount());
		assertTrue(sut.getUserBets().containsKey("user1"));
		assertTrue(sut.getUserBets().containsKey("user2"));
	}

	@Test
	void testRemoveUser() {

		sut.addUserBet("user1", 100.0);
		int userBetsSize = sut.getUserBets().size();
		double amount = sut.getBetAmount();
		double delta = 1e-2;

		sut.removeUser(user1);

		assertEquals(userBetsSize, sut.getUserBets().size() + 1);
		assertTrue(Math.abs(amount - sut.getBetAmount() - 100.0) <= delta);

	}


}