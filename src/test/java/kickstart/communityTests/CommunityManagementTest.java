package kickstart.communityTests;

import jakarta.validation.constraints.AssertTrue;
import kickstart.community.*;
import kickstart.lotteries.lotto.slips.LottoBettingSlip;
import kickstart.lotteries.lotto.slips.LottoNumbers;
import kickstart.lotteries.toto.slips.TotoBettingSlip;
import kickstart.user.RegistrationForm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cglib.core.Local;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import kickstart.user.User;
import kickstart.user.UserManagement;

import java.time.LocalDateTime;
import java.util.*;


@SpringBootTest
@Transactional
public class CommunityManagementTest {


    @Autowired
    private CommunityRepository communityRepository;

	@Autowired
    private UserManagement userManagement;

    @Autowired
    private CommunityManagement sut;

	User loggedInUser;
	User user2;
	@Autowired
	private CommunityManagement communityManagement;

	@BeforeEach
	void setup() {
		RegistrationForm form = new RegistrationForm("testUser", "1", 100.00, null);
		this.loggedInUser = userManagement.createUser(form);
		form = new RegistrationForm("testUser2", "1", 100.00, null);
		this.user2 = userManagement.createUser(form);


		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		when(authentication.isAuthenticated()).thenReturn(true);
		when(authentication.getName()).thenReturn("testUser");
		when(securityContext.getAuthentication()).thenReturn(authentication);

		SecurityContextHolder.setContext(securityContext);

	}

    @Test
    void testAddCommunityToCommunityRepository() {
        User admin = userManagement.getLoggedInUser();
        String name = "Peter";
        String password = "1234##";
        Community community = new Community(admin, name, password);

        sut.addCommunityToCommunityRepository(community);

		assertTrue(communityRepository.findAll().contains(community));
    }

    @Test
    void testConvertCommunityFormToCommunity() {
        String name = "name";
        String password = "password";
        CommunityForm communityForm = new CommunityForm();
        communityForm.setName(name);
        communityForm.setPassword(password);
		User admin = userManagement.getLoggedInUser();
        Community community = sut.convertCommunityFormToCommunity(communityForm);

        assertNotNull(community);
        assertEquals(name, community.getName());
        assertEquals(password, community.getPassword());
        assertEquals(admin, community.getAdmin());
    }

	@Test
	void testJoinCommunityByJoinCommunityFormNotEntered() {
		String name = "name";
		String password = "password";
		User user = userManagement.getLoggedInUser();
		JoinCommunityForm joinCommunityForm = new JoinCommunityForm();
		joinCommunityForm.setName(name);
		joinCommunityForm.setPassword(password);
		Community community = new Community(user2, name, password);
		sut.addCommunityToCommunityRepository(community);

		sut.joinCommunityByJoinCommunityForm(joinCommunityForm);

		assertTrue(community.getCommunityMembers().contains(user));
		assertTrue(community.getCommunityMembers().contains(user2));
	}

	@Test
	void testJoinCommunityByJoinCommunityFormAlreadyEntered() { //only enter once
		String name = "name";
		String password = "password";
		User user = userManagement.getLoggedInUser();
		JoinCommunityForm joinCommunityForm = new JoinCommunityForm();
		joinCommunityForm.setName(name);
		joinCommunityForm.setPassword(password);
		Community community = new Community(user, name, password);
		sut.addCommunityToCommunityRepository(community);

		sut.joinCommunityByJoinCommunityForm(joinCommunityForm);

		assertEquals(1, community.getCommunityMembers().size());
	}

	@Test
	void testJoinCommunityByJoinCommunityFormWrongPassword() {
		String name = "name";
		String password = "password";
		User user = userManagement.getLoggedInUser();
		JoinCommunityForm joinCommunityForm = new JoinCommunityForm();
		joinCommunityForm.setName(name);
		joinCommunityForm.setPassword("wrongPassword");
		Community community = new Community(user2, name, password);
		sut.addCommunityToCommunityRepository(community);

		sut.joinCommunityByJoinCommunityForm(joinCommunityForm);

		assertTrue((!community.getCommunityMembers().contains(user)));
	}

	@Test
	void testLeaveCommunityByNameWhenLastMemberDeletesCommunity() {
		String name = "name";
		String password = "password";
		User user = userManagement.getLoggedInUser();
		Community community = new Community(user, name, password);
		sut.addCommunityToCommunityRepository(community);

		sut.leaveCommunityByName("name");

		assertNull(sut.findCommunityByName("name"));
	}

	@Test
	void testLeaveCommunityByNameRemovesMember() {
		String name = "name";
		String password = "password";
		User user = userManagement.getLoggedInUser();
		JoinCommunityForm joinCommunityForm = new JoinCommunityForm();
		joinCommunityForm.setName(name);
		joinCommunityForm.setPassword(password);
		Community community = new Community(user2, name, password);
		sut.addCommunityToCommunityRepository(community);
		sut.joinCommunityByJoinCommunityForm(joinCommunityForm);

		assertEquals(2, community.getCommunityMembers().size());

		sut.leaveCommunityByName("name");

		assertEquals(1, community.getCommunityMembers().size());
	}

	@Test
	public void testAddCommunitySlipLotto() {
		long communityId = 1L;
		Community community = new Community();
		ReflectionTestUtils.setField(community, "id", communityId);
		communityRepository.save(community);
		List<Community> communities = communityRepository.findAll();
		List<LottoNumbers> numbersList = List.of(
			new LottoNumbers(Set.of(1, 2, 3, 4, 5, 6), 1),
			new LottoNumbers(Set.of(7, 8, 9, 10, 11, 12), 2)
		);
		LottoBettingSlip slip = new LottoBettingSlip(LocalDateTime.now(), new User.UserIdentifier(UUID.randomUUID()), communityId, 10.0, numbersList, 5) {
			//workaround for abstract class?
			@Override
			public Long getId() {
				return 1L;
			}
		};

		sut.addCommunitySlipLotto(slip);

		assertEquals(1, communityRepository.findById(communityId).getCommunityBettingInfos().size());
	}

	@Test
	public void testAddCommunitySlipToto() {
		Community community = new Community();
		community.setName("name");

		communityRepository.save(community);
		List<Community> communities = communityRepository.findAll();
		long communityId = communities.getFirst().getId();
		Map<String, String> predictions = new HashMap<>();
		TotoBettingSlip slip = new TotoBettingSlip(LocalDateTime.now(), 10, predictions, new User.UserIdentifier(UUID.randomUUID()), communityId) {
			@Override
			public Long getId() {
				return 1L;
			}
		};

		sut.addCommunitySlipToto(slip);

		int communityBettingInfosSize = communityRepository.findById(communityId).getCommunityBettingInfos().size();
		assertEquals(1, communityBettingInfosSize);

	}

	@Test
	public void testCreateNewCommunityByCommunityForm() {
		CommunityForm communityForm = new CommunityForm();
		communityForm.setName("name");
		communityForm.setPassword("password");

		sut.createNewCommunityByCommunityForm(communityForm);

		assertEquals(1, communityRepository.count());
	}

	@Test
	public void testCreateNewCommunityByJoinCommunityFormOnlyAddsOnce() {
		CommunityForm communityForm = new CommunityForm();
		communityForm.setName("name");
		communityForm.setPassword("password");

		sut.createNewCommunityByCommunityForm(communityForm);
		sut.createNewCommunityByCommunityForm(communityForm);

		assertEquals(1, communityRepository.count());
	}
}
