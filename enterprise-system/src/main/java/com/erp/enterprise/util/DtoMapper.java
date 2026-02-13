package com.erp.enterprise.util;

import com.erp.enterprise.dto.finance.*;
import com.erp.enterprise.dto.hr.*;
import com.erp.enterprise.entity.finance.*;
import com.erp.enterprise.dto.inventory.*;
import com.erp.enterprise.entity.hr.*;
import com.erp.enterprise.entity.inventory.*;
import com.erp.enterprise.dto.sales.*;
import com.erp.enterprise.entity.sales.*;


import org.springframework.lang.NonNull;
import java.util.List;
import java.util.stream.Collectors;

// Utility class to map between Entities and DTOs
// Business Logic: Centralized conversion logic
public class DtoMapper {

    // ==================== Department Mapping ====================

    // Convert Department Entity to DTO
    @NonNull
    public static DepartmentDTO toDepartmentDTO(@NonNull Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setCreatedAt(department.getCreatedAt());
        dto.setUpdatedAt(department.getUpdatedAt());

        // Set manager info if exists
        if (department.getManager() != null) {
            dto.setManagerId(department.getManager().getId());
            dto.setManagerName(department.getManager().getFullName());
        }

        return dto;
    }

    // Convert DepartmentDTO to Entity
    @NonNull
    public static Department toDepartmentEntity(@NonNull DepartmentDTO dto) {
        Department department = new Department();
        department.setId(dto.getId());
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());

