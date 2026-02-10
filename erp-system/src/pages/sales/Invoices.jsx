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

      {/* Filter */}
      <Card padding={false} className="p-4">
        <div className="flex items-center gap-4">
          <Filter size={20} className="text-gray-400 dark:text-gray-500" />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-md focus:outline-none focus:border-gray-500 dark:focus:border-gray-400 transition-all duration-200 text-sm cursor-pointer"
          >
            <option value="ALL">All Status</option>
            <option value="DRAFT">Draft</option>
            <option value="SENT">Sent</option>
            <option value="PARTIAL">Partial Payment</option>
            <option value="PAID">Paid</option>
            <option value="OVERDUE">Overdue</option>
          </select>
        </div>
      </Card>

      {/* Table */}
      <DataTable
        columns={columns}
        data={filteredInvoices}
        loading={loading}
        emptyMessage="No invoices found"
        enableRowSelection
      />
    </div>
  );
};

export default Invoices;
