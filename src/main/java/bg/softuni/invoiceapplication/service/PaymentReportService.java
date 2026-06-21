package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.PaymentReportByCurrencyDTO;

import java.util.List;

public interface PaymentReportService {

    List<PaymentReportByCurrencyDTO> getReportsByCurrency();
}
