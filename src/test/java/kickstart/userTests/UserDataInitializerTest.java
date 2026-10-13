package kickstart.userTests;
import kickstart.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.salespointframework.useraccount.*;

import static org.mockito.Mockito.*;

import java.util.*;

class UserDataInitializerTest {

	@Mock
	private UserAccountManagement userAccountManagement;

	@Mock
	private UserManagement userManagement;

	private UserDataInitializer userDataInitializer;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		userDataInitializer = new UserDataInitializer(userAccountManagement, userManagement);
	}


	@Test
	void testInitializeAdminExists() {
		// mock Verhalten
		UserAccount mockAdminAccount = mock(UserAccount.class);
		when(userAccountManagement.findByUsername("admin")).thenReturn(Optional.of(mockAdminAccount));

		userDataInitializer.initialize();

		verify(userAccountManagement, never()).create(anyString(), any(Password.UnencryptedPassword.class), any(Role.class));
		verify(userManagement, never()).createUser(any(RegistrationForm.class));
	}

	@Test
	void testInitializeAdminCreated() {
		when(userAccountManagement.findByUsername("admin")).thenReturn(Optional.empty());

		userDataInitializer.initialize();

		//tests
		verify(userAccountManagement, times(1))
			.create(eq("admin"), eq(Password.UnencryptedPassword.of("adminPass")), eq(Role.of("ADMIN")));
	}
	@Test
	void testInitializeUsersCreated() {
		when(userAccountManagement.findByUsername("admin")).thenReturn(Optional.empty());

		userDataInitializer.initialize();

		//tests
		verify(userManagement, times(7)).createUser(any(RegistrationForm.class));
		verify(userManagement).createUser(argThat(form ->
			form.getName().equals("spielesüchtig") && form.getPassword().equals("userPass")));
		verify(userManagement).createUser(argThat(form ->
			form.getName().equals("1") && form.getPassword().equals("1")));
	}

}