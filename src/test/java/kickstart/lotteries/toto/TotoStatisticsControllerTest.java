package kickstart.lotteries.toto;

import kickstart.lotteries.toto.slips.TotoBettingSlipService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TotoStatisticsControllerTest {

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetTotoStatistics() {
        TotoBettingSlipService service = Mockito.mock(TotoBettingSlipService.class);
        TotoStatisticsController controller = new TotoStatisticsController(service);

        Model model = Mockito.mock(Model.class);

        Map<LocalDate, Map<String,Object>> stats = new HashMap<>();
        stats.put(LocalDate.now(), Map.of("revenue", 100.0,"payout",50.0,"slipCount",2));
        when(service.calculateStatistics()).thenReturn(stats);

        String view = controller.getTotoStatistics(model);
        assertEquals("totoStatistik", view);
        verify(model, times(1)).addAttribute("statisticsByDate", stats);
    }
}

