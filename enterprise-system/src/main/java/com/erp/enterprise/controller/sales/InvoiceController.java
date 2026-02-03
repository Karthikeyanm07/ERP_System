package com.erp.enterprise.controller.sales;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.sales.InvoiceCreateRequest;
import com.erp.enterprise.dto.sales.InvoiceDTO;
import com.erp.enterprise.service.sales.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Invoice Controller
 *
 * Base URL: /api/invoices
 *
 * Available endpoints:
 * - GET    /api/invoices                         -> Get all invoices
 * - GET    /api/invoices/{id}                    -> Get by ID
 * - GET    /api/invoices/number/{number}         -> Get by invoice number
 * - GET    /api/invoices/customer/{customerId}   -> Get by customer
 * - GET    /api/invoices/status/{status}         -> Get by status
 * - GET    /api/invoices/date-range              -> Get by date range
 * - GET    /api/invoices/overdue                 -> Get overdue invoices
 * - POST   /api/invoices                         -> Create standalone invoice
 * - POST   /api/invoices/from-sales-order/{salesOrderId} -> Create invoice from sales order
 * - PUT    /api/invoices/{id}                    -> Update invoice
 * - DELETE /api/invoices/{id}                    -> Delete invoice
 */
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getAllInvoices() {
        List<InvoiceDTO> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(
                ApiResponse.success("Invoices retrieved successfully", invoices)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getInvoiceById(@PathVariable Long id) {
        InvoiceDTO invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Invoice retrieved successfully", invoice)
        );
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> getInvoiceByNumber(@PathVariable String number) {
        InvoiceDTO invoice = invoiceService.getInvoiceByNumber(number);
        return ResponseEntity.ok(
                ApiResponse.success("Invoice retrieved successfully", invoice)
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getInvoicesByCustomer(
            @PathVariable Long customerId) {

        List<InvoiceDTO> invoices = invoiceService.getInvoicesByCustomer(customerId);
        return ResponseEntity.ok(
                ApiResponse.success("Customer invoices retrieved successfully", invoices)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getInvoicesByStatus(
            @PathVariable String status) {

        List<InvoiceDTO> invoices = invoiceService.getInvoicesByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success("Invoices by status retrieved successfully", invoices)
        );
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<InvoiceDTO> invoices = invoiceService.getInvoicesByDateRange(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success("Invoices in date range retrieved successfully", invoices)
        );
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<InvoiceDTO>>> getOverdueInvoices() {
        List<InvoiceDTO> invoices = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(
                ApiResponse.success("Overdue invoices retrieved successfully", invoices)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceDTO>> createInvoice(
            @Valid @RequestBody InvoiceCreateRequest request) {

        InvoiceDTO created = invoiceService.createInvoice(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created successfully", created));
    }

    @PostMapping("/from-sales-order/{salesOrderId}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> createInvoiceFromSalesOrder(
            @PathVariable Long salesOrderId,
            @Valid @RequestBody InvoiceCreateRequest request) {

        InvoiceDTO created = invoiceService.createInvoiceFromSalesOrder(salesOrderId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created from sales order successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceDTO>> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceDTO invoiceDTO) {

        InvoiceDTO updated = invoiceService.updateInvoice(id, invoiceDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Invoice updated successfully", updated)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(
                ApiResponse.success("Invoice deleted successfully", null)
        );
    }
}