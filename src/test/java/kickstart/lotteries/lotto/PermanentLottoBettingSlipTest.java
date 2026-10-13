package kickstart.lotteries.lotto;


import kickstart.lotteries.lotto.slips.PermanentLottoBettingSlip;
import kickstart.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import kickstart.lotteries.lotto.slips.Duration;


class PermanentLottoBettingSlipTest {

	private PermanentLottoBettingSlip bettingSlip;

	@BeforeEach
	void setUp() {
		LocalDateTime dateSubmitted = LocalDateTime.now();
		bettingSlip = new PermanentLottoBettingSlip(
			dateSubmitted,
			new User.UserIdentifier(UUID.randomUUID()),
			123L,
			10.0,
			Collections.emptyList(),
			7,
			Duration.MONTHLY
		);
	}

	@Test
	void testGetEndDateMonthly() {
		LocalDateTime expectedEndDate = bettingSlip.getDateSubmitted().plusMonths(1);
		assertEquals(expectedEndDate, bettingSlip.getEndDate());
	}

	@Test
	void testGetEndDateSemiannual() {
		PermanentLottoBettingSlip bettingSlipSemiannual = new PermanentLottoBettingSlip(
			LocalDateTime.now(),
			new User.UserIdentifier(UUID.randomUUID()),
			123L,
			15.0,
			Collections.emptyList(),
			8,
			Duration.SEMIANNUAL
		);
		LocalDateTime expectedEndDate = bettingSlipSemiannual.getDateSubmitted().plusMonths(6);
		assertEquals(expectedEndDate, bettingSlipSemiannual.getEndDate());
	}

	@Test
	void testGetEndDateAnnual() {
		PermanentLottoBettingSlip bettingSlipAnnual = new PermanentLottoBettingSlip(
			LocalDateTime.now(),
			new User.UserIdentifier(UUID.randomUUID()),
			123L,
			20.0,
			Collections.emptyList(),
			9,
			Duration.ANNUALY
		);
		LocalDateTime expectedEndDate = bettingSlipAnnual.getDateSubmitted().plusYears(1);
		assertEquals(expectedEndDate, bettingSlipAnnual.getEndDate());
	}

	@Test
	void testGetDurationType() {
		assertEquals("MONTHLY", bettingSlip.getDurationType());
	}

	@Test
	void testSetDuration() {
		bettingSlip.setDuration(Duration.SEMIANNUAL);
		assertEquals(Duration.SEMIANNUAL, bettingSlip.getDuration());
	}

	@Test
	void testConstructorWithNullDuration() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			new PermanentLottoBettingSlip(
				LocalDateTime.now(),
				new User.UserIdentifier(UUID.randomUUID()),
				123L,
				10.0,
				Collections.emptyList(),
				7,
				null
			);
		});
		assertEquals("Duration cannot be null", exception.getMessage());
	}

}

