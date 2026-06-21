package bg.softuni.invoiceapplication.repository;

import bg.softuni.invoiceapplication.model.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findTopByOrderByInvoiceSequenceDesc();

    List<Invoice> findByClientCompanyNameContainingIgnoreCaseOrderByInvoiceSequenceDesc(String clientCompanyName);

    boolean existsByClientId(UUID clientId);
}
