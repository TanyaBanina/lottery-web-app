package kickstart.lotteries.lotto.slips;

import kickstart.community.CommunityManagement;
import kickstart.lotteries.lotto.Lotto;
import kickstart.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.time.LocalDateTime;
import java.util.*;

/**
 * Service class responsible for handling the business logic related to Lotto betting slips.
 */
@Service
public class LottoBettingSlipService {

	private final Lotto lotto;
	private final CommunityManagement communityManagement;

	@Autowired
	public LottoBettingSlipService(Lotto lotto, CommunityManagement communityManagement) {
		this.lotto = lotto;
		this.communityManagement = communityManagement;
	}

	public LottoBettingSlip getBetById(Long betId) {
		return betId != null ? lotto.getBettingSlipRepository().findById(betId).orElse(null) : null;
	}



	public double calculateBetAmount(int selectedNumbersCount, String duration, LottoBettingSlip bet) {
		if (bet != null) {
			return bet.getBetAmount();
		}

		double multiplier = 1;
		if (duration != null && !duration.isEmpty()) {
            multiplier = switch (duration) {
                case "MONTHLY" -> 4;
                case "SEMIANNUAL" -> 26;
                case "ANNUALY" -> 52;
                default -> throw new IllegalArgumentException("Unknown duration: " + duration);
            };
		}

		return selectedNumbersCount * multiplier * LottoBettingSlip.SLOT_PRICE;
	}


	public Set<Integer> getSelectedNumbersForSlot(int slotId, List<LottoNumbers> selectedNumbersList,
												   Set<Integer> selectedNumbers, Long betId) {
		Set<Integer> selectedNumbersForSlot = new HashSet<>();

		if (betId != null) {
			LottoBettingSlip bet = lotto.getBettingSlipRepository().findById(betId)
				.orElseThrow(() -> new IllegalArgumentException("Wette ist nicht gefunden: " + betId));

			selectedNumbersForSlot = bet.getNumbersList().stream()
				.filter(lottoNumbers -> lottoNumbers.getSlotId() == slotId)
				.map(LottoNumbers::getNumbers)
				.findFirst()
				.orElse(new HashSet<>());
		} else {
			selectedNumbersForSlot = selectedNumbersList.stream()
				.filter(lottoNumbers -> lottoNumbers.getSlotId() == slotId)
				.map(LottoNumbers::getNumbers)
				.findFirst()
				.orElse(new HashSet<>());

			if (selectedNumbers != null && !selectedNumbers.isEmpty()) {
				selectedNumbersForSlot = selectedNumbers;
			}
		}

		return selectedNumbersForSlot;
	}


	public String handleInvalidNumbersSelection(RedirectAttributes redirectAttributes, List<Integer> selectedNumbers,
												 Integer slotId, Long betId, Long communityId) {
		redirectAttributes.addFlashAttribute("selectedNumbers", selectedNumbers);
		redirectAttributes.addAttribute("communityId", communityId);
		redirectAttributes.addFlashAttribute("error", "Bitte wählen Sie genau 6 Zahlen aus.");
		redirectAttributes.addAttribute("betId", betId);
		return "redirect:/spielen/" + slotId + "?slot=" + slotId;
	}


	public String handleBetSubmission(List<Integer> selectedNumbers, Integer slotId, Long betId,
									   RedirectAttributes redirectAttributes) {
		LottoBettingSlip bet = lotto.getBettingSlipRepository().findById(betId).orElse(null);

		if (bet == null) {
			redirectAttributes.addFlashAttribute("error", "Wette ist nicht gefunden.");
			return "redirect:/error";
		}

		try {
			bet.setNumbersForSlot(slotId - 1, new HashSet<>(selectedNumbers), true);
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("error", "Maximale Anzahl von Slots erreicht.");
			return "redirect:/spielen";
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/spielen/" + slotId + "?slot=" + slotId;
		}

		lotto.getBettingSlipRepository().save(bet);
		return handleSuccessfulSlotSubmission(redirectAttributes, null, betId);
	}


