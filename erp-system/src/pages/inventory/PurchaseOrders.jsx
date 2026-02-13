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
import DataTable from "../../components/common/DataTable";
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
      bg: "bg-yellow-50 dark:bg-yellow-900/30",
      text: "text-yellow-700 dark:text-yellow-400",
      border: "border-yellow-200 dark:border-yellow-700",
      dot: "bg-yellow-500",
      hoverBg: "hover:bg-yellow-100 dark:hover:bg-yellow-900/50",
      next: ["APPROVE", "CANCEL"],
    },
    APPROVED: {
      bg: "bg-blue-50 dark:bg-blue-900/30",
      text: "text-blue-700 dark:text-blue-400",
      border: "border-blue-200 dark:border-blue-700",
      dot: "bg-blue-500",
      hoverBg: "hover:bg-blue-100 dark:hover:bg-blue-900/50",
      next: ["RECEIVE", "CANCEL"],
    },
    RECEIVED: {
      bg: "bg-green-50 dark:bg-green-900/30",
      text: "text-green-700 dark:text-green-400",
      border: "border-green-200 dark:border-green-700",
      dot: "bg-green-500",
      hoverBg: "hover:bg-green-100 dark:hover:bg-green-900/50",
      next: [],
    },
    CANCELLED: {
      bg: "bg-red-50 dark:bg-red-900/30",
      text: "text-red-700 dark:text-red-400",
      border: "border-red-200 dark:border-red-700",
      dot: "bg-red-500",
      hoverBg: "hover:bg-red-100 dark:hover:bg-red-900/50",
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
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [formData, setFormData] = useState({
    supplierId: "",
    warehouseId: "",
    orderDate: new Date().toISOString().split("T")[0],
    expectedDeliveryDate: "",
    items: [{ productId: "", quantity: 1, unitPrice: "" }],
  });

  // Confirmation dialog state
  const [confirmDialog, setConfirmDialog] = useState({
    isOpen: false,
    type: null,
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
    fetchProducts();
    fetchWarehouses();
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

  const fetchProducts = async () => {
    try {
      const data = await execute(inventoryApi.getProducts);
      setProducts(Array.isArray(data) ? data : []);
    } catch (error) {
      setProducts([]);
    }
  };

  const fetchWarehouses = async () => {
    try {
      const data = await execute(inventoryApi.getWarehouses);
      setWarehouses(Array.isArray(data) ? data : []);
    } catch (error) {
      setWarehouses([]);
    }
  };

  // Clear all filters
  const clearFilters = () => {
    setSearchTerm("");
    setStatusFilter("ALL");
    setSupplierFilter("ALL");
    setDateRange({ startDate: "", endDate: "" });
  };

  // Client-side combined filtering
  const filteredOrders = orders.filter((order) => {
    if (searchTerm) {
      const search = searchTerm.toLowerCase();
      const poMatch = (order.poNumber || "").toLowerCase().includes(search);
      const supplierMatch = (order.supplierName || order.supplier?.name || "")
        .toLowerCase()
        .includes(search);
      if (!poMatch && !supplierMatch) return false;
    }
    if (statusFilter !== "ALL" && order.status !== statusFilter) return false;
    if (
      supplierFilter !== "ALL" &&
      order.supplierId !== parseInt(supplierFilter)
    ) {
      return false;
    }
    if (dateRange.startDate && dateRange.endDate) {
      const orderDate = new Date(order.orderDate);
      const start = new Date(dateRange.startDate);
      const end = new Date(dateRange.endDate);
      if (orderDate < start || orderDate > end) return false;
    }
    return true;
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleItemChange = (index, field, value) => {
    const updatedItems = [...formData.items];
    updatedItems[index] = { ...updatedItems[index], [field]: value };
    setFormData((prev) => ({ ...prev, items: updatedItems }));
  };

  const addItem = () => {
    setFormData((prev) => ({
      ...prev,
      items: [...prev.items, { productId: "", quantity: 1, unitPrice: "" }],
    }));
  };

  const removeItem = (index) => {
    if (formData.items.length <= 1) return;
    const updatedItems = formData.items.filter((_, i) => i !== index);
    setFormData((prev) => ({ ...prev, items: updatedItems }));
  };

  const resetForm = () => {
    setFormData({
      supplierId: "",
      warehouseId: "",
      orderDate: new Date().toISOString().split("T")[0],
      expectedDeliveryDate: "",
      items: [{ productId: "", quantity: 1, unitPrice: "" }],
    });
    setErrors({});
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.supplierId) newErrors.supplierId = "Supplier is required";
    if (!formData.warehouseId) newErrors.warehouseId = "Warehouse is required";
    if (!formData.orderDate) newErrors.orderDate = "Order date is required";
    const hasValidItems = formData.items.some(
      (item) => item.productId && item.quantity > 0
    );
    if (!hasValidItems) newErrors.items = "At least one item is required";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    if (!validateForm()) return;
    setFormLoading(true);

    const submitData = {
      supplierId: parseInt(formData.supplierId),
      warehouseId: parseInt(formData.warehouseId),
      orderDate: formData.orderDate,
      expectedDeliveryDate: formData.expectedDeliveryDate || null,
      items: formData.items
        .filter((item) => item.productId && item.quantity > 0)
        .map((item) => ({
          productId: parseInt(item.productId),
          quantity: parseInt(item.quantity),
          unitPrice: parseFloat(item.unitPrice) || 0,
        })),
    };

    try {
      await execute(inventoryApi.createPurchaseOrder, submitData);
      await fetchOrders();
      setIsModalOpen(false);
      resetForm();
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
      accessorKey: "poNumber",
      header: "Order #",
      cell: ({ getValue }) => (
        <span className="font-medium text-blue-600 dark:text-blue-400">{getValue()}</span>
      ),
    },
    {
      id: "supplier",
      header: "Supplier",
      cell: ({ row }) => row.original.supplierName || row.original.supplier?.name || "-",
    },
    {
      accessorKey: "totalAmount",
      header: "Amount",
      cell: ({ getValue }) => (
        <span className="font-semibold">
          ₹{parseFloat(getValue() || 0).toLocaleString()}
        </span>
      ),
    },
    {
      accessorKey: "orderDate",
      header: "Order Date",
      cell: ({ getValue }) => (getValue() ? new Date(getValue()).toLocaleDateString() : "-"),
    },
    {
      accessorKey: "expectedDate",
      header: "Expected",
      cell: ({ getValue }) => (getValue() ? new Date(getValue()).toLocaleDateString() : "-"),
    },
    {
      accessorKey: "status",
      header: "Status",
      size: 150,
      cell: ({ getValue, row }) => (
        <StatusDropdown
          currentStatus={getValue()}
          onStatusChange={(action) => {
            if (action === "APPROVE") handleApprove(row.original.id);
            if (action === "RECEIVE") handleReceive(row.original.id);
            if (action === "CANCEL") handleCancel(row.original.id);
          }}
          onDelete={() => handleDelete(row.original.id)}
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

      {/* Table */}
      <DataTable
        columns={columns}
        data={filteredOrders}
        loading={loading}
        emptyMessage="No purchase orders found"
        enableRowSelection
        searchPlaceholder="Search PO# or supplier..."
        filters={
          <>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm cursor-pointer focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200 appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%236b7280%22%20stroke-width%3D%222%22%3E%3Cpath%20d%3D%22m6%209%206%206%206-6%22%2F%3E%3C%2Fsvg%3E')] bg-[length:16px] bg-[right_8px_center] bg-no-repeat pr-8"
            >
              <option value="ALL">All Statuses</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="RECEIVED">Received</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
            <select
              value={supplierFilter}
              onChange={(e) => setSupplierFilter(e.target.value)}
              className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm cursor-pointer focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200 appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%236b7280%22%20stroke-width%3D%222%22%3E%3Cpath%20d%3D%22m6%209%206%206%206-6%22%2F%3E%3C%2Fsvg%3E')] bg-[length:16px] bg-[right_8px_center] bg-no-repeat pr-8"
            >
              <option value="ALL">All Suppliers</option>
              {suppliers.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
            <div className="flex items-center gap-2">
              <input
                type="date"
                value={dateRange.startDate}
                onChange={(e) =>
                  setDateRange((prev) => ({ ...prev, startDate: e.target.value }))
                }
                className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200"
              />
              <span className="text-gray-400 dark:text-gray-500 text-xs">to</span>
              <input
                type="date"
                value={dateRange.endDate}
                onChange={(e) =>
                  setDateRange((prev) => ({ ...prev, endDate: e.target.value }))
                }
                className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200"
              />
            </div>
          </>
        }
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Create Purchase Order"
        size="lg"
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
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Supplier *
              </label>
              <select
                name="supplierId"
                value={formData.supplierId}
                onChange={handleChange}
                className={`w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 ${
                  errors.supplierId ? "border-red-500" : "border-gray-300"
                }`}
              >
                <option value="">Select Supplier</option>
                {suppliers.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
              {errors.supplierId && (
                <p className="text-red-500 text-xs mt-1">{errors.supplierId}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Warehouse *
              </label>
              <select
                name="warehouseId"
                value={formData.warehouseId}
                onChange={handleChange}
                className={`w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 ${
                  errors.warehouseId ? "border-red-500" : "border-gray-300"
                }`}
              >
                <option value="">Select Warehouse</option>
                {warehouses.map((w) => (
                  <option key={w.id} value={w.id}>
                    {w.name} ({w.location})
                  </option>
                ))}
              </select>
              {errors.warehouseId && (
                <p className="text-red-500 text-xs mt-1">{errors.warehouseId}</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Order Date *"
              name="orderDate"
              type="date"
              value={formData.orderDate}
              onChange={handleChange}
              error={errors.orderDate}
            />
            <Input
              label="Expected Delivery Date"
              name="expectedDeliveryDate"
              type="date"
              value={formData.expectedDeliveryDate}
              onChange={handleChange}
            />
          </div>

          {/* Order Items */}
          <div>
            <div className="flex justify-between items-center mb-2">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">
                Order Items *
              </label>
              <Button
                type="button"
                size="sm"
                variant="outline"
                onClick={addItem}
              >
                <Plus size={16} /> Add Item
              </Button>
            </div>
            {errors.items && (
              <p className="text-red-500 text-xs mb-2">{errors.items}</p>
            )}
            <div className="space-y-2">
              {formData.items.map((item, index) => (
                <div key={index} className="flex gap-2 items-end">
                  <div className="flex-1">
                    <select
                      value={item.productId}
                      onChange={(e) =>
                        handleItemChange(index, "productId", e.target.value)
                      }
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
                    >
                      <option value="">Select Product</option>
                      {products.map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <input
                    type="number"
                    placeholder="Qty"
                    value={item.quantity}
                    onChange={(e) =>
                      handleItemChange(index, "quantity", e.target.value)
                    }
                    className="w-20 px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
                  />
                  <input
                    type="number"
                    placeholder="Price"
                    value={item.unitPrice}
                    onChange={(e) =>
                      handleItemChange(index, "unitPrice", e.target.value)
                    }
                    className="w-24 px-3 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
                  />
                  <button
                    type="button"
                    onClick={() => removeItem(index)}
                    className="p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-900/30 rounded-lg"
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>
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
