package kickstart.userTests;
import kickstart.user.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class BankAccountTest {


	private BankAccount account;
	@BeforeEach
	void setUp() {
		// Jeder Test startet mit einem Konto mit 100.0 initialem Guthaben
		account = new BankAccount(100.0);
	}

	@Test
	void testConstructorWithPositiveBalance() {
		assertEquals(100.0, account.getBalance(), "Das Startguthaben sollte korrekt initialisiert werden.");
	}

	@Test
	void testConstructorWithNegativeBalance() {
		//wirft exception wenn negativ
		IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class,
			() -> new BankAccount(-100.0),
			"Negatives Startguthaben sollte eine Exception auslösen."
		);
		assertEquals("Startguthaben darf nicht negativ sein.", exception.getMessage());
	}

	@Test
	void testDepositWithNonPositiveAmountReturnsEmpty() {

		double balance = account.getBalance();
		account.deposit(-50.0);
		assertEquals(balance, account.getBalance());
	}


	//testen getBalancce und deposit/withdraw
	@Test
	void testGetBalanceAfterDeposit() {
		account.deposit(50.0); // Einzahlung
		assertEquals(150.0, account.getBalance(), "getBalance sollte den neuen Kontostand nach der Einzahlung zurückgeben.");
	}

	@Test
	void testGetBalanceAfterWithdrawal() {
		account.withdraw(30.0); // Abhebung
		assertEquals(70.0, account.getBalance(), "getBalance sollte den neuen Kontostand nach der Abhebung zurückgeben.");
	}
}
