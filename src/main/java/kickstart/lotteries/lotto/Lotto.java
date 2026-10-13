package kickstart.lotteries.lotto;

import kickstart.community.CommunityManagement;
import kickstart.lotteries.lotto.results.Result;
import kickstart.lotteries.lotto.results.PastResultsRepository;
import kickstart.lotteries.lotto.slips.*;

import kickstart.profil.konto.Message;
import kickstart.profil.konto.MessageManagement;
import kickstart.user.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import kickstart.user.User.UserIdentifier;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class that handles the 6 aus 49 lottery logic.
 */
@Service
public class Lotto {

	// Fields
	private static LocalDateTime nextDrawDate;  // Next draw date
	@Autowired
	private LottoBettingSlipRepository bettingSlipRepository;
	@Autowired
	private PastResultsRepository pastResultsRepository;
	@Autowired
	private CommunityManagement communityManagement;
	@Autowired
	private UserManagement userManagement;
	@Autowired
	private MessageManagement messageManagement;


	/**
	 * Initializes the repositories required for managing user data, betting slips, and past results.
	 * Also calculates and sets the next draw date upon instantiation.
	 *
	 * @param bettingSlipRepository   Repository for managing betting slips in the lottery system.
	 * @param pastResultsRepository   Repository for storing and retrieving past lottery results.
	 */
	public Lotto(LottoBettingSlipRepository bettingSlipRepository,
				 PastResultsRepository pastResultsRepository,
				 CommunityManagement communityManagement, UserManagement userManagement, MessageManagement messageManagement) {
		this.pastResultsRepository = pastResultsRepository;
		this.bettingSlipRepository = bettingSlipRepository;
		this.communityManagement = communityManagement;
		this.userManagement = userManagement;
		this.messageManagement = messageManagement;

		nextDrawDate = calculateNextDrawDate();
	}

	/**
	 * Enumeration representing possible winning categories.
	 */
	public enum WinningCategory {
		JACKPOT, SIX_CORRECT, FIVE_CORRECT_SUPER, FIVE_CORRECT,
		FOUR_CORRECT_SUPER, FOUR_CORRECT, THREE_CORRECT_SUPER,
		THREE_CORRECT, TWO_CORRECT_SUPER, TWO_CORRECT,
		ONE_CORRECT_SUPER, ZERO_CORRECT_SUPER, NO_WIN
	}

	public CommunityManagement getCommunityManagement() {
		return communityManagement;
	}

	public UserManagement getUserManagement() {
		return userManagement;
	}

	public LottoBettingSlipRepository getBettingSlipRepository() {
		return bettingSlipRepository;
	}

	public PastResultsRepository getPastResultsRepository() {
		return pastResultsRepository;
	}

	/**
	 * Saves a new result of a draw to the repository.
	 *
	 * @param date               the date of the draw
	 * @param winningNumbers     the winning numbers
	 * @param winningSuperNumber the winning super number
	 */
	public void savePastResult(LocalDateTime date, Set<Integer> winningNumbers, Integer winningSuperNumber) {
		Result result = new Result(date, winningNumbers, winningSuperNumber);
		pastResultsRepository.save(result);
	}

	/**
	 * Saves a new betting slip to the repository.
	 *
	 * @param slip the betting slip to be saved
	 */
	public void saveSlip(LottoBettingSlip slip) {
		bettingSlipRepository.save(slip);
	}
	public List<Result> getPastResults() {
		return pastResultsRepository.findAll();
	}
	public static LocalDateTime getNextDrawDate() {
		return nextDrawDate;
	}


	/**
	 * Generates a set of 6 unique random numbers between 1 and 49.
	 *
	 * @return a set of 6 random numbers
	 */
	public Set<Integer> generateLotteryNumbers() {
		Set<Integer> lotteryNumbers = new HashSet<>();
		Random random = new Random();

		while (lotteryNumbers.size() < 6) {

			int number = random.nextInt(49) + 1;
			lotteryNumbers.add(number);
		}
		return lotteryNumbers;
	}

	/**
	 * Generates a random super number between 1 and 10.
	 *
	 * @return a random super number
	 */
	public int generateSuperNumber() {
		return new Random().nextInt(10) + 1; // Random super number between 1 and 10
	}

