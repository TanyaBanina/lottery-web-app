package kickstart.user;

public enum PaymentType {

	FUSSBALLTOTO,
	LOTTO,
	GEMEINSCHAFTSWETTE,
	EINZAHLUNG,
	AUSZAHLUNG,
	GUTSCHEINKAUF,
	GUTSCHRIFT,
	ERSTEINZAHLUNG;

	@Override
	public String toString() {
		// Konvertiert das Enum in einen String
		return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
	}
}