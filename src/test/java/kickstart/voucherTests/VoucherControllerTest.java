package kickstart.voucherTests;

import kickstart.voucher.Voucher;
import kickstart.voucher.VoucherController;
import kickstart.voucher.VoucherManagement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class VoucherControllerTest {

	@Mock
	private VoucherManagement voucherManagement;

	@InjectMocks
	private VoucherController controller;

	private MockMvc mockMvc;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void testBuyVoucher() throws Exception {
		// Arrange
		double amount = 10.99;
		String voucherCode = "ABC123";

		Voucher voucher = new Voucher(amount, voucherCode, null);
		when(voucherManagement.createVoucher(anyDouble())).thenReturn(voucher);

		// Act & Assert
		mockMvc.perform(post("/voucher/buy")
				.param("amount", "10.99")
				.param("voucherCode", "ABC123"))
			.andExpect(status().isFound())
			.andExpect(view().name("redirect:/konto"));
	}

}