        // Manager is set separately in service layer (needs to fetch from DB)
        return department;
    }

    // ==================== Employee Mapping ====================

    // Convert Employee Entity to DTO
    @NonNull
    public static EmployeeDTO toEmployeeDTO(@NonNull Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setDesignation(employee.getDesignation());
        dto.setDateOfJoining(employee.getDateOfJoining());
        dto.setSalary(employee.getSalary());
        dto.setStatus(employee.getStatus());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());

        // Set department info if exists
        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getId());
            dto.setDepartmentName(employee.getDepartment().getName());
        }

        return dto;
    }

    // Convert EmployeeDTO to Entity (for updates)
    public static Employee toEmployeeEntity(EmployeeDTO dto) {
        if (dto == null) {
            return null;
        }

        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setEmployeeCode(dto.getEmployeeCode());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setDesignation(dto.getDesignation());
        employee.setDateOfJoining(dto.getDateOfJoining());
        employee.setSalary(dto.getSalary());
        employee.setStatus(dto.getStatus());

        // Department is set separately in service layer
        return employee;
    }

    // ==================== Employee Response Mapping (Secure) ====================

    /**
     * Convert Employee Entity to List Response DTO (excludes sensitive data)
     * Used for: GET /api/employees (list view)
     * Security: No salary, no phone, no personal details
     */
    @NonNull
    public static EmployeeListResponse toEmployeeListResponse(@NonNull Employee employee) {
        EmployeeListResponse response = new EmployeeListResponse();
        response.setId(employee.getId());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setDesignation(employee.getDesignation());
        response.setStatus(employee.getStatus());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());

        // Set department info if exists
        if (employee.getDepartment() != null) {
            response.setDepartmentId(employee.getDepartment().getId());
            response.setDepartmentName(employee.getDepartment().getName());
        }

        return response;
    }

    /**
     * Convert Employee Entity to Detail Response DTO (includes all data)
     * Used for: GET /api/employees/{id} (detail view)
     * Security: Includes sensitive data - should be access-controlled
     */
    @NonNull
    public static EmployeeDetailResponse toEmployeeDetailResponse(@NonNull Employee employee) {
        EmployeeDetailResponse response = new EmployeeDetailResponse();
        response.setId(employee.getId());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        response.setDesignation(employee.getDesignation());
        response.setDateOfJoining(employee.getDateOfJoining());
        response.setSalary(employee.getSalary());
        response.setStatus(employee.getStatus());
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());

        // Set department info if exists
        if (employee.getDepartment() != null) {
            response.setDepartmentId(employee.getDepartment().getId());
            response.setDepartmentName(employee.getDepartment().getName());
        }

        return response;
    }

    // ==================== Attendance Mapping ====================

    @NonNull
    public static AttendanceDTO toAttendanceDTO(@NonNull Attendance attendance) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(attendance.getId());
        dto.setDate(attendance.getDate());
        dto.setClockIn(attendance.getClockIn());
        dto.setClockOut(attendance.getClockOut());
        dto.setStatus(attendance.getStatus());
        dto.setRemarks(attendance.getRemarks());
        dto.setWorkHours(attendance.getWorkHours());
        dto.setCreatedAt(attendance.getCreatedAt());
        dto.setUpdatedAt(attendance.getUpdatedAt());

        if (attendance.getEmployee() != null) {
            dto.setEmployeeId(attendance.getEmployee().getId());
            dto.setEmployeeCode(attendance.getEmployee().getEmployeeCode());
            dto.setEmployeeName(attendance.getEmployee().getFullName());
        }

        return dto;
    }

    // ==================== Leave Type Mapping ====================

    public static LeaveTypeDTO toLeaveTypeDTO(LeaveType leaveType) {
        if (leaveType == null) {
            return null;
        }

        LeaveTypeDTO dto = new LeaveTypeDTO();
        dto.setId(leaveType.getId());
        dto.setName(leaveType.getName());
        dto.setDescription(leaveType.getDescription());
        dto.setDaysAllowed(leaveType.getDaysAllowed());

        return dto;
    }

    public static LeaveType toLeaveTypeEntity(LeaveTypeDTO dto) {
        if (dto == null) {
            return null;
        }

        LeaveType leaveType = new LeaveType();
        leaveType.setId(dto.getId());
        leaveType.setName(dto.getName());
        leaveType.setDescription(dto.getDescription());
        leaveType.setDaysAllowed(dto.getDaysAllowed());

        return leaveType;
    }

    // ==================== Leave Request Mapping ====================

    @NonNull
    public static LeaveRequestDTO toLeaveRequestDTO(@NonNull LeaveRequest leaveRequest) {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(leaveRequest.getId());
        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setDaysCount(leaveRequest.getDaysCount());
        dto.setReason(leaveRequest.getReason());
        dto.setStatus(leaveRequest.getStatus());
        dto.setApprovedAt(leaveRequest.getApprovedAt());
        dto.setCreatedAt(leaveRequest.getCreatedAt());
        dto.setUpdatedAt(leaveRequest.getUpdatedAt());

        if (leaveRequest.getEmployee() != null) {
            dto.setEmployeeId(leaveRequest.getEmployee().getId());
            dto.setEmployeeCode(leaveRequest.getEmployee().getEmployeeCode());
            dto.setEmployeeName(leaveRequest.getEmployee().getFullName());
        }

        if (leaveRequest.getLeaveType() != null) {
            dto.setLeaveTypeId(leaveRequest.getLeaveType().getId());
            dto.setLeaveTypeName(leaveRequest.getLeaveType().getName());
        }

        if (leaveRequest.getApprovedBy() != null) {
            dto.setApprovedById(leaveRequest.getApprovedBy().getId());
            dto.setApprovedByName(leaveRequest.getApprovedBy().getFullName());
        }
        return dto;
    }
    // Add these methods to the existing DtoMapper class

// ==================== Account Mapping ====================

    @NonNull
    public static AccountDTO toAccountDTO(@NonNull Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountCode(account.getAccountCode());
        dto.setAccountName(account.getAccountName());
        dto.setAccountType(account.getAccountType());
        dto.setBalance(account.getBalance());
        dto.setIsActive(account.getIsActive());
        dto.setCreatedAt(account.getCreatedAt());
        dto.setUpdatedAt(account.getUpdatedAt());

        if (account.getParentAccount() != null) {
            dto.setParentAccountId(account.getParentAccount().getId());
            dto.setParentAccountName(account.getParentAccount().getAccountName());
        }

        return dto;
    }

    public static Account toAccountEntity(@NonNull AccountDTO dto) {
        Account account = new Account();
        account.setId(dto.getId());
        account.setAccountCode(dto.getAccountCode());
        account.setAccountName(dto.getAccountName());
        account.setAccountType(dto.getAccountType());
        account.setBalance(dto.getBalance());
        account.setIsActive(dto.getIsActive());

        return account;
    }

