package kickstart.community;

import kickstart.lotteries.lotto.slips.LottoBettingSlip;
import kickstart.lotteries.lotto.slips.LottoNumbers;
import kickstart.lotteries.toto.Match;
import kickstart.lotteries.toto.slips.TotoBettingSlip;
import kickstart.profil.konto.Message;
import kickstart.profil.konto.MessageManagement;
import kickstart.user.PaymentType;
import kickstart.user.User;
import kickstart.user.UserManagement;
import org.jetbrains.annotations.TestOnly;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Transactional
public class CommunityManagement {

    private final CommunityRepository communityRepository;
    private final UserManagement userManagement;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final DateTimeFormatter preciseFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	private final MessageManagement messageManagement;

    public CommunityManagement(CommunityRepository communityRepository,
							   UserManagement userManagement, MessageManagement messageManagement) {
        this.communityRepository = communityRepository;
        this.userManagement = userManagement;
		this.messageManagement = messageManagement;
	}

	/**
	 * function to store Community instead of using the repo interface directly
	 * @param community
	 */
    public void addCommunityToCommunityRepository(Community community) {
        String password = community.getPassword();
        String hashedPassword = CommunityHelper.hashPassword(password);
        community.setPassword(hashedPassword);
        communityRepository.save(community);
    }

    public Community findCommunityById(long id) {
        return communityRepository.findById(id);
    }

	/**
	 * Distribute Lotto earnings among community members based on their portion of the bet.
	 */
    public void evaluateCommunityLottoBettingSlip(LottoBettingSlip lottoBettingSlip) {
        long communityId = lottoBettingSlip.getCommunityId();
        long bettingSlipId = lottoBettingSlip.getId();
        Community community = findCommunityById(communityId);

        if (community == null) {
            return;
        }

        CommunityBettingInfo bettingInfo = community.getCommunityBettingInfoByBettingSlipId(bettingSlipId);
        if (bettingInfo == null) {
            return;
        }

        double basePrice = bettingInfo.getBasePrice();
        double totalWinningAmount = lottoBettingSlip.getWinningAmount();

        // If basePrice is essentially zero, avoid division
        double epsilon = 1e-9;
        double betAmountToWinningAmount = (Math.abs(basePrice) < epsilon) ? 0.0 : totalWinningAmount / basePrice;

        for (Map.Entry<String, Double> entry : bettingInfo.getUserBets().entrySet()) {
            double bet = entry.getValue();
            double smallEpsilon = 1e-3;
            if (Math.abs(bet) < smallEpsilon) { //so it doesnt add values like 0.0003
                continue;
            }

            String username = entry.getKey();
            double userWin = Math.round(bet * betAmountToWinningAmount * 100.0) / 100.0; // *100 / 100 for rounding
            User user = userManagement.getUserByName(username);
            if (user != null && userWin > 0) {
                userManagement.depositToUser(user.getId().getIdentifier(), userWin, PaymentType.GEMEINSCHAFTSWETTE);

				String content = "Deine Lotto Gemeinschaftswette von " + lottoBettingSlip.getDrawDate().format(preciseFormatter) + " Uhr wurde ausgewertet. Du erhaeltst" + userWin +" €.";
				User.UserIdentifier userId = user.getId();

				Message message = new Message(content, userId);
				messageManagement.addMessage(message);
            }
        }
    }

    /**
     * Distribute Toto winnings among community members based on their portion of the bet.
     */
    public void evaluateCommunityTotoBettingSlip(TotoBettingSlip totoBettingSlip) {
        Long communityId = totoBettingSlip.getCommunityId();
        if (communityId == null) {
            return;
        }

        Community community = findCommunityById(communityId);
        if (community == null) {
            return;
        }

        CommunityBettingInfo bettingInfo = community.getCommunityBettingInfoByBettingSlipId(totoBettingSlip.getId());
        if (bettingInfo == null) {
            return;
        }

        double basePrice = bettingInfo.getBasePrice();
        double totalWinning = totoBettingSlip.getWinningAmount();
        double epsilon = 1e-9;
        double betAmountToWinningAmount = (Math.abs(basePrice) < epsilon) ? 0.0 : totalWinning / basePrice;

        for (Map.Entry<String, Double> entry : bettingInfo.getUserBets().entrySet()) {
            double bet = entry.getValue();
            double smallEpsilon = 1e-3;
            if (Math.abs(bet) < smallEpsilon) {
                continue;
            }

            String username = entry.getKey();
            double userWin = Math.round(bet * betAmountToWinningAmount * 100.0) / 100.0;
            User user = userManagement.getUserByName(username);
            if (user != null && userWin > 0) {
                userManagement.depositToUser(user.getId().getIdentifier(), userWin, PaymentType.GEMEINSCHAFTSWETTE);

				String content = "Deine Toto Gemeinschaftswette von " + totoBettingSlip.getDateSubmitted().format(preciseFormatter) + " Uhr wurde ausgewertet. Du erhaeltst" + userWin +" €.";
				User.UserIdentifier userId = user.getId();

				Message message = new Message(content, userId);
				messageManagement.addMessage(message);
            }
        }
    }

