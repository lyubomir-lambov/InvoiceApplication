package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.InvoiceShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;

import java.util.List;

public interface InvoiceService {
    List<InvoiceShowAllDTO> findAllInvoices();
    InvoiceCreateRequestDTO prepareCreateInvoiceForm();
    Invoice createInvoice(InvoiceCreateRequestDTO invoiceCreateRequestDTO);
}
