package kickstart.user;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class RegistrationForm {
	private final @NotEmpty String  password;
	private @NotEmpty String  name;
	private @NotNull Double initialBalance;
	private String referralCode;

	public RegistrationForm(String name, String password, Double initialBalance, String referralCode) {
		//keine Exception, da sonst die html mit dem Form nicht geöffnet werden kann, Exceptions werden in html abgefangen
		this.name = name;
		this.password = password;
		this.initialBalance = initialBalance;
		this.referralCode = referralCode;
	}


	//getter Methoden:
	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public Double getInitialBalance() {
		return initialBalance;
	}

	public String getReferralCode() {return referralCode;}

	/**
	 * setUsername für ändern des Namens durch Admin
	 * @param username als String
	 */
	public void setUsername(String username) {
		this.name = username;
	}
}
