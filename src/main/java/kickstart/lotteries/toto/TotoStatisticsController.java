package kickstart.lotteries.toto;

import kickstart.lotteries.toto.slips.TotoBettingSlipService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TotoStatisticsController {

    private final TotoBettingSlipService totoBettingSlipService;

    public TotoStatisticsController(TotoBettingSlipService totoBettingSlipService) {
        this.totoBettingSlipService = totoBettingSlipService;
    }

    @GetMapping("/totoStatistik")
    @PreAuthorize("hasRole('ADMIN')")
    public String getTotoStatistics(Model model) {
        var statisticsByDate = totoBettingSlipService.calculateStatistics();
        model.addAttribute("statisticsByDate", statisticsByDate);
        return "totoStatistik";
    }
}

