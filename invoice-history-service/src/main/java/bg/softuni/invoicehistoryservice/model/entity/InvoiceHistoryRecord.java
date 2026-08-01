package bg.softuni.invoicehistoryservice.model.entity;

import bg.softuni.invoicehistoryservice.model.enums.InvoiceHistoryAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice_history_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceHistoryRecord extends BaseEntity {

    @Column(nullable = false)
    private UUID invoiceId;

    @Column(nullable = false, length = 10)
    private String invoiceNumber;

    @Column(nullable = false)
    private String invoiceType;

    @Column(nullable = false)
    private String invoiceStatus;

    @Column(nullable = false)
    private Integer revisionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceHistoryAction action;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String clientDisplayName;

    @Column(nullable = false)
    private String clientCompanyName;

    @Column(nullable = false)
    private LocalDate issueDate;

    private LocalDate dueDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalWithoutVat;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalVat;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalWithVat;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String snapshotJson;

    @Column(nullable = false)
    private String performedByUsername;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;
}
