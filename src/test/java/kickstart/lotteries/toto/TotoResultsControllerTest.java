package kickstart.lotteries.toto;

import kickstart.lotteries.toto.slips.TotoBettingSlip;
import kickstart.lotteries.toto.slips.TotoBettingSlipService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TotoResultsControllerTest {

    @Test
    void testShowTotoResults() {
        TotoBettingSlipService service = Mockito.mock(TotoBettingSlipService.class);
        TotoResultsController controller = new TotoResultsController(service);

        Model model = Mockito.mock(Model.class);
        when(service.getAllSlips()).thenReturn(List.of(
                new TotoBettingSlip(LocalDateTime.now(),10.0, Map.of("M1","1"),null,null)
        ));

        String view = controller.showTotoResults(model);
        assertEquals("totoResults", view);

        verify(model,times(1)).addAttribute(eq("slips"), anyList());
    }
}

