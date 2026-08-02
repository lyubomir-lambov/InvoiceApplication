package bg.softuni.invoiceapplication;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceApplicationTests {

    @Test
    void applicationClassShouldBeAvailable() {
        assertThat(InvoiceApplication.class).isNotNull();
    }

}
