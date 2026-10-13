package kickstart.user;

import jakarta.persistence.*;


@Entity
public class Payments {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String dateTime;
	private Double amount;

	@Enumerated(EnumType.STRING) // Speichert das Enum als String in der DB
	private PaymentType type;

	@ManyToOne
	private User user;

	private String amountAsString;
	private double balance;

	protected Payments() {
		// Für JPA
	}

	public Payments(String dateTime, Double amount, PaymentType type, User user, double balance) {
		this.dateTime = dateTime;
		this.amount = amount;
		this.type = type;
		this.user = user;
		if (amount > 0) {
			amountAsString = "+" + amount.toString();
		}
		else {
			amountAsString = amount.toString();
		}
		this.balance = balance;
	}

	public Long getId() {
		return id;
	}

	public String getDateTime() {
		return dateTime;
	}

	public Double getAmount() {
		return amount;
	}

	public PaymentType getType() {
		return type;
	}

	public User getUser() {
		return user;
	}

	public String getAmountAsString() {
		return amountAsString;
	}

	public double getBalance() {
		return balance;
	}
}
