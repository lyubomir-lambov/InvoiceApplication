package bg.softuni.invoiceapplication.repository;

import bg.softuni.invoiceapplication.model.entity.Invoice;
import bg.softuni.invoiceapplication.model.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findTopByOrderByInvoiceSequenceDesc();

    List<Invoice> findByClientCompanyNameContainingIgnoreCaseOrderByInvoiceSequenceDesc(String clientCompanyName);

    List<Invoice> findAllByStatusAndDueDateBefore(InvoiceStatus status, LocalDate dueDate);

    boolean existsByClientId(UUID clientId);
}
