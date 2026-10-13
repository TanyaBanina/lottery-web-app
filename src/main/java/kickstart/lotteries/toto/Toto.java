package kickstart.lotteries.toto;

import kickstart.community.CommunityManagement;
import kickstart.lotteries.toto.slips.TotoBettingSlip;
import kickstart.lotteries.toto.slips.TotoBettingSlipService;
import kickstart.lotteries.toto.slips.TotoStatus;
import kickstart.lotteries.toto.results.Bundesliga1Results;
import kickstart.lotteries.toto.results.Bundesliga2Results;
import kickstart.profil.konto.Message;
import kickstart.profil.konto.MessageManagement;
import kickstart.user.PaymentType;
import kickstart.user.User;
import kickstart.user.UserManagement;
import kickstart.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Toto {

    @Autowired
    private TotoBettingSlipService bettingSlipService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityManagement communityManagement;
	@Autowired
	private UserManagement userManagement;
	@Autowired
	private MessageManagement messageManagement;

	public Toto() {
    }

    /**
     * Compare user's predictions with actual results (both are "1","0","2").
     * correctCount: number of exact matches.
     * Winnings:
     * - all correct: betAmount * 10
     * - >=7 correct: betAmount * 2
     * - otherwise: 0
     */
    public double calculateWinnings(TotoBettingSlip slip, List<String> actualResults) {
        int correctCount = 0;
        List<String> userPreds = slip.getPredictions().values().stream().toList();
        for (int i = 0; i < actualResults.size(); i++) {
            if (userPreds.get(i).equals(actualResults.get(i))) {
                correctCount++;
            }
        }

        if (correctCount == actualResults.size()) {
            return slip.getBetAmount() * 4;
        } else if (correctCount >= 6) {
            return slip.getBetAmount() * 2;
        }
        return 0.0;
    }

    public void processSingleBettingSlip(TotoBettingSlip slip, List<String> actualResults) {
        double winnings = calculateWinnings(slip, actualResults);
        slip.setWinningAmount(winnings);

        if (slip.getCommunityId() == null) {
            if (winnings > 0) {
                User user = userRepository.findById(slip.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));
//                user.getBankAccount().deposit(winnings);
//				userManagement.depositToLoggedInUser(winnings, PaymentType.FUSSBALLTOTO);
				userManagement.depositToUser(slip.getUserId().getIdentifier(), winnings, PaymentType.FUSSBALLTOTO);
				String content = "Deine Fussballwette von " + slip.getDateSubmitted().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +" Uhr wurde ausgewertet. Du erhaeltst" + winnings +" €.";
				User.UserIdentifier userId = user.getId();

				Message message = new Message(content, userId);
				messageManagement.addMessage(message);

                userRepository.save(user);
            }
        } else {
            // community scenario
            communityManagement.evaluateCommunityTotoBettingSlip(slip);
        }

        slip.setStatus(TotoStatus.EXPIRED);
        bettingSlipService.updateSlip(slip);
    }

    @Service
    public class TotoDrawService {

        /**
         * Scheduled draw: for simplicity, actualResults from Bundesliga1Results or Bundesliga2Results
         * are integers converted to strings ("0","1","2"). The slip predictions must match this pattern.
         */
        @Scheduled(cron = "0 0 0 * * ?", zone = "Europe/Berlin")
        public void performDraw() {
            List<TotoBettingSlip> dueSlips = bettingSlipService.findDuePendingSlips();
            for (TotoBettingSlip slip : dueSlips) {
                List<String> actualResults;
                if ("bundesliga1".equalsIgnoreCase(slip.getLeague())) {
                    // Convert matchResult int to string
                    actualResults = Bundesliga1Results.getMatchdayResults().stream()
                        .map(mr -> String.valueOf(mr.getMatchResult()))
                        .collect(Collectors.toList());
                } else {
                    actualResults = Bundesliga2Results.getMatchdayResults().stream()
                        .map(mr -> String.valueOf(mr.getMatchResult()))
                        .collect(Collectors.toList());
                }

                processSingleBettingSlip(slip, actualResults);
            }
        }
    }
}

