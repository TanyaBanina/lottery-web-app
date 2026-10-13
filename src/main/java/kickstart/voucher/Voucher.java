package kickstart.voucher;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Voucher {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long voucherId;

	private Double amount;
	private String voucherCode;
	private UUID referrer;

	public Voucher(Double amount, String voucherCode, UUID referrer) {
		this.amount = amount;
		this.voucherCode = voucherCode;
		this.referrer = referrer;
	}

	public Voucher() {
	}

	public Double getAmount() {
		return amount;
	}

	public String getVoucherCode() {
		return voucherCode;
	}
	public UUID getReferrer() {
		return referrer;
	}
}
