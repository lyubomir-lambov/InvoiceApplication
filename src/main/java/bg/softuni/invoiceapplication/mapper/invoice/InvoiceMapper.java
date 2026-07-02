package bg.softuni.invoiceapplication.mapper.invoice;

import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceDetailsDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceLineItemCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceLineItemShowDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.entity.InvoiceLineItem;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class InvoiceMapper {

    private static final int MONEY_SCALE = 2;

    public List<InvoiceShowAllDTO> fromAllInvoicesToInvoiceShowAllDTOs(List<Invoice> invoices) {
        if (invoices == null) {
            return null;
        }

        return invoices.stream()
                .map(this::fromInvoiceToInvoiceShowAllDTO)
                .toList();
    }

    public InvoiceShowAllDTO fromInvoiceToInvoiceShowAllDTO(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        return InvoiceShowAllDTO.builder()
                .id(invoice.getId())
                .invoiceType(invoice.getInvoiceType())
                .invoiceNumber(invoice.getInvoiceNumber())
                .currency(invoice.getCurrency() == null ? InvoiceCurrency.BGN : invoice.getCurrency())
                .status(invoice.getStatus() == null ? InvoiceStatus.ISSUED : invoice.getStatus())
                .clientCompanyName(invoice.getClientCompanyName())
                .issueDate(invoice.getIssueDate())
                .totalAmount(calculateInvoiceTotalAmount(invoice))
                .build();
    }

    public InvoiceDetailsDTO fromInvoiceToInvoiceDetailsDTO(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        return InvoiceDetailsDTO.builder()
                .id(invoice.getId())
                .invoiceType(invoice.getInvoiceType())
                .invoiceNumber(invoice.getInvoiceNumber())
                .currency(invoice.getCurrency() == null ? InvoiceCurrency.BGN : invoice.getCurrency())
                .status(invoice.getStatus() == null ? InvoiceStatus.ISSUED : invoice.getStatus())
                .clientCompanyName(invoice.getClientCompanyName())
                .clientDisplayName(invoice.getClientDisplayName())
                .clientLegalRepresentative(invoice.getClientLegalRepresentative())
                .clientEmail(invoice.getClientEmail())
                .clientPhoneNumber(invoice.getClientPhoneNumber())
                .clientVatRegistered(invoice.isClientVatRegistered())
                .clientVatNumber(invoice.getClientVatNumber())
                .clientCountry(invoice.getClientCountry())
                .clientAddress(invoice.getClientAddress())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .subtotalAmount(calculateInvoiceSubtotalAmount(invoice))
                .vatAmount(calculateInvoiceVatAmount(invoice))
                .totalAmount(calculateInvoiceTotalAmount(invoice))
                .lineItems(fromAllInvoiceLineItemsToInvoiceLineItemShowDTOs(invoice.getLineItems()))
                .build();
    }

    public InvoiceEditRequestDTO fromInvoiceToInvoiceEditRequestDTO(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        return InvoiceEditRequestDTO.builder()
                .id(invoice.getId())
                .invoiceType(invoice.getInvoiceType())
                .invoiceNumber(invoice.getInvoiceNumber())
                .currency(invoice.getCurrency() == null ? InvoiceCurrency.BGN : invoice.getCurrency())
                .status(invoice.getStatus() == null ? InvoiceStatus.ISSUED : invoice.getStatus())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .clientId(invoice.getClient().getId())
                .lineItems(fromAllInvoiceLineItemsToInvoiceLineItemCreateRequestDTOs(invoice.getLineItems()))
                .build();
    }

    public InvoiceLineItem fromInvoiceLineItemCreateRequestDTOToInvoiceLineItem(InvoiceLineItemCreateRequestDTO lineItemDTO) {
        if (lineItemDTO == null) {
            return null;
        }

        return InvoiceLineItem.builder()
                .description(lineItemDTO.getDescription())
                .quantity(lineItemDTO.getQuantity())
                .measurementUnit(lineItemDTO.getMeasurementUnit())
                .unitPrice(lineItemDTO.getUnitPrice())
                .vatRate(lineItemDTO.getVatRate())
                .build();
    }

    private List<InvoiceLineItemShowDTO> fromAllInvoiceLineItemsToInvoiceLineItemShowDTOs(List<InvoiceLineItem> lineItems) {
        if (lineItems == null) {
            return null;
        }

        return lineItems.stream()
                .map(lineItem -> InvoiceLineItemShowDTO.builder()
                        .description(lineItem.getDescription())
                        .quantity(lineItem.getQuantity())
                        .measurementUnit(lineItem.getMeasurementUnit())
                        .unitPrice(lineItem.getUnitPrice())
                        .vatRate(lineItem.getVatRate())
                        .lineTotalWithoutVat(lineItem.getLineTotalWithoutVat())
                        .vatAmount(lineItem.getVatAmount())
                        .lineTotalWithVat(lineItem.getLineTotalWithVat())
                        .build())
                .toList();
    }

    private List<InvoiceLineItemCreateRequestDTO> fromAllInvoiceLineItemsToInvoiceLineItemCreateRequestDTOs(List<InvoiceLineItem> lineItems) {
        if (lineItems == null) {
            return null;
        }

        return lineItems.stream()
                .map(lineItem -> InvoiceLineItemCreateRequestDTO.builder()
                        .description(lineItem.getDescription())
                        .quantity(lineItem.getQuantity())
                        .measurementUnit(lineItem.getMeasurementUnit())
                        .unitPrice(lineItem.getUnitPrice())
                        .vatRate(lineItem.getVatRate())
                        .build())
                .toList();
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
