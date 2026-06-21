package bg.softuni.invoiceapplication.repository;

import bg.softuni.invoiceapplication.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("""
            SELECT p
            FROM Payment p
            WHERE LOWER(p.client.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))
            ORDER BY p.paymentDate DESC
            """)
    List<Payment> findPaymentsByCompanyName(@Param("companyName") String companyName);

    boolean existsByClientId(UUID clientId);
}
