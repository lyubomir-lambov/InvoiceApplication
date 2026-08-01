package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.client.InvoiceHistoryClient;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryResponseDTO;
import bg.softuni.invoiceapplication.service.InvoiceHistoryIntegrationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InvoiceHistoryIntegrationServiceImpl implements InvoiceHistoryIntegrationService {

    private final InvoiceHistoryClient invoiceHistoryClient;
    private final String invoiceHistoryApiKey;

    public InvoiceHistoryIntegrationServiceImpl(InvoiceHistoryClient invoiceHistoryClient,
                                                @Value("${invoice.history.api-key}") String invoiceHistoryApiKey) {
        this.invoiceHistoryClient = invoiceHistoryClient;
        this.invoiceHistoryApiKey = invoiceHistoryApiKey;
    }

    @Override
    public InvoiceHistoryResponseDTO createHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
        return invoiceHistoryClient.createHistoryRecord(invoiceHistoryApiKey, invoiceHistoryCreateRequestDTO);
    }

    @Override
    public List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId) {
        return invoiceHistoryClient.findHistoryByInvoiceId(invoiceHistoryApiKey, invoiceId);
    }
}
