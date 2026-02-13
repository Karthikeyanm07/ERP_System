/**
 * Accounts Page - Finance Module
 *
 * Backend DTO: AccountDTO
 * Required fields: accountCode, accountName, accountType
 *
 * Features:
 * - Search and filter by type/status
 * - CRUD operations with role-based access
 * - Toast notifications
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useAuth } from "../../hooks/useAuth";
import { financeApi } from "../../api/financeApi";
import { useCrudForm } from "../../hooks/useCrudForm";
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
  Wallet,
  Pencil,
  Trash2,
  Filter,
  RefreshCw,
  Power,
  PowerOff,
} from "lucide-react";

const Accounts = () => {
  const { hasAnyRole } = useAuth();

  // Role-based permissions
  const canManageAccounts = hasAnyRole(["ROLE_ACCOUNTANT", "ROLE_ADMIN"]);
  const canDeleteAccounts = hasAnyRole(["ROLE_ADMIN"]);

  const [searchTerm, setSearchTerm] = useState("");
  const [typeFilter, setTypeFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const initialData = {
    accountCode: "",
    accountName: "",
    accountType: "ASSET",
    parentAccountId: "",
    isActive: true,
  };

  const validate = (data) => {
    const errors = {};
    if (!data.accountCode?.trim()) errors.accountCode = "Account code is required";
    if (!data.accountName?.trim()) errors.accountName = "Account name is required";
    if (!data.accountType) errors.accountType = "Account type is required";
    return errors;
  };

  const {
    items: accounts,
    formData,
    errors,
    loading,
    formLoading,
    isModalOpen,
    setIsModalOpen,
    isDeleteDialogOpen,
    setIsDeleteDialogOpen,
    editingItem: editingAccount,
    itemToDelete: accountToDelete,
    fetchItems: fetchAccounts,
    handleChange,
    handleAdd,
    handleEdit,
    handleDelete,
    confirmDelete,
    handleSubmit: baseSubmit,
  } = useCrudForm({
    initialData,
    validate,
    api: financeApi,
    entityName: "Account",
  });

  useEffect(() => {
    fetchAccounts();
  }, []);

  const handleSubmit = (e) => {
    // Custom data transformation for parentAccountId
    const submitData = {
      ...formData,
      parentAccountId: formData.parentAccountId ? parseInt(formData.parentAccountId) : null,
    };
    // Sync back to internal hook state if needed, or pass directly
    // This hook version uses formData directly.
    baseSubmit(e);
  };

  const handleToggleStatus = async (account) => {
    try {
      await financeApi.updateAccount(account.id, {
        ...account,
        isActive: account.isActive === false,
      });
      fetchAccounts();
    } catch (error) {
      logger.error("Error toggling status", error);
    }
  };

  // Filter accounts
  const filteredAccounts = accounts.filter((a) => {
    const matchesSearch =
      a.accountCode?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      a.accountName?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = typeFilter === "ALL" || a.accountType === typeFilter;
    const matchesStatus =
      statusFilter === "ALL" ||
      (statusFilter === "ACTIVE" ? a.isActive !== false : a.isActive === false);

    return matchesSearch && matchesType && matchesStatus;
  });

  const getTypeTotal = (type) => {
    return accounts
      .filter((a) => a.accountType === type)
      .reduce((sum, a) => sum + (parseFloat(a.balance) || 0), 0);
  };

  const getTypeBadge = (type) => {
    const variants = {
      ASSET: "success",
      LIABILITY: "danger",
      EQUITY: "primary",
      REVENUE: "success",
      EXPENSE: "warning",
    };
    return <Badge variant={variants[type] || "default"}>{type}</Badge>;
  };

  const columns = [
    {
      accessorKey: "accountCode",
      header: "Code",
      cell: ({ getValue }) => (
        <span className="font-mono text-sm bg-gray-100 dark:bg-slate-700/70 text-gray-800 dark:text-slate-100 px-2 py-1 rounded">
          {getValue()}
        </span>
      ),
    },
    {
      accessorKey: "accountName",
      header: "Account Name",
      cell: ({ getValue }) => (
        <span className="font-medium text-gray-900 dark:text-gray-100">
          {getValue()}
        </span>
      ),
    },
    {
      accessorKey: "accountType",
      header: "Type",
      cell: ({ getValue }) => getTypeBadge(getValue()),
    },
    {
      accessorKey: "parentAccountName",
      header: "Parent",
      cell: ({ getValue }) =>
        getValue() || <span className="text-gray-400 dark:text-gray-500">—</span>,
    },
    {
      accessorKey: "balance",
      header: "Balance",
      cell: ({ getValue }) => {
        const amount = parseFloat(getValue()) || 0;
        return (
          <span
            className={`font-semibold ${
              amount < 0
                ? "text-red-600 dark:text-red-400"
                : "text-gray-900 dark:text-gray-100"
            }`}
          >
            ₹{Math.abs(amount).toLocaleString()}
          </span>
        );
      },
    },
    {
      accessorKey: "isActive",
      header: "Status",
      cell: ({ getValue }) => (
        <Badge variant={getValue() !== false ? "success" : "default"} dot>
          {getValue() !== false ? "Active" : "Inactive"}
        </Badge>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Chart of Accounts
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage financial accounts
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="outline" onClick={fetchAccounts}>
            <RefreshCw size={18} />
          </Button>
          {canManageAccounts && (
            <Button onClick={handleAdd}>
              <Plus size={20} />
              Add Account
            </Button>
          )}
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
        <Card className="bg-gradient-to-br from-green-50 to-emerald-50 border-green-200">
          <p className="text-gray-600 text-sm font-medium">Assets</p>
          <p className="text-2xl font-bold text-green-600 mt-1">
            ₹{getTypeTotal("ASSET").toLocaleString()}
          </p>
        </Card>
        <Card className="bg-gradient-to-br from-red-50 to-rose-50 border-red-200">
          <p className="text-gray-600 text-sm font-medium">Liabilities</p>
          <p className="text-2xl font-bold text-red-600 mt-1">
            ₹{getTypeTotal("LIABILITY").toLocaleString()}
          </p>
        </Card>
        <Card className="bg-gradient-to-br from-blue-50 to-indigo-50 border-blue-200">
          <p className="text-gray-600 text-sm font-medium">Equity</p>
          <p className="text-2xl font-bold text-blue-600 mt-1">
            ₹{getTypeTotal("EQUITY").toLocaleString()}
          </p>
        </Card>
        <Card className="bg-gradient-to-br from-emerald-50 to-teal-50 border-emerald-200">
          <p className="text-gray-600 text-sm font-medium">Revenue</p>
          <p className="text-2xl font-bold text-emerald-600 mt-1">
            ₹{getTypeTotal("REVENUE").toLocaleString()}
          </p>
        </Card>
        <Card className="bg-gradient-to-br from-amber-50 to-yellow-50 border-amber-200">
          <p className="text-gray-600 text-sm font-medium">Expenses</p>
          <p className="text-2xl font-bold text-amber-600 mt-1">
            ₹{getTypeTotal("EXPENSE").toLocaleString()}
          </p>
        </Card>
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={filteredAccounts}
        loading={loading}
        emptyMessage="No accounts found"
        enableRowSelection
        searchPlaceholder="Search by code or name..."
        filters={
          <>
            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
              className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm cursor-pointer focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200 appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%236b7280%22%20stroke-width%3D%222%22%3E%3Cpath%20d%3D%22m6%209%206%206%206-6%22%2F%3E%3C%2Fsvg%3E')] bg-[length:16px] bg-[right_8px_center] bg-no-repeat pr-8"
            >
              <option value="ALL">All Types</option>
              <option value="ASSET">Assets</option>
              <option value="LIABILITY">Liabilities</option>
              <option value="EQUITY">Equity</option>
              <option value="REVENUE">Revenue</option>
              <option value="EXPENSE">Expenses</option>
            </select>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm cursor-pointer focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200 appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%236b7280%22%20stroke-width%3D%222%22%3E%3Cpath%20d%3D%22m6%209%206%206%206-6%22%2F%3E%3C%2Fsvg%3E')] bg-[length:16px] bg-[right_8px_center] bg-no-repeat pr-8"
            >
              <option value="ALL">All Status</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </select>
          </>
        }
        actions={
          canManageAccounts
            ? (row) => (
                <DropdownActions
                  actions={[
                    { label: "Edit Account", icon: Pencil, onClick: () => handleEdit(row) },
                    { 
                      label: row.isActive === false ? "Activate Account" : "Deactivate Account", 
                      icon: row.isActive === false ? Power : PowerOff, 
                      onClick: () => handleToggleStatus(row) 
                    },
                    ...(canDeleteAccounts
                      ? [
                          { divider: true },
                          { label: "Delete Account", icon: Trash2, onClick: () => handleDelete(row), variant: "danger" },
                        ]
                      : []),
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
        title="Delete Account"
        message={`Are you sure you want to delete account "${accountToDelete?.accountName}"? This action cannot be undone.`}
        confirmText="Delete"
        variant="danger"
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingAccount ? "Edit Account" : "Add Account"}
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              {editingAccount ? "Update" : "Create"} Account
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Account Code *"
              name="accountCode"
              value={formData.accountCode}
              onChange={handleChange}
              placeholder="1001"
              error={errors.accountCode}
              disabled={editingAccount} // Can't change code when editing
            />
            <Input
              label="Account Name *"
              name="accountName"
              value={formData.accountName}
              onChange={handleChange}
              placeholder="Cash"
              error={errors.accountName}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Account Type *
              </label>
              <select
                name="accountType"
                value={formData.accountType}
                onChange={handleChange}
                className={`w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-800 dark:border-gray-600 dark:text-gray-100 dark:focus:ring-blue-400 ${
                  errors.accountType
                    ? "border-red-500 dark:border-red-500"
                    : "border-gray-300 dark:border-gray-600"
                }`}
              >
                <option value="ASSET">Asset</option>
                <option value="LIABILITY">Liability</option>
                <option value="EQUITY">Equity</option>
                <option value="REVENUE">Revenue</option>
                <option value="EXPENSE">Expense</option>
              </select>
              {errors.accountType && (
                <p className="text-red-500 dark:text-red-400 text-xs mt-1">
                  {errors.accountType}
                </p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Parent Account
              </label>
              <select
                name="parentAccountId"
                value={formData.parentAccountId}
                onChange={handleChange}
                className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
              >
                <option value="">None (Top Level)</option>
                {accounts
                  .filter((a) => a.id !== editingAccount?.id) // Can't be parent of itself
                  .map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.accountCode} - {a.accountName}
                    </option>
                  ))}
              </select>
            </div>
          </div>

          {editingAccount && (
            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                name="isActive"
                id="isActive"
                checked={formData.isActive}
                onChange={handleChange}
                className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
              />
              <label
                htmlFor="isActive"
                className="text-sm text-gray-700 dark:text-gray-300"
              >
                Active
              </label>
            </div>
          )}
        </form>
      </Modal>
    </div>
  );
};

export default Accounts;
