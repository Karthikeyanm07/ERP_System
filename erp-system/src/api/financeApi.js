/**
 * Finance Module API
 * 
 * NOTE: Backend returns wrapped response { success, message, data }
 * We extract the actual data from response.data.data
 */

import axios from './axiosConfig';

export const financeApi = {
  // ============== ACCOUNTS ==============
  
  getAccounts: async () => {
    const response = await axios.get('/accounts');
    return response.data.data || response.data;
  },

  getAccountById: async (id) => {
    const response = await axios.get(`/accounts/${id}`);
    return response.data.data || response.data;
  },

  getAccountsByType: async (type) => {
    const response = await axios.get(`/accounts/type/${type}`);
    return response.data.data || response.data;
  },

  getActiveAccounts: async () => {
    const response = await axios.get('/accounts/active');
    return response.data.data || response.data;
  },

  searchAccounts: async (keyword) => {
    const response = await axios.get('/accounts/search', { params: { keyword } });
    return response.data.data || response.data;
  },

  createAccount: async (accountData) => {
    const response = await axios.post('/accounts', accountData);
    return response.data.data || response.data;
  },

  updateAccount: async (id, accountData) => {
    const response = await axios.put(`/accounts/${id}`, accountData);
    return response.data.data || response.data;
  },

  deleteAccount: async (id) => {
    const response = await axios.delete(`/accounts/${id}`);
    return response.data.data || response.data;
  },

  // ============== TRANSACTIONS ==============
  
  getTransactions: async () => {
    const response = await axios.get('/transactions');
    return response.data.data || response.data;
  },

  getRecentTransactions: async () => {
    const response = await axios.get('/transactions/recent');
    return response.data.data || response.data;
  },

  getTransactionsByDateRange: async (startDate, endDate) => {
    const response = await axios.get('/transactions/date-range', {
      params: { startDate, endDate }
    });
    return response.data.data || response.data;
  },

  getTransactionsByDate: async (date) => {
    const response = await axios.get(`/transactions/date/${date}`);
    return response.data.data || response.data;
  },

  createTransaction: async (transactionData) => {
    const response = await axios.post('/transactions', transactionData);
    return response.data.data || response.data;
  },

  deleteTransaction: async (id) => {
    const response = await axios.delete(`/transactions/${id}`);
    return response.data.data || response.data;
  },

  // ============== EXPENSES ==============
  
  getExpenses: async () => {
    const response = await axios.get('/expenses');
    return response.data.data || response.data;
  },

  getExpensesByStatus: async (status) => {
    const response = await axios.get(`/expenses/status/${status}`);
    return response.data.data || response.data;
  },

  getExpensesByCategory: async (category) => {
    const response = await axios.get(`/expenses/category/${category}`);
    return response.data.data || response.data;
  },

  getExpensesByDateRange: async (startDate, endDate) => {
    const response = await axios.get('/expenses/date-range', {
      params: { startDate, endDate }
    });
    return response.data.data || response.data;
  },

  createExpense: async (expenseData) => {
    const response = await axios.post('/expenses', expenseData);
    return response.data.data || response.data;
  },

  updateExpense: async (id, expenseData) => {
    const response = await axios.put(`/expenses/${id}`, expenseData);
    return response.data.data || response.data;
  },

  approveExpense: async (id) => {
    const response = await axios.put(`/expenses/${id}/approve`);
    return response.data.data || response.data;
  },

  rejectExpense: async (id) => {
    const response = await axios.put(`/expenses/${id}/reject`);
    return response.data.data || response.data;
  },

  markExpenseAsPaid: async (id) => {
    const response = await axios.put(`/expenses/${id}/paid`);
    return response.data.data || response.data;
  },

  deleteExpense: async (id) => {
    const response = await axios.delete(`/expenses/${id}`);
    return response.data.data || response.data;
  },
};