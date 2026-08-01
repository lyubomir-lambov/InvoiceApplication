package bg.softuni.invoiceapplication.mapper.invoicehistory;

import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.entity.InvoiceLineItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class InvoiceHistoryMapper {

    private static final int MONEY_SCALE = 2;

    private final ObjectMapper objectMapper;

    public InvoiceHistoryMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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

    private Map<String, Object> createInvoiceSnapshot(Invoice invoice) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", invoice.getId());
        snapshot.put("invoiceType", invoice.getInvoiceType());
        snapshot.put("invoiceNumber", invoice.getInvoiceNumber());
        snapshot.put("currency", invoice.getCurrency());
        snapshot.put("status", invoice.getStatus());
        snapshot.put("issueDate", invoice.getIssueDate());
        snapshot.put("dueDate", invoice.getDueDate());
        snapshot.put("clientDisplayName", invoice.getClientDisplayName());
        snapshot.put("clientCompanyName", invoice.getClientCompanyName());
        snapshot.put("clientLegalRepresentative", invoice.getClientLegalRepresentative());
        snapshot.put("clientEmail", invoice.getClientEmail());
        snapshot.put("clientPhoneNumber", invoice.getClientPhoneNumber());
        snapshot.put("clientVatRegistered", invoice.isClientVatRegistered());
        snapshot.put("clientVatNumber", invoice.getClientVatNumber());
        snapshot.put("clientCountry", invoice.getClientCountry());
        snapshot.put("clientAddress", invoice.getClientAddress());
        snapshot.put("lineItems", createLineItemSnapshots(invoice.getLineItems()));
        snapshot.put("totalWithoutVat", calculateInvoiceSubtotalAmount(invoice));
        snapshot.put("totalVat", calculateInvoiceVatAmount(invoice));
        snapshot.put("totalWithVat", calculateInvoiceTotalAmount(invoice));
        return snapshot;
    }

    private List<Map<String, Object>> createLineItemSnapshots(List<InvoiceLineItem> lineItems) {
        return lineItems.stream()
                .map(this::createLineItemSnapshot)
                .toList();
    }

    private Map<String, Object> createLineItemSnapshot(InvoiceLineItem lineItem) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("description", lineItem.getDescription());
        snapshot.put("quantity", lineItem.getQuantity());
        snapshot.put("measurementUnit", lineItem.getMeasurementUnit());
        snapshot.put("unitPrice", lineItem.getUnitPrice());
        snapshot.put("vatRate", lineItem.getVatRate());
        snapshot.put("lineTotalWithoutVat", lineItem.getLineTotalWithoutVat());
        snapshot.put("vatAmount", lineItem.getVatAmount());
        snapshot.put("lineTotalWithVat", lineItem.getLineTotalWithVat());
        return snapshot;
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
}
