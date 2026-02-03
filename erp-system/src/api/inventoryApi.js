/**
 * Inventory API
 * 
 * NOTE: Backend returns wrapped response { success, message, data }
 * We extract the actual data from response.data.data
 */

import axios from './axiosConfig';

export const inventoryApi = {
  // ============== PRODUCTS ==============
  
  getProducts: async () => {
    const response = await axios.get('/products');
    return response.data.data || response.data;
  },

  getProductById: async (id) => {
    const response = await axios.get(`/products/${id}`);
    return response.data.data || response.data;
  },

  getActiveProducts: async () => {
    const response = await axios.get('/products/active');
    return response.data.data || response.data;
  },

  getProductsByCategory: async (categoryId) => {
    const response = await axios.get(`/products/category/${categoryId}`);
    return response.data.data || response.data;
  },

  searchProducts: async (keyword) => {
    const response = await axios.get('/products/search', { params: { keyword } });
    return response.data.data || response.data;
  },

  getLowStockProducts: async () => {
    const response = await axios.get('/products/low-stock');
    return response.data.data || response.data;
  },

  createProduct: async (productData) => {
    const response = await axios.post('/products', productData);
    return response.data.data || response.data;
  },

  updateProduct: async (id, productData) => {
    const response = await axios.put(`/products/${id}`, productData);
    return response.data.data || response.data;
  },

  deleteProduct: async (id) => {
    const response = await axios.delete(`/products/${id}`);
    return response.data.data || response.data;
  },

  // ============== SUPPLIERS ==============
  
  getSuppliers: async () => {
    const response = await axios.get('/suppliers');
    return response.data.data || response.data;
  },

  createSupplier: async (supplierData) => {
    const response = await axios.post('/suppliers', supplierData);
    return response.data.data || response.data;
  },

  updateSupplier: async (id, supplierData) => {
    const response = await axios.put(`/suppliers/${id}`, supplierData);
    return response.data.data || response.data;
  },

  deleteSupplier: async (id) => {
    const response = await axios.delete(`/suppliers/${id}`);
    return response.data.data || response.data;
  },

  // ============== STOCK ==============
  
  getStock: async () => {
    const response = await axios.get('/stock');
    return response.data.data || response.data;
  },

  getStockById: async (id) => {
    const response = await axios.get(`/stock/${id}`);
    return response.data.data || response.data;
  },

  getStockByProduct: async (productId) => {
    const response = await axios.get(`/stock/product/${productId}`);
    return response.data.data || response.data;
  },

  getStockByWarehouse: async (warehouseId) => {
    const response = await axios.get(`/stock/warehouse/${warehouseId}`);
    return response.data.data || response.data;
  },

  getStockByProductAndWarehouse: async (productId, warehouseId) => {
    const response = await axios.get(`/stock/product/${productId}/warehouse/${warehouseId}`);
    return response.data.data || response.data;
  },

  getLowStockItems: async () => {
    const response = await axios.get('/stock/low-stock');
    return response.data.data || response.data;
  },

  // ============== WAREHOUSES ==============
  
  getWarehouses: async () => {
    const response = await axios.get('/warehouses');
    return response.data.data || response.data;
  },

  getWarehouseById: async (id) => {
    const response = await axios.get(`/warehouses/${id}`);
    return response.data.data || response.data;
  },

  getActiveWarehouses: async () => {
    const response = await axios.get('/warehouses/active');
    return response.data.data || response.data;
  },

  createWarehouse: async (warehouseData) => {
    const response = await axios.post('/warehouses', warehouseData);
    return response.data.data || response.data;
  },

  updateWarehouse: async (id, warehouseData) => {
    const response = await axios.put(`/warehouses/${id}`, warehouseData);
    return response.data.data || response.data;
  },

  deleteWarehouse: async (id) => {
    const response = await axios.delete(`/warehouses/${id}`);
    return response.data.data || response.data;
  },

  // ============== PURCHASE ORDERS ==============
  
  getPurchaseOrders: async () => {
    const response = await axios.get('/purchase-orders');
    return response.data.data || response.data;
  },

  getPurchaseOrderById: async (id) => {
    const response = await axios.get(`/purchase-orders/${id}`);
    return response.data.data || response.data;
  },

  getPurchaseOrderByNumber: async (poNumber) => {
    const response = await axios.get(`/purchase-orders/number/${poNumber}`);
    return response.data.data || response.data;
  },

  getPurchaseOrdersBySupplier: async (supplierId) => {
    const response = await axios.get(`/purchase-orders/supplier/${supplierId}`);
    return response.data.data || response.data;
  },

  getPurchaseOrdersByWarehouse: async (warehouseId) => {
    const response = await axios.get(`/purchase-orders/warehouse/${warehouseId}`);
    return response.data.data || response.data;
  },

  getPurchaseOrdersByStatus: async (status) => {
    const response = await axios.get(`/purchase-orders/status/${status}`);
    return response.data.data || response.data;
  },

  getPurchaseOrdersByDateRange: async (startDate, endDate) => {
    const response = await axios.get('/purchase-orders/date-range', {
      params: { startDate, endDate }
    });
    return response.data.data || response.data;
  },

  getRecentPurchaseOrders: async () => {
    const response = await axios.get('/purchase-orders/recent');
    return response.data.data || response.data;
  },

  createPurchaseOrder: async (orderData) => {
    const response = await axios.post('/purchase-orders', orderData);
    return response.data.data || response.data;
  },

  receivePurchaseOrder: async (id) => {
    const response = await axios.put(`/purchase-orders/${id}/receive`);
    return response.data.data || response.data;
  },

  approvePurchaseOrder: async (id) => {
    const response = await axios.put(`/purchase-orders/${id}/approve`);
    return response.data.data || response.data;
  },

  cancelPurchaseOrder: async (id) => {
    const response = await axios.put(`/purchase-orders/${id}/cancel`);
    return response.data.data || response.data;
  },

  deletePurchaseOrder: async (id) => {
    const response = await axios.delete(`/purchase-orders/${id}`);
    return response.data.data || response.data;
  },
};