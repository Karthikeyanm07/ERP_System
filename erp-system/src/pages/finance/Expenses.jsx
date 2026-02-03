/**
 * Expenses Page - Finance Module
 *
 * Backend DTO: ExpenseCreateRequest
 * Required: expenseCode, category, amount, expenseDate
 *
 * Features:
 * - Status, category, and date range filters
 * - Approve/Reject/Mark Paid workflow
 * - Delete for PENDING/REJECTED expenses
 * - Role-based access control
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { useAuth } from "../../hooks/useAuth";
import { financeApi } from "../../api/financeApi";
import { useToast } from "../../components/common/Toast";
import Table from "../../components/common/Table";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Card from "../../components/common/Card";
import Badge from "../../components/common/Badge";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import {
  Plus,
  Receipt,
  Check,
  X,
  DollarSign,
  Trash2,
  Filter,
  RefreshCw,
  Calendar,
} from "lucide-react";

const Expenses = () => {
  const { execute, loading } = useApi();
  const { hasAnyRole } = useAuth();
  const toast = useToast();

  // Role-based permissions
  const canManageExpenses = hasAnyRole(["ROLE_ACCOUNTANT", "ROLE_ADMIN"]);

  const [expenses, setExpenses] = useState([]);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [categoryFilter, setCategoryFilter] = useState("ALL");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [expenseToDelete, setExpenseToDelete] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Form data matching ExpenseCreateRequest
  const [formData, setFormData] = useState({
    expenseCode: "",
    category: "",
    amount: "",
    expenseDate: new Date().toISOString().split("T")[0],
    vendorName: "",
    description: "",
  });

  const categories = [
    "Office Supplies",
    "Travel",
    "Utilities",
    "Marketing",
    "Equipment",
    "Software",
    "Professional Services",
    "Miscellaneous",
  ];

  useEffect(() => {
    fetchExpenses();
  }, []);

  const fetchExpenses = async () => {
    try {
      const data = await execute(financeApi.getExpenses);
      setExpenses(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching expenses:", error);
      toast.error("Failed to load expenses");
      setExpenses([]);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const resetForm = () => {
    setFormData({
      expenseCode: "",
      category: "",
      amount: "",
      expenseDate: new Date().toISOString().split("T")[0],
      vendorName: "",
      description: "",
    });
    setErrors({});
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.expenseCode.trim()) {
      newErrors.expenseCode = "Expense code is required";
    }
    if (!formData.category) {
      newErrors.category = "Category is required";
    }
    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      newErrors.amount = "Amount must be greater than 0";
    }
    if (!formData.expenseDate) {
      newErrors.expenseDate = "Date is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.warning("Please fix the form errors");
      return;
    }

    setFormLoading(true);

    const submitData = {
      expenseCode: formData.expenseCode,
      category: formData.category,
      amount: parseFloat(formData.amount),
      expenseDate: formData.expenseDate,
      vendorName: formData.vendorName || null,
      description: formData.description || null,
    };

    try {
      await financeApi.createExpense(submitData);
      toast.success("Expense submitted successfully");
      await fetchExpenses();
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      logger.error("Error creating expense:", error);
      toast.error(error.response?.data?.message || "Error submitting expense");
    } finally {
      setFormLoading(false);
    }
  };

  const handleApprove = async (id) => {
    try {
      await financeApi.approveExpense(id);
      toast.success("Expense approved successfully");
      await fetchExpenses();
    } catch (error) {
      toast.error(error.response?.data?.message || "Error approving expense");
    }
  };

  const handleReject = async (id) => {
    try {
      await financeApi.rejectExpense(id);
      toast.success("Expense rejected");
      await fetchExpenses();
    } catch (error) {
      toast.error(error.response?.data?.message || "Error rejecting expense");
    }
  };

  const handleMarkAsPaid = async (id) => {
    try {
      await financeApi.markExpenseAsPaid(id);
      toast.success("Expense marked as paid");
      await fetchExpenses();
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Error marking expense as paid"
      );
    }
  };

  const handleDelete = (expense) => {
    setExpenseToDelete(expense);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!expenseToDelete) return;

    try {
      await financeApi.deleteExpense(expenseToDelete.id);
      toast.success("Expense deleted successfully");
      await fetchExpenses();
      setIsDeleteDialogOpen(false);
      setExpenseToDelete(null);
    } catch (error) {
      toast.error(error.response?.data?.message || "Error deleting expense");
    }
  };

  const clearFilters = () => {
    setStatusFilter("ALL");
    setCategoryFilter("ALL");
    setStartDate("");
    setEndDate("");
  };

  // Filter expenses
  const filteredExpenses = expenses.filter((e) => {
    const matchesStatus = statusFilter === "ALL" || e.status === statusFilter;
    const matchesCategory =
      categoryFilter === "ALL" || e.category === categoryFilter;

    let matchesDate = true;
    if (startDate && endDate) {
      const expenseDate = new Date(e.expenseDate);
      matchesDate =
        expenseDate >= new Date(startDate) && expenseDate <= new Date(endDate);
    }

    return matchesStatus && matchesCategory && matchesDate;
  });

  const getStatusBadge = (status) => {
    const variants = {
      PENDING: { variant: "warning", label: "Pending" },
      APPROVED: { variant: "success", label: "Approved" },
      REJECTED: { variant: "danger", label: "Rejected" },
      PAID: { variant: "primary", label: "Paid" },
    };
    const { variant, label } = variants[status] || variants.PENDING;
    return <Badge variant={variant}>{label}</Badge>;
  };

  // Calculate totals
  const totalPending = expenses
    .filter((e) => e.status === "PENDING")
    .reduce((sum, e) => sum + (parseFloat(e.amount) || 0), 0);
  const totalApproved = expenses
    .filter((e) => e.status === "APPROVED")
    .reduce((sum, e) => sum + (parseFloat(e.amount) || 0), 0);
  const totalPaid = expenses
    .filter((e) => e.status === "PAID")
    .reduce((sum, e) => sum + (parseFloat(e.amount) || 0), 0);
  const pendingCount = expenses.filter((e) => e.status === "PENDING").length;

  // Get unique categories from expenses
  const usedCategories = [
    ...new Set(expenses.map((e) => e.category).filter(Boolean)),
  ];

  const columns = [
    {
      key: "expenseCode",
      header: "Code",
      render: (value) => (
        <span className="font-mono text-sm bg-gray-100 dark:bg-slate-700/70 text-gray-800 dark:text-slate-100 px-2 py-1 rounded">
          {value}
        </span>
      ),
    },
    {
      key: "expenseDate",
      header: "Date",
      render: (value) => (value ? new Date(value).toLocaleDateString() : "-"),
    },
    {
      key: "category",
      header: "Category",
      render: (value) => <Badge variant="default">{value}</Badge>,
    },
    {
      key: "vendorName",
      header: "Vendor",
      render: (value) =>
        value || <span className="text-gray-400 dark:text-gray-500">—</span>,
    },
    {
      key: "amount",
      header: "Amount",
      render: (value) => (
        <span className="font-semibold text-gray-900 dark:text-gray-100">
          ₹{parseFloat(value || 0).toLocaleString()}
        </span>
      ),
    },
    {
      key: "status",
      header: "Status",
      render: (value) => getStatusBadge(value),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Expenses
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage business expenses
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="outline" onClick={fetchExpenses}>
            <RefreshCw size={18} />
          </Button>
          {canManageExpenses && (
            <Button onClick={() => setIsModalOpen(true)}>
              <Plus size={20} />
              New Expense
            </Button>
          )}
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Card className="bg-gradient-to-br from-amber-50 to-yellow-50 border-amber-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">Pending</p>
              <p className="text-2xl font-bold text-amber-600 mt-1">
                ${totalPending.toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-amber-100 rounded-xl">
              <Receipt className="text-amber-600" size={24} />
            </div>
          </div>
          <p className="text-xs text-gray-500 mt-2">
            {pendingCount} expenses awaiting approval
          </p>
        </Card>
        <Card className="bg-gradient-to-br from-green-50 to-emerald-50 border-green-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">Approved</p>
              <p className="text-2xl font-bold text-green-600 mt-1">
                ${totalApproved.toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-green-100 rounded-xl">
              <Check className="text-green-600" size={24} />
            </div>
          </div>
        </Card>
        <Card className="bg-gradient-to-br from-blue-50 to-indigo-50 border-blue-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">Paid</p>
              <p className="text-2xl font-bold text-blue-600 mt-1">
                ${totalPaid.toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-blue-100 rounded-xl">
              <DollarSign className="text-blue-600" size={24} />
            </div>
          </div>
        </Card>
        <Card className="bg-gradient-to-br from-purple-50 to-violet-50 border-purple-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">Total</p>
              <p className="text-2xl font-bold text-purple-600 mt-1">
                $
                {expenses
                  .reduce((sum, e) => sum + (parseFloat(e.amount) || 0), 0)
                  .toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-purple-100 rounded-xl">
              <Receipt className="text-purple-600" size={24} />
            </div>
          </div>
        </Card>
      </div>

      {/* Filters */}
      <Card padding={false} className="p-4">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Filter size={18} className="text-gray-500 dark:text-gray-400" />
            <span className="font-medium text-gray-700 dark:text-gray-300">
              Filters
            </span>
          </div>
          <Button variant="ghost" size="sm" onClick={clearFilters}>
            Clear All
          </Button>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
          >
            <option value="ALL">All Status</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="REJECTED">Rejected</option>
            <option value="PAID">Paid</option>
          </select>

          {/* Category Filter */}
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
          >
            <option value="ALL">All Categories</option>
            {usedCategories.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>

          {/* Date Range */}
          <div className="flex items-center gap-2">
            <Calendar size={18} className="text-gray-400 dark:text-gray-500" />
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
              placeholder="Start Date"
            />
          </div>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
            placeholder="End Date"
          />
        </div>
      </Card>

      {/* Table */}
      <Table
        columns={columns}
        data={filteredExpenses}
        loading={loading}
        emptyMessage="No expenses found"
        actions={
          canManageExpenses
            ? (row) => (
                <div className="flex items-center gap-1">
                  {row.status === "PENDING" && (
                    <>
                      <button
                        onClick={() => handleApprove(row.id)}
                        className="p-1.5 text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                        title="Approve"
                      >
                        <Check size={18} />
                      </button>
                      <button
                        onClick={() => handleReject(row.id)}
                        className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="Reject"
                      >
                        <X size={18} />
                      </button>
                    </>
                  )}
                  {row.status === "APPROVED" && (
                    <button
                      onClick={() => handleMarkAsPaid(row.id)}
                      className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                      title="Mark as Paid"
                    >
                      <DollarSign size={18} />
                    </button>
                  )}
                  {(row.status === "PENDING" || row.status === "REJECTED") && (
                    <button
                      onClick={() => handleDelete(row)}
                      className="p-1.5 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Delete"
                    >
                      <Trash2 size={18} />
                    </button>
                  )}
                </div>
              )
            : null
        }
      />

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={confirmDelete}
        title="Delete Expense"
        message={`Are you sure you want to delete expense "${expenseToDelete?.expenseCode}"? This action cannot be undone.`}
        confirmText="Delete"
        variant="danger"
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Submit Expense"
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              Submit
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Expense Code *"
              name="expenseCode"
              value={formData.expenseCode}
              onChange={handleChange}
              placeholder="EXP-001"
              error={errors.expenseCode}
            />
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Category *
              </label>
              <select
                name="category"
                value={formData.category}
                onChange={handleChange}
                className={`w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 ${
                  errors.category ? "border-red-500" : "border-gray-300"
                }`}
              >
                <option value="">Select Category</option>
                {categories.map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
              {errors.category && (
                <p className="text-red-500 text-xs mt-1">{errors.category}</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Amount *"
              name="amount"
              type="number"
              step="0.01"
              value={formData.amount}
              onChange={handleChange}
              placeholder="0.00"
              error={errors.amount}
            />
            <Input
              label="Date *"
              name="expenseDate"
              type="date"
              value={formData.expenseDate}
              onChange={handleChange}
              error={errors.expenseDate}
            />
          </div>

          <Input
            label="Vendor Name"
            name="vendorName"
            value={formData.vendorName}
            onChange={handleChange}
            placeholder="Supplier or vendor"
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={2}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Details about this expense"
            />
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Expenses;
