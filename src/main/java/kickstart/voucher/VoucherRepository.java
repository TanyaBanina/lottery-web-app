package kickstart.voucher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
	Optional<Voucher> findByVoucherCode(String voucherCode);
	Optional<Voucher> findByReferrer(UUID userId);
}
