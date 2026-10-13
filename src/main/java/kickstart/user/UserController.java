package kickstart.user;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
public class UserController {

	private final UserManagement userManagement;

	public UserController(UserManagement userManagement) {

		Assert.notNull(userManagement, "UserManagement darf nicht null sein");

		this.userManagement = userManagement;
	}

	@PostMapping("/register")
	public String registerNew(@Valid RegistrationForm form, Errors result, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			return "register";
		}
		try {
			// Falls alles in Ordnung ist legen wir einen Customer an
			userManagement.createUser(form);
			redirectAttributes.addFlashAttribute("successMessage", "Benutzer erfolgreich registriert");
		} catch (Exception e) {
			// Fehlerbehandlung: Wenn etwas schief geht, Fehlernachricht hinzufügen
			redirectAttributes.addFlashAttribute("errorMessage", "Benutzer konnte nicht registriert werden: " + e.getMessage());
		}

		return "redirect:/";
	}

	@GetMapping("/register")
	public String register(Model model, RegistrationForm form) {
		return "register";
	}

	/**
	 * Admin Profil Seite inkl userListe werden angezeigt
	 * @param model
	 * @return
	 */
	@GetMapping("/profilAdmin")
	@PreAuthorize("hasRole('ADMIN')")
	public String users(Model model) {
		model.addAttribute("userList", userManagement.findAll());
		return "profilAdmin";
	}

	/**
	 * Admin kann User löschen
	 * @param userId
	 * @param redirectAttributes
	 * @return
	 */
	@PostMapping("/profilAdmin/delete/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public String removeUser(@PathVariable UUID userId, RedirectAttributes redirectAttributes) {

		try {
			// Benutzer löschen
			userManagement.deleteUser(userId);
			redirectAttributes.addFlashAttribute("successMessage", "Benutzer erfolgreich gelöscht!");
		} catch (Exception e) {
			// Fehlerbehandlung: Wenn etwas schief geht, Fehlernachricht hinzufügen
			redirectAttributes.addFlashAttribute("errorMessage", "Fehler beim Löschen des Benutzers: " + e.getMessage());
		}

		// Weiterleitung zur Profil-Admin-Seite
		return "redirect:/profilAdmin";
	}

	/**
	 * Admin-html, um die Usernamen zu ändern
	 */
	@GetMapping("/profilAdmin/edit/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public String showEditUsernameForm(@PathVariable UUID userId, Model model) {
		User user = userManagement.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("Benutzer nicht gefunden"));
		// Stelle sicher, dass das form-Objekt mit einem `username`-Feld vorhanden ist.
		RegistrationForm form = new RegistrationForm(user.getUserAccount().getUsername(), "", 10.0,
			null);
		form.setUsername(user.getUserAccount().getUsername());  // Username des Nutzers setzen
		model.addAttribute("user", user);
		model.addAttribute("form", form);

		return "editUsername";
	}


	/**
	 * Verarbeitet das Formular zum Bearbeiten des Benutzernamens
	 * @param userId
	 * @param name
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@PostMapping("/profilAdmin/edit/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public String changeUsername(@PathVariable UUID userId,
								 @RequestParam String name,
								 Model model,
								 RedirectAttributes redirectAttributes) {
		try {
			userManagement.changeUsername(userId, name); // Benutzername ändern
			redirectAttributes.addFlashAttribute(
				"successMessage", "Benutzername erfolgreich geändert!"
			);
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/profilAdmin"; // Zurück zur Admin-Profilseite
	}
}


