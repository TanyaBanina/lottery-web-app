package kickstart.community;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import kickstart.lotteries.BettingSlip;
import kickstart.lotteries.lotto.slips.LottoBettingSlip;
import kickstart.lotteries.lotto.slips.LottoBettingSlipRepository;
import kickstart.lotteries.toto.slips.TotoBettingSlip;
import kickstart.lotteries.toto.slips.TotoBettingSlipRepository;
import kickstart.user.PaymentType;
import kickstart.user.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kickstart.user.UserManagement;

@PreAuthorize("hasRole('USER')")
@Controller
public class CommunityController {

    private final UserManagement userManagement;
    private final CommunityManagement communityManagement;
	private final CommunityRepository communityRepository;
	private final LottoBettingSlipRepository lottoBettingSlipRepository;
	private final TotoBettingSlipRepository totoBettingSlipRepository;

	public CommunityController(UserManagement userManagement,
							   CommunityManagement communityManagement,
							   CommunityRepository communityRepository,
							   LottoBettingSlipRepository lottoBettingSlipRepository, TotoBettingSlipRepository totoBettingSlipRepository) {

		this.userManagement = userManagement;
        this.communityManagement = communityManagement;
		this.communityRepository = communityRepository;
		this.lottoBettingSlipRepository = lottoBettingSlipRepository;
		this.totoBettingSlipRepository = totoBettingSlipRepository;
	}

    
    @GetMapping("/gemeinschaft")
    public String gemeinschaft(Model model) {
		User user = userManagement.getLoggedInUser();
        List<Community> communities = user.getCommunities();

		model.addAttribute("user", user);
        model.addAttribute("communities", communities);
        model.addAttribute("communityForm", new CommunityForm());
        model.addAttribute("joinCommunityForm", new JoinCommunityForm());


        return "gemeinschaft"; 
    }

	/**
	 * join a community by
	 * @param joinCommunityForm
	 * @return gemeinschaft.html
	 */
    @PostMapping("/joinCommunity")
    public String joinCommunity(JoinCommunityForm joinCommunityForm, Model model) {

        communityManagement.joinCommunityByJoinCommunityForm(joinCommunityForm);

        return "redirect:/gemeinschaft";


    }

	/**
	 * get the community interface to see al members and the ongoing bets
	 * @param communityName - name of community
	 * @param model
	 * @return meinegemeinschaft.html
	 */
    @GetMapping("/gemeinschaft/{communityName}")
    public String getCommunityInfo(@PathVariable("communityName") String communityName, Model model) {

        Community community = communityManagement.findCommunityByName(communityName);
        String currentUserName = userManagement.getLoggedInUserName();
        List<String> names = community.getCommunityMemberNames();
		long communityId = community.getId();

		List<CommunityBettingInfo> communityBettingInfos = community.getOngoingBets();
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("communityBettingInfos", communityBettingInfos);
		model.addAttribute("communityId", communityId);
        model.addAttribute("communityName", communityName);
        model.addAttribute("names", names);

        return "meinegemeinschaft"; 
        
    }

	/**
	 * removes a bet from a CommunityBettingInfo
	 * @param removedBetAmount - amount to remove
	 * @param bettingSlipId
	 * @param communityName
	 * @param userName
	 * @param request
	 * @param model
	 * @return redirect to getCommunityInfo()
	 */
	@PostMapping("/removeUserBetAmountFromCommunityBet")
	public String removeUserBetAmountFromCommunityBet(@RequestParam("removedBetAmount") Double removedBetAmount,
													  @RequestParam("bettingSlipId") Long bettingSlipId,
													  @RequestParam("communityName") String communityName,
													  @RequestParam("userName") String userName,
													  HttpServletRequest request,
													  Model model) {
		Community community = communityManagement.findCommunityByName(communityName);
		userManagement.depositToLoggedInUser(removedBetAmount, PaymentType.GEMEINSCHAFTSWETTE);

		CommunityBettingInfo bettingInfo = community.getCommunityBettingInfoByBettingSlipId(bettingSlipId);
		LottoBettingSlip bettingSlip = lottoBettingSlipRepository.findById(bettingSlipId).get();
		bettingInfo.addUserBet(userName, -removedBetAmount); //subtractUserBet later
		communityRepository.save(community);
		if (bettingSlip != null) {
			bettingSlip.setBetAmount(bettingInfo.getBetAmount());
			lottoBettingSlipRepository.save(bettingSlip);
		}
		model.addAttribute("betAmount", removedBetAmount);

		return "redirect:/gemeinschaft/" + community.getName();
	}


