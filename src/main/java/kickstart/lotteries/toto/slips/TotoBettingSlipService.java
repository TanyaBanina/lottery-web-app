package kickstart.lotteries.toto.slips;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TotoBettingSlipService {

    private final List<TotoBettingSlip> bettingSlips = new ArrayList<>();
    private final TotoBettingSlipRepository totoBettingSlipRepository;

    TotoBettingSlipService(TotoBettingSlipRepository totoBettingSlipRepository) {
        this.totoBettingSlipRepository = totoBettingSlipRepository;
    }

    public void saveSlip(TotoBettingSlip slip) {
        if (!bettingSlips.contains(slip)) {
            bettingSlips.add(slip);
        }
        // Just save to DB, no file writing
        totoBettingSlipRepository.save(slip);
    }

    public List<TotoBettingSlip> getAllSlips() {
        List<TotoBettingSlip> dbSlips = totoBettingSlipRepository.findAll();
        for (TotoBettingSlip s : dbSlips) {
            if (!bettingSlips.contains(s)) {
                bettingSlips.add(s);
            }
        }
        return bettingSlips;
    }

    public List<TotoBettingSlip> findDuePendingSlips() {
        return totoBettingSlipRepository.findByStatusAndDrawDateLessThanEqual(TotoStatus.PENDING, java.time.LocalDateTime.now());
    }

    public void updateSlip(TotoBettingSlip slip) {
        totoBettingSlipRepository.save(slip);
    }

    /**
     * Calculate statistics for Toto bets, aggregated by draw date (day).
     */
    public java.util.Map<java.time.LocalDate, java.util.Map<String, Object>> calculateStatistics() {
        List<TotoBettingSlip> slips = getAllSlips();

        return slips.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                slip -> slip.getDrawDate().toLocalDate()
            )).entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                java.util.Map.Entry::getKey,
                entry -> {
                    java.time.LocalDate date = entry.getKey();
                    List<TotoBettingSlip> dailySlips = entry.getValue();

                    double totalRevenue = dailySlips.stream().mapToDouble(TotoBettingSlip::getBetAmount).sum();
                    double totalPayout = dailySlips.stream().mapToDouble(TotoBettingSlip::getWinningAmount).sum();
                    int slipCount = dailySlips.size();

                    java.util.Map<String, Object> stats = new java.util.HashMap<>();
                    stats.put("revenue", totalRevenue);
                    stats.put("payout", totalPayout);
                    stats.put("slipCount", slipCount);

                    return stats;
                }
            ));
    }
}

