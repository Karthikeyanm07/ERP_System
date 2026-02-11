/**
 * Invoices Page - Sales Module
 *
 * Backend DTO: InvoiceCreateRequest
 * Required: invoiceNumber, salesOrderId, invoiceDate, dueDate, createdById
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { salesApi } from "../../api/salesApi";
import { useToast } from "../../components/common/Toast";
import DataTable from "../../components/common/DataTable";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import Badge from "../../components/common/Badge";
import MetricCard from "../../components/common/MetricCard";
import { FileText, Filter, DollarSign, AlertCircle } from "lucide-react";

const Invoices = () => {
  const { execute, loading } = useApi();
  const toast = useToast();

  const [invoices, setInvoices] = useState([]);
  const [statusFilter, setStatusFilter] = useState("ALL");

  useEffect(() => {
    fetchInvoices();
  }, []);

  const fetchInvoices = async () => {
    try {
      const data = await execute(salesApi.getInvoices);
      setInvoices(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching invoices", error);
      toast.error("Failed to load invoices");
      setInvoices([]);
    }
  };

  const getStatusBadge = (status) => {
    const variants = {
      DRAFT: { variant: "default", label: "Draft" },
      SENT: { variant: "primary", label: "Sent" },
      PARTIAL: { variant: "warning", label: "Partial" },
      PAID: { variant: "success", label: "Paid" },
      OVERDUE: { variant: "danger", label: "Overdue" },
      CANCELLED: { variant: "danger", label: "Cancelled" },
    };
    const { variant, label } = variants[status] || variants.DRAFT;
    return <Badge variant={variant}>{label}</Badge>;
  };

  const isOverdue = (dueDate, status) => {
    if (status === "PAID" || status === "CANCELLED") return false;
    return new Date(dueDate) < new Date();
  };

  const filteredInvoices = invoices.filter((inv) => {
    if (statusFilter === "ALL") return true;
    if (statusFilter === "OVERDUE") return isOverdue(inv.dueDate, inv.status);
    return inv.status === statusFilter;
  });

  const totalOutstanding = invoices
    .filter((inv) => inv.status !== "PAID" && inv.status !== "CANCELLED")
    .reduce(
      (sum, inv) =>
        sum +
        ((parseFloat(inv.totalAmount) || 0) -
          (parseFloat(inv.paidAmount) || 0)),
      0
    );

  const totalPaid = invoices
    .filter((inv) => inv.status === "PAID")
    .reduce((sum, inv) => sum + (parseFloat(inv.totalAmount) || 0), 0);

  const overdueCount = invoices.filter((inv) =>
    isOverdue(inv.dueDate, inv.status)
  ).length;

  const columns = [
    {
      accessorKey: "invoiceNumber",
      header: "Invoice #",
      cell: ({ getValue }) => <span className="font-mono font-medium">{getValue()}</span>,
    },
    {
      accessorKey: "customerName",
      header: "Customer",
      cell: ({ getValue }) => getValue() || "-",
    },
    {
      accessorKey: "invoiceDate",
      header: "Date",
      cell: ({ getValue }) => (getValue() ? new Date(getValue()).toLocaleDateString() : "-"),
    },
    {
      accessorKey: "dueDate",
      header: "Due Date",
      cell: ({ getValue, row }) => {
        const overdue = isOverdue(getValue(), row.original.status);
        return (
          <span className={overdue ? "text-red-600 font-medium" : ""}>
            {getValue() ? new Date(getValue()).toLocaleDateString() : "-"}
            {overdue && <AlertCircle className="inline ml-1" size={14} />}
          </span>
        );
      },
    },
    {
      accessorKey: "totalAmount",
      header: "Amount",
      cell: ({ getValue }) => (
        <span className="font-semibold">
          ${parseFloat(getValue() || 0).toLocaleString()}
        </span>
      ),
    },
    {
      accessorKey: "paidAmount",
      header: "Paid",
      cell: ({ getValue }) => (
        <span className="text-green-600 dark:text-green-400">
          ₹{parseFloat(getValue() || 0).toLocaleString()}
        </span>
      ),
    },
    {
      accessorKey: "status",
      header: "Status",
      cell: ({ getValue }) => getStatusBadge(getValue()),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Invoices
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage customer invoices and payments
          </p>
        </div>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard
          title="Total Invoices"
          value={invoices.length}
          icon={FileText}
          accent="blue"
        />
        <MetricCard
          title="Paid"
          value={`₹${totalPaid.toLocaleString()}`}
          icon={DollarSign}
          accent="green"
        />
        <MetricCard
          title="Outstanding"
          value={`₹${totalOutstanding.toLocaleString()}`}
          icon={AlertCircle}
          accent="amber"
        />
        <MetricCard
          title="Overdue"
          value={overdueCount}
          icon={AlertCircle}
          accent="rose"
        />
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={filteredInvoices}
        loading={loading}
        emptyMessage="No invoices found"
        enableRowSelection
        searchPlaceholder="Search invoices..."
        filters={
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm cursor-pointer focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200 appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%236b7280%22%20stroke-width%3D%222%22%3E%3Cpath%20d%3D%22m6%209%206%206%206-6%22%2F%3E%3C%2Fsvg%3E')] bg-[length:16px] bg-[right_8px_center] bg-no-repeat pr-8"
          >
            <option value="ALL">All Status</option>
            <option value="DRAFT">Draft</option>
            <option value="SENT">Sent</option>
            <option value="PARTIAL">Partial Payment</option>
            <option value="PAID">Paid</option>
            <option value="OVERDUE">Overdue</option>
          </select>
        }
      />
    </div>
  );
};

export default Invoices;
