package bg.softuni.invoicehistoryservice.repository;

import bg.softuni.invoicehistoryservice.model.entity.InvoiceHistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceHistoryRepository extends JpaRepository<InvoiceHistoryRecord, UUID> {

    List<InvoiceHistoryRecord> findByInvoiceIdOrderByRevisionNumberDesc(UUID invoiceId);

    Optional<InvoiceHistoryRecord> findTopByInvoiceIdOrderByRevisionNumberDesc(UUID invoiceId);
}
