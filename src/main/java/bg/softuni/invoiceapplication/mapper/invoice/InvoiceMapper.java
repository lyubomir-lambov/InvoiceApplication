package bg.softuni.invoiceapplication.mapper.invoice;

import bg.softuni.invoiceapplication.model.dto.InvoiceDetailsDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceLineItemCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceLineItemShowDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.entity.InvoiceLineItem;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class InvoiceMapper {

    public List<InvoiceShowAllDTO> fromAllInvoicesToInvoiceShowAllDTOs(List<Invoice> invoices) {
        if (invoices == null) {
            return null;
        }

        List<InvoiceShowAllDTO> invoiceShowAllDTOs = new ArrayList<>();
        invoices.forEach(invoice -> invoiceShowAllDTOs.add(fromInvoiceToInvoiceShowAllDTO(invoice)));
        return invoiceShowAllDTOs;
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

        InvoiceEditRequestDTO invoiceEditRequestDTO = new InvoiceEditRequestDTO();
        invoiceEditRequestDTO.setId(invoice.getId());
        invoiceEditRequestDTO.setInvoiceType(invoice.getInvoiceType());
        invoiceEditRequestDTO.setInvoiceNumber(invoice.getInvoiceNumber());
        invoiceEditRequestDTO.setCurrency(invoice.getCurrency() == null ? InvoiceCurrency.BGN : invoice.getCurrency());
        invoiceEditRequestDTO.setStatus(invoice.getStatus() == null ? InvoiceStatus.ISSUED : invoice.getStatus());
        invoiceEditRequestDTO.setIssueDate(invoice.getIssueDate());
        invoiceEditRequestDTO.setDueDate(invoice.getDueDate());
        invoiceEditRequestDTO.setClientId(invoice.getClient().getId());
        invoiceEditRequestDTO.setLineItems(fromAllInvoiceLineItemsToInvoiceLineItemCreateRequestDTOs(invoice.getLineItems()));

        return invoiceEditRequestDTO;
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

        List<InvoiceLineItemShowDTO> invoiceLineItemShowDTOs = new ArrayList<>();
        lineItems.forEach(lineItem -> {
            InvoiceLineItemShowDTO invoiceLineItemShowDTO = InvoiceLineItemShowDTO.builder()
                    .description(lineItem.getDescription())
                    .quantity(lineItem.getQuantity())
                    .measurementUnit(lineItem.getMeasurementUnit())
                    .unitPrice(lineItem.getUnitPrice())
                    .vatRate(lineItem.getVatRate())
                    .lineTotalWithoutVat(lineItem.getLineTotalWithoutVat())
                    .vatAmount(lineItem.getVatAmount())
                    .lineTotalWithVat(lineItem.getLineTotalWithVat())
                    .build();
            invoiceLineItemShowDTOs.add(invoiceLineItemShowDTO);
        });
        return invoiceLineItemShowDTOs;
    }

    private List<InvoiceLineItemCreateRequestDTO> fromAllInvoiceLineItemsToInvoiceLineItemCreateRequestDTOs(List<InvoiceLineItem> lineItems) {
        if (lineItems == null) {
            return null;
        }

        List<InvoiceLineItemCreateRequestDTO> invoiceLineItemCreateRequestDTOs = new ArrayList<>();
        lineItems.forEach(lineItem -> {
            InvoiceLineItemCreateRequestDTO invoiceLineItemCreateRequestDTO = new InvoiceLineItemCreateRequestDTO();
            invoiceLineItemCreateRequestDTO.setDescription(lineItem.getDescription());
            invoiceLineItemCreateRequestDTO.setQuantity(lineItem.getQuantity());
            invoiceLineItemCreateRequestDTO.setMeasurementUnit(lineItem.getMeasurementUnit());
            invoiceLineItemCreateRequestDTO.setUnitPrice(lineItem.getUnitPrice());
            invoiceLineItemCreateRequestDTO.setVatRate(lineItem.getVatRate());

            invoiceLineItemCreateRequestDTOs.add(invoiceLineItemCreateRequestDTO);
        });
        return invoiceLineItemCreateRequestDTOs;
    }

    private BigDecimal calculateInvoiceTotalAmount(Invoice invoice) {
        return invoice.getLineItems()
                .stream()
                .map(InvoiceLineItem::getLineTotalWithVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateInvoiceSubtotalAmount(Invoice invoice) {
        return invoice.getLineItems()
                .stream()
                .map(InvoiceLineItem::getLineTotalWithoutVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateInvoiceVatAmount(Invoice invoice) {
        return invoice.getLineItems()
                .stream()
                .map(InvoiceLineItem::getVatAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
