package kickstart.userTests;

import kickstart.user.*;
import kickstart.community.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.Password;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountManagement;

import java.util.*;


@SpringBootTest
class UserTest {

	private UserAccountManagement userAccountManagement; // Mock für UserAccountManager
	private UserAccount mockUserAccount; // Mock für UserAccount
	private BankAccount mockBankAccount; // Mock für BankAccount
	@Autowired
	private UserManagement userManagement;


	@BeforeEach
	void setUp() {
		userAccountManagement = mock(UserAccountManagement.class);
		mockUserAccount = mock(UserAccount.class);
		mockBankAccount = mock(BankAccount.class);

		// Verhalten der Mocks definieren
		when(userAccountManagement.create("testuser", Password.UnencryptedPassword.of("password"), Role.of("USER")))
			.thenReturn(mockUserAccount);
		when(mockBankAccount.getBalance()).thenReturn(100.0);
	}

	@Test
	void testUserConstructor() {
		User user = new User(mockUserAccount, mockBankAccount);

		// Verifizierungen
		assertNotNull(user.getId(), "User ID sollte generiert werden.");
		assertEquals(mockUserAccount, user.getUserAccount(), "UserAccount sollte korrekt gesetzt werden.");
		assertEquals(mockBankAccount, user.getBankAccount(), "BankAccount sollte korrekt gesetzt werden.");
		assertTrue(user.getCommunities().isEmpty(), "Communities-Liste sollte initial leer sein.");
	}

	@Test
	void testGetUserAccount() {

		// User-Objekt erstellen
		User user = new User(mockUserAccount, mockBankAccount);

		// Act
		UserAccount actualUserAccount = user.getUserAccount();

		// Verifizierung
		assertNotNull(actualUserAccount, "getUserAccount() sollte nicht null zurückgeben.");
		assertEquals(mockUserAccount, actualUserAccount, "getUserAccount() sollte den korrekten UserAccount zurückgeben.");
	}

	@Test
	void testGetBankAccount() {
		User user = new User(mockUserAccount, mockBankAccount);
		BankAccount actualBankAccount = user.getBankAccount();

		// Verifizierung
		assertNotNull(actualBankAccount, "getBankAccount() sollte nicht null zurückgeben.");
	}

	@Test
	void testSetBankAccount() {

		User user = new User(mockUserAccount, mockBankAccount);
		BankAccount newBankAccount = mock(BankAccount.class);
		when(newBankAccount.getBalance()).thenReturn(200.0);

		user.setBankAccount(newBankAccount);

		assertEquals(newBankAccount, user.getBankAccount(), "Der BankAccount sollte korrekt gesetzt werden.");
	}
	@Test
	void testGetIdentifier() {
		User user = new User(mockUserAccount, mockBankAccount);

		assertNotNull(user.getId(), "Die ID sollte nicht null sein.");
		assertNotNull(user.getId().getIdentifier(), "Der Identifier sollte nicht null sein.");

		String actualUuidString = user.getId().getIdentifier().toString();
		assertTrue(actualUuidString.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"),
			"Der Identifier sollte eine gültige UUID-Format sein.");
	}
	@Test
	void testCommunityMethodsInUser() {
		User user = new User(mockUserAccount, mockBankAccount);

		assertNotNull(user.getCommunities(), "Die Communities-Liste sollte nicht null sein.");
		assertTrue(user.getCommunities().isEmpty(), "Die Communities-Liste sollte anfangs leer sein.");

		Community community1 = new Community(user, "Community 1", "password1");
		Community community2 = new Community(user, "Community 2", "password2");

		//Test getCommunities
		user.getCommunities().add(community1);
		user.getCommunities().add(community2);
		assertEquals(2, user.getCommunities().size(), "Sollte 2 zurückgeben");
		//Test removeCommunities
		user.removeCommunity(community1);
		assertEquals(1, user.getCommunities().size(), "Die Liste sollte nur noch 1 Community enthalten.");
		assertFalse(user.getCommunities().contains(community1), "Community 1 sollte entfernt worden sein.");
		//Test der setCommunity
		List<Community> newCommunityList = List.of(community1);  // Erstellen einer neuen Liste
		user.setCommunity(newCommunityList);  // Liste der Communities des Users setzen

		assertEquals(1, user.getCommunities().size(), "Die Liste sollte nun 1 Community enthalten.");
		assertTrue(user.getCommunities().contains(community1), "Community 1 sollte in der Liste enthalten sein.");

	}

}