	/**
	 * add a user bet to a CommunityBettingInfo
	 * @param betAmount - amount to add
	 * @param bettingSlipId
	 * @param communityName
	 * @param userName
	 * @param request
	 * @param model
	 * @return redirect to getCommunityInfo()
	 */
	@PostMapping("/addUserBetAmountToCommunityBet")
	public String addUserBetToCommunityBet(@RequestParam("betAmount") Double betAmount,
										   @RequestParam("bettingSlipId") Long bettingSlipId,
										   @RequestParam("communityName") String communityName,
										   @RequestParam("userName") String userName,
										   HttpServletRequest request,
										   Model model) {
		Community community = communityManagement.findCommunityByName(communityName);
		if (userManagement.withdrawFromLoggedInUser(betAmount, PaymentType.GEMEINSCHAFTSWETTE)) { //handle error case
			CommunityBettingInfo bettingInfo = community.getCommunityBettingInfoByBettingSlipId(bettingSlipId);
			bettingInfo.addUserBet(userName, betAmount);

			if (lottoBettingSlipRepository.findById(bettingSlipId).isPresent()) {
				LottoBettingSlip bettingSlip = lottoBettingSlipRepository.findById(bettingSlipId).get();
				communityRepository.save(community);
				if (bettingSlip != null) {
					bettingSlip.setBetAmount(bettingInfo.getBetAmount());
					lottoBettingSlipRepository.save(bettingSlip);
				}
			} else {
				TotoBettingSlip totoBettingSlip = totoBettingSlipRepository.findById(bettingSlipId).get();
				communityRepository.save(community);
				if (totoBettingSlip != null) {
					totoBettingSlip.setBetAmount(bettingInfo.getBetAmount());
					totoBettingSlipRepository.save(totoBettingSlip);
				}
			}
			model.addAttribute("betAmount", betAmount);
		}
		return "redirect:/gemeinschaft/" + community.getName();
	}


	/**
	 * opens the ui interface for a CommunityBettingInfo so user can remove or place bets
	 * @param communityName - name of the Community
	 * @param bettingSlipId - id of the bettingslip
	 * @param model
	 * @return communityJoinBet.html - with info about the community bet
	 */
	@GetMapping("/community/{communityName}/options/{id}")
	public String getCommunityBetOptions(@PathVariable("communityName") String communityName,
										 @PathVariable("id") Long bettingSlipId,
										 Model model) {
		Community community = communityRepository.findByName(communityName);
		CommunityBettingInfo communityBettingInfo = community.getCommunityBettingInfoByBettingSlipId(bettingSlipId);
		Optional<LottoBettingSlip> bettingSlipOptional = lottoBettingSlipRepository.findById(bettingSlipId);
		String currentUserName = userManagement.getLoggedInUserName();
		int removeIndex = (int) (communityBettingInfo.getUserBets().get(currentUserName) / communityBettingInfo.getBasePrice());


		if (bettingSlipOptional.isPresent()) {
			LottoBettingSlip bettingSlip = bettingSlipOptional.get();

			model.addAttribute("bettingSlip", bettingSlip);
		}
		model.addAttribute("removeIndex", removeIndex);
		model.addAttribute("bettingSlipId", bettingSlipId);
		model.addAttribute("currentUserName", currentUserName);
		model.addAttribute("communityBettingInfo", communityBettingInfo);



		return "communityJoinBet";
	}

	/**
	 * leave a community upong pressing the leave button
	 * @param communityName
	 * @return gemeinschaft.html
	 */
    @PostMapping("/leaveCommunity")
    public String leaveCommunity(@RequestParam("communityName") String communityName) {
        communityManagement.leaveCommunityByName(communityName);

        return "redirect:/gemeinschaft";
    }

	/**
	 * creates a new Community if possible by using a communityForm
	 * @param communityForm
	 * @return gemeinschaft.html
	 */
    @PostMapping("/createCommunity")
    public String createNewCommunity(CommunityForm communityForm) {
		communityManagement.createNewCommunityByCommunityForm(communityForm);

        return "redirect:/gemeinschaft";
    }
}