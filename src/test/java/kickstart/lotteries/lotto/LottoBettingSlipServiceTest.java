package kickstart.lotteries.lotto;

import kickstart.community.CommunityManagement;
import kickstart.lotteries.lotto.slips.*;
import kickstart.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LottoBettingSlipServiceTest {

	@Mock
	private Lotto lotto;

	@Mock
	private LottoBettingSlipRepository bettingSlipRepository;

	@Mock
	private CommunityManagement communityManagement;

	@Mock
	private RedirectAttributes redirectAttributes;


	private LottoBettingSlip bettingSlip;

	@Mock
	private SessionStatus sessionStatus;

	@InjectMocks
	private LottoBettingSlipService service;


	@BeforeEach
	public void setup() {

	}


	@Test
	void testGetBetById_whenBetExists() {
		Long betId = 1L;
		LottoBettingSlip mockBettingSlip = new SingleLottoBettingSlip();
		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.of(mockBettingSlip));

		LottoBettingSlip result = service.getBetById(betId);

		assertNotNull(result);
		assertEquals(mockBettingSlip, result);
		verify(bettingSlipRepository).findById(betId);
	}

	@Test
	void testGetBetById_whenBetDoesNotExist() {
		Long betId = 1L;
		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.empty());

		LottoBettingSlip result = service.getBetById(betId);

		assertNull(result);
		verify(bettingSlipRepository).findById(betId);
	}

	@Test
	void testGetBetById_whenBetIdIsNull() {
		LottoBettingSlip result = service.getBetById(null);

		assertNull(result);
		verifyNoInteractions(bettingSlipRepository);
	}


	@Test
	public void testCalculateBetAmount_WithBet() {
		double result = service.calculateBetAmount(6, "SEMIANNUAL", bettingSlip);

		assertEquals(156.0, result);
	}

	@Test
	public void testCalculateBetAmount_NoBet() {
		double result = service.calculateBetAmount(6, "SEMIANNUAL", null);

		double expectedAmount = 6 * 26 * LottoBettingSlip.SLOT_PRICE;
		assertEquals(expectedAmount, result);
	}

	@Test
	public void testCalculateBetAmount_UnknownDuration() {
		assertThrows(IllegalArgumentException.class, () -> {
			service.calculateBetAmount(6, "UNKNOWN", null);
		});
	}

	@Test
	public void testCalculateBetAmount_WithNullDuration() {
		double result = service.calculateBetAmount(6, null, null);

		double expectedAmount = 6 * LottoBettingSlip.SLOT_PRICE;
		assertEquals(expectedAmount, result);
	}

	@Test
	public void testCalculateBetAmount_WithEmptyDuration() {
		double result = service.calculateBetAmount(6, "", null);

		double expectedAmount = 6 * LottoBettingSlip.SLOT_PRICE;
		assertEquals(expectedAmount, result);
	}

	@Test
	public void testCalculateBetAmount_WithBetAndNullDuration() {
		LottoBettingSlip mockBettingSlip = mock(LottoBettingSlip.class);
		when(mockBettingSlip.getBetAmount()).thenReturn(50.0);

		double result = service.calculateBetAmount(6, null, mockBettingSlip);

		assertEquals(50.0, result);
	}

	@Test
	public void testCalculateBetAmount_DurationMonthly() {
		double result = service.calculateBetAmount(6, "MONTHLY", null);

		double expectedAmount = 6 * 4 * LottoBettingSlip.SLOT_PRICE;
		assertEquals(expectedAmount, result);
	}

	@Test
	public void testCalculateBetAmount_DurationSemiAnnual() {
		double result = service.calculateBetAmount(6, "SEMIANNUAL", null);

		double expectedAmount = 6 * 26 * LottoBettingSlip.SLOT_PRICE;
		assertEquals(expectedAmount, result);
	}

	@Test
	public void testCalculateBetAmount_DurationAnnual() {
		double result = service.calculateBetAmount(6, "ANNUALY", null);

		double expectedAmount = 6 * 52 * LottoBettingSlip.SLOT_PRICE;
		assertEquals(expectedAmount, result);
	}


	@Test
	void testGetSelectedNumbersForSlot_whenBetIdIsNotNullAndBetExists() {
		int slotId = 1;
		Long betId = 1L;
		Set<Integer> expectedNumbers = Set.of(5, 10, 15);

		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		LottoNumbers lottoNumbers = new LottoNumbers(expectedNumbers, slotId);
		LottoBettingSlip mockBettingSlip = mock(LottoBettingSlip.class);
		when(mockBettingSlip.getNumbersList()).thenReturn(List.of(lottoNumbers));

		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.of(mockBettingSlip));

		Set<Integer> result = service.getSelectedNumbersForSlot(slotId, List.of(), null, betId);

		assertEquals(expectedNumbers, result);
		verify(bettingSlipRepository).findById(betId);
	}

	@Test
	void testGetSelectedNumbersForSlot_whenBetIdIsNotNullAndBetNotFound() {
		int slotId = 1;
		Long betId = 1L;
		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.empty());

		Exception exception = assertThrows(IllegalArgumentException.class,
			() -> service.getSelectedNumbersForSlot(slotId, List.of(), null, betId));

		assertEquals("Wette ist nicht gefunden: " + betId, exception.getMessage());
		verify(bettingSlipRepository).findById(betId);
	}

	@Test
	void testGetSelectedNumbersForSlot_whenBetIdIsNullAndSelectedNumbersListContainsSlot() {
		int slotId = 1;
		Set<Integer> expectedNumbers = Set.of(3, 6, 9);
		LottoNumbers lottoNumbers = new LottoNumbers(expectedNumbers, slotId);

		Set<Integer> result = service.getSelectedNumbersForSlot(slotId, List.of(lottoNumbers), null, null);

		assertEquals(expectedNumbers, result);
	}

	@Test
	void testGetSelectedNumbersForSlot_whenBetIdIsNullAndSelectedNumbersProvided() {
		int slotId = 1;
		Set<Integer> selectedNumbers = Set.of(7, 14, 21);

		Set<Integer> result = service.getSelectedNumbersForSlot(slotId, List.of(), selectedNumbers, null);

		assertEquals(selectedNumbers, result);
	}

	@Test
	void testGetSelectedNumbersForSlot_whenAllInputsEmptyOrNull() {
		int slotId = 1;

		Set<Integer> result = service.getSelectedNumbersForSlot(slotId, List.of(), null, null);

		assertTrue(result.isEmpty());
	}


	@Test
	public void testHandleInvalidNumbersSelection() {

		List<Integer> selectedNumbers = List.of(1, 2, 3, 4, 5);
		Long betId = 1L;
		Long communityId = 2L;
		Integer slotId = 1;

		String result = service.handleInvalidNumbersSelection(redirectAttributes, selectedNumbers, slotId, betId, communityId);

		verify(redirectAttributes).addFlashAttribute("selectedNumbers", selectedNumbers);
		verify(redirectAttributes).addAttribute("communityId", communityId);
		verify(redirectAttributes).addFlashAttribute("error", "Bitte wählen Sie genau 6 Zahlen aus.");
		assertEquals("redirect:/spielen/1?slot=1", result);
	}


	@Test
	void testHandleBetSubmission_whenBetNotFound() {
		Long betId = 1L;
		List<Integer> selectedNumbers = List.of(5, 10, 15);
		Integer slotId = 2;

		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.empty());

		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

		String result = service.handleBetSubmission(selectedNumbers, slotId, betId, redirectAttributes);

		assertEquals("redirect:/error", result);
		verify(redirectAttributes).addFlashAttribute("error", "Wette ist nicht gefunden.");
		verifyNoMoreInteractions(bettingSlipRepository);
	}

	@Test
	void testHandleBetSubmission_whenMaxSlotsReached() {
		Long betId = 1L;
		List<Integer> selectedNumbers = List.of(5, 10, 15);
		Integer slotId = 2;

		LottoBettingSlip mockBet = mock(LottoBettingSlip.class);
		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.of(mockBet));
		doThrow(new IllegalStateException("Maximale Anzahl von Slots erreicht."))
			.when(mockBet).setNumbersForSlot(eq(slotId - 1), anySet(), eq(true));

		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

		String result = service.handleBetSubmission(selectedNumbers, slotId, betId, redirectAttributes);

		assertEquals("redirect:/spielen", result);
		verify(redirectAttributes).addFlashAttribute("error", "Maximale Anzahl von Slots erreicht.");
	}

	@Test
	void testHandleBetSubmission_whenInvalidSlotOrNumbers() {
		Long betId = 1L;
		List<Integer> selectedNumbers = List.of(5, 10, 15);
		Integer slotId = 2;

		LottoBettingSlip mockBet = mock(LottoBettingSlip.class);
		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.of(mockBet));
		doThrow(new IllegalArgumentException("Ungültige Eingabe."))
			.when(mockBet).setNumbersForSlot(eq(slotId - 1), anySet(), eq(true));

		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

		String result = service.handleBetSubmission(selectedNumbers, slotId, betId, redirectAttributes);

		assertEquals("redirect:/spielen/2?slot=2", result);
		verify(redirectAttributes).addFlashAttribute("error", "Ungültige Eingabe.");
	}

	@Test
	void testHandleBetSubmission_successfulSubmission() {
		Long betId = 1L;
		List<Integer> selectedNumbers = List.of(5, 10, 15);
		Integer slotId = 2;

		LottoBettingSlip mockBet = mock(LottoBettingSlip.class);
		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.of(mockBet));
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

		LottoBettingSlipService spyService = spy(service);
		doReturn("redirect:/success").when(spyService)
			.handleSuccessfulSlotSubmission(eq(redirectAttributes), isNull(), eq(betId));

		String result = spyService.handleBetSubmission(selectedNumbers, slotId, betId, redirectAttributes);

		assertEquals("redirect:/success", result);
		verify(mockBet).setNumbersForSlot(eq(slotId - 1), eq(new HashSet<>(selectedNumbers)), eq(true));
		verify(bettingSlipRepository).save(mockBet);
	}


	@Test
	void testHandleSlotUpdateOrCreate_whenSlotExists_shouldUpdateSlot() {
		List<LottoNumbers> selectedNumbersList = new ArrayList<>();
		Set<Integer> initialNumbers = new HashSet<>(List.of(1, 2, 3));
		Integer slotId = 1;
		selectedNumbersList.add(new LottoNumbers(initialNumbers, slotId));

		List<Integer> updatedNumbers = List.of(4, 5, 6);

		service.handleSlotUpdateOrCreate(selectedNumbersList, updatedNumbers, slotId);

		assertEquals(1, selectedNumbersList.size());
		assertTrue(selectedNumbersList.get(0).getNumbers().containsAll(updatedNumbers));
	}

	@Test
	void testHandleSlotUpdateOrCreate_whenSlotDoesNotExist_shouldCreateNewSlot() {
		List<LottoNumbers> selectedNumbersList = new ArrayList<>();
		Integer slotId = 1;

		List<Integer> newNumbers = List.of(4, 5, 6);

		service.handleSlotUpdateOrCreate(selectedNumbersList, newNumbers, slotId);

		assertEquals(1, selectedNumbersList.size());
		assertTrue(selectedNumbersList.get(0).getNumbers().containsAll(newNumbers));
		assertEquals(slotId, selectedNumbersList.get(0).getSlotId());
	}

	@Test
	void testHandleSlotUpdateOrCreate_whenMultipleSlots_shouldUpdateCorrectSlot() {
		List<LottoNumbers> selectedNumbersList = new ArrayList<>();
		Set<Integer> numbersForSlot1 = new HashSet<>(List.of(1, 2, 3));
		Set<Integer> numbersForSlot2 = new HashSet<>(List.of(7, 8, 9));
		selectedNumbersList.add(new LottoNumbers(numbersForSlot1, 1));
		selectedNumbersList.add(new LottoNumbers(numbersForSlot2, 2));

		List<Integer> updatedNumbersForSlot1 = List.of(4, 5, 6);
		Integer slotIdToUpdate = 1;

		service.handleSlotUpdateOrCreate(selectedNumbersList, updatedNumbersForSlot1, slotIdToUpdate);

		assertEquals(2, selectedNumbersList.size());
		assertTrue(selectedNumbersList.get(0).getNumbers().containsAll(updatedNumbersForSlot1));
		assertTrue(selectedNumbersList.get(1).getNumbers().containsAll(numbersForSlot2));
	}


	@Test
	public void testHandleSuccessfulSlotSubmission_ShouldRedirectToSpielen() {
		Long betId = 1L;
		Long communityId = 1L;

		String result = service.handleSuccessfulSlotSubmission(redirectAttributes, communityId, betId);

		assertEquals("redirect:/spielen", result);
		verify(redirectAttributes).addFlashAttribute("message", "Slot wurde erfolgreich gespeichert.");
		verify(redirectAttributes).addAttribute("communityId", communityId);
		verify(redirectAttributes).addAttribute("betId", betId);
	}


	@Test
	public void testSubmissionTooLate_WhenTodayIsAfterNextDrawMinusOneDay() {
		LocalDateTime today = LocalDateTime.of(2024, 12, 9, 10, 0);
		LocalDateTime nextDrawDate = LocalDateTime.of(2024, 12, 10, 23, 59, 59);

		try (MockedStatic<Lotto> mockedLotto = mockStatic(Lotto.class)) {
			mockedLotto.when(Lotto::getNextDrawDate).thenReturn(nextDrawDate);

			boolean result = service.isSubmissionTooLate(redirectAttributes);

			assertTrue(result, "The submission should be considered too late");
			verify(redirectAttributes).addFlashAttribute(eq("tooLateError"), eq("Die Abgabe ist einen Tag vor der Ziehung geschlossen."));
		}
	}


	@Test
	public void testSubmissionTooLate_WhenTodayIsAfterNextDraw() {

		LocalDateTime today = LocalDateTime.of(2024, 12, 11, 10, 0);
		LocalDateTime nextDrawDate = LocalDateTime.of(2024, 12, 10, 23, 59, 59);

		try (MockedStatic<Lotto> mockedLotto = mockStatic(Lotto.class)) {
			mockedLotto.when(Lotto::getNextDrawDate).thenReturn(nextDrawDate);

			boolean result = service.isSubmissionTooLate(redirectAttributes);

			assertTrue(result, "The submission should be considered too late");
			verify(redirectAttributes).addFlashAttribute(eq("tooLateError"), eq("Die Abgabe ist einen Tag vor der Ziehung geschlossen."));
		}
	}


	@Test
	void testUpdateExistingBettingSlip_whenBetExists_shouldUpdateAndReturnSlip() {
		Long betId = 1L;
		int superNumber = 7;
		double betAmount = 100.0;
		User.UserIdentifier userId = new User.UserIdentifier();
		Long communityId = 123L;
		List<LottoNumbers> numbersList = new ArrayList<>();
		LocalDateTime dateSubmitted = LocalDateTime.now();

		LottoBettingSlip existingSlip = new SingleLottoBettingSlip(dateSubmitted, userId, communityId, betAmount, numbersList, superNumber);

		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.of(existingSlip));

		LottoBettingSlip result = service.updateExistingBettingSlip(betId, superNumber, betAmount, redirectAttributes);

		assertNotNull(result);
		assertEquals(superNumber, result.getSuperNumber());
		assertEquals(betAmount, result.getBetAmount(), 0.0);
		verify(lotto).saveSlip(result);
		verify(redirectAttributes).addFlashAttribute(eq("message"), eq("Tippschein wurde erfolgreich aktualisiert!")); // Перевіряємо, що повідомлення додано
	}



	@Test
	void testUpdateExistingBettingSlip_whenBetNotFound_shouldReturnErrorMessage() {
		Long betId = 1L;
		int superNumber = 7;
		double betAmount = 100.0;

		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.empty());

		LottoBettingSlip result = service.updateExistingBettingSlip(betId, superNumber, betAmount, redirectAttributes);

		assertNull(result);
		verify(redirectAttributes).addFlashAttribute(eq("error"), eq("Wette nicht gefunden."));
	}


	@Test
	void testUpdateExistingBettingSlip_whenBetNotFound_shouldNotCallSave() {
		Long betId = 1L;
		int superNumber = 7;
		double betAmount = 100.0;

		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.empty());

		LottoBettingSlip result = service.updateExistingBettingSlip(betId, superNumber, betAmount, redirectAttributes);

		verify(lotto, never()).saveSlip(any(LottoBettingSlip.class));
	}


	@Test
	void testUpdateExistingBettingSlip_whenBetExists_shouldAddSuccessMessage() {
		Long betId = 1L;
		int superNumber = 7;
		double betAmount = 100.0;
		User.UserIdentifier userId = new User.UserIdentifier();
		Long communityId = 123L;
		List<LottoNumbers> numbersList = new ArrayList<>();
		LocalDateTime dateSubmitted = LocalDateTime.now();

		LottoBettingSlip existingSlip = new SingleLottoBettingSlip(dateSubmitted, userId, communityId, betAmount, numbersList, superNumber);

		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.of(existingSlip));

		LottoBettingSlip result = service.updateExistingBettingSlip(betId, superNumber, betAmount, redirectAttributes);

		verify(redirectAttributes).addFlashAttribute(eq("message"), eq("Tippschein wurde erfolgreich aktualisiert!"));
	}


	@Test
	void testCreateNewSingleBettingSlipSuccess() {
		User.UserIdentifier userId = new User.UserIdentifier();
		List<LottoNumbers> validNumbers = List.of(new LottoNumbers(Set.of(1, 2, 3, 4, 5, 6), 1));
		LottoBettingSlip slip = service.createNewBettingSlip(
			userId, null, 12.0, validNumbers, 7, null, redirectAttributes, sessionStatus);

		assertNotNull(slip);
		assertTrue(slip instanceof SingleLottoBettingSlip);
		assertTrue(slip.isValid());
		assertEquals(12.0, slip.getBetAmount());
		verify(lotto, times(1)).saveSlip(slip);
		verify(redirectAttributes).addFlashAttribute("message", "Tippschein wurde erfolgreich abgeschickt!");
		verify(sessionStatus).setComplete();
	}


	@Test
	void testCreateNewPermanentBettingSlipSuccess() {
		User.UserIdentifier userId = new User.UserIdentifier();
		List<LottoNumbers> validNumbers = List.of(new LottoNumbers(Set.of(1, 2, 3, 4, 5, 6), 1));
		Duration duration = Duration.MONTHLY;
		LottoBettingSlip slip = service.createNewBettingSlip(
			userId, null, 20.0, validNumbers, 8, duration, redirectAttributes, sessionStatus);

		assertNotNull(slip);
		assertTrue(slip instanceof PermanentLottoBettingSlip);
		assertEquals(duration, ((PermanentLottoBettingSlip) slip).getDuration());
		assertTrue(slip.isValid());
		verify(lotto, times(1)).saveSlip(slip);
		verify(redirectAttributes).addFlashAttribute("message", "Tippschein wurde erfolgreich abgeschickt!");
		verify(sessionStatus).setComplete();
	}

	@Test
	void testCreateNewBettingSlipInvalidNumbers() {
		User.UserIdentifier userId = new User.UserIdentifier();
		List<LottoNumbers> invalidNumbers = List.of(new LottoNumbers(Set.of(1, 2, 3, 4), 1));
		LottoBettingSlip slip = service.createNewBettingSlip(
			userId, null, 10.0, invalidNumbers, 9, null, redirectAttributes, sessionStatus);

		assertNull(slip);
		verify(lotto, never()).saveSlip(any());
		verify(redirectAttributes).addFlashAttribute("error", "Ungültiges Tippschein. Vor dem Senden muss mindestens ein Slot gefüllt sein.");
		verify(sessionStatus, never()).setComplete();
	}

	@Test
	void testCreateNewBettingSlipEmptyNumbersList() {
		User.UserIdentifier userId = new User.UserIdentifier();
		List<LottoNumbers> emptyNumbers = List.of();
		LottoBettingSlip slip = service.createNewBettingSlip(
			userId, null, 10.0, emptyNumbers, 9, null, redirectAttributes, sessionStatus);

		assertNull(slip);
		verify(lotto, never()).saveSlip(any());
		verify(redirectAttributes).addFlashAttribute("error", "Ungültiges Tippschein. Vor dem Senden muss mindestens ein Slot gefüllt sein.");
		verify(sessionStatus, never()).setComplete();
	}

	@Test
	void testCreateNewBettingSlipWithCommunityId() {
		User.UserIdentifier userId = new User.UserIdentifier();
		List<LottoNumbers> validNumbers = List.of(new LottoNumbers(Set.of(1, 2, 3, 4, 5, 6), 1));
		Long communityId = 123L;
		LottoBettingSlip slip = service.createNewBettingSlip(
			userId, communityId, 15.0, validNumbers, 10, null, redirectAttributes, sessionStatus);

		assertNotNull(slip);
		assertTrue(slip.isValid());
		assertEquals(communityId, slip.getCommunityId());
		verify(lotto, times(1)).saveSlip(slip);
		verify(communityManagement, times(1)).addCommunitySlipLotto(slip);
		verify(redirectAttributes).addFlashAttribute("message", "Tippschein wurde erfolgreich abgeschickt!");
		verify(sessionStatus).setComplete();
	}


	@Test
	void testHandleBettingSlipRandomNumbers_whenBetExists_shouldGenerateRandomNumbers() {
		Long betId = 1L;
		LottoBettingSlip bet = new SingleLottoBettingSlip();
		bet.setNumbersList(new ArrayList<>());

		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.of(bet));

		service.handleBettingSlipRandomNumbers(betId);

		assertEquals(12, bet.getNumbersList().size());
		verify(bettingSlipRepository).save(bet);
	}

	@Test
	void testHandleBettingSlipRandomNumbers_whenBetNotExists_shouldDoNothing() {
		Long betId = 999L;
		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.empty());

		service.handleBettingSlipRandomNumbers(betId);

		verify(bettingSlipRepository, never()).save(any(LottoBettingSlip.class));
	}

	@Test
	void testHandleBettingSlipRandomNumbers_shouldClearPreviousNumbersAndGenerateNewOnes() {
		Long betId = 1L;
		LottoBettingSlip bet = new SingleLottoBettingSlip();

		LottoNumbers lottoNumbers = new LottoNumbers();
		lottoNumbers.setNumbers(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6)));
		bet.setNumbersList(new ArrayList<>(Collections.singletonList(lottoNumbers)));

		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.of(bet));

		service.handleBettingSlipRandomNumbers(betId);

		assertTrue(bet.getNumbersList().isEmpty() || bet.getNumbersList().size() == 12);
	}


	@Test
	void testHandleBettingSlipRandomNumbers_shouldCalculateBetAmount() {
		Long betId = 1L;
		LottoBettingSlip bet = new SingleLottoBettingSlip();
		bet.setNumbersList(new ArrayList<>());

		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.of(bet));

		service.handleBettingSlipRandomNumbers(betId);

		double expectedBetAmount = 12 * LottoBettingSlip.SLOT_PRICE;
		assertEquals(expectedBetAmount, bet.getBetAmount(), 0.001);
	}


	@Test
	void testHandleSlotUpdateInBettingSlip_shouldThrowExceptionIfBetNotFound() {
		Long betId = 999L;
		int slotId = 1;
		Set<Integer> generatedNumbers = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6));

		when(lotto.getBettingSlipRepository()).thenReturn(bettingSlipRepository);
		when(bettingSlipRepository.findById(betId)).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			service.handleSlotUpdateInBettingSlip(betId, slotId, generatedNumbers, redirectAttributes);
		});

		assertEquals("Wette ist nicht gefunden: " + betId, exception.getMessage());
	}


	@Test
	public void testGenerateRandomNumbersForSlots_ShouldGenerateCorrectNumberOfSlotsAndNumbers() {
		List<LottoNumbers> numbersList = new ArrayList<>();
		int slotCount = 12;
		int numbersPerSlot = 6;
		int maxNumber = 49;

		service.generateRandomNumbersForSlots(numbersList, slotCount, numbersPerSlot, maxNumber);

		assertEquals(slotCount, numbersList.size());
		for (LottoNumbers lottoNumbers : numbersList) {
			assertEquals(numbersPerSlot, lottoNumbers.getNumbers().size());
			assertTrue(lottoNumbers.getNumbers().stream().allMatch(num -> num >= 1 && num <= maxNumber));
		}
	}

	@Test
	public void testUpdateSlotInSelectedNumbersList_ShouldUpdateSlotNumbers() {
		int slotId = 1;
		List<LottoNumbers> selectedNumbersList = new ArrayList<>();
		Set<Integer> generatedNumbers = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6));

		service.updateSlotInSelectedNumbersList(slotId, selectedNumbersList, generatedNumbers);

		assertEquals(1, selectedNumbersList.size());
		assertEquals(generatedNumbers, selectedNumbersList.get(0).getNumbers());
	}


	@Test
	public void testClearSlotNumbers_ShouldClearSlotNumbersForSelectedList() {
		Long betId = null;
		int slotId = 1;
		List<LottoNumbers> selectedNumbersList = new ArrayList<>();
		Set<Integer> generatedNumbers = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6));
		selectedNumbersList.add(new LottoNumbers(generatedNumbers, slotId));

		service.clearSlotNumbers(betId, slotId, selectedNumbersList);

		assertTrue(selectedNumbersList.isEmpty());
	}


	@Test
	public void testClearSlotNumbers_ShouldHandleEmptyList() {
		Long betId = null;
		int slotId = 1;

		List<LottoNumbers> selectedNumbersList = new ArrayList<>();

		service.clearSlotNumbers(betId, slotId, selectedNumbersList);

		assertTrue(selectedNumbersList.isEmpty(), "Selected numbers list should remain empty after calling clearSlotNumbers");
	}


	@Test
	public void testClearSlotNumbers_ShouldClearAllNumbersWhenSlotIdIsZero() {
		Long betId = null;
		int slotIdToClear = 0;

		Set<Integer> generatedNumbers1 = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6));
		Set<Integer> generatedNumbers2 = new HashSet<>(Set.of(7, 8, 9, 10, 11, 12));

		List<LottoNumbers> selectedNumbersList = new ArrayList<>();
		selectedNumbersList.add(new LottoNumbers(generatedNumbers1, 1));
		selectedNumbersList.add(new LottoNumbers(generatedNumbers2, 2));

		service.clearSlotNumbers(betId, slotIdToClear, selectedNumbersList);

		assertTrue(selectedNumbersList.isEmpty(), "All selected numbers list should be empty");
	}


	@Test
	public void testClearSlotNumbers_ShouldHandleEmptyListGracefully() {
		Long betId = null;
		int slotIdToClear = 1;

		List<LottoNumbers> selectedNumbersList = new ArrayList<>();

		service.clearSlotNumbers(betId, slotIdToClear, selectedNumbersList);

		assertTrue(selectedNumbersList.isEmpty(), "The list should remain empty after calling clearSlotNumbers on an empty list");
	}


	@Test
	public void testClearSlotNumbers_ShouldUpdateSlotIdsAfterClear() {
		Long betId = null;
		int slotIdToClear = 1;

		Set<Integer> generatedNumbers1 = new HashSet<>(Set.of(1, 2, 3, 4, 5, 6));
		Set<Integer> generatedNumbers2 = new HashSet<>(Set.of(7, 8, 9, 10, 11, 12));

		List<LottoNumbers> selectedNumbersList = new ArrayList<>();
		selectedNumbersList.add(new LottoNumbers(generatedNumbers1, 1));
		selectedNumbersList.add(new LottoNumbers(generatedNumbers2, 2));

		service.clearSlotNumbers(betId, slotIdToClear, selectedNumbersList);

		assertEquals(1, selectedNumbersList.get(0).getSlotId(), "Slot ID should be updated correctly for the remaining slots");
	}

}


