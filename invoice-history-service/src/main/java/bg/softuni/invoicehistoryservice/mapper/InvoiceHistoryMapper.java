package bg.softuni.invoicehistoryservice.mapper;

import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryCreateRequestDTO;
import bg.softuni.invoicehistoryservice.model.dto.InvoiceHistoryResponseDTO;
import bg.softuni.invoicehistoryservice.model.entity.InvoiceHistoryRecord;
import org.springframework.stereotype.Component;

@Component
public class InvoiceHistoryMapper {

    public InvoiceHistoryRecord fromCreateRequestDTOToInvoiceHistoryRecord(InvoiceHistoryCreateRequestDTO invoiceHistoryCreateRequestDTO) {
        if (invoiceHistoryCreateRequestDTO == null) {
            return null;
        }

        return InvoiceHistoryRecord.builder()
                .invoiceId(invoiceHistoryCreateRequestDTO.getInvoiceId())
                .invoiceNumber(invoiceHistoryCreateRequestDTO.getInvoiceNumber())
                .invoiceType(invoiceHistoryCreateRequestDTO.getInvoiceType())
                .invoiceStatus(invoiceHistoryCreateRequestDTO.getInvoiceStatus())
                .action(invoiceHistoryCreateRequestDTO.getAction())
                .currency(invoiceHistoryCreateRequestDTO.getCurrency())
                .clientDisplayName(invoiceHistoryCreateRequestDTO.getClientDisplayName())
                .clientCompanyName(invoiceHistoryCreateRequestDTO.getClientCompanyName())
                .issueDate(invoiceHistoryCreateRequestDTO.getIssueDate())
                .dueDate(invoiceHistoryCreateRequestDTO.getDueDate())
                .totalWithoutVat(invoiceHistoryCreateRequestDTO.getTotalWithoutVat())
                .totalVat(invoiceHistoryCreateRequestDTO.getTotalVat())
                .totalWithVat(invoiceHistoryCreateRequestDTO.getTotalWithVat())
                .snapshotJson(invoiceHistoryCreateRequestDTO.getSnapshotJson())
                .performedByUsername(invoiceHistoryCreateRequestDTO.getPerformedByUsername())
                .build();
    }

    public InvoiceHistoryResponseDTO fromInvoiceHistoryRecordToResponseDTO(InvoiceHistoryRecord invoiceHistoryRecord) {
        if (invoiceHistoryRecord == null) {
            return null;
        }

        return InvoiceHistoryResponseDTO.builder()
                .id(invoiceHistoryRecord.getId())
                .invoiceId(invoiceHistoryRecord.getInvoiceId())
                .invoiceNumber(invoiceHistoryRecord.getInvoiceNumber())
                .invoiceType(invoiceHistoryRecord.getInvoiceType())
                .invoiceStatus(invoiceHistoryRecord.getInvoiceStatus())
                .revisionNumber(invoiceHistoryRecord.getRevisionNumber())
                .action(invoiceHistoryRecord.getAction())
                .currency(invoiceHistoryRecord.getCurrency())
                .clientDisplayName(invoiceHistoryRecord.getClientDisplayName())
                .clientCompanyName(invoiceHistoryRecord.getClientCompanyName())
                .issueDate(invoiceHistoryRecord.getIssueDate())
                .dueDate(invoiceHistoryRecord.getDueDate())
                .totalWithoutVat(invoiceHistoryRecord.getTotalWithoutVat())
                .totalVat(invoiceHistoryRecord.getTotalVat())
                .totalWithVat(invoiceHistoryRecord.getTotalWithVat())
                .snapshotJson(invoiceHistoryRecord.getSnapshotJson())
                .performedByUsername(invoiceHistoryRecord.getPerformedByUsername())
                .createdOn(invoiceHistoryRecord.getCreatedOn())
                .build();
    }
}
