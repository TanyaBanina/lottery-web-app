package kickstart.voucherTests;

import kickstart.voucher.Voucher;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VoucherTest {

	@Test
	void testVoucherCreation() {
		//neeues Voucher-Objekt erstellen
		Voucher voucher = new Voucher(10.99, "ABC123", null);

		//Eingaben überprüfen
		assertEquals(10.99, voucher.getAmount(), 0.01);
		assertEquals("ABC123", voucher.getVoucherCode());


	}

	@Test
	void testDefaultConstructor() {

		//Erstellen eines neues Voucher-Objekts mit Standardkonstruktor
		Voucher voucher = new Voucher();

		//Überprüfen, ob alle Felder null sind
		assertNull(voucher.getAmount());
		assertNull(voucher.getVoucherCode());
		assertNull(voucher.getVoucherCode());
	}

	@Test
	void testGetterMethods() {
		Voucher voucher = new Voucher(20.00, "DEF456", null);

		assertEquals(20.00, voucher.getAmount(), 0.01);
		assertEquals("DEF456", voucher.getVoucherCode());
	}

}
