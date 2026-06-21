package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.model.dto.PaymentReportByCurrencyDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.entity.InvoiceLineItem;
import bg.softuni.invoiceapplication.model.entity.Payment;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import bg.softuni.invoiceapplication.repository.InvoiceRepository;
import bg.softuni.invoiceapplication.repository.PaymentRepository;
import bg.softuni.invoiceapplication.service.PaymentReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentReportServiceImpl implements PaymentReportService {

    private static final int MONEY_SCALE = 2;

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public PaymentReportServiceImpl(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentReportByCurrencyDTO> getReportsByCurrency() {
        Map<InvoiceCurrency, BigDecimal> invoiceTotalsByCurrency = emptyCurrencyTotals();
        Map<InvoiceCurrency, BigDecimal> paymentTotalsByCurrency = emptyCurrencyTotals();

        invoiceRepository.findAll()
                .forEach(invoice -> addInvoiceTotal(invoiceTotalsByCurrency, invoice));

        paymentRepository.findAll()
                .forEach(payment -> addPaymentTotal(paymentTotalsByCurrency, payment));

        List<PaymentReportByCurrencyDTO> reports = new ArrayList<>();
        for (InvoiceCurrency currency : InvoiceCurrency.values()) {
            BigDecimal invoiceTotal = invoiceTotalsByCurrency.get(currency);
            BigDecimal paymentTotal = paymentTotalsByCurrency.get(currency);

            reports.add(PaymentReportByCurrencyDTO.builder()
                    .currency(currency)
                    .invoiceTotal(invoiceTotal)
                    .paymentTotal(paymentTotal)
                    .dueAmount(invoiceTotal.subtract(paymentTotal).setScale(MONEY_SCALE, RoundingMode.HALF_UP))
                    .build());
        }

        return reports;
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
        BigDecimal invoiceTotal = invoice.getLineItems()
                .stream()
                .map(InvoiceLineItem::getLineTotalWithVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        if (invoice.getInvoiceType() == InvoiceType.CREDIT_NOTE) {
            invoiceTotal = invoiceTotal.negate();
        }

        invoiceTotalsByCurrency.merge(currency, invoiceTotal, BigDecimal::add);
    }

    private void addPaymentTotal(Map<InvoiceCurrency, BigDecimal> paymentTotalsByCurrency, Payment payment) {
        InvoiceCurrency currency = payment.getCurrency() == null ? InvoiceCurrency.BGN : payment.getCurrency();
        BigDecimal paymentTotal = payment.getAmount().setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        paymentTotalsByCurrency.merge(currency, paymentTotal, BigDecimal::add);
    }
}