	public void handleSlotUpdateOrCreate(List<LottoNumbers> selectedNumbersList, List<Integer> selectedNumbers,
										  Integer slotId) {
		boolean slotUpdated = selectedNumbersList.stream()
			.filter(lottoNumbers -> lottoNumbers.getSlotId().equals(slotId))
			.peek(lottoNumbers -> lottoNumbers.setNumbers(new HashSet<>(selectedNumbers)))
			.findFirst()
			.isPresent();

		if (!slotUpdated) {
			selectedNumbersList.add(new LottoNumbers(new HashSet<>(selectedNumbers), slotId));
		}
	}


	public String handleSuccessfulSlotSubmission(RedirectAttributes redirectAttributes, Long communityId,
												  Long betId) {
		redirectAttributes.addFlashAttribute("message", "Slot wurde erfolgreich gespeichert.");
		redirectAttributes.addFlashAttribute("selectedNumbers", new HashSet<>());

		if (communityId != null) {
			redirectAttributes.addAttribute("communityId", communityId);
		}

		if (betId != null) {
			redirectAttributes.addAttribute("betId", betId);
		}

		return "redirect:/spielen";
	}

	public boolean hasEnoughMoney(User.UserIdentifier userId, double price, RedirectAttributes redirectAttributes){
		Optional<User> userOptional = lotto.getUserManagement().getUsers().findById(userId);
		if (userOptional.isEmpty()) {
			throw new IllegalArgumentException("User not found");
		}

		User user = userOptional.get();

		double totalPendingAmount = lotto.getBettingSlipRepository().findByUserId(userId).stream()
			.filter(slip -> slip.getStatus() == Status.PENDING)
			.mapToDouble(LottoBettingSlip::getBetAmount)
			.sum();

		if (price > (user.getBankAccount().getBalance() - totalPendingAmount)) {
			redirectAttributes.addFlashAttribute("tooPoorError", "Nicht genügend Guthaben.");
			return false;
		}
		return true;
	}

	public boolean isSubmissionTooLate(RedirectAttributes redirectAttributes) {
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime nextDrawDate = Lotto.getNextDrawDate();
		if (!today.isBefore(nextDrawDate.minusDays(1))) {
			redirectAttributes.addFlashAttribute("tooLateError", "Die Abgabe ist einen Tag vor der Ziehung geschlossen.");
			return true;
		}
		return false;
	}


	public LottoBettingSlip updateExistingBettingSlip(Long betId, int superNumber, double betAmount, RedirectAttributes redirectAttributes) {
		LottoBettingSlip slip = lotto.getBettingSlipRepository().findById(betId).orElse(null);

		if (slip != null) {
			slip.setSuperNumber(superNumber);
			slip.setBetAmount(betAmount);
			lotto.saveSlip(slip);
			redirectAttributes.addFlashAttribute("message", "Tippschein wurde erfolgreich aktualisiert!");
		} else {
			redirectAttributes.addFlashAttribute("error", "Wette nicht gefunden.");
		}

		return slip;
	}


	public LottoBettingSlip createNewBettingSlip(User.UserIdentifier slipUserId,
												 Long communityId,
												 double betAmount,
												 List<LottoNumbers> selectedNumbersList,
												 int superNumber,
												 Duration duration,
												 RedirectAttributes redirectAttributes,
												 SessionStatus status) {

		LottoBettingSlip slip = (duration != null)
			? new PermanentLottoBettingSlip(LocalDateTime.now(), slipUserId, communityId, betAmount, selectedNumbersList, superNumber, duration)
			: new SingleLottoBettingSlip(LocalDateTime.now(), slipUserId, communityId, betAmount, selectedNumbersList, superNumber);

		boolean hasSelectedNumbers = selectedNumbersList.stream()
			.anyMatch(slot -> slot.getNumbers() != null && !slot.getNumbers().isEmpty());

		if (slip.isValid() && hasSelectedNumbers) {
			lotto.saveSlip(slip);
			if (communityId != null) {
				communityManagement.addCommunitySlipLotto(slip);
			}
			redirectAttributes.addFlashAttribute("message", "Tippschein wurde erfolgreich abgeschickt!");
			status.setComplete();
		} else {
			redirectAttributes.addFlashAttribute("error", "Ungültiges Tippschein. Vor dem Senden muss mindestens ein Slot gefüllt sein.");
			slip = null;
		}

		return slip;
	}


