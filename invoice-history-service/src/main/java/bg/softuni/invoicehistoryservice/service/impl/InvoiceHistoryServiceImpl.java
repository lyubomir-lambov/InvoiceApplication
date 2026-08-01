package bg.softuni.invoicehistoryservice.service.impl;

import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryResponseDTO;
import bg.softuni.invoicehistoryservice.model.entity.InvoiceHistoryRecord;
import bg.softuni.invoicehistoryservice.repository.InvoiceHistoryRepository;
import bg.softuni.invoicehistoryservice.service.InvoiceHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InvoiceHistoryServiceImpl implements InvoiceHistoryService {

    private final InvoiceHistoryRepository invoiceHistoryRepository;

    public InvoiceHistoryServiceImpl(InvoiceHistoryRepository invoiceHistoryRepository) {
        this.invoiceHistoryRepository = invoiceHistoryRepository;
    }

    @Override
    @Transactional
    public InvoiceHistoryResponseDTO createHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
        if (invoiceHistoryCreateRequestDTO == null) {
            throw new IllegalArgumentException("Invoice history create request must not be null");
        }

        InvoiceHistoryRecord invoiceHistoryRecord = fromCreateRequestDTOToInvoiceHistoryRecord(invoiceHistoryCreateRequestDTO);
        invoiceHistoryRecord.setRevisionNumber(getNextRevisionNumber(invoiceHistoryCreateRequestDTO.getInvoiceId()));

        return fromInvoiceHistoryRecordToResponseDTO(invoiceHistoryRepository.save(invoiceHistoryRecord));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("Invoice id must not be null");
        }

        return invoiceHistoryRepository.findByInvoiceIdOrderByRevisionNumberDesc(invoiceId)
                .stream()
                .map(this::fromInvoiceHistoryRecordToResponseDTO)
                .toList();
    }

    private Integer getNextRevisionNumber(UUID invoiceId) {
        return invoiceHistoryRepository.findTopByInvoiceIdOrderByRevisionNumberDesc(invoiceId)
                .map(invoiceHistoryRecord -> invoiceHistoryRecord.getRevisionNumber() + 1)
                .orElse(1);
    }

    private InvoiceHistoryRecord fromCreateRequestDTOToInvoiceHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
        return InvoiceHistoryRecord.builder()
                .invoiceId(invoiceHistoryCreateRequestDTO.getInvoiceId())
                .invoiceNumber(invoiceHistoryCreateRequestDTO.getInvoiceNumber())
                .invoiceType(invoiceHistoryCreateRequestDTO.getInvoiceType())
                .invoiceStatus(invoiceHistoryCreateRequestDTO.getInvoiceStatus())
                .action(invoiceHistoryCreateRequestDTO.getAction())
                .currency(invoiceHistoryCreateRequestDTO.getCurrency())
                .clientDisplayName(invoiceHistoryCreateRequestDTO.getClientDisplayName())
                .clientCompanyName(invoiceHistoryCreateRequestDTO.getClientCompanyName())
                .issueDate(invoiceHistoryCreateRequestDTO.getIssueDate())
                .dueDate(invoiceHistoryCreateRequestDTO.getDueDate())
                .totalWithoutVat(invoiceHistoryCreateRequestDTO.getTotalWithoutVat())
                .totalVat(invoiceHistoryCreateRequestDTO.getTotalVat())
                .totalWithVat(invoiceHistoryCreateRequestDTO.getTotalWithVat())
                .snapshotJson(invoiceHistoryCreateRequestDTO.getSnapshotJson())
                .performedByUsername(invoiceHistoryCreateRequestDTO.getPerformedByUsername())
                .build();
    }

    private InvoiceHistoryResponseDTO fromInvoiceHistoryRecordToResponseDTO(InvoiceHistoryRecord invoiceHistoryRecord) {
        return InvoiceHistoryResponseDTO.builder()
                .id(invoiceHistoryRecord.getId())
                .invoiceId(invoiceHistoryRecord.getInvoiceId())
                .invoiceNumber(invoiceHistoryRecord.getInvoiceNumber())
                .invoiceType(invoiceHistoryRecord.getInvoiceType())
                .invoiceStatus(invoiceHistoryRecord.getInvoiceStatus())
                .revisionNumber(invoiceHistoryRecord.getRevisionNumber())
                .action(invoiceHistoryRecord.getAction())
                .currency(invoiceHistoryRecord.getCurrency())
                .clientDisplayName(invoiceHistoryRecord.getClientDisplayName())
                .clientCompanyName(invoiceHistoryRecord.getClientCompanyName())
                .issueDate(invoiceHistoryRecord.getIssueDate())
                .dueDate(invoiceHistoryRecord.getDueDate())
                .totalWithoutVat(invoiceHistoryRecord.getTotalWithoutVat())
                .totalVat(invoiceHistoryRecord.getTotalVat())
                .totalWithVat(invoiceHistoryRecord.getTotalWithVat())
                .snapshotJson(invoiceHistoryRecord.getSnapshotJson())
                .performedByUsername(invoiceHistoryRecord.getPerformedByUsername())
                .createdOn(invoiceHistoryRecord.getCreatedOn())
                .build();
    }
}
