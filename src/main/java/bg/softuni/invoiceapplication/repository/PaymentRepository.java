package bg.softuni.invoiceapplication.repository;

import bg.softuni.invoiceapplication.model.entity.Payment;
import bg.softuni.invoiceapplication.model.enums.InvoiceCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByClientIdOrderByPaymentDateDesc(UUID clientId);

    @Query("""
            SELECT p
            FROM Payment p
            WHERE LOWER(p.client.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))
            ORDER BY p.paymentDate DESC
            """)
    List<Payment> findPaymentsByCompanyName(@Param("companyName") String companyName);

    boolean existsByClientId(UUID clientId);

    @Query("""
            SELECT SUM(p.amount)
            FROM Payment p
            WHERE p.client.id = :clientId
            AND p.currency = :currency
            """)
    BigDecimal sumPaymentsByClientIdAndCurrency(
            @Param("clientId") UUID clientId,
            @Param("currency") InvoiceCurrency currency
    );
}