// ==================== Transaction Entry Mapping ====================

    @NonNull
    public static TransactionEntryDTO toTransactionEntryDTO(@NonNull TransactionEntry entry) {
        TransactionEntryDTO dto = new TransactionEntryDTO();
        dto.setId(entry.getId());
        dto.setEntryType(entry.getEntryType());
        dto.setAmount(entry.getAmount());
        dto.setDescription(entry.getDescription());

        if (entry.getAccount() != null) {
            dto.setAccountId(entry.getAccount().getId());
            dto.setAccountCode(entry.getAccount().getAccountCode());
            dto.setAccountName(entry.getAccount().getAccountName());
        }

        return dto;
    }

// ==================== Transaction Mapping ====================

    @NonNull
    public static TransactionDTO toTransactionDTO(@NonNull Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setTransactionCode(transaction.getTransactionCode());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setDescription(transaction.getDescription());
        dto.setReferenceNumber(transaction.getReferenceNumber());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());

        if (transaction.getCreatedBy() != null) {
            dto.setCreatedById(transaction.getCreatedBy().getId());
            dto.setCreatedByName(transaction.getCreatedBy().getUsername());
        }

        // Map entries
        if (transaction.getEntries() != null) {
            List<TransactionEntryDTO> entryDTOs = transaction.getEntries().stream()
                    .map(DtoMapper::toTransactionEntryDTO)
                    .collect(Collectors.toList());
            dto.setEntries(entryDTOs);
        }

        return dto;
    }

// ==================== Expense Mapping ====================

    @NonNull
    public static ExpenseDTO toExpenseDTO(@NonNull Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setExpenseCode(expense.getExpenseCode());
        dto.setCategory(expense.getCategory());
        dto.setAmount(expense.getAmount());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setVendorName(expense.getVendorName());
        dto.setDescription(expense.getDescription());
        dto.setStatus(expense.getStatus());
        dto.setCreatedAt(expense.getCreatedAt());
        dto.setUpdatedAt(expense.getUpdatedAt());

        if (expense.getEmployee() != null) {
            dto.setEmployeeId(expense.getEmployee().getId());
            dto.setEmployeeCode(expense.getEmployee().getEmployeeCode());
            dto.setEmployeeName(expense.getEmployee().getFullName());
        }

        return dto;
    }

    // ==================== Category Mapping ====================

    public static CategoryDTO toCategoryDTO(Category category) {
        if (category == null) {
            return null;
        }

        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        if (category.getParentCategory() != null) {
            dto.setParentCategoryId(category.getParentCategory().getId());
            dto.setParentCategoryName(category.getParentCategory().getName());
        }

        return dto;
    }

    public static Category toCategoryEntity(CategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        return category;
    }

// ==================== Supplier Mapping ====================

    public static SupplierDTO toSupplierDTO(Supplier supplier) {
        if (supplier == null) {
            return null;
        }

        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setSupplierCode(supplier.getSupplierCode());
        dto.setName(supplier.getName());
        dto.setContactPerson(supplier.getContactPerson());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setAddress(supplier.getAddress());
        dto.setCity(supplier.getCity());
        dto.setCountry(supplier.getCountry());
        dto.setIsActive(supplier.getIsActive());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());

        return dto;
    }

    public static Supplier toSupplierEntity(SupplierDTO dto) {
        if (dto == null) {
            return null;
        }

        Supplier supplier = new Supplier();
        supplier.setId(dto.getId());
        supplier.setSupplierCode(dto.getSupplierCode());
        supplier.setName(dto.getName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());
        supplier.setAddress(dto.getAddress());
        supplier.setCity(dto.getCity());
        supplier.setCountry(dto.getCountry());
        supplier.setIsActive(dto.getIsActive());

        return supplier;
    }

