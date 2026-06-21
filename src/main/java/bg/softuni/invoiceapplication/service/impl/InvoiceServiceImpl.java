package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.mapper.invoice.InvoiceMapper;
import bg.softuni.invoiceapplication.model.dto.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceDetailsDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceEditRequestDTO;
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
                .clientLegalRepresentative(client.getLegalRepresentative())
                .clientEmail(client.getEmail())
                .clientPhoneNumber(client.getPhoneNumber())
                .clientVatRegistered(client.isVatRegistered())
                .clientVatNumber(client.getVatNumber())
                .clientCountry(client.getCountry())
                .clientAddress(client.getAddress())
                .build();

        invoiceCreateRequestDTO.getLineItems()
                .stream()
                .map(invoiceMapper::fromInvoiceLineItemCreateRequestDTOToInvoiceLineItem)
                .forEach(invoice::addLineItem);

        return invoiceRepository.save(invoice);
    }

    @Override
    public InvoiceEditRequestDTO getInvoiceForEdit(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(invoiceMapper::fromInvoiceToInvoiceEditRequestDTO)
                .orElseThrow(() -> new RuntimeException("Invoice with id " + invoiceId + " does not exist"));
    }

    @Override
    @Transactional
    public void editInvoice(InvoiceEditRequestDTO invoiceEditRequestDTO) {
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
        invoice.setClientDisplayName(client.getDisplayName());
        invoice.setClientCompanyName(client.getCompanyName());
        invoice.setClientLegalRepresentative(client.getLegalRepresentative());
        invoice.setClientEmail(client.getEmail());
        invoice.setClientPhoneNumber(client.getPhoneNumber());
        invoice.setClientVatRegistered(client.isVatRegistered());
        invoice.setClientVatNumber(client.getVatNumber());
        invoice.setClientCountry(client.getCountry());
        invoice.setClientAddress(client.getAddress());

        invoice.getLineItems().clear();
        invoiceEditRequestDTO.getLineItems()
                .stream()
                .map(invoiceMapper::fromInvoiceLineItemCreateRequestDTOToInvoiceLineItem)
                .forEach(invoice::addLineItem);
    }

    @Override
    @Transactional
    public void cancelInvoice(UUID invoiceId) {
        updateInvoiceStatus(invoiceId, InvoiceStatus.CANCELLED);
    }

    @Override
    @Transactional
    public void restoreInvoice(UUID invoiceId) {
        updateInvoiceStatus(invoiceId, InvoiceStatus.ISSUED);
    }

    private void updateInvoiceStatus(UUID invoiceId, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice with id " + invoiceId + " does not exist"));

        invoice.setStatus(status);
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
