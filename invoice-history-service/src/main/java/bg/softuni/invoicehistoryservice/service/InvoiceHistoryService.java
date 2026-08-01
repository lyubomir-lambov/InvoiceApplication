package bg.softuni.invoicehistoryservice.service;

import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface InvoiceHistoryService {

    InvoiceHistoryResponseDTO createHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO);

    List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId);
}
