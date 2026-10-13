package kickstart.lotteries.lotto;

import static org.junit.jupiter.api.Assertions.*;

import kickstart.lotteries.lotto.slips.LottoNumbers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class LottoNumbersTest {

	private LottoNumbers lottoNumbers;

	@BeforeEach
	void setUp() {
		Set<Integer> numbers = new HashSet<>();
		numbers.add(1);
		numbers.add(2);
		numbers.add(3);
		numbers.add(4);
		numbers.add(5);
		numbers.add(6);
		lottoNumbers = new LottoNumbers(numbers, 1);
	}

	@Test
	void testConstructorAndGetters() {

		Set<Integer> expectedNumbers = new HashSet<>();
		expectedNumbers.add(1);
		expectedNumbers.add(2);
		expectedNumbers.add(3);
		expectedNumbers.add(4);
		expectedNumbers.add(5);
		expectedNumbers.add(6);

		assertEquals(expectedNumbers, lottoNumbers.getNumbers(), "The numbers should match the ones passed to the constructor.");
		assertEquals(1, lottoNumbers.getSlotId(), "The slotId should match the one passed to the constructor.");
	}

	@Test
	void testSetters() {

		Set<Integer> newNumbers = new HashSet<>();
		newNumbers.add(7);
		newNumbers.add(8);
		newNumbers.add(9);
		lottoNumbers.setNumbers(newNumbers);
		lottoNumbers.setSlotId(2);

		assertEquals(newNumbers, lottoNumbers.getNumbers(), "The numbers should match the ones set by the setter.");
		assertEquals(2, lottoNumbers.getSlotId(), "The slotId should match the one set by the setter.");
	}

	@Test
	void testDefaultConstructor() {

		LottoNumbers defaultLottoNumbers = new LottoNumbers();

		assertNull(defaultLottoNumbers.getNumbers(), "The numbers should be null by default.");
		assertNull(defaultLottoNumbers.getSlotId(), "The slotId should be null by default.");
	}
}

