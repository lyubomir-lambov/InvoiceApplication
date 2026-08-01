package bg.softuni.invoicehistoryservice.service.impl;

import bg.softuni.invoicehistoryservice.mapper.InvoiceHistoryMapper;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryResponseDTO;
import bg.softuni.invoicehistoryservice.model.entity.InvoiceHistoryRecord;
import bg.softuni.invoicehistoryservice.repository.InvoiceHistoryRepository;
import bg.softuni.invoicehistoryservice.service.InvoiceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceHistoryServiceImpl implements InvoiceHistoryService {

    private final InvoiceHistoryRepository invoiceHistoryRepository;
    private final InvoiceHistoryMapper invoiceHistoryMapper;

    @Override
    @Transactional
    public InvoiceHistoryResponseDTO createHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
        if (invoiceHistoryCreateRequestDTO == null) {
            throw new IllegalArgumentException("Invoice history create request must not be null");
        }

        InvoiceHistoryRecord invoiceHistoryRecord = invoiceHistoryMapper.fromCreateRequestDTOToInvoiceHistoryRecord(invoiceHistoryCreateRequestDTO);
        invoiceHistoryRecord.setRevisionNumber(getNextRevisionNumber(invoiceHistoryCreateRequestDTO.getInvoiceId()));

        return invoiceHistoryMapper.fromInvoiceHistoryRecordToResponseDTO(invoiceHistoryRepository.save(invoiceHistoryRecord));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(UUID invoiceId) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("Invoice id must not be null");
        }

        return invoiceHistoryRepository.findByInvoiceIdOrderByRevisionNumberDesc(invoiceId)
                .stream()
                .map(invoiceHistoryMapper::fromInvoiceHistoryRecordToResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public void clearHistoryByInvoiceId(UUID invoiceId) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("Invoice id must not be null");
        }

        invoiceHistoryRepository.deleteByInvoiceId(invoiceId);
    }

    private Integer getNextRevisionNumber(UUID invoiceId) {
        return invoiceHistoryRepository.findTopByInvoiceIdOrderByRevisionNumberDesc(invoiceId)
                .map(invoiceHistoryRecord -> invoiceHistoryRecord.getRevisionNumber() + 1)
                .orElse(1);
    }

}