	/**
	 * Checks if a given betting slip is a winning slip based on the draw results.
	 *
	 * @param slip the betting slip to check
	 * @return the winning category of the slip
	 */
	public Map<LottoNumbers, WinningCategory> checkIfWinningSlip(LottoBettingSlip slip) {
		LocalDateTime drawDate = slip.getDrawDate();

		//Result result = pastResultsRepository.findByDrawDate(drawDate);
		Result result = pastResultsRepository.findTopByOrderByDrawDateDesc();

		if (result == null) {
			throw new IllegalArgumentException("Kein Result für dieses Date: " + drawDate);
		}

		Set<Integer> winningNumbers = result.getWinningNumbers();
		int winningSuperNumber = result.getSuperNumber();
		Map<LottoNumbers, WinningCategory> winnings = new HashMap<>();
		int superNumber = slip.getSuperNumber();

		for (LottoNumbers lottoNumbers : slip.getNumbersList()) {
			int matchingMainNumbers = countMatchingMainNumbers(lottoNumbers.getNumbers(), winningNumbers);
			WinningCategory category = determineWinningCategory(matchingMainNumbers, superNumber, winningSuperNumber);
			winnings.put(lottoNumbers, category);
		}

		return winnings;
	}
	/**
	 * Determines winning category based on matching numbers and super number.
	 */
	private WinningCategory determineWinningCategory(int matchingMainNumbers, int superNumber, int winningSuperNumber) {
		boolean superNumberMatches = superNumber == winningSuperNumber;

        return switch (matchingMainNumbers) {
            case 6 -> superNumberMatches ? WinningCategory.JACKPOT : WinningCategory.SIX_CORRECT;
            case 5 -> superNumberMatches ? WinningCategory.FIVE_CORRECT_SUPER : WinningCategory.FIVE_CORRECT;
            case 4 -> superNumberMatches ? WinningCategory.FOUR_CORRECT_SUPER : WinningCategory.FOUR_CORRECT;
            case 3 -> superNumberMatches ? WinningCategory.THREE_CORRECT_SUPER : WinningCategory.THREE_CORRECT;
            case 2 -> superNumberMatches ? WinningCategory.TWO_CORRECT_SUPER : WinningCategory.TWO_CORRECT;
            case 1 -> superNumberMatches ? WinningCategory.ONE_CORRECT_SUPER : WinningCategory.NO_WIN;
            case 0 -> superNumberMatches ? WinningCategory.ZERO_CORRECT_SUPER : WinningCategory.NO_WIN;
            default -> WinningCategory.NO_WIN;
        };
	}



	int countMatchingMainNumbers(Set<Integer> ticketNumbers, Set<Integer> winningNumbers) {
		int matchCount = 0;
		for (Integer number : ticketNumbers) {
			if (winningNumbers.contains(number)) {
				matchCount++;
			}
		}
		return matchCount;
	}

