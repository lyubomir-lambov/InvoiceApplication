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
    Invoice createInvoice(InvoiceCreateRequestDTO invoiceCreateRequestDTO, String performedByUsername);
    InvoiceEditRequestDTO getInvoiceForEdit(UUID invoiceId);
    void editInvoice(InvoiceEditRequestDTO invoiceEditRequestDTO, String performedByUsername);
    void cancelInvoice(UUID invoiceId, String performedByUsername);
    void restoreInvoice(UUID invoiceId, String performedByUsername);
    int markOverdueInvoices();
    int markNoLongerOverdueInvoices();
}
