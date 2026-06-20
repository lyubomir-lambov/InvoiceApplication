package bg.softuni.invoiceapplication.model.dto;

import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class InvoiceDetailsDTO {

    private UUID id;

    private InvoiceType invoiceType;

    private String invoiceNumber;

    private InvoiceCurrency currency;

    private InvoiceStatus status;

    private String clientCompanyName;

    private String clientDisplayName;

    private String clientLegalRepresentative;

    private String clientEmail;

    private String clientPhoneNumber;

    private boolean clientVatRegistered;

    private String clientVatNumber;

    private Country clientCountry;

    private String clientAddress;

    private LocalDate issueDate;

    private LocalDate dueDate;

    private BigDecimal subtotalAmount;

    private BigDecimal vatAmount;

    private BigDecimal totalAmount;

    @Default
    private List<InvoiceLineItemShowDTO> lineItems = new ArrayList<>();
}
