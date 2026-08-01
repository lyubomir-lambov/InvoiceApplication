package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface InvoiceHistoryIntegrationService {

    InvoiceHistoryResponseDTO createHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO);

    List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId);

    void clearHistoryByInvoiceId(UUID invoiceId);
}
