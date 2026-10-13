package kickstart.lotteries.lotto;


import kickstart.community.Community;
import kickstart.community.CommunityManagement;
import kickstart.community.CommunityRepository;
import kickstart.lotteries.lotto.slips.*;
import kickstart.lotteries.lotto.results.Result;

import kickstart.profil.konto.Message;
import kickstart.profil.konto.MessageManagement;
import kickstart.user.User;
import kickstart.user.UserManagement;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.context.SecurityContextHolder;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.time.LocalDate;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;

import kickstart.user.User.UserIdentifier;


@Controller

@SessionAttributes({"userId", "selectedNumbersList", "superNumber", "selectedNumbers"})
public class LottoController {


	private final UserManagement userManagement;
	private final LottoBettingSlipService lottoService;
	private final CommunityManagement communityManagement;
	private final CommunityRepository communityRepository;
	private final MessageManagement messageManagement;

	@Autowired
	public LottoController(UserManagement userManagement, LottoBettingSlipService lottoService, CommunityManagement communityManagement, CommunityRepository communityRepository, MessageManagement messageManagement) {
		this.userManagement = userManagement;
		this.lottoService = lottoService;
		this.communityManagement = communityManagement;
		this.communityRepository = communityRepository;
		this.messageManagement = messageManagement;
	}

	@ModelAttribute("selectedNumbersList")
	public List<LottoNumbers> initializeSelectedNumbersList() {
		return new ArrayList<>();
	}

	@ModelAttribute("selectedNumbers")
	public Set<Integer> initializeSelectedNumbers() {
		return new HashSet<>();
	}

	@ModelAttribute("superNumber")
	public int initializeSuperNumber() {
		return 0;
	}

