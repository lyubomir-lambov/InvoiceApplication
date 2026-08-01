package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.mapper.invoice.InvoiceMapper;
import bg.softuni.invoiceapplication.mapper.invoicehistory.InvoiceHistoryMapper;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceDetailsDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceLineItemCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.service.InvoiceHistoryIntegrationService;
import bg.softuni.invoiceapplication.service.InvoiceService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private static final String CREATED_ACTION = "CREATED";
    private static final String UPDATED_ACTION = "UPDATED";
    private static final String SYSTEM_USERNAME = "system";

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceHistoryMapper invoiceHistoryMapper;
    private final InvoiceHistoryIntegrationService invoiceHistoryIntegrationService;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              ClientRepository clientRepository,
                              InvoiceMapper invoiceMapper,
                              InvoiceHistoryMapper invoiceHistoryMapper,
                              InvoiceHistoryIntegrationService invoiceHistoryIntegrationService) {
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.invoiceMapper = invoiceMapper;
        this.invoiceHistoryMapper = invoiceHistoryMapper;
        this.invoiceHistoryIntegrationService = invoiceHistoryIntegrationService;
    }

    @Override
    public List<InvoiceShowAllDTO> findAllInvoices() {
        return invoiceMapper.fromAllInvoicesToInvoiceShowAllDTOs(
                invoiceRepository.findAll(Sort.by(Sort.Order.desc("invoiceSequence"))));
    }

    @Override
    public List<InvoiceShowAllDTO> findInvoicesByCompanyName(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return findAllInvoices();
        }

        String searchedCompanyName = companyName.trim();
        return invoiceMapper.fromAllInvoicesToInvoiceShowAllDTOs(
                invoiceRepository.findByClientCompanyNameContainingIgnoreCaseOrderByInvoiceSequenceDesc(searchedCompanyName));
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

        return InvoiceCreateRequestDTO.builder()
                .invoiceType(InvoiceType.INVOICE)
                .currency(InvoiceCurrency.BGN)
                .invoiceSequence(nextInvoiceSequence)
                .invoiceNumber(formatInvoiceNumber(nextInvoiceSequence))
                .issueDate(issueDate)
                .dueDate(issueDate.plusDays(DEFAULT_DUE_DAYS))
                .lineItems(List.of(InvoiceLineItemCreateRequestDTO.builder().build()))
                .build();
    }

    @Override
    @Transactional
    public Invoice createInvoice(InvoiceCreateRequestDTO invoiceCreateRequestDTO, String performedByUsername) {
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
                .build();

        updateClientSnapshot(invoice, client);
        invoiceCreateRequestDTO.getLineItems()
                .stream()
                .map(invoiceMapper::fromInvoiceLineItemCreateRequestDTOToInvoiceLineItem)
                .forEach(invoice::addLineItem);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        invoiceHistoryIntegrationService.createHistoryRecord(
                invoiceHistoryMapper.fromInvoiceToHistoryCreateRequestDTO(
                        savedInvoice,
                        CREATED_ACTION,
                        getPerformedByUsername(performedByUsername)));

        return savedInvoice;
    }

    @Override
    public InvoiceEditRequestDTO getInvoiceForEdit(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(invoiceMapper::fromInvoiceToInvoiceEditRequestDTO)
                .orElseThrow(() -> new RuntimeException("Invoice with id " + invoiceId + " does not exist"));
    }

    @Override
    @Transactional
    public void editInvoice(InvoiceEditRequestDTO invoiceEditRequestDTO, String performedByUsername) {
        if (invoiceEditRequestDTO == null) {
            throw new IllegalArgumentException("Invoice edit request must not be null");
        }

        Invoice invoice = invoiceRepository.findById(invoiceEditRequestDTO.getId())
                .orElseThrow(() -> new RuntimeException("Invoice with id " + invoiceEditRequestDTO.getId() + " does not exist"));

        Client client = clientRepository.findById(invoiceEditRequestDTO.getClientId())
                .orElseThrow(() -> new RuntimeException("Client with id " + invoiceEditRequestDTO.getClientId() + " does not exist"));

        if (!client.isActive() && !client.getId().equals(invoice.getClient().getId())) {
            throw new RuntimeException("Cannot move invoice to inactive client");
        }

        if (invoiceEditRequestDTO.getDueDate().isBefore(invoiceEditRequestDTO.getIssueDate())) {
            throw new RuntimeException("Due date cannot be before issue date");
        }

        invoice.setInvoiceType(invoiceEditRequestDTO.getInvoiceType());
        invoice.setCurrency(invoiceEditRequestDTO.getCurrency());
        invoice.setIssueDate(invoiceEditRequestDTO.getIssueDate());
        invoice.setDueDate(invoiceEditRequestDTO.getDueDate());
        invoice.setClient(client);
        updateClientSnapshot(invoice, client);

        invoice.getLineItems().clear();
        invoiceEditRequestDTO.getLineItems()
                .stream()
                .map(invoiceMapper::fromInvoiceLineItemCreateRequestDTOToInvoiceLineItem)
                .forEach(invoice::addLineItem);

        invoiceHistoryIntegrationService.createHistoryRecord(
                invoiceHistoryMapper.fromInvoiceToHistoryCreateRequestDTO(
                        invoice,
                        UPDATED_ACTION,
                        getPerformedByUsername(performedByUsername)));
    }

    private void updateClientSnapshot(Invoice invoice, Client client) {
        invoice.setClientDisplayName(client.getDisplayName());
        invoice.setClientCompanyName(client.getCompanyName());
        invoice.setClientLegalRepresentative(client.getLegalRepresentative());
        invoice.setClientEmail(client.getEmail());
        invoice.setClientPhoneNumber(client.getPhoneNumber());
        invoice.setClientVatRegistered(client.isVatRegistered());
        invoice.setClientVatNumber(client.getVatNumber());
        invoice.setClientCountry(client.getCountry());
        invoice.setClientAddress(client.getAddress());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void cancelInvoice(UUID invoiceId) {
        updateInvoiceStatus(invoiceId, InvoiceStatus.CANCELLED);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void restoreInvoice(UUID invoiceId) {
        updateInvoiceStatus(invoiceId, InvoiceStatus.ISSUED);
    }

    private void updateInvoiceStatus(UUID invoiceId, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice with id " + invoiceId + " does not exist"));

        invoice.setStatus(status);
    }

    private String getPerformedByUsername(String performedByUsername) {
        return performedByUsername == null || performedByUsername.isBlank()
                ? SYSTEM_USERNAME
                : performedByUsername;
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