	public void handleBettingSlipRandomNumbers(Long betId) {
		LottoBettingSlip bet = lotto.getBettingSlipRepository().findById(betId).orElse(null);

		if (bet != null) {
			bet.getNumbersList().clear();
			generateRandomNumbersForSlots(bet.getNumbersList(), 12, 6, 49);
			double betAmount = bet.getNumbersList().size() * LottoBettingSlip.SLOT_PRICE;
			bet.setBetAmount(betAmount);
			lotto.getBettingSlipRepository().save(bet);
		}
	}


	public void handleSlotUpdateInBettingSlip(Long betId, int slotId, Set<Integer> generatedNumbers, RedirectAttributes redirectAttributes) {
		LottoBettingSlip bet = lotto.getBettingSlipRepository().findById(betId)
			.orElseThrow(() -> new IllegalArgumentException("Wette ist nicht gefunden: " + betId));

		Set<Integer> betNumbers;
		if (bet.getNumbersList().size() >= slotId) {
			betNumbers = bet.getNumbersForSlot(slotId - 1);
		} else {
			betNumbers = new HashSet<>();
		}

		betNumbers.clear();
		betNumbers.addAll(generatedNumbers);

		bet.setNumbersForSlot(slotId - 1, betNumbers, true);

		double betAmount = bet.getNumbersList().size() * LottoBettingSlip.SLOT_PRICE;
		bet.setBetAmount(betAmount);

		lotto.getBettingSlipRepository().save(bet);

		redirectAttributes.addFlashAttribute("message", "Slot wurde erfolgreich aktualisiert.");
		redirectAttributes.addAttribute("betId", betId);
	}


	public void generateRandomNumbersForSlots(List<LottoNumbers> numbersList, int slotCount, int numbersPerSlot, int maxNumber) {
		numbersList.clear();

		Random random = new Random();

		for (int slotId = 1; slotId <= slotCount; slotId++) {
			Set<Integer> slotNumbers = new HashSet<>();

			while (slotNumbers.size() < numbersPerSlot) {
				slotNumbers.add(random.nextInt(maxNumber) + 1);
			}

			numbersList.add(new LottoNumbers(slotNumbers, slotId));
		}
	}


	public void updateSlotInSelectedNumbersList(int slotId, List<LottoNumbers> selectedNumbersList, Set<Integer> generatedNumbers) {
		LottoNumbers existingSlot = selectedNumbersList.stream()
			.filter(lottoNumbers -> lottoNumbers.getSlotId() == slotId)
			.findFirst()
			.orElse(null);

		if (existingSlot != null) {
			existingSlot.getNumbers().clear();
			existingSlot.getNumbers().addAll(generatedNumbers);
		} else {
			selectedNumbersList.add(new LottoNumbers(generatedNumbers, slotId));
		}
	}


	public void clearSlotNumbers(Long betId, int slotId, List<LottoNumbers> selectedNumbersList) {
		if (betId != null) {
			LottoBettingSlip bet = lotto.getBettingSlipRepository().findById(betId)
				.orElseThrow(() -> new IllegalArgumentException("BetId ist nicht gefunden: " + betId));

			if (slotId == 0) {
				bet.getNumbersList().clear();
			} else {
				bet.getNumbersList().removeIf(lottoNumbers -> lottoNumbers.getSlotId() == slotId);
			}

			for (int i = 0; i < bet.getNumbersList().size(); i++) {
				bet.getNumbersList().get(i).setSlotId(i + 1);
			}

			bet.setBetAmount(bet.getNumbersList().size() * LottoBettingSlip.SLOT_PRICE);
			lotto.getBettingSlipRepository().save(bet);

		} else {
			if (slotId == 0) {
				selectedNumbersList.clear();
			} else {
				selectedNumbersList.removeIf(lottoNumbers -> lottoNumbers.getSlotId() == slotId);
			}

			for (int i = 0; i < selectedNumbersList.size(); i++) {
				selectedNumbersList.get(i).setSlotId(i + 1);
			}
		}
	}
}
