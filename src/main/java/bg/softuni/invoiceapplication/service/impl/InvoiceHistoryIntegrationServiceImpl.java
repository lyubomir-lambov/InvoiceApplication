package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.client.InvoiceHistoryClient;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryResponseDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.snapshot.InvoiceHistorySnapshotDTO;
import bg.softuni.invoiceapplication.service.InvoiceHistoryIntegrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InvoiceHistoryIntegrationServiceImpl implements InvoiceHistoryIntegrationService {

    private final InvoiceHistoryClient invoiceHistoryClient;
    private final ObjectMapper objectMapper;
    private final String invoiceHistoryApiKey;

    public InvoiceHistoryIntegrationServiceImpl(InvoiceHistoryClient invoiceHistoryClient,
                                                ObjectMapper objectMapper,
                                                @Value("${invoice.history.api-key}") String invoiceHistoryApiKey) {
        this.invoiceHistoryClient = invoiceHistoryClient;
        this.objectMapper = objectMapper;
        this.invoiceHistoryApiKey = invoiceHistoryApiKey;
    }

    @Override
    public InvoiceHistoryResponseDTO createHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
        return attachSnapshot(invoiceHistoryClient.createHistoryRecord(invoiceHistoryApiKey, invoiceHistoryCreateRequestDTO));
    }

    @Override
    public List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId) {
        return invoiceHistoryClient.findHistoryByInvoiceId(invoiceHistoryApiKey, invoiceId)
                .stream()
                .map(this::attachSnapshot)
                .toList();
    }

    private InvoiceHistoryResponseDTO attachSnapshot(InvoiceHistoryResponseDTO invoiceHistoryResponseDTO) {
        if (invoiceHistoryResponseDTO == null
                || invoiceHistoryResponseDTO.getSnapshotJson() == null
                || invoiceHistoryResponseDTO.getSnapshotJson().isBlank()) {
            return invoiceHistoryResponseDTO;
        }

        try {
            invoiceHistoryResponseDTO.setSnapshot(
                    objectMapper.readValue(invoiceHistoryResponseDTO.getSnapshotJson(), InvoiceHistorySnapshotDTO.class));
            return invoiceHistoryResponseDTO;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not read invoice history snapshot", e);
        }
    }
}