// ==================== Warehouse Mapping ====================

    public static WarehouseDTO toWarehouseDTO(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }

        WarehouseDTO dto = new WarehouseDTO();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setLocation(warehouse.getLocation());
        dto.setIsActive(warehouse.getIsActive());
        dto.setCreatedAt(warehouse.getCreatedAt());
        dto.setUpdatedAt(warehouse.getUpdatedAt());

        if (warehouse.getManager() != null) {
            dto.setManagerId(warehouse.getManager().getId());
            dto.setManagerName(warehouse.getManager().getFullName());
        }

        return dto;
    }

    public static Warehouse toWarehouseEntity(WarehouseDTO dto) {
        if (dto == null) {
            return null;
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setId(dto.getId());
        warehouse.setName(dto.getName());
        warehouse.setLocation(dto.getLocation());
        warehouse.setIsActive(dto.getIsActive());

        return warehouse;
    }

// ==================== Product Mapping ====================

    public static ProductDTO toProductDTO(Product product) {
        if (product == null) {
            return null;
        }

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setProductCode(product.getProductCode());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setUnit(product.getUnit());
        dto.setUnitPrice(product.getUnitPrice());
        dto.setReorderLevel(product.getReorderLevel());
        dto.setIsActive(product.getIsActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        return dto;
    }

    public static Product toProductEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        Product product = new Product();
        product.setId(dto.getId());
        product.setProductCode(dto.getProductCode());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setUnit(dto.getUnit());
        product.setUnitPrice(dto.getUnitPrice());
        product.setReorderLevel(dto.getReorderLevel());
        product.setIsActive(dto.getIsActive());

        return product;
    }

// ==================== Stock Mapping ====================

    public static StockDTO toStockDTO(Stock stock) {
        if (stock == null) {
            return null;
        }

        StockDTO dto = new StockDTO();
        dto.setId(stock.getId());
        dto.setQuantity(stock.getQuantity());
        dto.setLastUpdated(stock.getLastUpdated());

        if (stock.getProduct() != null) {
            dto.setProductId(stock.getProduct().getId());
            dto.setProductCode(stock.getProduct().getProductCode());
            dto.setProductName(stock.getProduct().getName());
            dto.setUnit(stock.getProduct().getUnit());
            dto.setUnitPrice(stock.getProduct().getUnitPrice());
            dto.setReorderLevel(stock.getProduct().getReorderLevel());
        }

        if (stock.getWarehouse() != null) {
            dto.setWarehouseId(stock.getWarehouse().getId());
            dto.setWarehouseName(stock.getWarehouse().getName());
        }

        return dto;
    }

// ==================== Purchase Order Item Mapping ====================

    public static PurchaseOrderItemDTO toPurchaseOrderItemDTO(PurchaseOrderItem item) {
        if (item == null) {
            return null;
        }

        PurchaseOrderItemDTO dto = new PurchaseOrderItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductCode(item.getProduct().getProductCode());
            dto.setProductName(item.getProduct().getName());
        }

        return dto;
    }

