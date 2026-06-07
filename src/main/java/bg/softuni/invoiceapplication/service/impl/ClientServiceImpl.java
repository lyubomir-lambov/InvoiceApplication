package bg.softuni.invoiceapplication.service.impl;

import bg.softuni.invoiceapplication.repository.ClientRepository;
import bg.softuni.invoiceapplication.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }
}
