package bg.softuni.invoicehistoryservice.service.impl;

import bg.softuni.invoicehistoryservice.exception.InvalidInvoiceHistoryRequestException;
import bg.softuni.invoicehistoryservice.mapper.InvoiceHistoryMapper;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryResponseDTO;
import bg.softuni.invoicehistoryservice.model.entity.InvoiceHistoryRecord;
import bg.softuni.invoicehistoryservice.repository.InvoiceHistoryRepository;
import bg.softuni.invoicehistoryservice.service.InvoiceHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceHistoryServiceImpl implements InvoiceHistoryService {

    private final InvoiceHistoryRepository invoiceHistoryRepository;
    private final InvoiceHistoryMapper invoiceHistoryMapper;

    @Override
    @Transactional
    public InvoiceHistoryResponseDTO createHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
        if (invoiceHistoryCreateRequestDTO == null) {
            throw new InvalidInvoiceHistoryRequestException("Invoice history create request must not be null");
        }

        InvoiceHistoryRecord invoiceHistoryRecord = invoiceHistoryMapper.fromCreateRequestDTOToInvoiceHistoryRecord(invoiceHistoryCreateRequestDTO);
        invoiceHistoryRecord.setRevisionNumber(getNextRevisionNumber(invoiceHistoryCreateRequestDTO.getInvoiceId()));

        InvoiceHistoryRecord savedInvoiceHistoryRecord = invoiceHistoryRepository.save(invoiceHistoryRecord);
        log.info("Invoice history record created for invoiceId={}, action={}, revision={}",
                savedInvoiceHistoryRecord.getInvoiceId(),
                savedInvoiceHistoryRecord.getAction(),
                savedInvoiceHistoryRecord.getRevisionNumber());

        return invoiceHistoryMapper.fromInvoiceHistoryRecordToResponseDTO(savedInvoiceHistoryRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId) {
        if (invoiceId == null) {
            throw new InvalidInvoiceHistoryRequestException("Invoice id must not be null");
        }

        List<InvoiceHistoryResponseDTO> invoiceHistory = invoiceHistoryRepository.findByInvoiceIdOrderByRevisionNumberDesc(invoiceId)
                .stream()
                .map(invoiceHistoryMapper::fromInvoiceHistoryRecordToResponseDTO)
                .toList();

        log.info("Invoice history requested for invoiceId={}, records={}", invoiceId, invoiceHistory.size());
        return invoiceHistory;
    }

    @Override
    @Transactional
    public void clearHistoryByInvoiceId(UUID invoiceId) {
        if (invoiceId == null) {
            throw new InvalidInvoiceHistoryRequestException("Invoice id must not be null");
        }

        invoiceHistoryRepository.deleteByInvoiceId(invoiceId);
        log.info("Invoice history cleared for invoiceId={}", invoiceId);
    }

    private Integer getNextRevisionNumber(UUID invoiceId) {
        return invoiceHistoryRepository.findTopByInvoiceIdOrderByRevisionNumberDesc(invoiceId)
                .map(invoiceHistoryRecord -> invoiceHistoryRecord.getRevisionNumber() + 1)
                .orElse(1);
    }

}
