package kickstart.user;

import java.io.Serializable;
import java.util.*;

import jakarta.persistence.*;
import org.jmolecules.ddd.types.Identifier;
import org.salespointframework.core.AbstractAggregateRoot;
import org.salespointframework.useraccount.UserAccount;

import kickstart.community.Community;
import kickstart.user.User.*;

@Entity
public class User extends AbstractAggregateRoot<UserIdentifier> {

	private @EmbeddedId UserIdentifier id = new UserIdentifier();

	@ManyToMany
	private List<Community> communities = new ArrayList<>();

	@OneToOne
	private UserAccount userAccount;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Payments> payments = new ArrayList<>();

	/**
	 * BankAccount hat OneToOne Beziehung
	 * cascade: Änderungen an der User-Entität werden automatisch auf die verknüpfte BankAccount-Entität übertragen
	 * zb wenn User gelöscht wird, wird auch der Bankaccount gelöscht
	 */
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private BankAccount bankAccount;


	private User() {}

	public User(UserAccount userAccount, BankAccount bankAccount) {
		this.bankAccount = bankAccount;
		this.userAccount = userAccount;
	}

	@Override
	public UserIdentifier getId() {
		return id;
	}


	public UserAccount getUserAccount() {return userAccount; }


	public List<Community> getCommunities() {
		return communities;
	}

	public void setCommunity(List<Community> communities) {
		this.communities = communities;
	}

	public void removeCommunity(Community community) {
		communities.remove(community);
	}
	public BankAccount getBankAccount() {
		return bankAccount;
	}

	public void setBankAccount(BankAccount bankAccount) {
		this.bankAccount = bankAccount;
	}

	/**
	 * gibt Liste an Payments zurück -> über User abrufbar
	 */
	public List<Payments> getPayments() {
		return payments;
	}

	/**
	 * Klasse um den UserIdentifier festzulegen -> getIdentifier()
	 */
	@Embeddable
	public static final class UserIdentifier implements Identifier, Serializable {

		private static final long serialVersionUID = 7740660930809051850L;
		private final UUID identifier;

		public UserIdentifier() {
			this(UUID.randomUUID());
		}

		public UserIdentifier(UUID identifier) {
			this.identifier = identifier;
		}

		@Override
		public int hashCode() {

			final int prime = 31;
			int result = 1;

			result = prime * result + (identifier == null ? 0 : identifier.hashCode());

			return result;
		}

		@Override
		public boolean equals(Object obj) {

			if (obj == this) {
				return true;
			}

			if (!(obj instanceof UserIdentifier that)) {
				return false;
			}
			return this.identifier.equals(that.identifier);
		}

		public UUID getIdentifier() {
			return identifier;
		}



	}
}
