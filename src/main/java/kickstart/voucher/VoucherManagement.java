package kickstart.voucher;
import kickstart.user.PaymentType;
import kickstart.user.UserManagement;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Random;

@Service
@Transactional
public class VoucherManagement {

	private final VoucherRepository voucherRepo;
	private final UserManagement userManagement;
	private final Random random = new Random();

	public VoucherManagement(VoucherRepository voucherRepo, UserManagement userManagement) {
		this.voucherRepo = voucherRepo;
		this.userManagement = userManagement;
	}

	/**
	 * erstellt neues Geutschein-Objekt und bucht zeitgleich den Betrag vom Kaufenden user ab
	 * @param amount als Double, Gutscheinwert
	 * @return
	 */
	public Voucher createVoucher(double amount) {
		String voucherCode = generateRandomVoucherCode();
		Voucher voucher = new Voucher(amount, voucherCode, userManagement.getLoggedInUser().getId().getIdentifier());
		userManagement.withdrawFromLoggedInUser(amount, PaymentType.GUTSCHEINKAUF);
		// Gutschein speichern
		Voucher savedVoucher = voucherRepo.save(voucher);
		return savedVoucher;
	}
	/**
	 * Erstellt einen zufälligen Gutscheincode aus Buchstaben und Zahlen.
	 */
	public String generateRandomVoucherCode() {
		int length = 10; // Länge des Gutscheincodes
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		StringBuilder voucherCode = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			int index = random.nextInt(characters.length());
			voucherCode.append(characters.charAt(index));
		}
		return voucherCode.toString();
	}

	/**
	 * Einlösen eines Gutscheins, wenn GutscheinCode vorhanden, danach Löschung
	 * @param voucherCode
	 */
	public void redeemVoucher(String voucherCode) {
		Voucher voucher = voucherRepo.findByVoucherCode(voucherCode)
			.orElseThrow(() -> new NoSuchElementException("Voucher not found for code: " + voucherCode));
		userManagement.depositToLoggedInUser(voucher.getAmount(), PaymentType.GUTSCHRIFT);
		voucherRepo.delete(voucher);
	}
}
