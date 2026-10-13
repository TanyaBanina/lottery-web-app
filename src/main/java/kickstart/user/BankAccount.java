package kickstart.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class BankAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long accountId;

	private Double balance;

	protected BankAccount() {
			// Für JPA
	}

	public BankAccount(Double initialBalance) {
		if (initialBalance < 0) {
			throw new IllegalArgumentException("Startguthaben darf nicht negativ sein.");
		}
		this.balance = initialBalance;
	}

	public Double getBalance() {
			return balance;
		}

	/**
	 * Geld aufladen
	 */
	public void deposit(Double amount) {
			if (amount <= 0) {
				return;
			}
			this.balance += amount;
		}


	/**
	 * Geld wird abgezogen vom Konto
	 */
	public void withdraw(double amount) {
		if (amount <= 0) {
			return;
		}
		if (amount > this.balance) {
			throw new IllegalArgumentException();
		}
		this.balance -= amount;
		}
	}

