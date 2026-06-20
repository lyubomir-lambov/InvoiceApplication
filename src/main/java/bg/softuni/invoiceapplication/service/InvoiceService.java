package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceDetailsDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {
    List<InvoiceShowAllDTO> findAllInvoices();
    InvoiceDetailsDTO findInvoiceById(UUID invoiceId);
    InvoiceCreateRequestDTO prepareCreateInvoiceForm();
    Invoice createInvoice(InvoiceCreateRequestDTO invoiceCreateRequestDTO);
    void cancelInvoice(UUID invoiceId);
}
