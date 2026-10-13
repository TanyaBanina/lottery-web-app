package kickstart.lotteries.lotto;

import kickstart.lotteries.lotto.results.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

	private Result result;

	@BeforeEach
	void setUp() {
		result = new Result(
			LocalDateTime.of(2023, 12, 1, 0, 0, 0, 123456789),
			Set.of(1, 2, 3, 4, 5, 6),
			7
		);
	}


	@Test
	void testConstructor() {
		assertNotNull(result);
		assertEquals(Set.of(1, 2, 3, 4, 5, 6), result.getWinningNumbers());
		assertEquals(7, result.getSuperNumber());

		LocalDateTime expectedDateTime = LocalDateTime.of(2023, 12, 1, 0, 0, 0, 123456789);
		assertEquals(expectedDateTime, result.getDrawDate());
	}

	@Test
	void testDefaultConstructor() {
		Result emptyResult = new Result();
		assertNotNull(emptyResult);
		assertNull(emptyResult.getId());
		assertNull(emptyResult.getDrawDate());
		assertNull(emptyResult.getWinningNumbers());
		assertNull(emptyResult.getSuperNumber());
	}

	@Test
	void testSetAndGetId() {
		result.setId(100L);
		assertEquals(100L, result.getId());
	}

	@Test
	void testSetAndGetDrawDate() {
		LocalDateTime newDate = LocalDateTime.of(2024, 1, 1, 0, 0);
		result.setDrawDate(newDate);
		assertEquals(newDate, result.getDrawDate());
	}


	@Test
	void testSetAndGetWinningNumbers() {
		Set<Integer> newWinningNumbers = Set.of(10, 20, 30, 40, 50, 60);
		result.setWinningNumbers(newWinningNumbers);
		assertEquals(newWinningNumbers, result.getWinningNumbers());
	}

	@Test
	void testSetAndGetSuperNumber() {
		result.setSuperNumber(10);
		assertEquals(10, result.getSuperNumber());
	}

	@Test
	void testWinningNumbersValidation() {
		Set<Integer> validNumbers = Set.of(1, 2, 3, 4, 5, 6);
		result.setWinningNumbers(validNumbers);
		assertEquals(6, result.getWinningNumbers().size());
		assertTrue(result.getWinningNumbers().contains(1));

		Set<Integer> invalidNumbers = Set.of(1, 2, 3);
		result.setWinningNumbers(invalidNumbers);
		assertNotEquals(6, result.getWinningNumbers().size());
	}

	@Test
	void testSuperNumberValidation() {
		result.setSuperNumber(0);
		assertEquals(0, result.getSuperNumber());

		result.setSuperNumber(10);
		assertEquals(10, result.getSuperNumber());
	}

	@Test
	void testWinningNumbersBoundaryConditions() {

		Set<Integer> validNumbers = Set.of(1, 2, 3, 4, 5, 49);
		result.setWinningNumbers(validNumbers);
		assertTrue(result.getWinningNumbers().contains(1));
		assertTrue(result.getWinningNumbers().contains(49));
	}

	@Test
	void testWinningNumbersOutOfRange() {
		Set<Integer> outOfRangeNumbers = Set.of(0, 50, 3, 4, 5, 6);
		result.setWinningNumbers(outOfRangeNumbers);
		assertTrue(result.getWinningNumbers().contains(50));
	}
}

