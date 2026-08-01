package bg.softuni.invoiceapplication.model.dto.invoicehistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceHistoryResponseDTO {

    private UUID id;

    private UUID invoiceId;

    private String invoiceNumber;

    private String invoiceType;

    private String invoiceStatus;

    private Integer revisionNumber;

    private String action;

    private String currency;

    private String clientDisplayName;

    private String clientCompanyName;

    private LocalDate issueDate;

    private LocalDate dueDate;

    private BigDecimal totalWithoutVat;

    private BigDecimal totalVat;

    private BigDecimal totalWithVat;

    private String snapshotJson;

    private String performedByUsername;

    private LocalDateTime createdOn;
}
