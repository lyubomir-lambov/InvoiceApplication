package bg.softuni.invoiceapplication.repository;

import bg.softuni.invoiceapplication.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    boolean existByDisplayName(String displayName);
    boolean existByVatNumber(String vatNumber);
}
