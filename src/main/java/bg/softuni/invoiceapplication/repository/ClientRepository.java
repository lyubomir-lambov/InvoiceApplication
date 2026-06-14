package bg.softuni.invoiceapplication.repository;

import bg.softuni.invoiceapplication.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {
    boolean existsByDisplayName(String displayName);

    List<Client> findByDisplayNameContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(String displayName, String companyName);

    boolean existsByVatNumber(String vatNumber);

    boolean existsByVatNumberAndIdNot(String vatNumber, UUID id);
}
