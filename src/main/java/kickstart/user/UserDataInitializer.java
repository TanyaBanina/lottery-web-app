package kickstart.user;

import java.util.List;

import org.salespointframework.core.DataInitializer;
import org.salespointframework.useraccount.Password.UnencryptedPassword;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@Component
@Order(10)
public class UserDataInitializer implements DataInitializer {

	private final UserAccountManagement userAccountManagement;
	private final UserManagement userManagement;

	public UserDataInitializer(UserAccountManagement userAccountManagement, UserManagement userManagement) {

		Assert.notNull(userAccountManagement, "UserAccountManagement darf nicht null sein");
		Assert.notNull(userManagement, "UserManagement darf nicht null sein");

		this.userAccountManagement = userAccountManagement;
		this.userManagement = userManagement;
	}

	/**
	 * TestNutzer erstellen inkl Admin in Datenbank laden
	 */
	@Override
	public void initialize() {

		if (userAccountManagement.findByUsername("admin").isPresent()) {
			return;
		}
		userAccountManagement.create(
			"admin", UnencryptedPassword.of("adminPass"), Role.of("ADMIN"));

		var password = "userPass";
		Double initialBalance = 100.00;

		List.of(
			new RegistrationForm("spielesüchtig", password, initialBalance, null),
			new RegistrationForm("gambler", password, initialBalance, null),
			new RegistrationForm("zocker", password, initialBalance, null),
			new RegistrationForm("e-gal", password, initialBalance, null),
			new RegistrationForm("ellen", password, initialBalance, null),
			new RegistrationForm("1", "1", initialBalance, null), //bitte drin lassen, damit ich gemeinschaft testen kann
			new RegistrationForm("2", "2", initialBalance, null)

		).forEach(userManagement::createUser);
	}
}
