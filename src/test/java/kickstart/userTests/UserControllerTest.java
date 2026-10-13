package kickstart.userTests;
import kickstart.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

	private UserController userController;

	@Mock
	private UserManagement userManagement;

	@Mock
	private Model model;

	@Mock
	private RedirectAttributes redirectAttributes;

	@Mock
	private Errors errors;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		userController = new UserController(userManagement);
	}

	@Test
	void testRegisterNew() {
		RegistrationForm form = new RegistrationForm("testuser", "password", 10.0, null);

		when(errors.hasErrors()).thenReturn(false);

		String result = userController.registerNew(form, errors, redirectAttributes);

		verify(userManagement, times(1)).createUser(form);
		verify(redirectAttributes).addFlashAttribute("successMessage", "Benutzer erfolgreich registriert");
		assertEquals("redirect:/", result);
	}

	@Test
	void testRemoveUser_Success() {
		UUID userId = UUID.randomUUID();

		String result = userController.removeUser(userId, redirectAttributes);

		verify(userManagement, times(1)).deleteUser(userId);
		verify(redirectAttributes).addFlashAttribute("successMessage", "Benutzer erfolgreich gelöscht!");
		assertEquals("redirect:/profilAdmin", result);
	}

	@Test
	void testShowEditUsernameForm() {
		UUID userId = UUID.randomUUID();
		User user = mock(User.class);
		UserAccount userAccount = mock(UserAccount.class);

		when(user.getUserAccount()).thenReturn(userAccount);
		when(userAccount.getUsername()).thenReturn("testuser");
		when(userManagement.findById(userId)).thenReturn(Optional.of(user));

		String result = userController.showEditUsernameForm(userId, model);

		verify(model).addAttribute(eq("user"), eq(user));
		verify(model).addAttribute(eq("form"), any(RegistrationForm.class));
		assertEquals("editUsername", result);
	}

	@Test
	void testChangeUsername_Success() {
		UUID userId = UUID.randomUUID();
		String newName = "newusername";

		String result = userController.changeUsername(userId, newName, model, redirectAttributes);

		verify(userManagement, times(1)).changeUsername(userId, newName);
		verify(redirectAttributes).addFlashAttribute("successMessage", "Benutzername erfolgreich geändert!");
		assertEquals("redirect:/profilAdmin", result);
	}
}
