package kickstart.lotteries.toto;

import kickstart.lotteries.toto.slips.TotoBettingSlip;
import kickstart.lotteries.toto.slips.TotoBettingSlipService;
import kickstart.profil.konto.Message;
import kickstart.profil.konto.MessageManagement;
import kickstart.user.PaymentType;
import kickstart.user.User;
import kickstart.user.UserManagement;
import kickstart.user.UserRepository;
import kickstart.user.User.UserIdentifier;
import kickstart.community.CommunityManagement;
import kickstart.lotteries.toto.results.Bundesliga1Results;
import kickstart.lotteries.toto.results.Bundesliga2Results;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TotoController {

    private final TotoBettingSlipService bettingSlipService;
    private final UserManagement userManagement;
    private final UserRepository userRepository;
    private final CommunityManagement communityManagement;
	private final MessageManagement messageManagement;

    public TotoController(TotoBettingSlipService bettingSlipService,
						  UserManagement userManagement,
						  UserRepository userRepository,
						  CommunityManagement communityManagement, MessageManagement messageManagement) {
        this.bettingSlipService = bettingSlipService;
        this.userManagement = userManagement;
        this.userRepository = userRepository;
        this.communityManagement = communityManagement;
		this.messageManagement = messageManagement;
	}

    @GetMapping("/bundesliga1")
    public String redirectToToto1PageBasedOnRole(Model model,
                                                 @RequestParam(value="communityId", required=false) Long communityId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("slips", bettingSlipService.getAllSlips());
            if (communityId != null) {
                model.addAttribute("communityId", communityId);
            }
            return "/totoStatistik1";
        } else if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_USER"))) {
            model.addAttribute("matches", Bundesliga1Matches.getPredefinedMatches());
            if (communityId != null) {
                model.addAttribute("communityId", communityId);
            }
            return "Bundesliga1Form";
        }
        throw new AccessDeniedException("You do not have access to this page.");
    }

    @PostMapping("/bundesliga1/submit")
    public String submitToto1(@RequestParam Map<String, String> formParams,
                              @RequestParam(value="communityId", required=false) Long communityId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        double betAmount = Double.parseDouble(formParams.get("betAmount"));
        Map<String, String> predictions = new LinkedHashMap<>();
        formParams.forEach((key, value) -> {
            if (key.startsWith("match-")) {
                String matchNum = key.substring("match-".length());
                predictions.put("Match " + matchNum, value);
            }
        });

        UserIdentifier userId = userManagement.getLoggedInUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (predictions.size() != 9) {
            redirectAttributes.addFlashAttribute("predictions", predictions);
            redirectAttributes.addAttribute("communityId", communityId);
            redirectAttributes.addFlashAttribute("error", "Bitte geben Sie für jedes Spiel einen Tipp ab.");
            return "redirect:/bundesliga1";
        }

        if (user.getBankAccount().getBalance() < betAmount) {
            redirectAttributes.addFlashAttribute("error", "Nicht genügend Guthaben.");
            return "redirect:/bundesliga1" + (communityId != null ? "?communityId=" + communityId : "");
        }

        if (communityId == null) {
			userManagement.withdrawFromLoggedInUser(betAmount, PaymentType.FUSSBALLTOTO);
//			user.getBankAccount().withdraw(betAmount);
            userRepository.save(user);
        }

        // predictions are "1","0","2" representing outcomes
        TotoBettingSlip slip = new TotoBettingSlip(LocalDateTime.now(), betAmount, predictions, userId, communityId);

		if (communityId == null) {
			String username = userManagement.getLoggedInUserName();
			String content = "Erfolgreich Fusballwette fuer 1. Bundesliga erstellt: \nKosten: " + betAmount +
				"€\n";
			int i = -1;
			List<Match> matches = Bundesliga1Matches.getPredefinedMatches();
			for (Map.Entry<String, String> entry : formParams.entrySet()) {
				if (i == -1) {
					i++;
					continue;
				}
				Match match = matches.get(i);
				String game = "";
				game += match.getTeamA() + " vs " + match.getTeamB();
				try {
					int value = Integer.parseInt(entry.getValue());

					switch (value) {
						case 0:
							game += " - Gewinner: " + match.getTeamA();
							break;
						case 1:
							game += " - Unentschieden";
							break;
						default:
							game += " - Gewinner: " + match.getTeamB();
							break;
					}
				} catch (NumberFormatException e) {
					System.out.println("Invalid number format in entry value: " + entry.getValue());
				}
				content += game;
				if (i % 2 == 1 ) {
					content += "\n";
				} else {
					content += "  |  ";
				}
				i++;
			}
			System.out.println(content);
			Message message = new Message(content, userId);
			messageManagement.addMessage(message);
		}


		slip.setLeague("bundesliga1");
        bettingSlipService.saveSlip(slip);

        if (communityId != null) {
            communityManagement.addCommunitySlipToto(slip);
            String communityName = communityManagement.findCommunityById(communityId).getName();
            return "redirect:/gemeinschaft/" + communityName;
        }

        model.addAttribute("predictions", predictions);
        return "Bundesliga1Confirmation";
    }

    @GetMapping("/bundesliga2")
    public String redirectToToto2PageBasedOnRole(Model model,
                                                 @RequestParam(value="communityId", required=false) Long communityId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("slips", bettingSlipService.getAllSlips());
            if (communityId != null) {
                model.addAttribute("communityId", communityId);
            }
            return "/totoStatistik2";
        } else if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_USER"))) {
            model.addAttribute("matches", Bundesliga2Matches.getPredefinedMatches());
            if (communityId != null) {
                model.addAttribute("communityId", communityId);
            }
            return "Bundesliga2Form";
        }
        throw new AccessDeniedException("You do not have access to this page.");
    }

    @PostMapping("/bundesliga2/submit")
    public String submitToto2(@RequestParam Map<String, String> formParams,
                              @RequestParam(value="communityId", required=false) Long communityId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        double betAmount = Double.parseDouble(formParams.get("betAmount"));
        Map<String, String> predictions = new LinkedHashMap<>();
        formParams.forEach((key, value) -> {
            if (key.startsWith("match-")) {
                String matchNum = key.substring("match-".length());
                predictions.put("Match " + matchNum, value);
            }
        });

        UserIdentifier userId = userManagement.getLoggedInUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (predictions.size() != 9) {
            redirectAttributes.addFlashAttribute("predictions", predictions);
            redirectAttributes.addFlashAttribute("error", "Bitte geben Sie für jedes Spiel einen Tipp ab.");
            return "redirect:/bundesliga2";
        }

        if (user.getBankAccount().getBalance() < betAmount) {
            redirectAttributes.addFlashAttribute("error", "Nicht genügend Guthaben.");
            return "redirect:/bundesliga2" + (communityId != null ? "?communityId=" + communityId : "");
        }
        if (communityId == null) {
			userManagement.withdrawFromLoggedInUser(betAmount, PaymentType.FUSSBALLTOTO);
//            user.getBankAccount().withdraw(betAmount);
            userRepository.save(user);
        }

        TotoBettingSlip slip = new TotoBettingSlip(LocalDateTime.now(), betAmount, predictions, userId, communityId);
        slip.setLeague("bundesliga2");
        bettingSlipService.saveSlip(slip);

        if (communityId != null) {
            communityManagement.addCommunitySlipToto(slip);
            String communityName = communityManagement.findCommunityById(communityId).getName();
            return "redirect:/gemeinschaft/" + communityName;
        }

        model.addAttribute("predictions", predictions);
        return "Bundesliga2Confirmation";
    }

    @GetMapping("/ergebnisse-bl1")
    public String redirectToToto1Results(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))
            || authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_USER"))) {
            model.addAttribute("matchresults", Bundesliga1Results.getMatchdayResults());
            return "Bundesliga1Results";
        }
        throw new AccessDeniedException("You do not have access to this page.");
    }

    @GetMapping("/ergebnisse-bl2")
    public String redirectToToto2Results(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))
            || authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_USER"))) {
            model.addAttribute("matchresults", Bundesliga2Results.getMatchdayResults());
            return "Bundesliga2Results";
        }
        throw new AccessDeniedException("You do not have access to this page.");
    }
}

