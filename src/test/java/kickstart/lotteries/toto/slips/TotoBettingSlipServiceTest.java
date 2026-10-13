package kickstart.lotteries.toto.slips;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TotoBettingSlipServiceTest {

    @Test
    void testServiceInitialization() {
        // Mock the repository since constructor is package-private and requires it
        TotoBettingSlipRepository repo = Mockito.mock(TotoBettingSlipRepository.class);

        // Create the service - should be accessible within the same package
        TotoBettingSlipService service = new TotoBettingSlipService(repo);

        // Just call a method to cover some lines
        assertNotNull(service.getAllSlips(), "getAllSlips() should not return null");
    }
}