	@ModelAttribute("userId")
	public User.UserIdentifier initializeUserId(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// Check for USER role idk what to do with admin here
		if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_USER"))) {
			return userManagement.getLoggedInUserId();
		}
		// Fallback, if no valid role found
		throw new AccessDeniedException("You do not have permission to access this page.");
	}


	// opens a window with slots
	@GetMapping("/spielen")
	@PreAuthorize("hasRole('USER')")
	public String showLotteryForm(
		Model model,
		@ModelAttribute("selectedNumbersList") List<LottoNumbers> selectedNumbersList,
		@RequestParam(value = "betId", required = false) Long betId,
		@RequestParam(value = "communityId", required = false) Long communityId,
		@RequestParam(value = "duration", required = false) String duration) {

		LottoBettingSlip bet = lottoService.getBetById(betId);

		Set<Integer> filledSlots;
		int slotId;
		if (bet != null) {
			filledSlots = bet.getNumbersList().stream()
				.map(LottoNumbers::getSlotId)
				.collect(Collectors.toSet());
			slotId = Math.min(bet.getNumbersList().size() + 1, 12);
		} else {
			filledSlots = selectedNumbersList.stream()
				.map(LottoNumbers::getSlotId)
				.collect(Collectors.toSet());
			slotId = Math.min(selectedNumbersList.size() + 1, 12);
		}

		double betAmount = lottoService.calculateBetAmount(selectedNumbersList.size(), duration, bet);

		model.addAttribute("betId", betId);
		model.addAttribute("communityId", communityId);
		model.addAttribute("slotId", slotId);
		model.addAttribute("filledSlots", filledSlots);
		model.addAttribute("superNumber", bet != null ? bet.getSuperNumber() : 0);
		model.addAttribute("duration", duration);
		model.addAttribute("betAmount", betAmount);

		return "slots";
	}

	@PreAuthorize("hasRole('USER')")
	@GetMapping("/spielen/{slotId}")
	public String showSlotSelection(@PathVariable int slotId,
									Model model,
									@ModelAttribute("selectedNumbersList") List<LottoNumbers> selectedNumbersList,
									@ModelAttribute(value = "selectedNumbers", binding = false) Set<Integer> selectedNumbers,
									@RequestParam(value = "betId", required = false) Long betId,
									@RequestParam(value = "communityId", required = false) Long communityId) {

		List<Integer> numbers = new ArrayList<>();
		for (int i = 1; i <= 49; i++) {
			numbers.add(i);
		}

		Set<Integer> selectedNumbersForSlot = lottoService.getSelectedNumbersForSlot(slotId, selectedNumbersList, selectedNumbers, betId);

		model.addAttribute("slotId", slotId);
		model.addAttribute("numbers", numbers); // Передаємо числа від 1 до 49
		model.addAttribute("betAmount", LottoBettingSlip.SLOT_PRICE);
		model.addAttribute("selectedNumbers", selectedNumbersForSlot);
		model.addAttribute("betId", betId);
		model.addAttribute("communityId", communityId);

		return "lottoForm";
	}


	//for script in slots
	@GetMapping("/spielen/updateBetAmount")
	@PreAuthorize("hasRole('USER')")
	@ResponseBody
	public double updateBetAmount(
		@ModelAttribute("selectedNumbersList") List<LottoNumbers> selectedNumbersList,
		@RequestParam(value = "duration", required = false) String duration) {

		double betAmount = lottoService.calculateBetAmount(selectedNumbersList.size(), duration, null);
		return betAmount; //JSON
	}


	@PreAuthorize("hasRole('USER')")
	@PostMapping("/submitSlot/{slotId}")
	public String submitSlot(@RequestParam(value = "numbers", required = false) List<Integer> selectedNumbers,
							 @PathVariable("slotId") Integer slotId,
							 @ModelAttribute("selectedNumbersList") List<LottoNumbers> selectedNumbersList,
							 RedirectAttributes redirectAttributes,
							 Model model,
							 @RequestParam(value = "betId", required = false) Long betId,
							 @RequestParam(value = "communityId", required = false) Long communityId) {

		if (selectedNumbers == null || selectedNumbers.size() != 6) {
			return lottoService.handleInvalidNumbersSelection(redirectAttributes, selectedNumbers, slotId, betId, communityId);
		}

		if (betId != null) {
			return lottoService.handleBetSubmission(selectedNumbers, slotId, betId, redirectAttributes);
		}

		lottoService.handleSlotUpdateOrCreate(selectedNumbersList, selectedNumbers, slotId);
		return lottoService.handleSuccessfulSlotSubmission(redirectAttributes, communityId, null);
	}


	// submit the betting slip in full
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/submit")
	public String submitBettingSlip(Model model,
									@ModelAttribute("selectedNumbersList") List<LottoNumbers> selectedNumbersList,
									@RequestParam("superNumber") int superNumber,
									@RequestParam(value = "betAmount") double betAmount,
									@SessionAttribute("userId") User.UserIdentifier userId,
									@RequestParam(value = "communityId", required = false) Long communityId,
									@RequestParam(value = "duration", required = false) Duration duration,
									@RequestParam(value = "betId", required = false) Long betId,
									SessionStatus status,
									RedirectAttributes redirectAttributes) {

		if (userId == null || !userId.equals(userManagement.getLoggedInUserId())) {
			redirectAttributes.addFlashAttribute("error", "Session für Benutzer abgelaufen. Sie sollen neu anmelden.");
			return "redirect:/spielen";
		}

		if (lottoService.isSubmissionTooLate(redirectAttributes)) {
			return "redirect:/spielen";
		}

		if(!lottoService.hasEnoughMoney(userId, betAmount, redirectAttributes)){
			redirectAttributes.addAttribute("communityId", communityId);
			return "redirect:/spielen";
		}

		if (selectedNumbersList == null || selectedNumbersList.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Sie können nicht leeren Tippschein schicken.");
			redirectAttributes.addAttribute("communityId", communityId);
			redirectAttributes.addAttribute("betId", betId);
			return "redirect:/spielen";
		}

			User.UserIdentifier slipUserId = (communityId != null) ? null : userId;
			LottoBettingSlip slip = (betId != null)
				? lottoService.updateExistingBettingSlip(betId, superNumber, betAmount, redirectAttributes)
				: lottoService.createNewBettingSlip(slipUserId, communityId, betAmount, selectedNumbersList, superNumber, duration, redirectAttributes, status);

			if (communityId == null) {
				String betDuration = switch (duration) {
					case null -> "Einzelwette";
					case Duration.MONTHLY-> "1 Monat";
					case Duration.SEMIANNUAL -> "6 Monate";
					case Duration.ANNUALY -> "1 Jahr";
					default -> throw new IllegalArgumentException("Ungültiger Wert");
				};
				String content = "Erfolgreich Lottowette erstellt: \n Kosten: " + slip.getBetAmount() + "€\n Zeitraum der Wette: " + betDuration + "\n" + "Zahlen:\n ";
				content += numberStringBuilder(slip.getNumbersList());
				content += "Superzahl: " + superNumber + "\n";
				String username = userManagement.getLoggedInUserName();
				Message message = new Message(content, userId);
				messageManagement.addMessage(message);
			}

		if (slip == null || slipUserId == null) {
			String communityName = communityManagement.findCommunityById(communityId).getName();
			return "redirect:/gemeinschaft/" + communityName;
		}

		return "redirect:/spielen";
	}

	public String numberStringBuilder(List<LottoNumbers> numbersList) {
		int i = 0;
		String content = "";
		for (LottoNumbers lottoNumbers: numbersList) //placeholder for now for the numbers of lotto, to be fixed 12.01.2025
		{
			i++;
			String singleList = "";
			for (int number: lottoNumbers.getNumbers()) {
				singleList += number + " ";
			}
			singleList += " ".repeat(18 - singleList.length());
			content += singleList;
			System.out.println(singleList);
			System.out.println(singleList.length());
			content += " | ";
			if (i % 6 == 0) {
				content += "\n";
			}
		}
		return content;
	}


	// number generation for all slots
	@PreAuthorize("hasRole('USER')")
	@PostMapping("/generate-random-numbers-all")
	public String generateRandomNumbers(
		Model model,
		@ModelAttribute("selectedNumbersList") List<LottoNumbers> selectedNumbersList,
		RedirectAttributes redirectAttributes,
		@RequestParam(value = "betId", required = false) Long betId,
		@RequestParam(value = "communityId", required = false) Long communityId) {

		if (betId != null) {
			lottoService.handleBettingSlipRandomNumbers(betId);
		} else {
			lottoService.generateRandomNumbersForSlots(selectedNumbersList, 12, 6, 49);
		}

		redirectAttributes.addFlashAttribute("message", "Nummern für alle Slots wurden generiert.");
		redirectAttributes.addAttribute("betId", betId);
		redirectAttributes.addAttribute("communityId", communityId);

		return "redirect:/spielen";
	}


	@PreAuthorize("hasRole('USER')")
	@PostMapping("/generate-random-numbers-slot")
	public String generateSlotNumbers(
		@RequestParam int slotId,
		@ModelAttribute("selectedNumbersList") List<LottoNumbers> selectedNumbersList,
		RedirectAttributes redirectAttributes,
		@RequestParam(value = "betId", required = false) Long betId,
		Model model,
		@RequestParam(value = "communityId", required = false) Long communityId) {

		List<LottoNumbers> numbersList = new ArrayList<>();

		lottoService.generateRandomNumbersForSlots(numbersList, 12, 6, 49);

		if (betId != null) {
			lottoService.handleSlotUpdateInBettingSlip(betId, slotId, numbersList.get(slotId - 1).getNumbers(), redirectAttributes);
		} else {
			lottoService.updateSlotInSelectedNumbersList(slotId, selectedNumbersList, numbersList.get(slotId - 1).getNumbers());
			redirectAttributes.addFlashAttribute("message", "Zufällige Zahlen wurden generiert.");
		}

		redirectAttributes.addAttribute("communityId", communityId);
		redirectAttributes.addAttribute("betId", betId);
		return "redirect:/spielen/" + slotId + "?slot=" + slotId;
	}


	@PreAuthorize("hasRole('USER')")
	@PostMapping("/clear-lotto")
	public String clearLottoTicket(Model model,
								   @ModelAttribute("selectedNumbersList") List<LottoNumbers> selectedNumbersList,
								   @RequestParam(value = "betId", required = false) Long betId,
								   RedirectAttributes redirectAttributes,
								   @RequestParam(value = "communityId", required = false) Long communityId) {

		lottoService.clearSlotNumbers(betId, 0, selectedNumbersList);

		redirectAttributes.addFlashAttribute("message", "Die Wette wurde erfolgreich entfernt.");
		redirectAttributes.addAttribute("betId", betId);
		redirectAttributes.addAttribute("communityId", communityId);

		return "redirect:/spielen";
	}


	@PreAuthorize("hasRole('USER')")
	@PostMapping("/clear-slot")
	public String clearSlotSelection(@RequestParam int slotId,
									 @ModelAttribute("selectedNumbersList") List<LottoNumbers> selectedNumbersList,
									 RedirectAttributes redirectAttributes,
									 @RequestParam(value = "betId", required = false) Long betId,
									 @RequestParam(value = "communityId", required = false) Long communityId,
									 Model model) {

		lottoService.clearSlotNumbers(betId, slotId, selectedNumbersList);

		redirectAttributes.addFlashAttribute("message", "Slot wurde gelöscht.");
		redirectAttributes.addAttribute("communityId", communityId);
		redirectAttributes.addAttribute("betId", betId);
		redirectAttributes.addFlashAttribute("selectedNumbers", new HashSet<>());

		return "redirect:/spielen";
	}



	/*
	// method can be removed as soon as the bets can be edited from community page
	// displays a list of bets with communityId
	@GetMapping("/gemeinschaft/{communityName}/bets")
	public String showCommunityBets(
		@PathVariable("communityName") String communityName,
		@RequestParam(value = "communityId", required = false) Long communityId,
		Model model) {

		System.out.println("CommunityId: " + communityId);

		communityId = 1L; // set temporarily

		System.out.println("CommunityId: " + communityId);

		List<LottoBettingSlip> communityBets;

		if (communityId != null) {
			communityBets = lottoService.lotto.getBettingSlipRepository().findByCommunityId(communityId);
		} else {
			communityBets = Collections.emptyList();
		}

		model.addAttribute("communityName", communityName);
		model.addAttribute("communityBets", communityBets);
		model.addAttribute("communityId", communityId);
		return "communityLottoBets";
	}
*/

}