	/**
	 * Retrieves the prize amount for a given winning category.
	 *
	 * @param category the winning category
	 * @return the prize amount
	 */
	public double getPrizeAmount(WinningCategory category) {
        return switch (category) {
            case JACKPOT -> 1000000.0;
            case SIX_CORRECT -> 50000.0;
            case FIVE_CORRECT_SUPER -> 10000.0;
            case FIVE_CORRECT -> 5000.0;
            case FOUR_CORRECT_SUPER -> 1000.0;
            case FOUR_CORRECT -> 500.0;
            case THREE_CORRECT_SUPER -> 100.0;
            case THREE_CORRECT -> 50.0;
            case TWO_CORRECT_SUPER -> 20.0;
            case TWO_CORRECT -> 10.0;
            case ONE_CORRECT_SUPER -> 5.0;
            case ZERO_CORRECT_SUPER -> 1.0;
            default -> 0.0;
        };
	}
	//helper functions for controller
	public Result getResultByDate(String selectedDate) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSSSSSSSS");
		LocalDateTime date = LocalDateTime.parse(selectedDate, dateFormatter);
		return pastResultsRepository.findByDrawDate(date);
	}

	public String getMostRecentDrawDate() {
		return getPastResults().stream()
			.map(Result::getDrawDate)
			.max(LocalDateTime::compareTo)
			.orElse(LocalDateTime.now()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSSSSSSSS"));
	}

	public List<Map<String, String>> getFormattedPastDrawDates() {
		DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSSSSSSSS");
		DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

		return pastResultsRepository.findAllByOrderByDrawDateDesc().stream()
			.map(result -> {
				Map<String, String> dateMap = new HashMap<>();
				dateMap.put("full", result.getDrawDate().format(fullFormatter));
				dateMap.put("display", result.getDrawDate().format(displayFormatter));
				return dateMap;
			})
			.collect(Collectors.toList());
	}
	public String getFormattedNextDrawDate() {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		return getNextDrawDate().format(dateTimeFormatter);
	}


	public Map<String, Map<String, Object>> calculateStatistics() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

		List<LottoBettingSlip> allBettingSlips = bettingSlipRepository.findAll();

		Map<LocalDateTime, List<LottoBettingSlip>> groupedByDate = allBettingSlips.stream()
			.collect(Collectors.groupingBy(LottoBettingSlip::getDrawDate));

		return groupedByDate.entrySet().stream()
			.sorted(Map.Entry.<LocalDateTime, List<LottoBettingSlip>>comparingByKey().reversed())
			.collect(Collectors.toMap(
				entry -> entry.getKey().format(formatter),
				entry -> {
					List<LottoBettingSlip> slips = entry.getValue();

					double totalRevenue = slips.stream().mapToDouble(LottoBettingSlip::getBetAmount).sum();
					double totalPayout = slips.stream().mapToDouble(LottoBettingSlip::getWinningAmount).sum();
					int slipCount = slips.size();

					Map<String, Object> stats = new HashMap<>();
					stats.put("revenue", totalRevenue);
					stats.put("payout", totalPayout);
					stats.put("slipCount", slipCount);

					return stats;
				},
				(oldValue, newValue) -> oldValue,
				LinkedHashMap::new
			));
	}

	/**
	 * Calculates the next Sunday at midnight.
	 *
	 * @return the next draw date and time
	 */
	LocalDateTime calculateNextDrawDate() {
		return LocalDateTime.now()
			.with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
			.withHour(0)
			.withMinute(0)
			.withSecond(0)
			.withNano(0);
	}

	/**
	 * Processes a betting slip, calculates winnings, and updates the user's account.
	 *
	 * @param slip the betting slip to process
	 */
	public void processBettingSlip(LottoBettingSlip slip) {
		Map<LottoNumbers, WinningCategory> winnings = checkIfWinningSlip(slip);

		double totalPrizeAmount = 0.0;

		for (WinningCategory category : winnings.values()) {
			totalPrizeAmount += getPrizeAmount(category);
		}
		slip.setWinningAmount(totalPrizeAmount);
		slip.setStatus(Status.EXPIRED);
		bettingSlipRepository.save(slip);

		try {
			if(slip.getCommunityId() == null){
				UserIdentifier userId = slip.getUserId();
				if(slip.getBetAmount()>0){
					userManagement.withdrawToUser(userId.getIdentifier(), slip.getBetAmount(), PaymentType.LOTTO);
				}
				if (totalPrizeAmount > 0) {
					userManagement.depositToUser(userId.getIdentifier(), totalPrizeAmount, PaymentType.LOTTO);
					String content = "Deine Lottowette von " + slip.getDateSubmitted().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +" Uhr wurde ausgewertet. Du erhältst " + totalPrizeAmount +" €.";
					//unterschiedliches datum hinschreiben bei longterm wetten

					Message message = new Message(content, userId);
					messageManagement.addMessage(message);
				}
			}
			else{
				communityManagement.evaluateCommunityLottoBettingSlip(slip);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("User doesnt have enough money!");
		}
	}

	public void performDraw() {
		// for testing
		LocalDateTime drawDate = LocalDateTime.now();
		performLotteryDraw(drawDate);
	}

	@Scheduled(cron = "0 0 0 * * SUN", zone = "Europe/Berlin")
	public void performScheduledDraw() {
		LocalDateTime drawDate = nextDrawDate;
		performLotteryDraw(drawDate);
	}

	private void performLotteryDraw(LocalDateTime drawDate) {

		createSingleBettingSlipsForActivePermSlips();

		Set<Integer> winningNumbers = generateLotteryNumbers();
		int winningSuperNumber = generateSuperNumber();

		savePastResult(drawDate, winningNumbers, winningSuperNumber);

		List<LottoBettingSlip> bettingSlips = bettingSlipRepository.findByStatus(Status.PENDING);

		for (LottoBettingSlip slip : bettingSlips) {
			slip.setDrawDate(drawDate);
			processBettingSlip(slip);
		}

		nextDrawDate = calculateNextDrawDate();
	}


	// Method to create SingleLottoBettingSlips from active PermanentLottoBettingSlips
	private void createSingleBettingSlipsForActivePermSlips() {
		List<PermanentLottoBettingSlip> activePermanentSlips = bettingSlipRepository.findByIsActiveTrue();
		List<PermanentLottoBettingSlip> expiredSlips = activePermanentSlips.stream()
			.filter(slip -> slip.getStatus() == Status.EXPIRED)
			.toList();

		for (PermanentLottoBettingSlip permSlip : expiredSlips) {
			List<LottoNumbers> copy = new ArrayList<>(permSlip.getNumbersList());
			SingleLottoBettingSlip singleSlip = new SingleLottoBettingSlip(
				LocalDateTime.now(),
				permSlip.getUserId(),
				permSlip.getCommunityId(),
				0.0,
				copy,
				permSlip.getSuperNumber()
			);
			bettingSlipRepository.save(singleSlip);
		}
	}
}
