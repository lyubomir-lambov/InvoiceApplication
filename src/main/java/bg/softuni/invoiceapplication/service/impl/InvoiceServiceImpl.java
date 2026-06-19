package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.model.dto.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.service.InvoiceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final int DEFAULT_DUE_DAYS = 14;
    private static final int INVOICE_NUMBER_LENGTH = 10;

    private final InvoiceRepository invoiceRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public InvoiceCreateRequestDTO prepareCreateInvoiceForm() {
        Long nextInvoiceSequence = getNextInvoiceSequence();
        LocalDate issueDate = LocalDate.now();

        InvoiceCreateRequestDTO invoiceCreateRequestDTO = new InvoiceCreateRequestDTO();
        invoiceCreateRequestDTO.setInvoiceType(InvoiceType.INVOICE);
        invoiceCreateRequestDTO.setInvoiceSequence(nextInvoiceSequence);
        invoiceCreateRequestDTO.setInvoiceNumber(formatInvoiceNumber(nextInvoiceSequence));
        invoiceCreateRequestDTO.setIssueDate(issueDate);
        invoiceCreateRequestDTO.setDueDate(issueDate.plusDays(DEFAULT_DUE_DAYS));

        return invoiceCreateRequestDTO;
    }

    @Override
    public Invoice createInvoice(InvoiceCreateRequestDTO invoiceCreateRequestDTO) {
        throw new UnsupportedOperationException("Invoice creation is not implemented yet");
    }

    private Long getNextInvoiceSequence() {
        return invoiceRepository.findTopByOrderByInvoiceSequenceDesc()
                .map(invoice -> invoice.getInvoiceSequence() + 1)
                .orElse(1L);
    }

    private String formatInvoiceNumber(Long invoiceSequence) {
        return String.format("%0" + INVOICE_NUMBER_LENGTH + "d", invoiceSequence);
    }
}
