package kickstart.user;

import kickstart.voucher.*;
import org.salespointframework.useraccount.Password.UnencryptedPassword;
import org.salespointframework.useraccount.*;
import org.springframework.data.util.Streamable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import kickstart.community.Community;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class UserManagement {

	public static final Role USER_ROLE = Role.of("USER");

	private final UserRepository users;
	private final UserAccountManagement userAccounts;
	private final PaymentsRepository transactions;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	private final VoucherRepository voucherRepo;
	private final Random random = new Random();


	public UserRepository getUsers() {
		return users;
	}

	public UserManagement(UserRepository users, UserAccountManagement userAccounts,
						  PaymentsRepository transactions, VoucherRepository voucherRepo) {

		Assert.notNull(users, "UserRepository darf nicht null sein");
		Assert.notNull(userAccounts, "UserAccountManagement darf nicht null sein");

		this.users = users;
		this.userAccounts = userAccounts;
		this.transactions = transactions;
		this.voucherRepo = voucherRepo;
	}

	/**
	 *erstellt User inkl BankAccount und UserAccount
	 */
	public User createUser(RegistrationForm form) {
		Assert.notNull(form, "Registration form darf nicht null sein");

		var password = UnencryptedPassword.of(form.getPassword());
		var userAccount = userAccounts.create(form.getName(), password, USER_ROLE);
		var bankAccount = new BankAccount(form.getInitialBalance());
		var referralCode = form.getReferralCode();

		User user = new User(userAccount, bankAccount);
		users.save(user);
		createInitialPayment(form, user);

		// Gutschriften für den Empfehlungs-Code verarbeiten
		if (referralCode != null && !referralCode.isEmpty()) {
			redeemReferralCode(referralCode, user);
		}
		return user;
	}

	private void createInitialPayment(RegistrationForm form, User user) {
		String formattedDate = LocalDateTime.now().format(formatter);
		double balance = form.getInitialBalance();
		Payments transaction = new Payments(
			formattedDate, balance, PaymentType.ERSTEINZAHLUNG, user, balance
		);
		// Zahlungsobjekt speichern
		transactions.save(transaction);
	}

	public User getUserByName(String username) {
		users.findByUserAccount_Username(username);
		Optional<User> userOptional = users.findByUserAccount_Username(username);
		if (userOptional.isPresent()) {
			return userOptional.get();
		} else {
			throw new IllegalArgumentException("Kein Benutzer mit dem Benutzernamen " + username+
				" gefunden.");
		}
	}

	/**
	 * Methode, um den aktuell authentifizierten Benutzer abzurufen
	 * -> returned User
	 */
	public User getLoggedInUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		//aktuellen Benutzer ausgeben
		if (authentication != null && authentication.isAuthenticated()) {
			String username = authentication.getName();
			// Benutzer anhand des Benutzernamens aus der Datenbank suchen
			Optional<User> userOptional = users.findByUserAccount_Username(username);
			//gefunden
			if (userOptional.isPresent()) {
				return userOptional.get();
			//nicht gefunden
			} else {
				throw new IllegalArgumentException("Kein Benutzer mit dem Benutzernamen " +
					username+ " gefunden.");
			}
		}
		throw new IllegalStateException("Kein gültiger Benutzer ist eingeloggt.");
	}

	/**
	 *Methode zum Ändern des Benutzernamens eines bestimmten Nutzers
	 */
	public void changeUsername(UUID userId, String newUsername) {
		// Stelle sicher, dass der Benutzer existiert
		User user = users.findById(new User.UserIdentifier(userId))
			.orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));
		// Überprüfe, ob der neue Benutzername bereits vergeben ist
		if (users.findByUserAccount_Username(newUsername).isPresent()) {
			throw new IllegalArgumentException("Der Benutzername ist bereits vergeben.");
		}
		// Setze den neuen Benutzernamen
		user.getUserAccount().setUsername(newUsername);
		users.save(user); // Speichert die Änderungen
	}

	/**
	 *gibt User zurück anhand der UUID
	 */
	public Optional<User> findById(UUID userId) {
		return users.findById(new User.UserIdentifier(userId));
	}

	/**
	 * Methode, um auf den Usernamen des aktuell eingelogten Users zu kommen
	 */
	public String getLoggedInUserName() {
		return getLoggedInUser().getUserAccount().getUsername();
	}

	/**
	 * // Methode um auf die ID des aktuellen Users zu kommen
	 */
	public User.UserIdentifier getLoggedInUserId() {
		return getLoggedInUser().getId();
	}

	public void addCommunityToLoggedInUser(Community community) {
		User user = getLoggedInUser();
		List<Community> communities = user.getCommunities();
		communities.add(community);
		user.setCommunity(communities);
		users.save(user);
	}

	public void removeCommunityFromLoggedInUser(Community community) {
		// spaeter noch checks for null etc
		User user = getLoggedInUser();
		user.removeCommunity(community);
		users.save(user);
	}

	/**
	 * aufrufen, wenn der aktuell eingelogte User etwas einzahlen möchte
	 */
	public void depositToLoggedInUser(Double amount, PaymentType paymentType) {
		User user = getLoggedInUser();
		BankAccount account = user.getBankAccount();
		account.deposit(amount);
		// Transaktion speichern
		String formattedDate = LocalDateTime.now().format(formatter);
		double balance = account.getBalance();
		Payments transaction = new Payments(formattedDate, amount, paymentType, user, balance);
		transactions.save(transaction);
		users.save(user); // Änderungen speichern
	}

	/**
	 * aufrufen, wenn etwas abgebucht werden soll beim eingeloggten User
	 */
	public boolean withdrawFromLoggedInUser(Double amount, PaymentType paymentType) {
		User user = getLoggedInUser();
		BankAccount account = user.getBankAccount();
		if (amount > account.getBalance()) return false; //if too big, just return and dont save the transaction

		account.withdraw(amount);
		// Transaktion speichern
		String formattedDate = LocalDateTime.now().format(formatter);
		double balance = account.getBalance();

		Payments transaction = new Payments(formattedDate, -amount, paymentType, user, balance);
		transactions.save(transaction);
		users.save(user); // Änderungen speichern
		return true;
	}

	/**
	 * Liste von Payments -> Historie
	 */
	public List<Payments> getTransactionsForLoggedInUser() {
		User loggedInUser = getLoggedInUser();
		return transactions.findByUser_Id(loggedInUser.getId());
	}

	/**
	 * deposit anahnd von UUID statt loggedIn
	 * @param userId
	 * @param amount
	 */
	public void depositToUser(UUID userId, Double amount, PaymentType paymentType) {
		//User anhand der ID abrufen
		User user = users.findById(new User.UserIdentifier(userId))
			.orElseThrow(() -> new IllegalArgumentException("User mit ID " + userId + " nicht gefunden"));
		//deposit
		user.getBankAccount().deposit(amount);
		//Payment

		String formattedDate = LocalDateTime.now().format(formatter);
		double balance = user.getBankAccount().getBalance();
		Payments transaction = new Payments(formattedDate, amount, paymentType, user, balance);
		//speichern
		transactions.save(transaction);
	}
	/**
	 * withdraw anahnd von UUID statt loggedIn
	 * @param userId als UUID
	 * @param amount als Double xx.xx
	 */
	public void withdrawToUser(UUID userId, Double amount, PaymentType paymentType) {
		//User anhand der ID abrufen
		User user = users.findById(new User.UserIdentifier(userId))
			.orElseThrow(() -> new IllegalArgumentException("User mit ID " + userId + " nicht gefunden"));
		//deposit
		user.getBankAccount().withdraw(amount);
		//Payment

		String formattedDate = LocalDateTime.now().format(formatter);
		double balance = user.getBankAccount().getBalance();
		Payments transaction = new Payments(formattedDate, -amount, paymentType, user, balance);
		//speichern
		transactions.save(transaction);
	}

	/**
	 *Löschen eines Users
	 * @param userId als UUID
	 */
	public void deleteUser(UUID userId){
		User user = users.findById(new User.UserIdentifier(userId))
			.orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));
		users.delete(user);

	}

	//GutscheinZeug

	/**
	 * erstellt neues Geutschein-Objekt und bucht zeitgleich den Betrag vom Kaufenden user ab
	 * @param amount als Double, Gutscheinwert
	 * @return
	 */
	public Voucher createVoucher(double amount) throws Exception{
		String voucherCode = generateRandomVoucherCode();
		if (withdrawFromLoggedInUser(amount, PaymentType.GUTSCHEINKAUF)) {
			// Gutschein erstellen
			Voucher voucher = new Voucher(amount, voucherCode, getLoggedInUser().getId().getIdentifier());

			// Gutschein speichern
			return voucherRepo.save(voucher);
		} else {
			// Fehler werfen, wenn nicht genug Guthaben vorhanden ist
			throw new Exception("Nicht genug Guthaben");
		}
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
		depositToLoggedInUser(voucher.getAmount(), PaymentType.GUTSCHRIFT);
		voucherRepo.delete(voucher);
	}

	//ReferralZeug
	/**
	 * erstellt referralCode, der weitergegeben werden kann
	 * @return
	 */
	public Voucher createReferralCode() {

		Optional<Voucher> existingVoucher = voucherRepo.findByReferrer(getLoggedInUser().getId().getIdentifier());

		if (existingVoucher.isPresent()) {
			// Wenn ein bestehender Code existiert, diesen zurückgeben
			return existingVoucher.get();
		}
		String referralCode = generateRandomVoucherCode();
		Voucher voucher = new Voucher(10.0, referralCode, getLoggedInUser().getId().getIdentifier());
		return voucherRepo.save(voucher);
	}

	/**
	 * einlösen der Codes beim registrieren
	 * @param referralCode
	 * @param user
	 */
	public void redeemReferralCode(String referralCode, User user) {
		Voucher voucher = voucherRepo.findByVoucherCode(referralCode)
			.orElseThrow(() -> new NoSuchElementException("Referral code not found: " + referralCode));
		UUID referrer = voucher.getReferrer();
		if (referrer == null) {
			throw new IllegalArgumentException("Invalid referral code.");
		}
		// Gutschriften für den Empfehlenden und den neuen Benutzer
		depositToUser(referrer, 10.0, PaymentType.GUTSCHRIFT);
		depositToUser(user.getId().getIdentifier(), voucher.getAmount(), PaymentType.GUTSCHRIFT);
	}

	public Streamable<User> findAll() {
		return users.findAll();
	}
}

