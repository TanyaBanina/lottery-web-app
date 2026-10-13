package kickstart.userTests;

import kickstart.user.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;



@SpringBootTest
@Transactional
class UserManagementIntegrationTest {

	@Autowired
	private UserManagement userManagement;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PaymentsRepository paymentsRepository;

	User user;

	@BeforeEach
	void setUp() {
		RegistrationForm form = new RegistrationForm("testUser", "1", 100.00, null);
		this.user = userManagement.createUser(form);
	}

	@Test
	void testDepositToUserIntegration() {

		// Einzahlung durchführen
		userManagement.depositToUser(user.getId().getIdentifier(), 50.0, PaymentType.FUSSBALLTOTO);

		// Prüfen, ob der Kontostand aktualisiert wurde
		User updatedUser = userRepository.findById(user.getId()).orElseThrow();
		assertEquals(150.0, updatedUser.getBankAccount().getBalance());

		// Prüfen, ob die Transaktion gespeichert wurde
		var transactions = paymentsRepository.findByUser_Id(user.getId());
		assertEquals(2, transactions.size());
		assertEquals(PaymentType.FUSSBALLTOTO, transactions.get(1).getType());
	}
	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
	}
}