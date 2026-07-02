package bg.softuni.invoiceapplication.service;

import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceDetailsDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceEditRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoices.InvoiceShowAllDTO;
import bg.softuni.invoiceapplication.model.entity.Invoice;

import java.util.List;
import java.util.UUID;

public interface InvoiceService {
    List<InvoiceShowAllDTO> findAllInvoices();
    List<InvoiceShowAllDTO> findInvoicesByCompanyName(String companyName);
    InvoiceDetailsDTO findInvoiceById(UUID invoiceId);
    InvoiceCreateRequestDTO prepareCreateInvoiceForm();
    Invoice createInvoice(InvoiceCreateRequestDTO invoiceCreateRequestDTO);
    InvoiceEditRequestDTO getInvoiceForEdit(UUID invoiceId);
    void editInvoice(InvoiceEditRequestDTO invoiceEditRequestDTO);
    void cancelInvoice(UUID invoiceId);
    void restoreInvoice(UUID invoiceId);
}
