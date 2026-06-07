package bg.softuni.invoiceapplication.repository;

import bg.softuni.invoiceapplication.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    boolean existsByDisplayName(String displayName);

    boolean existsByVatNumber(String vatNumber);

    boolean existsByVatNumberAndIdNot(String vatNumber, UUID id);
}
