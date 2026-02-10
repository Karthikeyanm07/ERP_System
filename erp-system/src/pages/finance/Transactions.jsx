/**
 * Transactions Page - Finance Module
 *
 * Backend DTO: TransactionCreateRequest
 * Required: transactionCode, transactionDate, createdById, entries[]
 *
 * Features:
 * - Date range filtering
 * - Type filtering (Income/Expense)
 * - Delete with confirmation
 * - Role-based access control
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { useAuth } from "../../hooks/useAuth";
import { financeApi } from "../../api/financeApi";
import { useToast } from "../../components/common/Toast";
import DataTable from "../../components/common/DataTable";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Card from "../../components/common/Card";
import Badge from "../../components/common/Badge";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import DropdownActions from "../../components/common/DropdownActions";
import {
  Plus,
  ArrowUpCircle,
  ArrowDownCircle,
  Filter,
  Trash2,
  RefreshCw,
  Calendar,
  TrendingUp,
} from "lucide-react";

const Transactions = () => {
  const { execute, loading } = useApi();
  const { hasAnyRole } = useAuth();
  const toast = useToast();

  // Role-based permissions
  const canManageTransactions = hasAnyRole(["ROLE_ACCOUNTANT", "ROLE_ADMIN"]);
  const canDeleteTransactions = hasAnyRole(["ROLE_ADMIN"]);

  const [transactions, setTransactions] = useState([]);
  const [typeFilter, setTypeFilter] = useState("ALL");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [transactionToDelete, setTransactionToDelete] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Simplified form - frontend handles as simple income/expense entries
  const [formData, setFormData] = useState({
    type: "INCOME",
    description: "",
    amount: "",
    date: new Date().toISOString().split("T")[0],
    reference: "",
  });

  useEffect(() => {
    fetchTransactions();
  }, []);

  const fetchTransactions = async () => {
    try {
      const data = await execute(financeApi.getTransactions);
      setTransactions(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching transactions", error);
      toast.error("Failed to load transactions");
      setTransactions([]);
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
      type: "INCOME",
      description: "",
      amount: "",
      date: new Date().toISOString().split("T")[0],
      reference: "",
    });
    setErrors({});
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.description.trim()) {
      newErrors.description = "Description is required";
    }
    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      newErrors.amount = "Amount must be greater than 0";
    }
    if (!formData.date) {
      newErrors.date = "Date is required";
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

    // Create a simplified transaction record
    const submitData = {
      transactionCode: `TXN-${Date.now()}`,
      transactionDate: formData.date,
      description: formData.description,
      referenceNumber: formData.reference || null,
      createdById: 1, // Default to admin
      entries: [
        {
          accountId: formData.type === "INCOME" ? 1 : 2, // Placeholder account IDs
          entryType: formData.type === "INCOME" ? "CREDIT" : "DEBIT",
          amount: parseFloat(formData.amount),
          description: formData.description,
        },
      ],
    };

    try {
      await financeApi.createTransaction(submitData);
      toast.success("Transaction recorded successfully");
      await fetchTransactions();
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      logger.error("Error creating transaction", error);
      toast.error(
        error.response?.data?.message || "Error recording transaction"
      );
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = (transaction) => {
    setTransactionToDelete(transaction);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!transactionToDelete) return;

    try {
      await financeApi.deleteTransaction(transactionToDelete.id);
      toast.success("Transaction deleted successfully");
      await fetchTransactions();
      setIsDeleteDialogOpen(false);
      setTransactionToDelete(null);
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Error deleting transaction"
      );
    }
  };

  const clearFilters = () => {
    setTypeFilter("ALL");
    setStartDate("");
    setEndDate("");
  };

  // Filter transactions
  const filteredTransactions = transactions.filter((t) => {
    // Type filter
    let matchesType = true;
    if (typeFilter === "INCOME") {
      matchesType =
        t.type === "INCOME" || t.entries?.some((e) => e.entryType === "CREDIT");
    } else if (typeFilter === "EXPENSE") {
      matchesType =
        t.type === "EXPENSE" || t.entries?.some((e) => e.entryType === "DEBIT");
    }

    // Date filter
    let matchesDate = true;
    if (startDate && endDate) {
      const txDate = new Date(t.transactionDate);
      matchesDate =
        txDate >= new Date(startDate) && txDate <= new Date(endDate);
    }

    return matchesType && matchesDate;
  });

  // Calculate totals
  const totalIncome = transactions
    .filter(
      (t) =>
        t.type === "INCOME" || t.entries?.some((e) => e.entryType === "CREDIT")
    )
    .reduce((sum, t) => {
      const amount =
        t.entries?.reduce(
          (entrySum, entry) => entrySum + (parseFloat(entry.amount) || 0),
          0
        ) / 2 || 0;
      return sum + amount;
    }, 0);

  const totalExpense = transactions
    .filter(
      (t) =>
        t.type === "EXPENSE" || t.entries?.some((e) => e.entryType === "DEBIT")
    )
    .reduce((sum, t) => {
      const amount =
        t.entries?.reduce(
          (entrySum, entry) => entrySum + (parseFloat(entry.amount) || 0),
          0
        ) / 2 || 0;
      return sum + amount;
    }, 0);

  const netBalance = totalIncome - totalExpense;

  const columns = [
    {
      accessorKey: "transactionCode",
      header: "Code",
      cell: ({ getValue }) => (
        <span className="font-mono text-sm bg-gray-100 dark:bg-slate-700/70 text-gray-800 dark:text-slate-100 px-2 py-1 rounded">
          {getValue()}
        </span>
      ),
    },
    {
      accessorKey: "transactionDate",
      header: "Date",
      cell: ({ getValue }) => (getValue() ? new Date(getValue()).toLocaleDateString() : "-"),
    },
    {
      accessorKey: "description",
      header: "Description",
      cell: ({ getValue }) => (
        <span className="font-medium text-gray-900 dark:text-gray-100">{getValue() || "-"}</span>
      ),
    },
    {
      accessorKey: "referenceNumber",
      header: "Reference",
      cell: ({ getValue }) => getValue() || <span className="text-gray-400">—</span>,
    },
    {
      accessorKey: "type",
      header: "Type",
      cell: ({ getValue, row }) => {
        const isIncome =
          getValue() === "INCOME" ||
          row.original.entries?.some((e) => e.entryType === "CREDIT");
        return (
          <Badge variant={isIncome ? "success" : "danger"}>
            {isIncome ? "Income" : "Expense"}
          </Badge>
        );
      },
    },
    {
      id: "totalAmount",
      header: "Amount",
      cell: ({ row }) => {
        const amount =
          row.original.entries?.reduce((sum, entry) => {
            return sum + (parseFloat(entry.amount) || 0);
          }, 0) / 2 || 0;

        const isIncome =
          row.original.type === "INCOME" ||
          row.original.entries?.some((e) => e.entryType === "CREDIT");
        return (
          <span
            className={`font-semibold ${
              isIncome
                ? "text-green-600 dark:text-green-400"
                : "text-red-600 dark:text-red-400"
            }`}
          >
            {isIncome ? "+" : "-"}₹{amount.toLocaleString()}
          </span>
        );
      },
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Transactions
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Track income and expenses
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="outline" onClick={fetchTransactions}>
            <RefreshCw size={18} />
          </Button>
          {canManageTransactions && (
            <Button onClick={() => setIsModalOpen(true)}>
              <Plus size={20} />
              New Transaction
            </Button>
          )}
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="bg-gradient-to-br from-green-50 to-emerald-50 border-green-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">Total Income</p>
              <p className="text-2xl font-bold text-green-600 mt-1">
                ${totalIncome.toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-green-100 rounded-xl">
              <ArrowUpCircle className="text-green-600" size={28} />
            </div>
          </div>
        </Card>
        <Card className="bg-gradient-to-br from-red-50 to-rose-50 border-red-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">
                Total Expenses
              </p>
              <p className="text-2xl font-bold text-red-600 mt-1">
                ${totalExpense.toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-red-100 rounded-xl">
              <ArrowDownCircle className="text-red-600" size={28} />
            </div>
          </div>
        </Card>
        <Card
          className={`bg-gradient-to-br ${
            netBalance >= 0
              ? "from-blue-50 to-indigo-50 border-blue-200"
              : "from-amber-50 to-yellow-50 border-amber-200"
          }`}
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">Net Balance</p>
              <p
                className={`text-2xl font-bold ${
                  netBalance >= 0 ? "text-blue-600" : "text-amber-600"
                } mt-1`}
              >
                ${netBalance.toLocaleString()}
              </p>
            </div>
            <div
              className={`p-3 rounded-xl ${
                netBalance >= 0 ? "bg-blue-100" : "bg-amber-100"
              }`}
            >
              <TrendingUp
                className={netBalance >= 0 ? "text-blue-600" : "text-amber-600"}
                size={28}
              />
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
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Type Filter */}
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            className="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-md focus:outline-none focus:border-gray-500 dark:focus:border-gray-400 transition-all duration-200 text-sm cursor-pointer"
          >
            <option value="ALL">All Transactions</option>
            <option value="INCOME">Income Only</option>
            <option value="EXPENSE">Expenses Only</option>
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
      <DataTable
        columns={columns}
        data={filteredTransactions}
        loading={loading}
        emptyMessage="No transactions found"
        enableRowSelection
        actions={
          canDeleteTransactions
            ? (row) => (
                <DropdownActions
                  actions={[
                    { label: "Delete Transaction", icon: Trash2, onClick: () => handleDelete(row), variant: "danger" },
                  ]}
                />
              )
            : null
        }
      />

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={confirmDelete}
        title="Delete Transaction"
        message={`Are you sure you want to delete transaction "${transactionToDelete?.transactionCode}"? This will also update account balances.`}
        confirmText="Delete"
        variant="danger"
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="New Transaction"
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              Record Transaction
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Type */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Type *
            </label>
            <div className="flex gap-4">
              <label
                className={`flex-1 flex items-center justify-center gap-2 p-3 rounded-lg border-2 cursor-pointer transition-all ${
                  formData.type === "INCOME"
                    ? "border-green-500 bg-green-50"
                    : "border-gray-200 hover:border-green-200"
                }`}
              >
                <input
                  type="radio"
                  name="type"
                  value="INCOME"
                  checked={formData.type === "INCOME"}
                  onChange={handleChange}
                  className="text-green-600 focus:ring-green-500"
                />
                <ArrowUpCircle className="text-green-600" size={20} />
                <span className="text-green-600 font-medium">Income</span>
              </label>
              <label
                className={`flex-1 flex items-center justify-center gap-2 p-3 rounded-lg border-2 cursor-pointer transition-all ${
                  formData.type === "EXPENSE"
                    ? "border-red-500 bg-red-50"
                    : "border-gray-200 hover:border-red-200"
                }`}
              >
                <input
                  type="radio"
                  name="type"
                  value="EXPENSE"
                  checked={formData.type === "EXPENSE"}
                  onChange={handleChange}
                  className="text-red-600 focus:ring-red-500"
                />
                <ArrowDownCircle className="text-red-600" size={20} />
                <span className="text-red-600 font-medium">Expense</span>
              </label>
            </div>
          </div>

          <Input
            label="Description *"
            name="description"
            value={formData.description}
            onChange={handleChange}
            placeholder="What is this transaction for?"
            error={errors.description}
          />

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
              name="date"
              type="date"
              value={formData.date}
              onChange={handleChange}
              error={errors.date}
            />
          </div>

          <Input
            label="Reference Number"
            name="reference"
            value={formData.reference}
            onChange={handleChange}
            placeholder="Invoice #, Receipt #, etc."
          />
        </form>
      </Modal>
    </div>
  );
};

export default Transactions;
