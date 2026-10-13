package kickstart.lotteries.toto;

import kickstart.lotteries.toto.slips.TotoBettingSlipService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TotoResultsController {

    private final TotoBettingSlipService bettingSlipService;

    public TotoResultsController(TotoBettingSlipService bettingSlipService) {
        this.bettingSlipService = bettingSlipService;
    }

    @GetMapping("/totoResults")
    public String showTotoResults(Model model) {
        model.addAttribute("slips", bettingSlipService.getAllSlips());
        return "totoResults";
    }
}

