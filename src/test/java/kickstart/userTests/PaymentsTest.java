package kickstart.userTests;
import kickstart.user.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class PaymentsTest {
	private Payments payments;
	private User mockUser;

	@BeforeEach
	void setUp() {
		mockUser = mock(User.class);
		payments = new Payments("2023-01-01", 100.0, PaymentType.FUSSBALLTOTO, mockUser, 500.0);
	}

	@Test
	void testConstructor() {
		assertEquals("2023-01-01", payments.getDateTime());
		assertEquals(100.0, payments.getAmount(), 0.01);
		assertEquals(PaymentType.FUSSBALLTOTO, payments.getType());
		assertSame(mockUser, payments.getUser());
		assertEquals(500.0, payments.getBalance(), 0.01);
	}

	@Test
	void testGetAmountAsString() {
		assertEquals("+100.0", payments.getAmountAsString(), String.valueOf(0.01));
	}

	@Test
	void testGetBalance() {
		assertEquals(500.0, payments.getBalance(), 0.01);
	}

	@Test
	void testConstructorWithNegativeAmount() {
		payments = new Payments("2023-01-02", -50.0, PaymentType.LOTTO, mockUser, 550.0);
		assertEquals("-50.0", payments.getAmountAsString(), String.valueOf(0.001));
		assertEquals(550.0, payments.getBalance(), 0.001);
	}

	@Test
	void testConstructorWithZeroAmount() {
		payments = new Payments("2023-01-03", 0.0, PaymentType.GEMEINSCHAFTSWETTE, mockUser, 550.0);
		assertEquals("0.0", payments.getAmountAsString(), String.valueOf(0.01));
		assertEquals(550.0, payments.getBalance(), 0.01);
	}
}
