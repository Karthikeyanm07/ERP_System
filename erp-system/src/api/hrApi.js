/**
 * HR Module API
 * 
 * NOTE: Backend returns wrapped response { success, message, data }
 * We extract the actual data from response.data.data
 */

import axios from './axiosConfig';

export const hrApi = {
  // Employees
  getEmployees: async () => {
    const response = await axios.get('/employees');
    return response.data.data || response.data;
  },

  getEmployeeById: async (id) => {
    const response = await axios.get(`/employees/${id}`);
    return response.data.data || response.data;
  },

  createEmployee: async (employeeData) => {
    const response = await axios.post('/employees', employeeData);
    return response.data.data || response.data;
  },

  updateEmployee: async (id, employeeData) => {
    const response = await axios.put(`/employees/${id}`, employeeData);
    return response.data.data || response.data;
  },

  deleteEmployee: async (id) => {
    const response = await axios.delete(`/employees/${id}`);
    return response.data.data || response.data;
  },

  searchEmployees: async (keyword) => {
    const response = await axios.get(`/employees/search?keyword=${keyword}`);
    return response.data.data || response.data;
  },

  // Get employees by department
  getEmployeesByDepartment: async (departmentId) => {
    const response = await axios.get(`/employees/department/${departmentId}`);
    return response.data.data || response.data;
  },

  // Get employees by status
  getEmployeesByStatus: async (status) => {
    const response = await axios.get(`/employees/status/${status}`);
    return response.data.data || response.data;
  },

  // Change employee status
  changeEmployeeStatus: async (id, status) => {
    const response = await axios.put(`/employees/${id}/status/${status}`);
    return response.data.data || response.data;
  },

  // Departments
  getDepartments: async () => {
    const response = await axios.get('/departments');
    return response.data.data || response.data;
  },

  createDepartment: async (departmentData) => {
    const response = await axios.post('/departments', departmentData);
    return response.data.data || response.data;
  },

  updateDepartment: async (id, departmentData) => {
    const response = await axios.put(`/departments/${id}`, departmentData);
    return response.data.data || response.data;
  },

  deleteDepartment: async (id) => {
    const response = await axios.delete(`/departments/${id}`);
    return response.data.data || response.data;
  },

  // Assign manager to department
  assignManager: async (departmentId, managerId) => {
    const response = await axios.put(`/departments/${departmentId}/manager/${managerId}`);
    return response.data.data || response.data;
  },

  // Attendance
  getAttendance: async (date) => {
    const url = date ? `/attendance?date=${date}` : '/attendance';
    const response = await axios.get(url);
    return response.data.data || response.data;
  },

  markAttendance: async (attendanceData) => {
    const response = await axios.post('/attendance', attendanceData);
    return response.data.data || response.data;
  },

  // Leave Types
  getLeaveTypes: async () => {
    const response = await axios.get('/leave-types');
    return response.data.data || response.data;
  },

  // Leave Management
  getLeaveRequests: async () => {
    const response = await axios.get('/leave-requests');
    return response.data.data || response.data;
  },

  submitLeaveRequest: async (leaveData) => {
    const response = await axios.post('/leave-requests', leaveData);
    return response.data.data || response.data;
  },

  approveLeave: async (id, approvalData) => {
    const response = await axios.put(`/leave-requests/${id}/process`, approvalData);
    return response.data.data || response.data;
  },
};
