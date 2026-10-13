package kickstart.voucherTests;

import kickstart.user.*;
import kickstart.voucher.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VoucherManagementTest {

	@Mock
	private VoucherRepository voucherRepo;

	@Mock
	private UserManagement userManagement;

	@InjectMocks
	private VoucherManagement voucherManagement;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testRedeemVoucher() {
		// Arrange
		String voucherCode = "ABC123";
		Voucher voucher = new Voucher(10.99, voucherCode, null);

		when(voucherRepo.findByVoucherCode(anyString()))
			.thenReturn(Optional.of(voucher));

		// Act
		voucherManagement.redeemVoucher(voucherCode);

		// Assert
		verify(voucherRepo).delete(voucher);
		verify(userManagement).depositToLoggedInUser(eq(10.99), eq(PaymentType.GUTSCHRIFT));
	}

}
