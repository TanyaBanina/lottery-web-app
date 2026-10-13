package kickstart.userTests;

import kickstart.user.*;


import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;


class RegistrationFormTest {

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);

	}

	@Test
		void testRegistrationForm() {
		// Test constructor
		RegistrationForm form = new RegistrationForm("John Doe", "securePassword123!", 100.00, null);
		assertEquals("John Doe", form.getName());
		assertEquals("securePassword123!", form.getPassword());
		assertEquals(100.00, form.getInitialBalance(), 0.01);

		// Test setter methods
		form.setUsername("Jane Smith");
		assertEquals("Jane Smith", form.getName());

	}

	@Test
	void testgetName(){
		// Erstellen eines neuen RegistrationForm Objekts mit einem Namen
		RegistrationForm form = new RegistrationForm("John Doe", "password123!", 100.00, null);

		// Überprüfen des Namens
		String expectedName = "John Doe";
		String actualName = form.getName();

		assertEquals(expectedName, actualName,
			"Der Name sollte 'John Doe' entsprechen");
	}

	@Test
	void testgetPassword(){
		// Erstellen eines neuen RegistrationForm Objekts mit einem Passwort
		RegistrationForm form = new RegistrationForm("John Doe", "securePassword123!", 100.00, null);

		// Überprüfen des Passworts
		String expectedPassword = "securePassword123!";
		String actualPassword = form.getPassword();

		assertEquals(expectedPassword, actualPassword,
			"Das Passwort sollte 'securePassword123!' entsprechen");

		// Testen mit einem anderen Passwort
		RegistrationForm form2 = new RegistrationForm("Jane Doe", "anotherSecurePassword456!", 50.00, null);
		String anotherExpectedPassword = "anotherSecurePassword456!";
		String anotherActualPassword = form2.getPassword();

		assertEquals(anotherExpectedPassword, anotherActualPassword,
			"Ein anderes Passwort sollte korrekt zurückgegeben werden");
	}

	@Test
	void testgetInitialBalance(){
		// Testen mit einem positiven Wert
		RegistrationForm form = new RegistrationForm("John Doe", "password123!", 100.00, null);
		assertEquals(100.00, form.getInitialBalance(),
			"Der Anfangsbetrag sollte 100.00 entsprechen");

		// Testen mit einem negativen Wert
		RegistrationForm formNegative = new RegistrationForm("Jane Doe", "anotherPassword!", -50.00, null);
		assertEquals(-50.00, formNegative.getInitialBalance(),
			"Ein negativer Anfangsbetrag sollte korrekt zurückgegeben werden");

		// Testen mit einem Null-Wert
		RegistrationForm formZero = new RegistrationForm("Bob Smith", "securePassword!", 0.00, null);
		assertEquals(0.00, formZero.getInitialBalance(),
			"Ein Anfangsbetrag von 0 sollte korrekt zurückgegeben werden");

		// Testen mit einem sehr großen Wert
		RegistrationForm formLarge = new RegistrationForm("Alice Johnson", "largeAmountPassword!", 99999999.99, null);
		assertEquals(99999999.99, formLarge.getInitialBalance(),
			"Ein sehr großer Anfangsbetrag sollte korrekt zurückgegeben werden");
	}

	@Test
	void testsetUsername(){
		// Erstellen eines neuen RegistrationForm Objekts
		RegistrationForm form = new RegistrationForm("John Doe", "password123!", 100.00, null);

		// Setzen eines neuen Benamens
		String newName = "Jane Smith";
		form.setUsername(newName);

		// Überprüfen des neuen Namens
		assertEquals(newName, form.getName(),
			"Der Benutzername sollte nach dem Setzen korrekt aktualisiert werden");

		// Testen mit einem leeren String
		form.setUsername("");
		assertEquals("", form.getName(),
			"Ein leerer String sollte ebenfalls akzeptiert werden");

		// Testen mit einem null-Wert
		form.setUsername(null);
		assertNull(form.getName(),
			"Null sollte als gültiger Benutzername akzeptiert werden");
	}
}

