/**
 * Purchase Orders Page - Inventory Module
 *
 * Explanation:
 * - Create and manage purchase orders to suppliers
 * - Track order status (Pending, Received, Cancelled)
 * - Receive POs to update stock
 */

import { useState, useEffect, useRef } from "react";
import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { inventoryApi } from "../../api/inventoryApi";
import Table from "../../components/common/Table";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Card from "../../components/common/Card";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import { createPortal } from "react-dom";
import {
  Plus,
  Check,
  ChevronDown,
  ThumbsUp,
  XCircle,
  Trash2,
  Search,
  Filter,
  Calendar,
  Clock3,
  ClipboardList,
  PackageCheck,
} from "lucide-react";
import MetricCard from "../../components/common/MetricCard";

/**
 * Custom Status Dropdown matching Employee page style
 */
const StatusDropdown = ({ currentStatus, onStatusChange, onDelete }) => {
  const [isOpen, setIsOpen] = useState(false);
  const buttonRef = useRef(null);
  const dropdownRef = useRef(null);

  const statusConfig = {
    PENDING: {
      bg: "bg-yellow-50",
      text: "text-yellow-700",
      border: "border-yellow-200",
      dot: "bg-yellow-500",
      hoverBg: "hover:bg-yellow-100",
      next: ["APPROVE", "CANCEL"],
    },
    APPROVED: {
      bg: "bg-blue-50",
      text: "text-blue-700",
      border: "border-blue-200",
      dot: "bg-blue-500",
      hoverBg: "hover:bg-blue-100",
      next: ["RECEIVE", "CANCEL"],
    },
    RECEIVED: {
      bg: "bg-green-50",
      text: "text-green-700",
      border: "border-green-200",
      dot: "bg-green-500",
      hoverBg: "hover:bg-green-100",
      next: [],
    },
    CANCELLED: {
      bg: "bg-red-50",
      text: "text-red-700",
      border: "border-red-200",
      dot: "bg-red-500",
      hoverBg: "hover:bg-red-100",
      next: [],
    },
  };

  const currentConfig = statusConfig[currentStatus] || statusConfig.PENDING;
  const nextSteps = currentConfig.next;

  // Calculate position when dropdown opens
  const getDropdownStyle = () => {
    if (!buttonRef.current) return {};

    const rect = buttonRef.current.getBoundingClientRect();
    const dropdownHeight = 160;
    const viewportHeight = window.innerHeight;
    const viewportWidth = window.innerWidth;

    // Check if dropdown would overflow bottom
    const wouldOverflowBottom = rect.bottom + dropdownHeight > viewportHeight;
    // Check if dropdown would overflow right
    const wouldOverflowRight = rect.left + 150 > viewportWidth;

    return {
      position: "fixed",
      top: wouldOverflowBottom
        ? `${rect.top - dropdownHeight - 4}px` // Position above
        : `${rect.bottom + 4}px`, // Position below
      left: wouldOverflowRight
        ? `${rect.right - 150}px` // Align right edge
        : `${rect.left}px`, // Align left edge
      zIndex: 9999,
    };
  };

  // Close on outside click
  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (e) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target) &&
        buttonRef.current &&
        !buttonRef.current.contains(e.target)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen]);

  return (
    <div className="relative inline-block">
      <button
        ref={buttonRef}
        onClick={(e) => {
          e.stopPropagation();
          if (nextSteps.length > 0 || currentStatus === "PENDING") {
            setIsOpen(!isOpen);
          }
        }}
        className={`inline-flex items-center gap-1.5 text-xs font-medium border rounded-md px-2.5 py-1 transition-all ${
          currentConfig.bg
        } ${currentConfig.text} ${currentConfig.border} ${
          nextSteps.length > 0 || currentStatus === "PENDING"
            ? `${currentConfig.hoverBg} cursor-pointer`
            : "cursor-default"
        }`}
      >
        <span
          className={`w-1.5 h-1.5 rounded-full ${currentConfig.dot}`}
        ></span>
        {currentStatus}
        {(nextSteps.length > 0 || currentStatus === "PENDING") && (
          <ChevronDown
            size={12}
            className={`ml-0.5 transition-transform ${
              isOpen ? "rotate-180" : ""
            }`}
          />
        )}
      </button>

      {isOpen &&
        createPortal(
          <>
            <div
              className="fixed inset-0"
              style={{ zIndex: 9998 }}
              onClick={() => setIsOpen(false)}
            />
            <div
              ref={dropdownRef}
              className="bg-white dark:bg-gray-800 rounded-lg shadow-xl border border-gray-200 dark:border-gray-700 py-1 min-w-[140px] overflow-hidden"
              style={getDropdownStyle()}
            >
              <div className="px-3 py-1.5 text-[10px] font-bold text-gray-400 dark:text-gray-500 uppercase tracking-wider border-b border-gray-50 dark:border-gray-700">
                Available Actions
              </div>

              {nextSteps.length > 0 ? (
                nextSteps.map((action) => (
                  <button
                    key={action}
                    onClick={(e) => {
                      e.stopPropagation();
                      onStatusChange(action);
                      setIsOpen(false);
                    }}
                    className="w-full flex items-center gap-2 px-3 py-2 text-xs font-medium text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors text-left"
                  >
                    {action === "APPROVE" && (
                      <ThumbsUp size={14} className="text-blue-500" />
                    )}
                    {action === "RECEIVE" && (
                      <Check size={14} className="text-green-500" />
                    )}
                    {action === "CANCEL" && (
                      <XCircle size={14} className="text-red-500" />
                    )}
                    {action.charAt(0) + action.slice(1).toLowerCase()}
                  </button>
                ))
              ) : (
                <div className="px-3 py-2 text-xs text-gray-400 dark:text-gray-500 italic">
                  No further actions
                </div>
              )}

              {currentStatus === "PENDING" && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete();
                    setIsOpen(false);
                  }}
                  className="w-full flex items-center gap-2 px-3 py-2 text-xs font-medium text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/30 border-t border-gray-100 dark:border-gray-700 transition-colors text-left"
                >
                  <Trash2 size={14} />
                  Delete Order
                </button>
              )}
            </div>
          </>,
          document.body
        )}
    </div>
  );
};

