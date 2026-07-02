package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportByCurrencyDTO;
import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportClientDTO;

import java.util.List;
import java.util.UUID;

public interface PaymentReportService {

    List<PaymentReportByCurrencyDTO> getReportsByCurrency(UUID clientId);

    List<PaymentReportClientDTO> getClientReports(UUID clientId);
}
