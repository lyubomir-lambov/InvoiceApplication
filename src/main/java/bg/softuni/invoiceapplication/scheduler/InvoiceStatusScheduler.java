package bg.softuni.invoiceapplication.scheduler;

import bg.softuni.invoiceapplication.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceStatusScheduler {

    private final InvoiceService invoiceService;

    @Scheduled(cron = "0 5 0 * * *")
    public void markOverdueInvoices() {
        int updatedInvoices = invoiceService.markOverdueInvoices();
        log.info("Marked {} invoices as overdue", updatedInvoices);
    }
}
