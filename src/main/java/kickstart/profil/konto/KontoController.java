package kickstart.profil.konto;

import java.util.Collections;
import java.util.List;

import kickstart.user.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import kickstart.lotteries.lotto.slips.LottoBettingSlipRepository;
import kickstart.user.User.UserIdentifier;

@Controller
public class KontoController {

	private final UserManagement userManagement;
	private final LottoBettingSlipRepository lottoBettingSlipRepository;
	private final PaymentsRepository paymentsRepository;
	private final MessageManagement messageManagement;

	public KontoController(UserManagement userManagement, LottoBettingSlipRepository lottoBettingSlipRepository, PaymentsRepository paymentsRepository, MessageManagement messageManagement) {
		this.userManagement = userManagement;
		this.lottoBettingSlipRepository = lottoBettingSlipRepository;
		this.paymentsRepository = paymentsRepository;
		this.messageManagement = messageManagement;
	}

	/**
	 * returns notifications.html with the Messages from the System to the user
	 * @param model
	 * @return
	 */
	@GetMapping("/notifications")
	@PreAuthorize("hasRole('USER')")
	public String notifications(Model model) {
		User.UserIdentifier userId = userManagement.getLoggedInUserId();
		List<Message> messages = messageManagement.getMessagesByUserId(userId);
		model.addAttribute("messages", messages.reversed());



		return "notifications";
	}

	/**
	 *
	 * @param model
	 * @return
	 */
	@GetMapping("/konto")
	@PreAuthorize("hasRole('USER')")
	public String getKonto(Model model) {

		String userName = userManagement.getLoggedInUserName();
		UserIdentifier userid = userManagement.getLoggedInUserId();
		List<Payments> payments = userManagement.getTransactionsForLoggedInUser();
		Collections.reverse(payments);

		TransferMoneyForm einzahlen = new TransferMoneyForm();
		einzahlen.setTransferMethod("einzahlen");
		TransferMoneyForm auszahlen = new TransferMoneyForm();
		auszahlen.setTransferMethod("auszahlen");

		Double balance = userManagement.getLoggedInUser().getBankAccount().getBalance();

		model.addAttribute("transferMoneyFormEinzahlen", einzahlen);
		model.addAttribute("transferMoneyFormAuszahlen", auszahlen);
		model.addAttribute("balance", balance);
		model.addAttribute("payments", payments);
		model.addAttribute("username", userName);
		return "konto";
	}

	/**
	 * adds or subtracts money from user based on transferMoneyForm.transferMethod
	 * @param transferMoneyForm
	 * @param model
	 * @return
	 */
	@PostMapping("/konto/transfer")
	@PreAuthorize("hasRole('USER')")
	public String transferMoney(TransferMoneyForm transferMoneyForm, Model model) {

		String transferMethod = transferMoneyForm.getTransferMethod();
		String amountAsString = transferMoneyForm.getAmount();
		double amount;

		try {
			amount = Double.parseDouble(amountAsString);
			if (amount >= 5000) return "redirect:/konto";

		} catch (NumberFormatException e) {
			return "redirect:/konto";
		}

		switch (transferMethod) {
			case "einzahlen":
			
				//check if amount is possible to pay form "card"

				userManagement.depositToLoggedInUser(amount, PaymentType.EINZAHLUNG);

				break;
		
			case "auszahlen":
				userManagement.withdrawFromLoggedInUser(amount, PaymentType.AUSZAHLUNG);
				break;
			default:
				break;
		}
		return "redirect:/konto";
	}

}


