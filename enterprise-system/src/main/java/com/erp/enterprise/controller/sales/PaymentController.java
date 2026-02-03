package com.erp.enterprise.controller.sales;

import com.erp.enterprise.dto.ApiResponse;
import com.erp.enterprise.dto.sales.PaymentCreateRequest;
import com.erp.enterprise.dto.sales.PaymentDTO;
import com.erp.enterprise.service.sales.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Payment Controller
 *
 * Base URL: /api/payments
 *
 * Available endpoints:
 * - GET    /api/payments                       -> Get all payments
 * - GET    /api/payments/{id}                  -> Get by ID
 * - GET    /api/payments/number/{number}       -> Get by payment number
 * - GET    /api/payments/invoice/{invoiceId}   -> Get by invoice
 * - GET    /api/payments/date-range            -> Get by date range
 * - GET    /api/payments/method/{method}       -> Get by payment method
 * - POST   /api/payments                       -> Create payment
 * - DELETE /api/payments/{id}                  -> Delete payment
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getAllPayments() {
        List<PaymentDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(
                ApiResponse.success("Payments retrieved successfully", payments)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentById(@PathVariable Long id) {
        PaymentDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Payment retrieved successfully", payment)
        );
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<ApiResponse<PaymentDTO>> getPaymentByNumber(@PathVariable String number) {
        PaymentDTO payment = paymentService.getPaymentByNumber(number);
        return ResponseEntity.ok(
                ApiResponse.success("Payment retrieved successfully", payment)
        );
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByInvoice(
            @PathVariable Long invoiceId) {

        List<PaymentDTO> payments = paymentService.getPaymentsByInvoice(invoiceId);
        return ResponseEntity.ok(
                ApiResponse.success("Invoice payments retrieved successfully", payments)
        );
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<PaymentDTO> payments = paymentService.getPaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(
                ApiResponse.success("Payments in date range retrieved successfully", payments)
        );
    }

    @GetMapping("/method/{method}")
    public ResponseEntity<ApiResponse<List<PaymentDTO>>> getPaymentsByMethod(
            @PathVariable String method) {

        List<PaymentDTO> payments = paymentService.getPaymentsByMethod(method);
        return ResponseEntity.ok(
                ApiResponse.success("Payments by method retrieved successfully", payments)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDTO>> createPayment(
            @Valid @RequestBody PaymentCreateRequest request) {

        PaymentDTO created = paymentService.createPayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment recorded successfully. Invoice updated.", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(
                ApiResponse.success("Payment deleted successfully. Invoice updated.", null)
        );
    }

}