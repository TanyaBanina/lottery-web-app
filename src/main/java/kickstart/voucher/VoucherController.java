package kickstart.voucher;

import kickstart.profil.konto.Message;
import kickstart.profil.konto.MessageManagement;
import kickstart.user.User;
import kickstart.user.UserManagement;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/voucher")
public class VoucherController {

	private final UserManagement userManagement;
	private final MessageManagement messageManagement;

	public VoucherController(UserManagement userManagement, MessageManagement messageManagement) {
		this.userManagement = userManagement;
		this.messageManagement = messageManagement;
	}

	@GetMapping("/buy")
	public String showBuyVoucherForm() {
		return "purchaseVoucher"; // View für den Kauf eines Gutscheins
	}

	// Verarbeitet den Kauf eines Gutscheins
	@PostMapping("/buy")
	public String buyVoucher(@RequestParam("amount") double amount, Model model, RedirectAttributes redirectAttributes)
	{
		try {
			Voucher voucher = userManagement.createVoucher(amount);
			model.addAttribute("voucher", voucher);

			User.UserIdentifier userId = userManagement.getLoggedInUserId();
			String username = userManagement.getLoggedInUserName();
			String content = "Erfolgreich Gutschein erstellt. Kosten: " + voucher.getAmount() + "€" +
				". Gutscheincode: " + voucher.getVoucherCode();
			Message message = new Message(content, userId);
			messageManagement.addMessage(message);

			return "boughtVoucher"; // Erfolgreiche Ansicht
		} catch (Exception e) {
			// Fehlerbehandlung: Wenn etwas schief geht, Fehlernachricht hinzufügen
			redirectAttributes.addFlashAttribute("errorMessage",
				"Gutschein konnte nicht erstellt werden: " + e.getMessage());
			return "redirect:/konto";
		}

	}

	@PostMapping("/redeem")
	public String redeemVoucher(@RequestParam("voucherCode") String voucherCode, RedirectAttributes redirectAttributes) {
		try {
			userManagement.redeemVoucher(voucherCode);
			redirectAttributes.addFlashAttribute("successMessage", "Gutschein erfolgreich eingelöst!");
		} catch (NoSuchElementException e) {
			redirectAttributes.addFlashAttribute("errorMessage",
				"Fehler bei der Einlösung des Gutscheins: " + e.getMessage());
		}
		return "redirect:/konto"; // Redirect nach erfolgreicher Einlösung
	}

	// Referral kaufen
	@GetMapping("/referral/create")
	public String createReferral(Model model) {
		Voucher referralVoucher = userManagement.createReferralCode();
		model.addAttribute("referralCode", referralVoucher.getVoucherCode());
		return "buyReferral"; // Zeigt eine neue Seite mit dem Code an
	}
}


