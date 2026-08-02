package bg.softuni.invoiceapplication.scheduler;

import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceDetailsDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.service.InvoiceService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceStatusSchedulerTest {

    @Test
    void markOverdueInvoices_shouldCallInvoiceService() {
        FakeInvoiceService fakeInvoiceService = new FakeInvoiceService();
        InvoiceStatusScheduler invoiceStatusScheduler = new InvoiceStatusScheduler(fakeInvoiceService);

        invoiceStatusScheduler.markOverdueInvoices();

        assertThat(fakeInvoiceService.markOverdueInvoicesCalls()).isEqualTo(1);
        assertThat(fakeInvoiceService.markNoLongerOverdueInvoicesCalls()).isZero();
    }

    @Test
    void markNoLongerOverdueInvoices_shouldCallInvoiceService() {
        FakeInvoiceService fakeInvoiceService = new FakeInvoiceService();
        InvoiceStatusScheduler invoiceStatusScheduler = new InvoiceStatusScheduler(fakeInvoiceService);

        invoiceStatusScheduler.markNoLongerOverdueInvoices();

        assertThat(fakeInvoiceService.markNoLongerOverdueInvoicesCalls()).isEqualTo(1);
        assertThat(fakeInvoiceService.markOverdueInvoicesCalls()).isZero();
    }

    private static class FakeInvoiceService implements InvoiceService {

        private int markOverdueInvoicesCalls;
        private int markNoLongerOverdueInvoicesCalls;

        @Override
        public List<InvoiceShowAllDTO> findAllInvoices() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<InvoiceShowAllDTO> findInvoicesByCompanyName(String companyName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InvoiceDetailsDTO findInvoiceById(UUID invoiceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InvoiceCreateRequestDTO prepareCreateInvoiceForm() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invoice createInvoice(InvoiceCreateRequestDTO invoiceCreateRequestDTO, String performedByUsername) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InvoiceEditRequestDTO getInvoiceForEdit(UUID invoiceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void editInvoice(InvoiceEditRequestDTO invoiceEditRequestDTO, String performedByUsername) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void cancelInvoice(UUID invoiceId, String performedByUsername) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void restoreInvoice(UUID invoiceId, String performedByUsername) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int markOverdueInvoices() {
            markOverdueInvoicesCalls++;
            return 3;
        }

        @Override
        public int markNoLongerOverdueInvoices() {
            markNoLongerOverdueInvoicesCalls++;
            return 2;
        }

        private int markOverdueInvoicesCalls() {
            return markOverdueInvoicesCalls;
        }

        private int markNoLongerOverdueInvoicesCalls() {
            return markNoLongerOverdueInvoicesCalls;
        }
    }
}
