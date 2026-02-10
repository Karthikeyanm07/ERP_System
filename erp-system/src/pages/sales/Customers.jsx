/**
 * Customers Page - Sales Module
 *
 * Backend DTO: CustomerDTO
 * Required fields: customerCode, name
 *
 * Features:
 * - Search functionality
 * - Status filter (Active/Inactive)
 * - Credit limit warning indicator
 * - Delete with confirmation
 * - Role-based access control
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { useAuth } from "../../hooks/useAuth";
import { salesApi } from "../../api/salesApi";
import { useToast } from "../../components/common/Toast";
import DataTable from "../../components/common/DataTable";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Card from "../../components/common/Card";
import Badge from "../../components/common/Badge";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import MetricCard from "../../components/common/MetricCard";
import SearchBar from "../../components/common/SearchBar";
import DropdownActions from "../../components/common/DropdownActions";
import {
  Plus,
  Pencil,
  Trash2,
  Users,
  RefreshCw,
  Filter,
  AlertTriangle,
  DollarSign,
  UserCheck,
} from "lucide-react";

const Customers = () => {
  const { execute, loading } = useApi();
  const { hasAnyRole } = useAuth();
  const toast = useToast();

  // Role-based permissions
  const canManageCustomers = hasAnyRole([
    "ROLE_SALES_STAFF",
    "ROLE_SALES",
    "ROLE_ADMIN",
  ]);
  const canDeleteCustomers = hasAnyRole(["ROLE_ADMIN"]);

  const [customers, setCustomers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [creditFilter, setCreditFilter] = useState("ALL");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [customerToDelete, setCustomerToDelete] = useState(null);
  const [editingCustomer, setEditingCustomer] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Form data matching CustomerDTO
  const [formData, setFormData] = useState({
    customerCode: "",
    name: "",
    contactPerson: "",
    email: "",
    phone: "",
    address: "",
    city: "",
    country: "",
    creditLimit: "",
  });

  useEffect(() => {
    fetchCustomers();
  }, []);

  const fetchCustomers = async () => {
    try {
      const data = await execute(salesApi.getCustomers);
      setCustomers(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching customers", error);
      toast.error("Failed to load customers");
      setCustomers([]);
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
      customerCode: "",
      name: "",
      contactPerson: "",
      email: "",
      phone: "",
      address: "",
      city: "",
      country: "",
      creditLimit: "",
    });
    setErrors({});
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.customerCode.trim()) {
      newErrors.customerCode = "Customer code is required";
    }
    if (!formData.name.trim()) {
      newErrors.name = "Customer name is required";
    }
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Please enter a valid email";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleAdd = () => {
    setEditingCustomer(null);
    resetForm();
    setIsModalOpen(true);
  };

  const handleEdit = (customer) => {
    setEditingCustomer(customer);
    setFormData({
      customerCode: customer.customerCode || "",
      name: customer.name || "",
      contactPerson: customer.contactPerson || "",
      email: customer.email || "",
      phone: customer.phone || "",
      address: customer.address || "",
      city: customer.city || "",
      country: customer.country || "",
      creditLimit: customer.creditLimit || "",
    });
    setErrors({});
    setIsModalOpen(true);
  };

  const handleDelete = (customer) => {
    setCustomerToDelete(customer);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!customerToDelete) return;

    try {
      await salesApi.deleteCustomer(customerToDelete.id);
      toast.success("Customer deleted successfully");
      await fetchCustomers();
      setIsDeleteDialogOpen(false);
      setCustomerToDelete(null);
    } catch (error) {
      toast.error(error.response?.data?.message || "Error deleting customer");
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.warning("Please fix the form errors");
      return;
    }

    setFormLoading(true);

    const submitData = {
      ...formData,
      creditLimit: formData.creditLimit
        ? parseFloat(formData.creditLimit)
        : null,
    };

    try {
      if (editingCustomer) {
        await salesApi.updateCustomer(editingCustomer.id, submitData);
        toast.success("Customer updated successfully");
      } else {
        await salesApi.createCustomer(submitData);
        toast.success("Customer created successfully");
      }
      await fetchCustomers();
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      logger.error("Error saving customer", error);
      toast.error(error.response?.data?.message || "Error saving customer");
    } finally {
      setFormLoading(false);
    }
  };

  const clearFilters = () => {
    setSearchTerm("");
    setStatusFilter("ALL");
    setCreditFilter("ALL");
  };

  // Check if customer is exceeding credit
  const isExceedingCredit = (customer) => {
    const limit = parseFloat(customer.creditLimit) || 0;
    const outstanding = parseFloat(customer.outstandingBalance) || 0;
    return limit > 0 && outstanding > limit;
  };

  // Filter customers
  const filteredCustomers = customers.filter((c) => {
    const matchesSearch =
      c.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.customerCode?.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesStatus =
      statusFilter === "ALL" ||
      (statusFilter === "ACTIVE" && c.isActive !== false) ||
      (statusFilter === "INACTIVE" && c.isActive === false);

    const matchesCredit =
      creditFilter === "ALL" ||
      (creditFilter === "EXCEEDING" && isExceedingCredit(c));

    return matchesSearch && matchesStatus && matchesCredit;
  });

  // Calculate stats
  const totalCustomers = customers.length;
  const activeCustomers = customers.filter((c) => c.isActive !== false).length;
  const exceedingCreditCount = customers.filter(isExceedingCredit).length;
  const totalCreditLimit = customers.reduce(
    (sum, c) => sum + (parseFloat(c.creditLimit) || 0),
    0
  );

  const columns = [
    {
      accessorKey: "name",
      header: "Customer",
      size: 200,
      cell: ({ getValue, row }) => (
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 bg-gradient-to-br from-green-500 to-teal-500 rounded-full flex items-center justify-center text-white font-medium text-sm flex-shrink-0">
            {getValue()?.[0]?.toUpperCase() || "C"}
          </div>
          <div className="min-w-0">
            <p className="font-medium text-gray-900 dark:text-gray-100 truncate">{getValue()}</p>
            <p className="text-xs text-gray-500 font-mono">
              {row.original.customerCode}
            </p>
          </div>
        </div>
      ),
    },
    {
      accessorKey: "contactPerson",
      header: "Contact",
      size: 120,
      cell: ({ getValue }) => <span className="truncate block">{getValue() || "—"}</span>,
    },
    {
      accessorKey: "email",
      header: "Email",
      size: 180,
      cell: ({ getValue }) => (
        <span className="truncate block" title={getValue()}>
          {getValue() || "—"}
        </span>
      ),
    },
    {
      accessorKey: "phone",
      header: "Phone",
      size: 120,
      cell: ({ getValue }) => <span className="text-gray-600 dark:text-gray-300">{getValue() || "—"}</span>,
    },
    {
      accessorKey: "creditLimit",
      header: "Credit",
      size: 140,
      cell: ({ getValue, row }) => {
        const exceeding = isExceedingCredit(row.original);
        return (
          <div className="flex items-center gap-1.5">
            <span
              className={`font-semibold ${
                exceeding
                  ? "text-red-600 dark:text-red-400"
                  : "text-gray-900 dark:text-gray-100"
              }`}
            >
              ${parseFloat(getValue() || 0).toLocaleString()}
            </span>
            {exceeding && (
              <span className="text-[10px] font-medium text-red-600 dark:text-red-300 bg-red-100 dark:bg-red-500/30 px-1.5 py-0.5 rounded">
                !
              </span>
            )}
          </div>
        );
      },
    },
    {
      accessorKey: "isActive",
      header: "Status",
      size: 90,
      cell: ({ getValue }) => (
        <span
          className={`inline-flex items-center gap-1 text-xs font-medium rounded-full px-2.5 py-1 ${
            getValue() !== false
              ? "bg-green-100 text-green-700 dark:bg-green-500/30 dark:text-green-200"
              : "bg-gray-100 text-gray-600 dark:bg-gray-600 dark:text-gray-200"
          }`}
        >
          <span className="w-1.5 h-1.5 rounded-full bg-current"></span>
          {getValue() !== false ? "Active" : "Inactive"}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Customers
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage your customer relationships
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="outline" onClick={fetchCustomers}>
            <RefreshCw size={18} />
          </Button>
          {canManageCustomers && (
            <Button onClick={handleAdd}>
              <Plus size={20} />
              Add Customer
            </Button>
          )}
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <MetricCard
          title="Total Customers"
          value={totalCustomers}
          icon={Users}
          accent="blue"
        />
        <MetricCard
          title="Active"
          value={activeCustomers}
          icon={UserCheck}
          accent="green"
        />
        <MetricCard
          title="Over Credit"
          value={exceedingCreditCount}
          icon={AlertTriangle}
          accent="amber"
        />
        <MetricCard
          title="Total Credit"
          value={`₹${totalCreditLimit.toLocaleString()}`}
          icon={DollarSign}
          accent="purple"
        />
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
          {/* Search */}
          <SearchBar
            value={searchTerm}
            onChange={setSearchTerm}
            placeholder="Search customers..."
          />
          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-md focus:outline-none focus:border-gray-500 dark:focus:border-gray-400 transition-all duration-200 text-sm cursor-pointer"
          >
            <option value="ALL">All Status</option>
            <option value="ACTIVE">Active Only</option>
            <option value="INACTIVE">Inactive Only</option>
          </select>
          {/* Credit Filter */}
          <select
            value={creditFilter}
            onChange={(e) => setCreditFilter(e.target.value)}
            className="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-md focus:outline-none focus:border-gray-500 dark:focus:border-gray-400 transition-all duration-200 text-sm cursor-pointer"
          >
            <option value="ALL">All Credit Status</option>
            <option value="EXCEEDING">Exceeding Credit Limit</option>
          </select>
        </div>
      </Card>

      {/* Table */}
      <DataTable
        columns={columns}
        data={filteredCustomers}
        loading={loading}
        emptyMessage="No customers found"
        enableRowSelection
        actions={
          canManageCustomers
            ? (row) => (
                <DropdownActions
                  actions={[
                    { label: "Edit Customer", icon: Pencil, onClick: () => handleEdit(row) },
                    ...(canDeleteCustomers
                      ? [
                          { divider: true },
                          { label: "Delete Customer", icon: Trash2, onClick: () => handleDelete(row), variant: "danger" },
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
        title="Delete Customer"
        message={`Are you sure you want to delete "${customerToDelete?.name}"? This action cannot be undone.`}
        confirmText="Delete"
        variant="danger"
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingCustomer ? "Edit Customer" : "Add Customer"}
        size="lg"
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              {editingCustomer ? "Update" : "Create"}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Customer Code *"
              name="customerCode"
              value={formData.customerCode}
              onChange={handleChange}
              placeholder="CUST-001"
              error={errors.customerCode}
            />
            <Input
              label="Customer Name *"
              name="name"
              value={formData.name}
              onChange={handleChange}
              error={errors.name}
            />
          </div>

          <Input
            label="Contact Person"
            name="contactPerson"
            value={formData.contactPerson}
            onChange={handleChange}
          />

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              error={errors.email}
            />
            <Input
              label="Phone"
              name="phone"
              value={formData.phone}
              onChange={handleChange}
            />
          </div>

          <Input
            label="Address"
            name="address"
            value={formData.address}
            onChange={handleChange}
          />

          <div className="grid grid-cols-3 gap-4">
            <Input
              label="City"
              name="city"
              value={formData.city}
              onChange={handleChange}
            />
            <Input
              label="Country"
              name="country"
              value={formData.country}
              onChange={handleChange}
            />
            <Input
              label="Credit Limit"
              name="creditLimit"
              type="number"
              value={formData.creditLimit}
              onChange={handleChange}
            />
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Customers;
