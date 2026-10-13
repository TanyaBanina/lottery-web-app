package kickstart.communityTests;

import kickstart.community.Community;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.apache.coyote.http11.Constants.a;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class CommunityTest {

	@Autowired
	private UserManagement userManagement;

	private User loggedInUser;

	@BeforeEach
	public void setUp() {
		RegistrationForm form = new RegistrationForm("testUser", "1", 100.00, null);
		this.loggedInUser = userManagement.createUser(form);

		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		when(authentication.isAuthenticated()).thenReturn(true);
		when(authentication.getName()).thenReturn("testUser");
		when(securityContext.getAuthentication()).thenReturn(authentication);

		SecurityContextHolder.setContext(securityContext);

	}

	@Test
	void constructorTest() {
		String name = "name";
		String password = "password";
		User admin = userManagement.getLoggedInUser();
		Community sut = new Community(admin, name, password);

		assertEquals(name, sut.getName());
		assertEquals(password, sut.getPassword());
		assertEquals(sut.getAdmin(), admin);
		assertEquals(1, sut.getCommunityMembers().size());
	}

	@Test
	void testGetNameAndSetName() {
		String name = "name";
		String password = "password";
		User admin = userManagement.getLoggedInUser();
		Community sut = new Community(admin, name, password);

		name = "CommunityName";
		sut.setName(name);
		assertEquals(name, sut.getName());
	}

	@Test
	void testGetPasswordAndSetPassword() {
		String name = "name";
		String password = "password";
		User admin = userManagement.getLoggedInUser();
		Community sut = new Community(admin, name, password);

		password = "password123";
		sut.setPassword(password);
		assertEquals(password, sut.getPassword());
	}

	@Test
	void testGetCommunityMembers() {
		String name = "name";
		String password = "password";
		User admin = userManagement.getLoggedInUser();
		Community sut = new Community(admin, name, password);

		assertEquals(1, sut.getCommunityMembers().size());
	}

	@Test
	void testAddCommunityMember() {
		String name = "name";
		String password = "password";
		User admin = userManagement.getLoggedInUser();
		Community sut = new Community(admin, name, password);
		RegistrationForm form = new RegistrationForm("testUser2", "1", 100.00, null);
		User user2 = userManagement.createUser(form);

		sut.addCommunityMember(user2);

		assertTrue(sut.getCommunityMembers().contains(user2));
	}

	@Test
	void testRemoveCommunityMember() {
		String name = "name";
		String password = "password";
		User admin = userManagement.getLoggedInUser();
		Community sut = new Community(admin, name, password);
		RegistrationForm form = new RegistrationForm("testUser2", "1", 100.00, null);
		User user2 = userManagement.createUser(form);
		sut.addCommunityMember(user2);
		assertTrue(sut.getCommunityMembers().contains(user2));

		sut.removeCommunityMember(user2);

		assertFalse(sut.getCommunityMembers().contains(user2));
	}

	@Test
	void testGetCommunityMemberNames() {
		String name = "name";
		String password = "password";
		User admin = userManagement.getLoggedInUser();
		Community sut = new Community(admin, name, password);
		RegistrationForm form = new RegistrationForm("testUser2", "1", 100.00, null);
		User user2 = userManagement.createUser(form);
		sut.addCommunityMember(user2);
		RegistrationForm form2 = new RegistrationForm("testUser3", "1", 100.00, null);
		User user3 = userManagement.createUser(form2);
		sut.addCommunityMember(user3);

		List<String> memberNames = sut.getCommunityMemberNames();

		assertTrue(memberNames.contains("testUser"));
		assertTrue(memberNames.contains("testUser2"));
		assertTrue(memberNames.contains("testUser3"));
		assertEquals(3, memberNames.size());
	}

	@Test
	public void testSetAdmin() {
		String name = "name";
		String password = "password";
		User admin = userManagement.getLoggedInUser();
		Community sut = new Community(admin, name, password);
		RegistrationForm form = new RegistrationForm("testUser2", "1", 100.00, null);
		User user2 = userManagement.createUser(form);
		sut.setAdmin(user2);

		assertEquals(sut.getAdmin(), user2);
	}

	@Test
	public void testShowRole() {
		String name = "name";
		String password = "password";
		User admin = userManagement.getLoggedInUser();
		Community sut = new Community(admin, name, password);
		RegistrationForm form = new RegistrationForm("testUser2", "1", 100.00, null);
		User user2 = userManagement.createUser(form);
		sut.addCommunityMember(user2);
		form = new RegistrationForm("testUser3", "1", 100.00, null);
		User user3 = userManagement.createUser(form);

		assertEquals("Admin", sut.showRole(admin));
		assertEquals("Mitglied", sut.showRole(user2));
		assertNull(sut.showRole(user3));

	}

	@Test
	public void testGetBettingInfoByBettingSlipId() {
		String name = "name";
		String password = "password";
		User admin = userManagement.getLoggedInUser();
		Community sut = new Community(admin, name, password);
		CommunityBettingInfo bettingInfo = new CommunityBettingInfo();
		bettingInfo.setBettingSlipId(1);
		CommunityBettingInfo bettingInfo2 = new CommunityBettingInfo();
		bettingInfo.setBettingSlipId(2);
		CommunityBettingInfo bettingInfo3 = new CommunityBettingInfo();
		bettingInfo.setBettingSlipId(3);
		sut.addCommunityBettingInfo(bettingInfo);
		sut.addCommunityBettingInfo(bettingInfo2);
		sut.addCommunityBettingInfo(bettingInfo3);

		assertEquals(sut.getCommunityBettingInfoByBettingSlipId(bettingInfo.getBettingSlipId()), bettingInfo);
		assertEquals(sut.getCommunityBettingInfoByBettingSlipId(bettingInfo.getBettingSlipId()), bettingInfo);
	}

	@Test
	public void TestGetOngoingBetsAndCount() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		Community sut = new Community();
		CommunityBettingInfo bettingInfo = new CommunityBettingInfo();
		bettingInfo.setDrawDate("10/01/2024");
		CommunityBettingInfo bettingInfo2 = new CommunityBettingInfo();
		bettingInfo2.setDrawDate("10/01/2024");
		CommunityBettingInfo bettingInfo3 = new CommunityBettingInfo();
		bettingInfo3.setDrawDate("10/01/2150");
		sut.addCommunityBettingInfo(bettingInfo);
		sut.addCommunityBettingInfo(bettingInfo2);
		sut.addCommunityBettingInfo(bettingInfo3);

		List<CommunityBettingInfo> bettingInfos = sut.getOngoingBets();

		assertFalse(bettingInfos.contains(bettingInfo));
		assertFalse(bettingInfos.contains(bettingInfo2));
		assertTrue(bettingInfos.contains(bettingInfo3));
		assertEquals(1, bettingInfos.size());
		assertEquals(1, sut.getOngoingBetsCount());





	}
}

