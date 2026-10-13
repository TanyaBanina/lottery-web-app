package kickstart.welcome;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.WebRequest;


//Error handeling -> immer wenn ein Whiteable Error auftritt, leitet er auf die error.html

@Controller
public class OurErrorController implements ErrorController {

	private final ErrorAttributes errorAttributes;

	public OurErrorController(ErrorAttributes errorAttributes) {
		this.errorAttributes = errorAttributes;
	}


	@GetMapping("/error")
	public String handleError(WebRequest webRequest, Model model) {
		var errorDetails = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
		model.addAttribute("timestamp", errorDetails.get("timestamp"));
		model.addAttribute("status", errorDetails.get("status"));
		model.addAttribute("error", errorDetails.get("error"));
		model.addAttribute("message", errorDetails.get("message"));
		model.addAttribute("path", errorDetails.get("path"));
		return "error"; // Verweist auf die error.html Template-Datei
	}
}