const PurchaseOrders = () => {
  const { execute, loading } = useApi();

  const [orders, setOrders] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [formData, setFormData] = useState({
    supplierId: "",
    items: "",
    totalAmount: "",
    expectedDate: "",
    notes: "",
  });

  // Confirmation dialog state
  const [confirmDialog, setConfirmDialog] = useState({
    isOpen: false,
    type: null, // 'cancel' or 'delete'
    orderId: null,
    title: "",
    message: "",
  });

  // Filters
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [supplierFilter, setSupplierFilter] = useState("ALL");
  const [dateRange, setDateRange] = useState({ startDate: "", endDate: "" });

  useEffect(() => {
    fetchOrders();
    fetchSuppliers();
  }, []);

  const fetchOrders = async () => {
    try {
      const data = await execute(inventoryApi.getPurchaseOrders);
      setOrders(data || []);
    } catch (error) {
      logger.error("Error fetching orders:", error);
    }
  };

  const fetchSuppliers = async () => {
    try {
      const data = await execute(inventoryApi.getSuppliers);
      setSuppliers(data || []);
    } catch (error) {
      logger.error("Error fetching suppliers:", error);
    }
  };

  // Clear all filters
  const clearFilters = () => {
    setSearchTerm("");
    setStatusFilter("ALL");
    setSupplierFilter("ALL");
    setDateRange({ startDate: "", endDate: "" });
  };

  // Client-side combined filtering - allows combining multiple filters
  const filteredOrders = orders.filter((order) => {
    // Search filter (PO number, supplier name)
    if (searchTerm) {
      const search = searchTerm.toLowerCase();
      const poMatch = (order.poNumber || "").toLowerCase().includes(search);
      const supplierMatch = (order.supplierName || order.supplier?.name || "")
        .toLowerCase()
        .includes(search);
      if (!poMatch && !supplierMatch) return false;
    }

    // Status filter
    if (statusFilter !== "ALL" && order.status !== statusFilter) {
      return false;
    }

    // Supplier filter
    if (
      supplierFilter !== "ALL" &&
      order.supplierId !== parseInt(supplierFilter)
    ) {
      return false;
    }

    // Date range filter
    if (dateRange.startDate && dateRange.endDate) {
      const orderDate = new Date(order.orderDate);
      const start = new Date(dateRange.startDate);
      const end = new Date(dateRange.endDate);
      if (orderDate < start || orderDate > end) {
        return false;
      }
    }

    return true;
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);

    try {
      await execute(inventoryApi.createPurchaseOrder, formData);
      await fetchOrders();
      setIsModalOpen(false);
      setFormData({
        supplierId: "",
        items: "",
        totalAmount: "",
        expectedDate: "",
        notes: "",
      });
    } catch (error) {
      logger.error("Error creating order:", error);
    } finally {
      setFormLoading(false);
    }
  };

  const handleReceive = async (id) => {
    try {
      await execute(inventoryApi.receivePurchaseOrder, id);
      await fetchOrders();
    } catch (error) {
      logger.error("Error receiving order:", error);
    }
  };

  const handleApprove = async (id) => {
    try {
      await execute(inventoryApi.approvePurchaseOrder, id);
      await fetchOrders();
    } catch (error) {
      logger.error("Error approving order:", error);
    }
  };

  const handleCancel = async (id) => {
    setConfirmDialog({
      isOpen: true,
      type: "cancel",
      orderId: id,
      title: "Cancel Purchase Order",
      message:
        "Are you sure you want to cancel this order? This action cannot be undone.",
    });
  };

  const handleDelete = async (id) => {
    setConfirmDialog({
      isOpen: true,
      type: "delete",
      orderId: id,
      title: "Delete Purchase Order",
      message:
        "Are you sure you want to delete this order? This action cannot be undone.",
    });
  };

  const handleConfirmAction = async () => {
    const { type, orderId } = confirmDialog;
    try {
      if (type === "cancel") {
        await execute(inventoryApi.cancelPurchaseOrder, orderId);
      } else if (type === "delete") {
        await execute(inventoryApi.deletePurchaseOrder, orderId);
      }
      await fetchOrders();
    } catch (error) {
      logger.error(`Error ${type}ing order:`, error);
    } finally {
      setConfirmDialog({
        isOpen: false,
        type: null,
        orderId: null,
        title: "",
        message: "",
      });
    }
  };

  // Stats
  const pending = orders.filter((o) => o.status === "PENDING").length;
  const approved = orders.filter((o) => o.status === "APPROVED").length;
  const received = orders.filter((o) => o.status === "RECEIVED").length;

  const columns = [
    {
      key: "poNumber",
      header: "Order #",
      render: (value) => (
        <span className="font-medium text-blue-600">{value}</span>
      ),
    },
    {
      key: "supplier",
      header: "Supplier",
      render: (_, row) => row.supplierName || row.supplier?.name || "-",
    },
    {
      key: "totalAmount",
      header: "Amount",
      render: (value) => (
        <span className="font-semibold">
          ₹{parseFloat(value || 0).toLocaleString()}
        </span>
      ),
    },
    {
      key: "orderDate",
      header: "Order Date",
      render: (value) => (value ? new Date(value).toLocaleDateString() : "-"),
    },
    {
      key: "expectedDate",
      header: "Expected",
      render: (value) => (value ? new Date(value).toLocaleDateString() : "-"),
    },
    {
      key: "status",
      header: "Status",
      width: "150px",
      render: (value, row) => (
        <StatusDropdown
          currentStatus={value}
          onStatusChange={(action) => {
            if (action === "APPROVE") handleApprove(row.id);
            if (action === "RECEIVE") handleReceive(row.id);
            if (action === "CANCEL") handleCancel(row.id);
          }}
          onDelete={() => handleDelete(row.id)}
        />
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Purchase Orders
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage orders from suppliers
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(true)}>
          <Plus size={20} />
          Create Order
        </Button>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard
          title="Total Orders"
          value={orders.length}
          icon={ClipboardList}
          accent="blue"
        />
        <MetricCard
          title="Pending"
          value={pending}
          icon={Clock3}
          accent="amber"
        />
        <MetricCard
          title="Approved"
          value={approved}
          icon={Check}
          accent="purple"
        />
        <MetricCard
          title="Received"
          value={received}
          icon={PackageCheck}
          accent="green"
        />
      </div>

      {/* Filters */}
      <Card padding={false} className="p-4">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Filter size={18} className="text-gray-500" />
            <span className="font-medium text-gray-700">Filters</span>
          </div>
          <button
            onClick={clearFilters}
            className="text-sm text-blue-600 hover:underline"
          >
            Clear All
          </button>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Search by PO Number */}
          <div className="relative">
            <Search
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500"
              size={20}
            />
            <input
              type="text"
              placeholder="Search PO# or supplier..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 dark:placeholder-gray-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
            />
          </div>

          {/* Status Filter */}
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
          >
            <option value="ALL">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="APPROVED">Approved</option>
            <option value="RECEIVED">Received</option>
            <option value="CANCELLED">Cancelled</option>
          </select>

          {/* Supplier Filter */}
          <select
            value={supplierFilter}
            onChange={(e) => setSupplierFilter(e.target.value)}
            className="px-4 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
          >
            <option value="ALL">All Suppliers</option>
            {suppliers.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>

          {/* Date Range */}
          <div className="flex items-center gap-2">
            <input
              type="date"
              value={dateRange.startDate}
              onChange={(e) =>
                setDateRange((prev) => ({ ...prev, startDate: e.target.value }))
              }
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            />
            <span className="text-gray-400">to</span>
            <input
              type="date"
              value={dateRange.endDate}
              onChange={(e) =>
                setDateRange((prev) => ({ ...prev, endDate: e.target.value }))
              }
              className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            />
          </div>
        </div>
      </Card>

      {/* Table */}
      <Table
        columns={columns}
        data={filteredOrders}
        loading={loading}
        emptyMessage="No purchase orders found"
        actions={null}
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Create Purchase Order"
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              Create Order
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Supplier
            </label>
            <select
              name="supplierId"
              value={formData.supplierId}
              onChange={handleChange}
              className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
              required
            >
              <option value="">Select Supplier</option>
              {suppliers.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Total Amount"
              name="totalAmount"
              type="number"
              value={formData.totalAmount}
              onChange={handleChange}
              required
            />
            <Input
              label="Expected Date"
              name="expectedDate"
              type="date"
              value={formData.expectedDate}
              onChange={handleChange}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Notes
            </label>
            <textarea
              name="notes"
              value={formData.notes}
              onChange={handleChange}
              rows={3}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Order details, items, etc."
            />
          </div>
        </form>
      </Modal>

      {/* Confirmation Dialog */}
      <ConfirmDialog
        isOpen={confirmDialog.isOpen}
        onClose={() =>
          setConfirmDialog({
            isOpen: false,
            type: null,
            orderId: null,
            title: "",
            message: "",
          })
        }
        onConfirm={handleConfirmAction}
        title={confirmDialog.title}
        message={confirmDialog.message}
        confirmText={
          confirmDialog.type === "delete" ? "Delete" : "Cancel Order"
        }
        variant="danger"
      />
    </div>
  );
};

export default PurchaseOrders;
