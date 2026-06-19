package bg.softuni.invoiceapplication.model.entity;

import bg.softuni.invoiceapplication.model.enums.Country;
import bg.softuni.invoiceapplication.model.enums.InvoiceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false)
    private InvoiceType invoiceType;

    @Column(name = "invoice_sequence", nullable = false, unique = true)
    private Long invoiceSequence;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 10)
    private String invoiceNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "client_display_name", nullable = false)
    private String clientDisplayName;

    @Column(name = "client_company_name", nullable = false)
    private String clientCompanyName;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "client_phone_number")
    private String clientPhoneNumber;

    @Column(name = "client_vat_registered", nullable = false)
    private boolean clientVatRegistered;

    @Column(name = "client_vat_number")
    private String clientVatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_country", nullable = false)
    private Country clientCountry;

    @Column(name = "client_address")
    private String clientAddress;

    @Builder.Default
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceLineItem> lineItems = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedOn;
}
