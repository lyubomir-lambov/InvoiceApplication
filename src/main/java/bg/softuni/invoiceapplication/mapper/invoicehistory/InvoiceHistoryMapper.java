package bg.softuni.invoiceapplication.mapper.invoicehistory;

import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.entity.InvoiceLineItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoiceHistoryMapper {

    private static final int MONEY_SCALE = 2;
    private static final int QUANTITY_SCALE = 2;

    private final ObjectMapper objectMapper;

    public InvoiceHistoryCreateRequestDTO fromInvoiceToHistoryCreateRequestDTO(Invoice invoice,
                                                                               String action,
                                                                               String performedByUsername) {
        if (invoice == null) {
            return null;
        }

        return InvoiceHistoryCreateRequestDTO.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceType(invoice.getInvoiceType().name())
                .invoiceStatus(invoice.getStatus().name())
                .action(action)
                .currency(invoice.getCurrency().name())
                .clientDisplayName(invoice.getClientDisplayName())
                .clientCompanyName(invoice.getClientCompanyName())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .totalWithoutVat(calculateInvoiceSubtotalAmount(invoice))
                .totalVat(calculateInvoiceVatAmount(invoice))
                .totalWithVat(calculateInvoiceTotalAmount(invoice))
                .snapshotJson(createSnapshotJson(invoice))
                .performedByUsername(performedByUsername)
                .build();
    }

    private String createSnapshotJson(Invoice invoice) {
        try {
            return objectMapper.writeValueAsString(createInvoiceSnapshot(invoice));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not create invoice history snapshot", e);
        }
    }

    private InvoiceSnapshot createInvoiceSnapshot(Invoice invoice) {
        return InvoiceSnapshot.builder()
                .id(invoice.getId())
                .invoiceType(invoice.getInvoiceType().name())
                .invoiceNumber(invoice.getInvoiceNumber())
                .currency(invoice.getCurrency().name())
                .status(invoice.getStatus().name())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .clientDisplayName(invoice.getClientDisplayName())
                .clientCompanyName(invoice.getClientCompanyName())
                .clientLegalRepresentative(invoice.getClientLegalRepresentative())
                .clientEmail(invoice.getClientEmail())
                .clientPhoneNumber(invoice.getClientPhoneNumber())
                .clientVatRegistered(invoice.isClientVatRegistered())
                .clientVatNumber(invoice.getClientVatNumber())
                .clientCountry(invoice.getClientCountry().name())
                .clientAddress(invoice.getClientAddress())
                .lineItems(createLineItemSnapshots(invoice.getLineItems()))
                .totalWithoutVat(calculateInvoiceSubtotalAmount(invoice))
                .totalVat(calculateInvoiceVatAmount(invoice))
                .totalWithVat(calculateInvoiceTotalAmount(invoice))
                .build();
    }

    private List<InvoiceLineItemSnapshot> createLineItemSnapshots(List<InvoiceLineItem> lineItems) {
        return lineItems.stream()
                .map(this::createLineItemSnapshot)
                .toList();
    }

    private InvoiceLineItemSnapshot createLineItemSnapshot(InvoiceLineItem lineItem) {
        return InvoiceLineItemSnapshot.builder()
                .description(lineItem.getDescription())
                .quantity(formatQuantity(lineItem.getQuantity()))
                .measurementUnit(lineItem.getMeasurementUnit().name())
                .unitPrice(formatMoney(lineItem.getUnitPrice()))
                .vatRate(lineItem.getVatRate().name())
                .lineTotalWithoutVat(lineItem.getLineTotalWithoutVat())
                .vatAmount(lineItem.getVatAmount())
                .lineTotalWithVat(lineItem.getLineTotalWithVat())
                .build();
    }

    private BigDecimal calculateInvoiceTotalAmount(Invoice invoice) {
        return invoice.getLineItems()
                .stream()
                .map(InvoiceLineItem::getLineTotalWithVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateInvoiceSubtotalAmount(Invoice invoice) {
        return invoice.getLineItems()
                .stream()
                .map(InvoiceLineItem::getLineTotalWithoutVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateInvoiceVatAmount(Invoice invoice) {
        return invoice.getLineItems()
                .stream()
                .map(InvoiceLineItem::getVatAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal formatMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal formatQuantity(BigDecimal quantity) {
        return quantity.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
    }

    @Getter
    @Builder
    private static class InvoiceSnapshot {
        private Object id;
        private String invoiceType;
        private String invoiceNumber;
        private String currency;
        private String status;
        private Object issueDate;
        private Object dueDate;
        private String clientDisplayName;
        private String clientCompanyName;
        private String clientLegalRepresentative;
        private String clientEmail;
        private String clientPhoneNumber;
        private boolean clientVatRegistered;
        private String clientVatNumber;
        private String clientCountry;
        private String clientAddress;
        private List<InvoiceLineItemSnapshot> lineItems;
        private BigDecimal totalWithoutVat;
        private BigDecimal totalVat;
        private BigDecimal totalWithVat;
    }

    @Getter
    @Builder
    private static class InvoiceLineItemSnapshot {
        private String description;
        private BigDecimal quantity;
        private String measurementUnit;
        private BigDecimal unitPrice;
        private String vatRate;
        private BigDecimal lineTotalWithoutVat;
        private BigDecimal vatAmount;
        private BigDecimal lineTotalWithVat;
    }
}
