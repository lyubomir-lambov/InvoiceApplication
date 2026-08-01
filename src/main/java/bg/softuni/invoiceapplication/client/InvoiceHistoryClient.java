package bg.softuni.invoiceapplication.client;

import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoiceapplication.model.dto.invoicehistory.InvoiceHistoryResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "invoice-history-service", url = "${invoice.history.service.url}")
public interface InvoiceHistoryClient {

    String API_KEY_HEADER = "X-API-Key";

    @PostMapping("/api/invoice-history")
    InvoiceHistoryResponseDTO createHistoryRecord(
            @RequestHeader(API_KEY_HEADER) String apiKey,
            @RequestBody InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO);

    @GetMapping("/api/invoice-history/invoices/{invoiceId}")
    List<InvoiceHistoryResponseDTO> findHistoryByInvoiceId(
            @RequestHeader(API_KEY_HEADER) String apiKey,
            @PathVariable UUID invoiceId);
}
