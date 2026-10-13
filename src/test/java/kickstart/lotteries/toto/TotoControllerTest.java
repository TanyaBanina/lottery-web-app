package kickstart.lotteries.toto;

import kickstart.community.Community;
import kickstart.community.CommunityManagement;
import kickstart.lotteries.toto.slips.TotoBettingSlip;
import kickstart.lotteries.toto.slips.TotoBettingSlipService;
import kickstart.profil.konto.MessageManagement;
import kickstart.user.BankAccount;
import kickstart.user.User;
import kickstart.user.User.UserIdentifier;
import kickstart.user.UserManagement;
import kickstart.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TotoControllerTest {

    private TotoController controller;
    private TotoBettingSlipService bettingSlipService;
    private UserManagement userManagement;
    private UserRepository userRepository;
    private CommunityManagement communityManagement;
    private Model model;
    private RedirectAttributes redirectAttributes;
	private MessageManagement messageManagement;

    @BeforeEach
    void setUp() {
        bettingSlipService = Mockito.mock(TotoBettingSlipService.class);
        userManagement = Mockito.mock(UserManagement.class);
        userRepository = Mockito.mock(UserRepository.class);
        communityManagement = Mockito.mock(CommunityManagement.class);
        model = mock(Model.class);
        redirectAttributes = mock(RedirectAttributes.class);
		messageManagement = Mockito.mock(MessageManagement.class);

        controller = new TotoController(bettingSlipService, userManagement, userRepository, communityManagement, messageManagement);
    }

    @Test
    void testRedirectToToto1PageBasedOnRole_Admin() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", Collections.singleton(() -> "ROLE_ADMIN"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        String view = controller.redirectToToto1PageBasedOnRole(model, null);
        assertEquals("/totoStatistik1", view);
    }

    @Test
    void testRedirectToToto1PageBasedOnRole_User() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Collections.singleton(() -> "ROLE_USER"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        String view = controller.redirectToToto1PageBasedOnRole(model, null);
        assertEquals("Bundesliga1Form", view);
    }

    @Test
    void testSubmitToto1_SuccessNonCommunity() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Collections.singleton(() -> "ROLE_USER"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        // Mock user with enough balance
        UserIdentifier uid = new UserIdentifier();
        User user = new User(null, new BankAccount(100.0));
        when(userManagement.getLoggedInUserId()).thenReturn(uid);
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));

        Map<String,String> formParams = new HashMap<>();
        formParams.put("betAmount","10");
        // Add all matches predictions
        for (int i=1; i<=9; i++){
            formParams.put("match-"+i,"1");
        }

        String result = controller.submitToto1(formParams, null, model, redirectAttributes);
        assertEquals("Bundesliga1Confirmation", result);
        verify(bettingSlipService, times(1)).saveSlip(any(TotoBettingSlip.class));
    }

    @Test
    void testSubmitToto1_InsufficientBalance() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Collections.singleton(() -> "ROLE_USER"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        // Mock user with not enough balance
        UserIdentifier uid = new UserIdentifier();
        User user = new User(null, new BankAccount(5.0)); // less than 10 needed
        when(userManagement.getLoggedInUserId()).thenReturn(uid);
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));

        Map<String,String> formParams = new HashMap<>();
        formParams.put("betAmount","10");
        for (int i=1; i<=9; i++){
            formParams.put("match-"+i,"1");
        }

        String result = controller.submitToto1(formParams, null, model, redirectAttributes);
        // Should redirect back to /bundesliga1 due to insufficient funds
        assertTrue(result.contains("redirect:/bundesliga1"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    void testSubmitToto1_NotAllPredictions() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Collections.singleton(() -> "ROLE_USER"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        UserIdentifier uid = new UserIdentifier();
        User user = new User(null, new BankAccount(100.0));
        when(userManagement.getLoggedInUserId()).thenReturn(uid);
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));

        Map<String,String> formParams = new HashMap<>();
        formParams.put("betAmount","10");
        // Missing predictions for one match
        for (int i=1; i<=8; i++){
            formParams.put("match-"+i,"1");
        }

        String result = controller.submitToto1(formParams, null, model, redirectAttributes);
        // Should redirect back due to missing predictions
        assertTrue(result.contains("redirect:/bundesliga1"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    void testSubmitToto2_SuccessCommunity() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Collections.singleton(() -> "ROLE_USER"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        UserIdentifier uid = new UserIdentifier();
        User user = new User(null, new BankAccount(100.0));
        when(userManagement.getLoggedInUserId()).thenReturn(uid);
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));

        Community community = new Community(user, "testComm", "pass");
        when(communityManagement.findCommunityById(1L)).thenReturn(community);

        Map<String,String> formParams = new HashMap<>();
        formParams.put("betAmount","10");
        for (int i=1; i<=9; i++){
            formParams.put("match-"+i,"1");
        }

        String result = controller.submitToto2(formParams, 1L, model, redirectAttributes);
        assertTrue(result.contains("redirect:/gemeinschaft/testComm"));
        verify(communityManagement, times(1)).addCommunitySlipToto(any(TotoBettingSlip.class));
    }

    @Test
    void testSubmitToto2_NotEnoughBalance() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Collections.singleton(() -> "ROLE_USER"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        UserIdentifier uid = new UserIdentifier();
        User user = new User(null, new BankAccount(1.0)); 
        when(userManagement.getLoggedInUserId()).thenReturn(uid);
        when(userRepository.findById(uid)).thenReturn(Optional.of(user));

        Map<String,String> formParams = new HashMap<>();
        formParams.put("betAmount","10");
        for (int i=1; i<=9; i++){
            formParams.put("match-"+i,"1");
        }

        String result = controller.submitToto2(formParams, null, model, redirectAttributes);
        assertTrue(result.contains("redirect:/bundesliga2"));
        verify(redirectAttributes, times(1)).addFlashAttribute(eq("error"), anyString());
    }

    @Test
    void testRedirectToToto1Results_User() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Collections.singleton(() -> "ROLE_USER"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        String view = controller.redirectToToto1Results(model);
        assertEquals("Bundesliga1Results", view);
    }

    @Test
    void testRedirectToToto1Results_Admin() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", Collections.singleton(() -> "ROLE_ADMIN"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        String view = controller.redirectToToto1Results(model);
        assertEquals("Bundesliga1Results", view);
    }

    @Test
    void testRedirectToToto2Results_User() {
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Collections.singleton(() -> "ROLE_USER"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        String view = controller.redirectToToto2Results(model);
        assertEquals("Bundesliga2Results", view);
    }

    @Test
    void testRedirectToToto2Results_Admin() {
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", Collections.singleton(() -> "ROLE_ADMIN"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        String view = controller.redirectToToto2Results(model);
        assertEquals("Bundesliga2Results", view);
    }
}

