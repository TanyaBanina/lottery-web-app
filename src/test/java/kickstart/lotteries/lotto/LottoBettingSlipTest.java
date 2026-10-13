package kickstart.lotteries.lotto;

import kickstart.lotteries.lotto.results.PastResultsRepository;
import kickstart.lotteries.lotto.slips.LottoBettingSlip;
import kickstart.lotteries.lotto.slips.LottoBettingSlipRepository;
import kickstart.lotteries.lotto.results.Result;
import kickstart.lotteries.lotto.slips.LottoNumbers;
import kickstart.user.User;
import kickstart.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LottoBettingSlipTest {

	private LottoBettingSlip slip;
	private final LocalDateTime now = LocalDateTime.now();

	@BeforeEach
	void setUp() {
		List<LottoNumbers> numbersList = List.of(
			new LottoNumbers(Set.of(1, 2, 3, 4, 5, 6), 1),
			new LottoNumbers(Set.of(7, 8, 9, 10, 11, 12), 2)
		);
		slip = new LottoBettingSlip(now, new User.UserIdentifier(UUID.randomUUID()), 100L, 10.0, numbersList, 5) {
			// abstract class inst workaround
		};
	}

	@Test
	void testSetAndGetCommunityId() {
		slip.setCommunityId(200L);
		assertEquals(200L, slip.getCommunityId());
	}

	@Test
	void testSetAndGetSuperNumber() {
		slip.setSuperNumber(7);
		assertEquals(7, slip.getSuperNumber());
	}

	@Test
	void testGetNumbersList() {
		assertNotNull(slip.getNumbersList());
		assertEquals(2, slip.getNumbersList().size());
	}

	@Test
	void testIsValid() {
		assertTrue(slip.isValid());
	}

	@Test
	void testIsValidExceedsMaxSlots() {
		List<LottoNumbers> invalidNumbersList = new ArrayList<>();
		for (int i = 0; i < 13; i++) {
			invalidNumbersList.add(new LottoNumbers(Set.of(1, 2, 3, 4, 5, 6), i + 1));
		}
		slip.setNumbersList(invalidNumbersList);
		assertFalse(slip.isValid());
	}

	@Test
	void testIsValidNumbersListIsNull() {
		slip.setNumbersList(null);
		assertFalse(slip.isValid());
	}

	@Test
	void testIsValidSetWithInvalidSize() {
		List<LottoNumbers> invalidNumbersList = new ArrayList<>();
		invalidNumbersList.add(new LottoNumbers(Set.of(1, 2, 3, 4, 5), 1));
		slip.setNumbersList(invalidNumbersList);
		assertFalse(slip.isValid());
	}

	@Test
	void testIsValidSetWithNumberOutOfRange() {
		List<LottoNumbers> invalidNumbersList = new ArrayList<>();
		invalidNumbersList.add(new LottoNumbers(Set.of(1, 2, 3, 4, 5, 50), 1));
		slip.setNumbersList(invalidNumbersList);
		assertFalse(slip.isValid());
	}

	@Test
	void testIsValidSetWithNegativeNumber() {
		List<LottoNumbers> invalidNumbersList = new ArrayList<>();
		invalidNumbersList.add(new LottoNumbers(Set.of(1, 2, 3, 4, 5, -1), 1));
		slip.setNumbersList(invalidNumbersList);
		assertFalse(slip.isValid());
	}

	@Test
	void testSetNumbersForSlot() {
		Set<Integer> newNumbers = Set.of(13, 14, 15, 16, 17, 18);
		slip.setNumbersForSlot(0, newNumbers, false);
		assertEquals(newNumbers, slip.getNumbersForSlot(0));
	}

	@Test
	void testSetNumbersForSlotOutOfBoundsWithoutAdd() {
		Set<Integer> newNumbers = Set.of(13, 14, 15, 16, 17, 18);
		assertThrows(IndexOutOfBoundsException.class, () -> slip.setNumbersForSlot(5, newNumbers, false));
	}

	@Test
	void testSetNumbersForSlotWithNullNumbers() {
		assertThrows(IllegalArgumentException.class, () -> slip.setNumbersForSlot(0, null, false),
			"Numbers set cannot be null");
	}

	@Test
	void testSetNumbersForSlotWithInvalidSize() {
		Set<Integer> invalidNumbers = Set.of(1, 2, 3, 4);
		Exception exception = assertThrows(IllegalArgumentException.class, () -> slip.setNumbersForSlot(0, invalidNumbers, false));
		assertEquals("Exactly 6 numbers must be provided. Provided: 4", exception.getMessage());
	}

	@Test
	void testSetNumbersForSlotAddNewSlotWhenAddSetTrue() {
		List<LottoNumbers> initialNumbersList = new ArrayList<>();
		slip.setNumbersList(initialNumbersList);

		Set<Integer> newNumbers = Set.of(19, 20, 21, 22, 23, 24);
		int initialSize = slip.getNumbersList().size();
		slip.setNumbersForSlot(initialSize, newNumbers, true);

		assertEquals(initialSize + 1, slip.getNumbersList().size());
		assertEquals(newNumbers, slip.getNumbersForSlot(initialSize));
	}

	@Test
	void testSetNumbersForSlotExceedsMaxSlots() {
		List<LottoNumbers> maxSlots = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			maxSlots.add(new LottoNumbers(Set.of(1, 2, 3, 4, 5, 6), i + 1));
		}
		slip.setNumbersList(maxSlots);

		Set<Integer> newNumbers = Set.of(19, 20, 21, 22, 23, 24);
		Exception exception = assertThrows(IllegalStateException.class, () -> slip.setNumbersForSlot(12, newNumbers, true));
		assertEquals("Cannot add more than 12 slots.", exception.getMessage());
	}

	@Test
	void testGetNumbersForSlot() {
		assertEquals(Set.of(1, 2, 3, 4, 5, 6), slip.getNumbersForSlot(0));
		assertEquals(Set.of(7, 8, 9, 10, 11, 12), slip.getNumbersForSlot(1));
	}

	@Test
	void testGetNumbersForSlotOutOfBounds() {
		assertThrows(IndexOutOfBoundsException.class, () -> slip.getNumbersForSlot(5));
	}

	@Test
	void testGetNumbersForSlotNegativeIndex() {
		Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> slip.getNumbersForSlot(-1));
		assertEquals("Slot index out of bounds", exception.getMessage());
	}

	@Test
	void testIsPermanentDefault() {
		assertFalse(slip.isPermanent());
	}

	@Test
	void testGetDurationTypeDefault() {
		assertNull(slip.getDurationType());
	}

	@Test
	void testGetDurationDefault() {
		assertNull(slip.getDuration());
	}
}