	/**
	 * creates a new community by communityForm and saves it in the Repository
	 * @param communityForm
	 */
    public void createNewCommunityByCommunityForm(CommunityForm communityForm) {
        Community community = convertCommunityFormToCommunity(communityForm);
        if (findCommunityByName(community.getName()) == null &&
            !community.getPassword().isEmpty() &&
            community.getName().length() <= 20) {

            addCommunityToCommunityRepository(community);
            userManagement.addCommunityToLoggedInUser(community);
        }
    }

	/**
	 * converts a communityForm to a Community
	 * @param communityForm
	 * @return community
	 */
    public Community convertCommunityFormToCommunity(CommunityForm communityForm) {
        String communityName = communityForm.getName();
        String password = communityForm.getPassword();
        User admin = userManagement.getLoggedInUser();
        return new Community(admin, communityName, password);
    }

	/**
	 * join a community by joinCommunityForm
	 * @param joinCommunityForm
	 */
    public void joinCommunityByJoinCommunityForm(JoinCommunityForm joinCommunityForm) {
        String communityName = joinCommunityForm.getName();
        Community community = communityRepository.findByName(communityName);
        if (community == null || community.getCommunitySize() == 30) {
            return;
        }

        String password = joinCommunityForm.getPassword();
        if (password == null) {
            return;
        }

        String hashedPassword = CommunityHelper.hashPassword(password);
        if (hashedPassword == null) {
            return;
        }

        if (hashedPassword.equals(community.getPassword())) {
            User loggedInUser = userManagement.getLoggedInUser();
            if (!community.getCommunityMembers().contains(loggedInUser)) {
                community.addCommunityMember(loggedInUser);
                userManagement.addCommunityToLoggedInUser(community);
            }
        }
    }

	/**
	 * leave a community
	 * @param communityName
	 */
    public void leaveCommunityByName(String communityName) {
        Community community = communityRepository.findByName(communityName);
        if (community == null) {
            return;
        }

        User user = userManagement.getLoggedInUser();
        community.removeCommunityMember(user);
        communityRepository.save(community);
        userManagement.removeCommunityFromLoggedInUser(community);

        if (community.getCommunityMembers().isEmpty()) {
            communityRepository.delete(community);
        } else {
            // Remove user only once above
        }
    }
	/**
	 * finds a community based on name
	 * @param communityName
	 */
    public Community findCommunityByName(String communityName) {
        return communityRepository.findByName(communityName);
    }

	/**
	 * adds a community bet for Lotto to the CommunityBettingInfos of the community
	 * @param lottoBettingSlip
	 */
    public void addCommunitySlipLotto(LottoBettingSlip lottoBettingSlip) {
        long communityId = lottoBettingSlip.getCommunityId();
        Community community = communityRepository.findById(communityId);
        if (community == null) {
            return;
        }

        long id = lottoBettingSlip.getId();
        String gameName = "Lotto";
        String submittedDateTime = lottoBettingSlip.getDateSubmitted().format(formatter);
        User betCreator = userManagement.getLoggedInUser();
        double overallBetAmount = 0.0;
        String drawDate = lottoBettingSlip.getDrawDate().format(formatter);
        Double basePrice = lottoBettingSlip.getBetAmount();
		String betInfo = "Zahlen: \n";
		int i = 0;
		for (LottoNumbers lottoNumbers: lottoBettingSlip.getNumbersList())
		{
			i++;
			String singleList = "";
			for (int number: lottoNumbers.getNumbers()) {
				singleList += number + " ";
			}
			singleList += " ".repeat(18 - singleList.length());
			betInfo += singleList;
			betInfo += " | ";
			if (i % 2 == 0) {
				betInfo += "\n";
			}
		}
		betInfo += "SuperZahl: " +lottoBettingSlip.getSuperNumber();

        CommunityBettingInfo communityBettingInfo = new CommunityBettingInfo(
            basePrice,
            community.getCommunityMembers(),
            id,
            gameName,
            submittedDateTime,
            betCreator,
            overallBetAmount,
            drawDate
        );
		communityBettingInfo.setInfo(betInfo);
        community.addCommunityBettingInfo(communityBettingInfo);
        communityRepository.save(community);
    }

	/**
	 * adds a community bet for Toto to the CommunityBettingInfos of the community
	 * @param slip
	 */
    public void addCommunitySlipToto(TotoBettingSlip slip) {
        Long communityId = slip.getCommunityId();
        if (communityId == null) {
            return;
        }

        Community community = communityRepository.findById(communityId).orElse(null);
        if (community == null) {
            return;
        }

        long id = slip.getId();
        String gameName = "Toto";
        String submittedDateTime = slip.getDateSubmitted().format(formatter);
        User betCreator = userManagement.getLoggedInUser();
        double overallBetAmount = 0.0;
        String drawDate = slip.getDrawDate().toLocalDate().format(formatter);
        double basePrice = slip.getBetAmount();
		String betInfo = "";
		for (Map.Entry<String, String> entry : slip.getPredictions().entrySet()) {
			betInfo += entry.getValue() + " | ";
		}



        CommunityBettingInfo info = new CommunityBettingInfo(
            basePrice,
            community.getCommunityMembers(),
            id,
            gameName,
            submittedDateTime,
            betCreator,
            overallBetAmount,
            drawDate
        );
		info.setInfo(betInfo);
        community.addCommunityBettingInfo(info);
        communityRepository.save(community);
    }


}

