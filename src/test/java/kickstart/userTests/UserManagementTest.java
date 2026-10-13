package kickstart.userTests;

import kickstart.user.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.*;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserManagementTest {

	@Autowired
	private UserManagement userManagement;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PaymentsRepository paymentsRepository;

	User user;

	@BeforeEach
	void setup() {
		RegistrationForm form = new RegistrationForm("testUser", "1", 100.00, null);
		this.user = userManagement.createUser(form);


		//mock Verhalten für authentication und Security -> eingeloggten User simulieren
		Authentication authentication = mock(Authentication.class);
		SecurityContext securityContext = mock(SecurityContext.class);

		when(authentication.isAuthenticated()).thenReturn(true);
		when(authentication.getName()).thenReturn("testUser");
		when(securityContext.getAuthentication()).thenReturn(authentication);

		SecurityContextHolder.setContext(securityContext);

	}

	@Test
	void testCreateUser() {
		RegistrationForm form = new RegistrationForm("testcreateUser", "testPass", 100.0, null);
		User createdUser = userManagement.createUser(form);

		//tests
		assertNotNull(createdUser);
		assertEquals(100.0, createdUser.getBankAccount().getBalance());
	}


	@Test
	void testGetLoggedInUser() {
		User loggedInUser = userManagement.getLoggedInUser();

		//tests
		assertNotNull(loggedInUser);
		assertEquals(user, loggedInUser);
		assertEquals("testUser", loggedInUser.getUserAccount().getUsername());
	}

	@Test
	void testGetLoggedInUserNotFound() {

		SecurityContextHolder.setContext(new SecurityContextImpl());
		Exception exception = assertThrows(IllegalStateException.class, userManagement::getLoggedInUser);

		//tests
		assertEquals("Kein gültiger Benutzer ist eingeloggt.", exception.getMessage());
	}

	@Test
	void testChangeUsername() {

		userManagement.changeUsername(user.getId().getIdentifier(), "newName");

		//tests
		assertEquals("newName", user.getUserAccount().getUsername());
	}

	@Test
	void testChangeUsernameAlreadyExists() {

		userManagement.createUser(new RegistrationForm("existingUsername", "1", 100.00, null));
		Exception exception = assertThrows(IllegalArgumentException.class,
			() -> userManagement.changeUsername(user.getId().getIdentifier(), "existingUsername"));
		// tests
		assertEquals("Der Benutzername ist bereits vergeben.", exception.getMessage());
	}

	@Test
	void testDepositToLoggedInUser() {
		userManagement.depositToLoggedInUser(50.0, PaymentType.FUSSBALLTOTO);

		//tests
		assertEquals(150.0, user.getBankAccount().getBalance(), 0.01);
	}


	@Test
	void testWithdrawFromLoggedInUser() {
		userManagement.withdrawFromLoggedInUser(30.0, PaymentType.FUSSBALLTOTO);

		//tests
		assertEquals(70.0, user.getBankAccount().getBalance(), 0.01);

	}


	@Test
	void testDepositToUser() {
		Double amount = 50.0;
		userManagement.depositToUser(user.getId().getIdentifier(), amount, PaymentType.LOTTO);

		//tests
		assertEquals(150.0, user.getBankAccount().getBalance());

		//test: speichern
		List<Payments> transactions = paymentsRepository.findByUser_Id(user.getId());
		assertFalse(transactions.isEmpty(), "Es sollte mindestens eine Transaktion im Repository vorhanden sein.");
		assertEquals(PaymentType.LOTTO, transactions.get(1).getType(), "Der Transaktionstyp sollte 'Lotto' sein.");
		assertEquals(amount, transactions.get(1).getAmount(), "Der Transaktionsbetrag sollte korrekt sein.");

		userManagement.depositToUser(user.getId().getIdentifier(), amount, PaymentType.LOTTO);
		userManagement.depositToUser(user.getId().getIdentifier(), amount, PaymentType.LOTTO);

		//test mehr einträge
		assertEquals(250.0, user.getBankAccount().getBalance(), 0.01);
		transactions = paymentsRepository.findByUser_Id(user.getId());
		assertEquals(4, transactions.size(), "Liste sollte drei Einträge haben.");

		// Überprüfe die Transaktionsdetails für jede Einzahlung
		for (int i = 1; i < transactions.size(); i++) {
			assertEquals(PaymentType.LOTTO, transactions.get(i).getType(), "Der Transaktionstyp sollte 'Lotto' sein.");
			assertEquals(amount, transactions.get(i).getAmount(), "Der Transaktionsbetrag sollte korrekt sein.");
		}
	}
	@Test
	void testWithdrawFromUser() {
		Double amount = 10.0;
		userManagement.withdrawToUser(user.getId().getIdentifier(), amount, PaymentType.LOTTO);

		//tests
		assertEquals(90.0, user.getBankAccount().getBalance());

		//test: speichern
		List<Payments> transactions = paymentsRepository.findByUser_Id(user.getId());
		assertFalse(transactions.isEmpty(), "Es sollte mindestens eine Transaktion im Repository vorhanden sein.");
		assertEquals(PaymentType.LOTTO, transactions.get(1).getType(), "Der Transaktionstyp sollte 'Lotto' sein.");
		assertEquals(amount, -transactions.get(1).getAmount(), "Der Transaktionsbetrag sollte korrekt sein.");

		userManagement.withdrawToUser(user.getId().getIdentifier(), amount, PaymentType.LOTTO);
		userManagement.withdrawToUser(user.getId().getIdentifier(), amount, PaymentType.LOTTO);

		//test mehr einträge
		assertEquals(70.0, user.getBankAccount().getBalance(), 0.01);
		transactions = paymentsRepository.findByUser_Id(user.getId());
		assertEquals(4, transactions.size(), "Liste sollte drei Einträge haben.");

		// Überprüfe die Transaktionsdetails für jede Einzahlung
		for (int i = 1; i < transactions.size(); i++) {
			assertEquals(PaymentType.LOTTO, transactions.get(i).getType(), "Der Transaktionstyp sollte 'Lotte' sein.");
			assertEquals(amount, -transactions.get(i).getAmount(), "Der Transaktionsbetrag sollte korrekt sein.");
		}

	}

	@Test
	void testFindAllUsers() {
		userManagement.createUser(new RegistrationForm("anotherUser", "password", 50.0, null));

		List<User> allUsers = userManagement.findAll().toList();

		// Tests
		assertNotNull(allUsers);
		assertEquals(9, allUsers.size(), "Es sollten neun Benutzer vorhanden sein.");
		assertTrue(allUsers.stream().anyMatch(u -> u.getUserAccount().getUsername().equals("testUser")));
		assertTrue(allUsers.stream().anyMatch(u -> u.getUserAccount().getUsername().equals("anotherUser")));
	}
	@Test
	void testGetTransactionsForLoggedInUser() {
		userManagement.depositToLoggedInUser(100.0, PaymentType.LOTTO);
		userManagement.withdrawFromLoggedInUser(50.0, PaymentType.LOTTO);

		List<Payments> transactions = userManagement.getTransactionsForLoggedInUser();

		// Tests
		assertNotNull(transactions);
		assertEquals(3, transactions.size(), "Es sollten drei Transaktionen vorhanden sein (Initial, Deposit, Withdraw).");
		assertEquals(PaymentType.LOTTO, transactions.get(1).getType());
		assertEquals(100.0, transactions.get(1).getAmount(), 0.01);
		assertEquals(PaymentType.LOTTO, transactions.get(2).getType());
		assertEquals(-50.0, transactions.get(2).getAmount(), 0.01);

	}

	@Test
	void testGetUsers(){
		userManagement.createUser(new RegistrationForm("anotherUser", "password", 50.0, null));
		UserRepository allUsers = userManagement.getUsers();
		assertNotNull(allUsers);

		User userByName = userManagement.getUserByName("anotherUser");
		assertEquals("anotherUser", userByName.getUserAccount().getUsername());

	}


	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
	}
}