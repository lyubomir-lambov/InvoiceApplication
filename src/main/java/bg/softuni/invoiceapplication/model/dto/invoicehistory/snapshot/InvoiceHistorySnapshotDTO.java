package bg.softuni.invoiceapplication.model.dto.invoicehistory.snapshot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceHistorySnapshotDTO {

    private UUID id;

    private String invoiceType;

    private String invoiceNumber;

    private String currency;

    private String status;

    private LocalDate issueDate;

    private LocalDate dueDate;

    private String clientDisplayName;

    private String clientCompanyName;

    private String clientLegalRepresentative;

    private String clientEmail;

    private String clientPhoneNumber;

    private boolean clientVatRegistered;

    private String clientVatNumber;

    private String clientCountry;

    private String clientAddress;

    private List<InvoiceHistoryLineItemSnapshotDTO> lineItems;

    private BigDecimal totalWithoutVat;

    private BigDecimal totalVat;

    private BigDecimal totalWithVat;
}