// ==================== Purchase Order Mapping ====================

    public static PurchaseOrderDTO toPurchaseOrderDTO(PurchaseOrder purchaseOrder) {
        if (purchaseOrder == null) {
            return null;
        }

        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(purchaseOrder.getId());
        dto.setPoNumber(purchaseOrder.getPoNumber());
        dto.setOrderDate(purchaseOrder.getOrderDate());
        dto.setExpectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate());
        dto.setTotalAmount(purchaseOrder.getTotalAmount());
        dto.setStatus(purchaseOrder.getStatus());
        dto.setCreatedAt(purchaseOrder.getCreatedAt());
        dto.setUpdatedAt(purchaseOrder.getUpdatedAt());

        if (purchaseOrder.getSupplier() != null) {
            dto.setSupplierId(purchaseOrder.getSupplier().getId());
            dto.setSupplierName(purchaseOrder.getSupplier().getName());
        }

        if (purchaseOrder.getWarehouse() != null) {
            dto.setWarehouseId(purchaseOrder.getWarehouse().getId());
            dto.setWarehouseName(purchaseOrder.getWarehouse().getName());
        }

        if (purchaseOrder.getCreatedBy() != null) {
            dto.setCreatedById(purchaseOrder.getCreatedBy().getId());
            dto.setCreatedByName(purchaseOrder.getCreatedBy().getUsername());
        }

        // Map items
        if (purchaseOrder.getItems() != null) {
            List<PurchaseOrderItemDTO> itemDTOs = purchaseOrder.getItems().stream()
                    .map(DtoMapper::toPurchaseOrderItemDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }

// ==================== Stock Movement Mapping ====================

    public static StockMovementDTO toStockMovementDTO(StockMovement movement) {
        if (movement == null) {
            return null;
        }

        StockMovementDTO dto = new StockMovementDTO();
        dto.setId(movement.getId());
        dto.setMovementType(movement.getMovementType());
        dto.setQuantity(movement.getQuantity());
        dto.setReferenceType(movement.getReferenceType());
        dto.setReferenceId(movement.getReferenceId());
        dto.setRemarks(movement.getRemarks());
        dto.setCreatedAt(movement.getCreatedAt());
        dto.setUpdatedAt(movement.getUpdatedAt());

        if (movement.getProduct() != null) {
            dto.setProductId(movement.getProduct().getId());
            dto.setProductCode(movement.getProduct().getProductCode());
            dto.setProductName(movement.getProduct().getName());
        }

        if (movement.getWarehouse() != null) {
            dto.setWarehouseId(movement.getWarehouse().getId());
            dto.setWarehouseName(movement.getWarehouse().getName());
        }

        if (movement.getCreatedBy() != null) {
            dto.setCreatedById(movement.getCreatedBy().getId());
            dto.setCreatedByName(movement.getCreatedBy().getUsername());
        }

        return dto;
    }

// Add these methods to the existing DtoMapper class

// ==================== Customer Mapping ====================

    /**
     * Converts Customer Entity to DTO
     *
     * Explanation:
     * - Maps all customer fields including credit management
     * - Available credit calculated in DTO getter method
     */
    public static CustomerDTO toCustomerDTO(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setCustomerCode(customer.getCustomerCode());
        dto.setName(customer.getName());
        dto.setContactPerson(customer.getContactPerson());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setCountry(customer.getCountry());
        dto.setCreditLimit(customer.getCreditLimit());
        dto.setOutstandingBalance(customer.getOutstandingBalance());
        dto.setIsActive(customer.getIsActive());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());

        return dto;
    }

    public static Customer toCustomerEntity(CustomerDTO dto) {
        if (dto == null) {
            return null;
        }

        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setCustomerCode(dto.getCustomerCode());
        customer.setName(dto.getName());
        customer.setContactPerson(dto.getContactPerson());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        customer.setCountry(dto.getCountry());
        customer.setCreditLimit(dto.getCreditLimit());
        customer.setOutstandingBalance(dto.getOutstandingBalance());
        customer.setIsActive(dto.getIsActive());

        return customer;
    }

// ==================== Sales Order Item Mapping ====================

    /**
     * Converts SalesOrderItem Entity to DTO
     *
     * Explanation:
     * - Maps line item with product details
     * - Includes product code/name for display
     */
    public static SalesOrderItemDTO toSalesOrderItemDTO(SalesOrderItem item) {
        if (item == null) {
            return null;
        }

        SalesOrderItemDTO dto = new SalesOrderItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
            dto.setProductCode(item.getProduct().getProductCode());
            dto.setProductName(item.getProduct().getName());
        }

        return dto;
    }

