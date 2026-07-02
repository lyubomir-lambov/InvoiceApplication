package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportByCurrencyDTO;
import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportClientDTO;
import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportInvoiceCurrencyGroupDTO;
import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportInvoiceDTO;
import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportPaymentCurrencyGroupDTO;
import bg.softuni.invoiceapplication.model.dto.reports.PaymentReportPaymentDTO;
import bg.softuni.invoiceapplication.model.entity.Client;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.entity.InvoiceLineItem;
import bg.softuni.invoiceapplication.model.entity.Payment;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.repository.PaymentRepository;
import bg.softuni.invoiceapplication.service.PaymentReportService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentReportServiceImpl implements PaymentReportService {

    private static final int MONEY_SCALE = 2;

    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public PaymentReportServiceImpl(ClientRepository clientRepository,
                                    InvoiceRepository invoiceRepository,
                                    PaymentRepository paymentRepository) {
        this.clientRepository = clientRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentReportByCurrencyDTO> getReportsByCurrency(UUID clientId) {
        Map<InvoiceCurrency, BigDecimal> invoiceTotalsByCurrency = emptyCurrencyTotals();
        Map<InvoiceCurrency, BigDecimal> paymentTotalsByCurrency = emptyCurrencyTotals();

        invoiceRepository.findAll()
                .stream()
                .filter(this::isInvoiceActiveForReport)
                .filter(invoice -> isInvoiceForClient(invoice, clientId))
                .forEach(invoice -> addInvoiceTotal(invoiceTotalsByCurrency, invoice));

        paymentRepository.findAll()
                .stream()
                .filter(payment -> isPaymentForClient(payment, clientId))
                .forEach(payment -> addPaymentTotal(paymentTotalsByCurrency, payment));

        List<PaymentReportByCurrencyDTO> reports = new ArrayList<>();
        for (InvoiceCurrency currency : InvoiceCurrency.values()) {
            BigDecimal invoiceTotal = invoiceTotalsByCurrency.get(currency);
            BigDecimal paymentTotal = paymentTotalsByCurrency.get(currency);

            if (invoiceTotal.signum() == 0 && paymentTotal.signum() == 0) {
                continue;
            }

            reports.add(PaymentReportByCurrencyDTO.builder()
                    .currency(currency)
                    .invoiceTotal(invoiceTotal)
                    .paymentTotal(paymentTotal)
                    .dueAmount(invoiceTotal.subtract(paymentTotal).setScale(MONEY_SCALE, RoundingMode.HALF_UP))
                    .build());
        }

        return reports;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentReportClientDTO> getClientReports(UUID clientId) {
        List<Client> clients = getClientsForReport(clientId);
        List<Invoice> activeInvoices = invoiceRepository.findAll()
                .stream()
                .filter(this::isInvoiceActiveForReport)
                .toList();
        List<Payment> payments = paymentRepository.findAll();

        return clients.stream()
                .map(client -> buildClientReport(client, activeInvoices, payments))
                .filter(clientReport -> clientId != null || hasReportData(clientReport))
                .toList();
    }

    private List<Client> getClientsForReport(UUID clientId) {
        if (clientId != null) {
            return clientRepository.findById(clientId)
                    .map(List::of)
                    .orElse(List.of());
        }

        return clientRepository.findAll(Sort.by(Sort.Order.asc("displayName")));
    }

    private PaymentReportClientDTO buildClientReport(Client client, List<Invoice> activeInvoices, List<Payment> payments) {
        List<Invoice> clientInvoices = activeInvoices.stream()
                .filter(invoice -> invoice.getClient().getId().equals(client.getId()))
                .sorted(Comparator.comparing((Invoice invoice) -> invoice.getCurrency() == null ? InvoiceCurrency.BGN : invoice.getCurrency())
                        .thenComparing(Invoice::getIssueDate, Comparator.reverseOrder()))
                .toList();

        List<Payment> clientPayments = payments.stream()
                .filter(payment -> payment.getClient().getId().equals(client.getId()))
                .sorted(Comparator.comparing((Payment payment) -> payment.getCurrency() == null ? InvoiceCurrency.BGN : payment.getCurrency())
                        .thenComparing(Payment::getPaymentDate, Comparator.reverseOrder()))
                .toList();

        return PaymentReportClientDTO.builder()
                .clientId(client.getId())
                .clientDisplayName(client.getDisplayName())
                .clientCompanyName(client.getCompanyName())
                .invoiceCurrencyGroups(buildInvoiceCurrencyGroups(clientInvoices))
                .paymentCurrencyGroups(buildPaymentCurrencyGroups(clientPayments))
                .build();
    }

    private boolean hasReportData(PaymentReportClientDTO clientReport) {
        return !clientReport.getInvoiceCurrencyGroups().isEmpty()
                || !clientReport.getPaymentCurrencyGroups().isEmpty();
    }

    private List<PaymentReportInvoiceCurrencyGroupDTO> buildInvoiceCurrencyGroups(List<Invoice> invoices) {
        List<PaymentReportInvoiceCurrencyGroupDTO> invoiceCurrencyGroups = new ArrayList<>();

        for (InvoiceCurrency currency : InvoiceCurrency.values()) {
            List<PaymentReportInvoiceDTO> invoiceRows = invoices.stream()
                    .filter(invoice -> currency.equals(invoice.getCurrency() == null ? InvoiceCurrency.BGN : invoice.getCurrency()))
                    .map(this::fromInvoiceToPaymentReportInvoiceDTO)
                    .toList();

            if (!invoiceRows.isEmpty()) {
                BigDecimal totalAmount = invoiceRows.stream()
                        .map(PaymentReportInvoiceDTO::getTotalAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

                invoiceCurrencyGroups.add(PaymentReportInvoiceCurrencyGroupDTO.builder()
                        .currency(currency)
                        .totalAmount(totalAmount)
                        .invoices(invoiceRows)
                        .build());
            }
        }

        return invoiceCurrencyGroups;
    }

    private PaymentReportInvoiceDTO fromInvoiceToPaymentReportInvoiceDTO(Invoice invoice) {
        return PaymentReportInvoiceDTO.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceType(invoice.getInvoiceType())
                .issueDate(invoice.getIssueDate())
                .totalAmount(calculateSignedInvoiceTotal(invoice))
                .currency(invoice.getCurrency() == null ? InvoiceCurrency.BGN : invoice.getCurrency())
                .build();
    }

    private List<PaymentReportPaymentCurrencyGroupDTO> buildPaymentCurrencyGroups(List<Payment> payments) {
        List<PaymentReportPaymentCurrencyGroupDTO> paymentCurrencyGroups = new ArrayList<>();

        for (InvoiceCurrency currency : InvoiceCurrency.values()) {
            List<PaymentReportPaymentDTO> paymentRows = payments.stream()
                    .filter(payment -> currency.equals(payment.getCurrency() == null ? InvoiceCurrency.BGN : payment.getCurrency()))
                    .map(this::fromPaymentToPaymentReportPaymentDTO)
                    .toList();

            if (!paymentRows.isEmpty()) {
                BigDecimal totalAmount = paymentRows.stream()
                        .map(PaymentReportPaymentDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

                paymentCurrencyGroups.add(PaymentReportPaymentCurrencyGroupDTO.builder()
                        .currency(currency)
                        .totalAmount(totalAmount)
                        .payments(paymentRows)
                        .build());
            }
        }

        return paymentCurrencyGroups;
    }

    private PaymentReportPaymentDTO fromPaymentToPaymentReportPaymentDTO(Payment payment) {
        return PaymentReportPaymentDTO.builder()
                .id(payment.getId())
                .paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP))
                .currency(payment.getCurrency() == null ? InvoiceCurrency.BGN : payment.getCurrency())
                .notes(payment.getNotes())
                .build();
    }

    private boolean isInvoiceForClient(Invoice invoice, UUID clientId) {
        return clientId == null || invoice.getClient().getId().equals(clientId);
    }

    private boolean isInvoiceActiveForReport(Invoice invoice) {
        return invoice.getStatus() != InvoiceStatus.CANCELLED;
    }

    private boolean isPaymentForClient(Payment payment, UUID clientId) {
        return clientId == null || payment.getClient().getId().equals(clientId);
    }

    private Map<InvoiceCurrency, BigDecimal> emptyCurrencyTotals() {
        Map<InvoiceCurrency, BigDecimal> totalsByCurrency = new EnumMap<>(InvoiceCurrency.class);
        for (InvoiceCurrency currency : InvoiceCurrency.values()) {
            totalsByCurrency.put(currency, BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        }
        return totalsByCurrency;
    }

    private void addInvoiceTotal(Map<InvoiceCurrency, BigDecimal> invoiceTotalsByCurrency, Invoice invoice) {
        InvoiceCurrency currency = invoice.getCurrency() == null ? InvoiceCurrency.BGN : invoice.getCurrency();
        BigDecimal invoiceTotal = calculateSignedInvoiceTotal(invoice);

        invoiceTotalsByCurrency.merge(currency, invoiceTotal, BigDecimal::add);
    }

    private BigDecimal calculateSignedInvoiceTotal(Invoice invoice) {
        BigDecimal invoiceTotal = invoice.getLineItems()
                .stream()
                .map(InvoiceLineItem::getLineTotalWithVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        if (invoice.getInvoiceType() == InvoiceType.CREDIT_NOTE) {
            invoiceTotal = invoiceTotal.negate();
        }

        return invoiceTotal;
    }

    private void addPaymentTotal(Map<InvoiceCurrency, BigDecimal> paymentTotalsByCurrency, Payment payment) {
        InvoiceCurrency currency = payment.getCurrency() == null ? InvoiceCurrency.BGN : payment.getCurrency();
        BigDecimal paymentTotal = payment.getAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        paymentTotalsByCurrency.merge(currency, paymentTotal, BigDecimal::add);
    }
}
