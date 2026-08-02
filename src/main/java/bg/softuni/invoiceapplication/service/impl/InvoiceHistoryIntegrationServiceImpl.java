package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.client.InvoiceHistoryClient;
import bg.softuni.invoiceapplication.exception.ApplicationException;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryResponseDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.snapshot.InvoiceHistorySnapshotDTO;
import bg.softuni.invoiceapplication.service.InvoiceHistoryIntegrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
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
        InvoiceHistoryResponseDTO invoiceHistoryResponseDTO = attachSnapshot(
                invoiceHistoryClient.createHistoryRecord(invoiceHistoryApiKey, invoiceHistoryCreateRequestDTO));
        log.info("Invoice history service create call completed: invoiceId={}, action={}, revision={}",
                invoiceHistoryResponseDTO.getInvoiceId(),
                invoiceHistoryResponseDTO.getAction(),
                invoiceHistoryResponseDTO.getRevisionNumber());
        return invoiceHistoryResponseDTO;
    }

    @Override
    public List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId) {
        List<InvoiceHistoryResponseDTO> invoiceHistory = invoiceHistoryClient.findHistoryByInvoiceId(invoiceHistoryApiKey, invoiceId)
                .stream()
                .map(this::attachSnapshot)
                .toList();
        log.info("Invoice history service get call completed: invoiceId={}, records={}", invoiceId, invoiceHistory.size());
        return invoiceHistory;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void clearHistoryByInvoiceId(UUID invoiceId) {
        invoiceHistoryClient.clearHistoryByInvoiceId(invoiceHistoryApiKey, invoiceId);
        log.info("Invoice history service clear call completed: invoiceId={}", invoiceId);
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
            throw new ApplicationException(
                    "Could not read invoice history snapshot",
                    "invoice_history_snapshot_read_error",
                    "Invoice History Snapshot Error",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }
}