// ==================== Sales Order Mapping ====================

    /**
     * Converts SalesOrder Entity to DTO
     *
     * Explanation:
     * - Maps complete order with all items
     * - Includes customer and warehouse names for display
     * - Converts list of items to DTOs
     */
    public static SalesOrderDTO toSalesOrderDTO(SalesOrder salesOrder) {
        if (salesOrder == null) {
            return null;
        }

        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setId(salesOrder.getId());
        dto.setOrderNumber(salesOrder.getOrderNumber());
        dto.setOrderDate(salesOrder.getOrderDate());
        dto.setDeliveryDate(salesOrder.getDeliveryDate());
        dto.setSubtotal(salesOrder.getSubtotal());
        dto.setTaxAmount(salesOrder.getTaxAmount());
        dto.setDiscountAmount(salesOrder.getDiscountAmount());
        dto.setTotalAmount(salesOrder.getTotalAmount());
        dto.setStatus(salesOrder.getStatus());
        dto.setCreatedAt(salesOrder.getCreatedAt());
        dto.setUpdatedAt(salesOrder.getUpdatedAt());

        if (salesOrder.getCustomer() != null) {
            dto.setCustomerId(salesOrder.getCustomer().getId());
            dto.setCustomerName(salesOrder.getCustomer().getName());
        }

        if (salesOrder.getWarehouse() != null) {
            dto.setWarehouseId(salesOrder.getWarehouse().getId());
            dto.setWarehouseName(salesOrder.getWarehouse().getName());
        }

        if (salesOrder.getCreatedBy() != null) {
            dto.setCreatedById(salesOrder.getCreatedBy().getId());
            dto.setCreatedByName(salesOrder.getCreatedBy().getUsername());
        }

        // Map items
        if (salesOrder.getItems() != null) {
            List<SalesOrderItemDTO> itemDTOs = salesOrder.getItems().stream()
                    .map(DtoMapper::toSalesOrderItemDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }

// ==================== Invoice Mapping ====================

    /**
     * Converts Invoice Entity to DTO
     *
     * Explanation:
     * - Maps invoice with payment tracking
     * - Calculates remaining amount
     * - Links to customer and optionally sales order
     */
    public static InvoiceDTO toInvoiceDTO(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setStatus(invoice.getStatus());
        dto.setRemainingAmount(invoice.getRemainingAmount());
        dto.setCreatedAt(invoice.getCreatedAt());
        dto.setUpdatedAt(invoice.getUpdatedAt());

        if (invoice.getSalesOrder() != null) {
            dto.setSalesOrderId(invoice.getSalesOrder().getId());
            dto.setSalesOrderNumber(invoice.getSalesOrder().getOrderNumber());
        }

        if (invoice.getCustomer() != null) {
            dto.setCustomerId(invoice.getCustomer().getId());
            dto.setCustomerName(invoice.getCustomer().getName());
        }

        return dto;
    }

// ==================== Payment Mapping ====================

    /**
     * Converts Payment Entity to DTO
     *
     * Explanation:
     * - Maps payment receipt with invoice link
     * - Includes invoice number for display
     * - Tracks payment method and reference
     */
    public static PaymentDTO toPaymentDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setPaymentNumber(payment.getPaymentNumber());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setReferenceNumber(payment.getReferenceNumber());
        dto.setRemarks(payment.getRemarks());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());

        if (payment.getInvoice() != null) {
            dto.setInvoiceId(payment.getInvoice().getId());
            dto.setInvoiceNumber(payment.getInvoice().getInvoiceNumber());
        }

        if (payment.getCreatedBy() != null) {
            dto.setCreatedById(payment.getCreatedBy().getId());
            dto.setCreatedByName(payment.getCreatedBy().getUsername());
        }

        return dto;
    }

// ==================== Customer Response DTO Mapping ====================

    /**
     * Converts Customer Entity to ListResponse DTO
     * 
     * Purpose: For list views (GET /api/customers)
     * Security: Excludes sensitive fields - creditLimit, outstandingBalance
     */
    public static CustomerListResponse toCustomerListResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerListResponse response = new CustomerListResponse();
        response.setId(customer.getId());
        response.setCustomerCode(customer.getCustomerCode());
        response.setName(customer.getName());
        response.setContactPerson(customer.getContactPerson());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setCity(customer.getCity());
        response.setIsActive(customer.getIsActive());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());

        // Note: creditLimit, outstandingBalance, address, country are NOT mapped

        return response;
    }

    /**
     * Converts Customer Entity to DetailResponse DTO
     * 
     * Purpose: For detail views (GET /api/customers/{id})
     * Security: Includes all fields - for authorized users only
     */
    public static CustomerDetailResponse toCustomerDetailResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerDetailResponse response = new CustomerDetailResponse();
        response.setId(customer.getId());
        response.setCustomerCode(customer.getCustomerCode());
        response.setName(customer.getName());
        response.setContactPerson(customer.getContactPerson());
        response.setEmail(customer.getEmail());
        response.setPhone(customer.getPhone());
        response.setAddress(customer.getAddress());
        response.setCity(customer.getCity());
        response.setCountry(customer.getCountry());
        response.setCreditLimit(customer.getCreditLimit());
        response.setOutstandingBalance(customer.getOutstandingBalance());
        response.setIsActive(customer.getIsActive());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());

        return response;
    }
}