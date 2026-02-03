/**
 * Sales Module API
 * 
 * NOTE: Backend returns wrapped response { success, message, data }
 * We extract the actual data from response.data.data
 */

import axios from './axiosConfig';

export const salesApi = {
  // ============== CUSTOMERS ==============
  
  getCustomers: async () => {
    const response = await axios.get('/customers');
    return response.data.data || response.data;
  },

  getCustomerById: async (id) => {
    const response = await axios.get(`/customers/${id}`);
    return response.data.data || response.data;
  },

  getActiveCustomers: async () => {
    const response = await axios.get('/customers/active');
    return response.data.data || response.data;
  },

  searchCustomers: async (keyword) => {
    const response = await axios.get('/customers/search', { params: { keyword } });
    return response.data.data || response.data;
  },

  getCustomersExceedingCredit: async () => {
    const response = await axios.get('/customers/exceeding-credit');
    return response.data.data || response.data;
  },

  createCustomer: async (customerData) => {
    const response = await axios.post('/customers', customerData);
    return response.data.data || response.data;
  },

  updateCustomer: async (id, customerData) => {
    const response = await axios.put(`/customers/${id}`, customerData);
    return response.data.data || response.data;
  },

  deleteCustomer: async (id) => {
    const response = await axios.delete(`/customers/${id}`);
    return response.data.data || response.data;
  },

  // ============== SALES ORDERS ==============
  
  getSalesOrders: async () => {
    const response = await axios.get('/sales-orders');
    return response.data.data || response.data;
  },

  getSalesOrderById: async (id) => {
    const response = await axios.get(`/sales-orders/${id}`);
    return response.data.data || response.data;
  },

  getSalesOrdersByStatus: async (status) => {
    const response = await axios.get(`/sales-orders/status/${status}`);
    return response.data.data || response.data;
  },

  getSalesOrdersByCustomer: async (customerId) => {
    const response = await axios.get(`/sales-orders/customer/${customerId}`);
    return response.data.data || response.data;
  },

  getSalesOrdersByDateRange: async (startDate, endDate) => {
    const response = await axios.get('/sales-orders/date-range', {
      params: { startDate, endDate }
    });
    return response.data.data || response.data;
  },

  getRecentSalesOrders: async () => {
    const response = await axios.get('/sales-orders/recent');
    return response.data.data || response.data;
  },

  createSalesOrder: async (orderData) => {
    const response = await axios.post('/sales-orders', orderData);
    return response.data.data || response.data;
  },

  confirmSalesOrder: async (id) => {
    const response = await axios.put(`/sales-orders/${id}/confirm`);
    return response.data.data || response.data;
  },

  shipSalesOrder: async (id) => {
    const response = await axios.put(`/sales-orders/${id}/ship`);
    return response.data.data || response.data;
  },

  deliverSalesOrder: async (id) => {
    const response = await axios.put(`/sales-orders/${id}/deliver`);
    return response.data.data || response.data;
  },

  cancelSalesOrder: async (id) => {
    const response = await axios.put(`/sales-orders/${id}/cancel`);
    return response.data.data || response.data;
  },

  deleteSalesOrder: async (id) => {
    const response = await axios.delete(`/sales-orders/${id}`);
    return response.data.data || response.data;
  },

  // ============== INVOICES ==============
  
  getInvoices: async () => {
    const response = await axios.get('/invoices');
    return response.data.data || response.data;
  },

  getInvoiceById: async (id) => {
    const response = await axios.get(`/invoices/${id}`);
    return response.data.data || response.data;
  },

  getInvoicesByStatus: async (status) => {
    const response = await axios.get(`/invoices/status/${status}`);
    return response.data.data || response.data;
  },

  getInvoicesByCustomer: async (customerId) => {
    const response = await axios.get(`/invoices/customer/${customerId}`);
    return response.data.data || response.data;
  },

  getInvoicesByDateRange: async (startDate, endDate) => {
    const response = await axios.get('/invoices/date-range', {
      params: { startDate, endDate }
    });
    return response.data.data || response.data;
  },

  getOverdueInvoices: async () => {
    const response = await axios.get('/invoices/overdue');
    return response.data.data || response.data;
  },

  createInvoice: async (invoiceData) => {
    const response = await axios.post('/invoices', invoiceData);
    return response.data.data || response.data;
  },

  createInvoiceFromSalesOrder: async (salesOrderId, invoiceData) => {
    const response = await axios.post(`/invoices/from-sales-order/${salesOrderId}`, invoiceData);
    return response.data.data || response.data;
  },

  updateInvoice: async (id, invoiceData) => {
    const response = await axios.put(`/invoices/${id}`, invoiceData);
    return response.data.data || response.data;
  },

  deleteInvoice: async (id) => {
    const response = await axios.delete(`/invoices/${id}`);
    return response.data.data || response.data;
  },

  // ============== PAYMENTS ==============
  
  getPayments: async () => {
    const response = await axios.get('/payments');
    return response.data.data || response.data;
  },

  createPayment: async (paymentData) => {
    const response = await axios.post('/payments', paymentData);
    return response.data.data || response.data;
  },
};