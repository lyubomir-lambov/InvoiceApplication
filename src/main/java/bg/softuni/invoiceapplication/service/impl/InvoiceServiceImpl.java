package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.mapper.invoice.InvoiceMapper;
import bg.softuni.invoiceapplication.model.dto.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceDetailsDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceLineItemCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.entity.InvoiceLineItem;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.service.InvoiceService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final int DEFAULT_DUE_DAYS = 14;
    private static final int INVOICE_NUMBER_LENGTH = 10;

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final InvoiceMapper invoiceMapper;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, ClientRepository clientRepository, InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.invoiceMapper = invoiceMapper;
    }

    @Override
    public List<InvoiceShowAllDTO> findAllInvoices() {
        return invoiceMapper.fromAllInvoicesToInvoiceShowAllDTOs(
                invoiceRepository.findAll(Sort.by(Sort.Order.desc("invoiceSequence"))));
    }

    @Override
    public InvoiceDetailsDTO findInvoiceById(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(invoiceMapper::fromInvoiceToInvoiceDetailsDTO)
                .orElseThrow(() -> new RuntimeException("Invoice with id " + invoiceId + " does not exist"));
    }

    @Override
    public InvoiceCreateRequestDTO prepareCreateInvoiceForm() {
        Long nextInvoiceSequence = getNextInvoiceSequence();
        LocalDate issueDate = LocalDate.now();

        InvoiceCreateRequestDTO invoiceCreateRequestDTO = new InvoiceCreateRequestDTO();
        invoiceCreateRequestDTO.setInvoiceType(InvoiceType.INVOICE);
        invoiceCreateRequestDTO.setCurrency(InvoiceCurrency.BGN);
        invoiceCreateRequestDTO.setInvoiceSequence(nextInvoiceSequence);
        invoiceCreateRequestDTO.setInvoiceNumber(formatInvoiceNumber(nextInvoiceSequence));
        invoiceCreateRequestDTO.setIssueDate(issueDate);
        invoiceCreateRequestDTO.setDueDate(issueDate.plusDays(DEFAULT_DUE_DAYS));
        invoiceCreateRequestDTO.getLineItems().add(new InvoiceLineItemCreateRequestDTO());

        return invoiceCreateRequestDTO;
    }

    @Override
    @Transactional
    public Invoice createInvoice(InvoiceCreateRequestDTO invoiceCreateRequestDTO) {
        if (invoiceCreateRequestDTO == null) {
            throw new IllegalArgumentException("Invoice create request must not be null");
        }

        Client client = clientRepository.findById(invoiceCreateRequestDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client with id " + invoiceCreateRequestDTO.getClientId() + " does not exist"));

        if (!client.isActive()) {
            throw new RuntimeException("Cannot create invoice for inactive client");
        }

        if (invoiceCreateRequestDTO.getDueDate().isBefore(invoiceCreateRequestDTO.getIssueDate())) {
            throw new RuntimeException("Due date cannot be before issue date");
        }

        Invoice lastInvoice = getLastInvoice();
        if (lastInvoice != null && invoiceCreateRequestDTO.getIssueDate().isBefore(lastInvoice.getIssueDate())) {
            throw new RuntimeException("Issue date cannot be before last invoice issue date");
        }

        Long nextInvoiceSequence = getNextInvoiceSequence(lastInvoice);

        Invoice invoice = Invoice.builder()
                .invoiceType(invoiceCreateRequestDTO.getInvoiceType())
                .invoiceSequence(nextInvoiceSequence)
                .invoiceNumber(formatInvoiceNumber(nextInvoiceSequence))
                .currency(invoiceCreateRequestDTO.getCurrency())
                .status(InvoiceStatus.ISSUED)
                .issueDate(invoiceCreateRequestDTO.getIssueDate())
                .dueDate(invoiceCreateRequestDTO.getDueDate())
                .client(client)
                .clientDisplayName(client.getDisplayName())
                .clientCompanyName(client.getCompanyName())
                .clientEmail(client.getEmail())
                .clientPhoneNumber(client.getPhoneNumber())
                .clientVatRegistered(client.isVatRegistered())
                .clientVatNumber(client.getVatNumber())
                .clientCountry(client.getCountry())
                .clientAddress(client.getAddress())
                .build();

        invoiceCreateRequestDTO.getLineItems()
                .stream()
                .map(lineItemDTO -> InvoiceLineItem.builder()
                        .description(lineItemDTO.getDescription())
                        .quantity(lineItemDTO.getQuantity())
                        .measurementUnit(lineItemDTO.getMeasurementUnit())
                        .unitPrice(lineItemDTO.getUnitPrice())
                        .vatRate(lineItemDTO.getVatRate())
                        .build())
                .forEach(invoice::addLineItem);

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public void cancelInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice with id " + invoiceId + " does not exist"));

        invoice.setStatus(InvoiceStatus.CANCELLED);
    }

    private Long getNextInvoiceSequence() {
        return getNextInvoiceSequence(getLastInvoice());
    }

    private Invoice getLastInvoice() {
        return invoiceRepository.findTopByOrderByInvoiceSequenceDesc()
                .orElse(null);
    }

    private Long getNextInvoiceSequence(Invoice lastInvoice) {
        return lastInvoice == null
                ? 1L
                : lastInvoice.getInvoiceSequence() + 1;
    }

    private String formatInvoiceNumber(Long invoiceSequence) {
        return String.format("%0" + INVOICE_NUMBER_LENGTH + "d", invoiceSequence);
    }
}
