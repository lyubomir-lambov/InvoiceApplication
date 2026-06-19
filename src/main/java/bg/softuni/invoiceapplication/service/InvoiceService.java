package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;

public interface InvoiceService {
    Invoice createInvoice(InvoiceCreateRequestDTO invoiceCreateRequestDTO);
}
