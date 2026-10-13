package kickstart.lotteries.lotto;

import kickstart.lotteries.lotto.results.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

@Controller
public class ResultsController {
	private final Lotto lotto;

	@Autowired
	public ResultsController(Lotto lotto) {
		this.lotto = lotto;
	}

	@PostMapping("/performDraw")
	public String performDrawButton(RedirectAttributes redirectAttributes){
		lotto.performDraw();
		redirectAttributes.addFlashAttribute("drawMessage", "Die Ziehung wurde erfolgreich durchgeführt!" );
		return "redirect:/6aus49Statistik";
	}

	@GetMapping("/aktuelleZahlen")
	public String showCurrentNumbers(
		@RequestParam(value = "selectedDate", required = false) String selectedDate,
		Model model
	) {
		if (selectedDate == null) {
			selectedDate = lotto.getMostRecentDrawDate();
		}

		Result result = lotto.getResultByDate(selectedDate);

		model.addAttribute("selectedDate", selectedDate);
		model.addAttribute("numbers", result != null ? result.getWinningNumbers() : Collections.emptyList());
		model.addAttribute("superNumber", result != null ? result.getSuperNumber() : null);
		model.addAttribute("dates", lotto.getFormattedPastDrawDates());
		model.addAttribute("drawDate", lotto.getFormattedNextDrawDate());

		return "numbers";
	}


	@GetMapping("/6aus49Statistik")
	@PreAuthorize("hasRole('ADMIN')")
	public String getStatistics(Model model) {
		Map<String, Map<String, Object>> statisticsByDate = lotto.calculateStatistics();
		model.addAttribute("statisticsByDate", statisticsByDate);
		return "6aus49Statistik";
	}

	@GetMapping("/quoten")
	public String showQuotenPage() {
		return "quoten";
	}

}
