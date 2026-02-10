/**
 * Payments Page - Sales Module
 *
 * Backend DTO: PaymentCreateRequest
 * Required: paymentNumber, invoiceId, amount, paymentDate, paymentMethod
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { salesApi } from "../../api/salesApi";
import { useToast } from "../../components/common/Toast";
import DataTable from "../../components/common/DataTable";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Card from "../../components/common/Card";
import Badge from "../../components/common/Badge";
import MetricCard from "../../components/common/MetricCard";
import { Plus, CreditCard, DollarSign, Wallet } from "lucide-react";

const Payments = () => {
  const { execute, loading } = useApi();
  const toast = useToast();

  const [payments, setPayments] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const [formData, setFormData] = useState({
    paymentNumber: "",
    invoiceId: "",
    amount: "",
    paymentDate: new Date().toISOString().split("T")[0],
    paymentMethod: "BANK_TRANSFER",
    reference: "",
  });

  const paymentMethods = [
    { value: "CASH", label: "Cash" },
    { value: "BANK_TRANSFER", label: "Bank Transfer" },
    { value: "CREDIT_CARD", label: "Credit Card" },
    { value: "CHEQUE", label: "Cheque" },
    { value: "ONLINE", label: "Online Payment" },
  ];

  useEffect(() => {
    fetchPayments();
    fetchInvoices();
  }, []);

  const fetchPayments = async () => {
    try {
      const data = await execute(salesApi.getPayments);
      setPayments(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching payments", error);
      toast.error("Failed to load payments");
      setPayments([]);
    }
  };

  const fetchInvoices = async () => {
    try {
      const data = await execute(salesApi.getInvoices);
      // Only show unpaid invoices
      setInvoices(
        Array.isArray(data) ? data.filter((i) => i.status !== "PAID") : []
      );
    } catch (error) {
      setInvoices([]);
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
      paymentNumber: "",
      invoiceId: "",
      amount: "",
      paymentDate: new Date().toISOString().split("T")[0],
      paymentMethod: "BANK_TRANSFER",
      reference: "",
    });
    setErrors({});
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.paymentNumber.trim()) {
      newErrors.paymentNumber = "Payment number is required";
    }
    if (!formData.invoiceId) {
      newErrors.invoiceId = "Invoice is required";
    }
    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      newErrors.amount = "Amount must be greater than 0";
    }
    if (!formData.paymentDate) {
      newErrors.paymentDate = "Payment date is required";
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
      paymentNumber: formData.paymentNumber,
      invoiceId: parseInt(formData.invoiceId),
      amount: parseFloat(formData.amount),
      paymentDate: formData.paymentDate,
      paymentMethod: formData.paymentMethod,
      referenceNumber: formData.reference || null,
    };

    try {
      await execute(salesApi.createPayment, submitData);
      toast.success("Payment recorded successfully");
      await fetchPayments();
      await fetchInvoices(); // Refresh invoice list
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      logger.error("Error creating payment", error);
      toast.error(error.response?.data?.message || "Error recording payment");
    } finally {
      setFormLoading(false);
    }
  };

  const getMethodBadge = (method) => {
    const variants = {
      CASH: "success",
      BANK_TRANSFER: "primary",
      CREDIT_CARD: "purple",
      CHEQUE: "warning",
      ONLINE: "info",
    };
    return (
      <Badge variant={variants[method] || "default"}>
        {method?.replace("_", " ")}
      </Badge>
    );
  };

  const totalReceived = payments.reduce(
    (sum, p) => sum + (parseFloat(p.amount) || 0),
    0
  );

  // Group by payment method
  const byMethod = paymentMethods.reduce((acc, m) => {
    acc[m.value] = payments
      .filter((p) => p.paymentMethod === m.value)
      .reduce((sum, p) => sum + (parseFloat(p.amount) || 0), 0);
    return acc;
  }, {});

  const columns = [
    {
      accessorKey: "paymentNumber",
      header: "Payment #",
      cell: ({ getValue }) => <span className="font-mono font-medium">{getValue()}</span>,
    },
    {
      accessorKey: "invoiceNumber",
      header: "Invoice",
      cell: ({ getValue }) => getValue() || "-",
    },
    {
      accessorKey: "customerName",
      header: "Customer",
      cell: ({ getValue }) => getValue() || "-",
    },
    {
      accessorKey: "paymentDate",
      header: "Date",
      cell: ({ getValue }) => (getValue() ? new Date(getValue()).toLocaleDateString() : "-"),
    },
    {
      accessorKey: "paymentMethod",
      header: "Method",
      cell: ({ getValue }) => getMethodBadge(getValue()),
    },
    {
      accessorKey: "amount",
      header: "Amount",
      cell: ({ getValue }) => (
        <span className="font-semibold text-green-600 dark:text-green-400">
          ₹{parseFloat(getValue() || 0).toLocaleString()}
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
            Payments
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Record and track received payments
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(true)}>
          <Plus size={20} />
          Record Payment
        </Button>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard
          title="Total Received"
          value={`₹${totalReceived.toLocaleString()}`}
          icon={DollarSign}
          accent="green"
        />
        <MetricCard
          title="Bank Transfer"
          value={`₹${byMethod["BANK_TRANSFER"]?.toLocaleString() || 0}`}
          icon={Wallet}
          accent="blue"
        />
        <MetricCard
          title="Credit Card"
          value={`₹${byMethod["CREDIT_CARD"]?.toLocaleString() || 0}`}
          icon={CreditCard}
          accent="purple"
        />
        <MetricCard
          title="Cash"
          value={`₹${byMethod["CASH"]?.toLocaleString() || 0}`}
          icon={DollarSign}
          accent="amber"
        />
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={payments}
        loading={loading}
        emptyMessage="No payments found"
        enableRowSelection
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Record Payment"
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              Record
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Payment Number *"
              name="paymentNumber"
              value={formData.paymentNumber}
              onChange={handleChange}
              placeholder="PAY-001"
              error={errors.paymentNumber}
            />
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Invoice *
              </label>
              <select
                name="invoiceId"
                value={formData.invoiceId}
                onChange={handleChange}
                className={`w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 ${
                  errors.invoiceId ? "border-red-500" : "border-gray-300"
                }`}
              >
                <option value="">Select Invoice</option>
                {invoices.map((inv) => (
                  <option key={inv.id} value={inv.id}>
                    {inv.invoiceNumber} - ₹
                    {parseFloat(inv.totalAmount || 0).toLocaleString()}
                  </option>
                ))}
              </select>
              {errors.invoiceId && (
                <p className="text-red-500 text-xs mt-1">{errors.invoiceId}</p>
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
              label="Payment Date *"
              name="paymentDate"
              type="date"
              value={formData.paymentDate}
              onChange={handleChange}
              error={errors.paymentDate}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Payment Method
              </label>
              <select
                name="paymentMethod"
                value={formData.paymentMethod}
                onChange={handleChange}
                className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
              >
                {paymentMethods.map((m) => (
                  <option key={m.value} value={m.value}>
                    {m.label}
                  </option>
                ))}
              </select>
            </div>
            <Input
              label="Reference"
              name="reference"
              value={formData.reference}
              onChange={handleChange}
              placeholder="Transaction ID"
            />
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Payments;
