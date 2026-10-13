package kickstart.lotteries.lotto;

import java.time.LocalDateTime;

import kickstart.lotteries.lotto.results.PastResultsRepository;
import kickstart.lotteries.lotto.slips.*;
import kickstart.lotteries.lotto.LottoController;
import kickstart.lotteries.lotto.Lotto;
import kickstart.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class LottoControllerUnitTest {

	@Autowired
	private MockMvc mockMvc;  // Ініціалізація MockMvc

	@InjectMocks
	private LottoController lottoController;

	@Mock
	private PastResultsRepository pastResultsRepository;

	@MockBean
	private LottoBettingSlipRepository bettingSlipRepository;

	@Mock
	private Lotto lotto;

	@Mock
	private Model model;

	@Mock
	private RedirectAttributes redirectAttributes;

	@Mock
	private SessionStatus sessionStatus;

	private List<LottoNumbers> selectedNumbersList;

	private Set<Integer> selectedNumbers;



	@BeforeEach
	public void setup() {
		selectedNumbersList = new ArrayList<>();
		selectedNumbers = new HashSet<>();

		MockitoAnnotations.openMocks(this);
	}


	/*
	@Test
	public void testShowLotteryForm() throws Exception {
		// Given: A list with some selected numbers
		LottoNumbers lottoNumbers = new LottoNumbers(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6)), 1);
		selectedNumbersList.add(lottoNumbers);

		// When: We call the showLotteryForm method
		String result = lottoController.showLotteryForm(model, selectedNumbersList);

		// Then: The numbers are passed to the model
		verify(model).addAttribute("betAmount", selectedNumbersList.size() * 1.0);
		verify(model).addAttribute("slotId", 2);  // Slot ID will be 2 because one slot is filled
		verify(model).addAttribute("filledSlots", Set.of(1));
		assert(result.equals("slots"));
	}


	@WithMockUser(username = "testUser", roles = {"USER"})
	@Test
	public void testShowSlotSelection() throws Exception {
		// Додаємо числа до списку
		List<LottoNumbers> selectedNumbersList = new ArrayList<>();
		selectedNumbersList.add(new LottoNumbers(new HashSet<>(Arrays.asList(5, 10, 15, 20, 25, 30)), 1));

		// Викликаємо метод і перевіряємо атрибути
		mockMvc.perform(get("/spielen/1")
				.sessionAttr("selectedNumbersList", selectedNumbersList))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("selectedNumbers"))
			.andExpect(model().attribute("selectedNumbers", new HashSet<>(Arrays.asList(5, 10, 15, 20, 25, 30))))
			.andExpect(model().attribute("slotId", 1))
			.andExpect(model().attributeExists("numbers"))  // Перевіряємо, чи генеруються числа
			.andExpect(model().attribute("einsatzAmount", 1.0));
	}





	@Test
	public void testShowCurrentNumbers() throws Exception {
		// тут треба тест для виведення лото чисел
	}



	@Test
	public void testSubmitSlot() throws Exception {
		// Given: Selected numbers for slot 1
		List<Integer> selectedNumbers = Arrays.asList(1, 2, 3, 4, 5, 6);
		int slotId = 1;

		// Create a mock for RedirectAttributes
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);

		// Initialize selectedNumbersList and currentSelectedNumbers
		List<LottoNumbers> selectedNumbersList = new ArrayList<>();
		List<Integer> currentSelectedNumbers = new ArrayList<>();

		// When: We call submitSlot method
		String result = lottoController.submitSlot(
			selectedNumbers, slotId, selectedNumbersList, currentSelectedNumbers, redirectAttributes, null
		);

		// Then: The slot is saved correctly
		assert(selectedNumbersList.size() == 1);
		assert(selectedNumbersList.get(0).getSlotId() == 1);
		assert(selectedNumbersList.get(0).getNumbers().equals(new HashSet<>(selectedNumbers)));

		// Verify that the message "Slot wurde erfolgreich gespeichert." was added to RedirectAttributes
		verify(redirectAttributes).addFlashAttribute("message", "Slot wurde erfolgreich gespeichert.");

		// Verify that selectedNumbers was cleared after saving
		verify(redirectAttributes).addFlashAttribute("selectedNumbers", new HashSet<>());

		assert(result.equals("redirect:/spielen"));
	}


	/*
	@WithMockUser(username = "testUser", roles = {"USER"})
	@Test
	public void testSubmitBettingSlip() throws Exception {
		// Mock parameters
		List<LottoNumbers> selectedNumbers = Arrays.asList(
			new LottoNumbers(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6)), 1)
		);
		double betAmount = 10.0;
		int superNumber = 7;

		User.UserIdentifier userId = new User.UserIdentifier();
		Duration duration = Duration.MONTHLY;

		// Mock save to return the created object
		LottoBettingSlip mockedSlip = new SingleLottoBettingSlip(LocalDateTime.now(), userId, betAmount, selectedNumbers, superNumber);
		when(bettingSlipRepository.save(any(LottoBettingSlip.class))).thenReturn(mockedSlip);

		// Execution of the request
		mockMvc.perform(post("/submit")
				.sessionAttr("selectedNumbersList", selectedNumbers)
				.param("superNumber", String.valueOf(superNumber))
				.param("betAmount", String.valueOf(betAmount))
				.sessionAttr("userId", userId)
				.param("duration", duration != null ? duration.name() : ""))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/spielen"));

		// Check what type of object was created
		ArgumentCaptor<LottoBettingSlip> slipCaptor = ArgumentCaptor.forClass(LottoBettingSlip.class);

		// Using captor for saved object
		verify(bettingSlipRepository, times(1)).save(slipCaptor.capture());

		// Get the captured object
		LottoBettingSlip capturedSlip = slipCaptor.getValue();
		if (duration != null) {
			// Check if it is a PermanentLottoBettingSlip
			assertTrue(capturedSlip instanceof PermanentLottoBettingSlip);
			assertEquals(duration, ((PermanentLottoBettingSlip) capturedSlip).getDuration());
		} else {
			// Check if it is a SingleLottoBettingSlip
			assertTrue(capturedSlip instanceof SingleLottoBettingSlip);
		}
	}


	@Test
	public void testGenerateRandomNumbersAll() throws Exception {
		// Given: A clear selectedNumbersList
		selectedNumbersList.clear();

		// When: We call generateRandomNumbersAll method
		String result = lottoController.generateRandomNumbers(model, selectedNumbersList, redirectAttributes);

		// Then: All 12 slots should be filled with random numbers
		assert(selectedNumbersList.size() == 12);
		selectedNumbersList.forEach(slot -> {
			assert(slot.getNumbers().size() == 6);  // Each slot should have 6 numbers
		});
		verify(redirectAttributes).addFlashAttribute("message", "Nummern für alle Slots wurden generiert.");
		assert(result.equals("redirect:/spielen"));
	}

	@Test
	public void testGenerateRandomNumbersSlot() throws Exception {
		// Given: A slotId and an empty selectedNumbersList
		int slotId = 1;
		List<LottoNumbers> selectedNumbersList = new ArrayList<>();
		selectedNumbersList.clear();

		// Mocking RedirectAttributes and Model
		RedirectAttributes redirectAttributes = mock(RedirectAttributes.class);
		Model model = mock(Model.class);

		// When: We call generateSlotNumbers method
		String result = lottoController.generateSlotNumbers(slotId, selectedNumbersList, redirectAttributes, model);

		// Then: The specific slot should be filled with 6 random numbers
		assert(selectedNumbersList.size() == 1);
		assert(selectedNumbersList.get(0).getSlotId() == slotId);
		assert(selectedNumbersList.get(0).getNumbers().size() == 6);

		// Check that all numbers are within the valid range (1 to 49)
		selectedNumbersList.get(0).getNumbers().forEach(number -> {
			assert(number >= 1 && number <= 49);
		});

		// Check the redirection result
		assert(result.equals("redirect:/spielen/" + slotId + "?slot=" + slotId));
	}


	@WithMockUser(username = "testUser", roles = {"USER"})
	@Test
	public void testClearLottoTicket() throws Exception {
		// Add some slots to the list
		selectedNumbersList.add(new LottoNumbers(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6)), 1));
		selectedNumbersList.add(new LottoNumbers(new HashSet<>(Arrays.asList(7, 8, 9, 10, 11, 12)), 2));

		// Make a POST request to clear
		mockMvc.perform(post("/clear-lotto")
				.sessionAttr("selectedNumbersList", selectedNumbersList)
				.sessionAttr("userId", new User.UserIdentifier()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/spielen"));

		// Check that the list has been cleared
		assertTrue(selectedNumbersList.isEmpty());
	}



	@WithMockUser(username = "testUser", roles = {"USER"})
	@Test
	public void testClearSlotSelection() throws Exception {
		// Add slots to the list
		selectedNumbersList.add(new LottoNumbers(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6)), 1));
		selectedNumbersList.add(new LottoNumbers(new HashSet<>(Arrays.asList(7, 8, 9, 10, 11, 12)), 2));

		// Make a POST request to clear slot 1
		mockMvc.perform(post("/clear-lotto")
				.sessionAttr("selectedNumbersList", selectedNumbersList)
				.sessionAttr("userId", new User.UserIdentifier()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/spielen"));

		// Check that there is only one slot left with the updated slotId = 1
		assertEquals(1, selectedNumbersList.size());
		assertEquals(1, selectedNumbersList.get(0).getSlotId()); // Змінюється очікуване значення

	}
	 */

}

