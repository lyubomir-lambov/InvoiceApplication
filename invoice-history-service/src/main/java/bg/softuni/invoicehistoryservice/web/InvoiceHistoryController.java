package bg.softuni.invoicehistoryservice.web;

import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryResponseDTO;
import bg.softuni.invoicehistoryservice.service.InvoiceHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoice-history")
@RequiredArgsConstructor
public class InvoiceHistoryController {

    private final InvoiceHistoryService invoiceHistoryService;

    @PostMapping
    public ResponseEntity<InvoiceHistoryResponseDTO> createHistoryRecord(
            @Valid @RequestBody InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(invoiceHistoryService.createHistoryRecord(invoiceHistoryCreateRequestDTO));
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<List<InvoiceHistoryResponseDTO>> findHistoryByInvoiceId(@PathVariable UUID invoiceId) {
        return ResponseEntity.ok(invoiceHistoryService.findHistoryByInvoiceId(invoiceId));
    }

    @DeleteMapping("/invoices/{invoiceId}")
    public ResponseEntity<Void> clearHistoryByInvoiceId(@PathVariable UUID invoiceId) {
        invoiceHistoryService.clearHistoryByInvoiceId(invoiceId);
        return ResponseEntity.noContent().build();
    }
}
