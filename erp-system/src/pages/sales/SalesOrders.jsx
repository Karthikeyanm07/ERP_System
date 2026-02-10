/**
 * Sales Orders Page - Sales Module
 *
 * Backend DTO: SalesOrderCreateRequest
 * Required: orderNumber, customerId, warehouseId, orderDate, createdById, items[]
 */

import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { salesApi } from "../../api/salesApi";
import { inventoryApi } from "../../api/inventoryApi";
import { useToast } from "../../components/common/Toast";
import DataTable from "../../components/common/DataTable";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Card from "../../components/common/Card";
import { useState, useRef, useEffect } from "react";
import { createPortal } from "react-dom";
import {
  Plus,
  ShoppingCart,
  Filter,
  Check,
  Truck,
  PackageCheck,
  XCircle,
  Trash2,
  ChevronDown,
  Clock3,
  Banknote,
} from "lucide-react";
import MetricCard from "../../components/common/MetricCard";

/**
 * Custom Status Dropdown matching Employee page style
 */
const StatusDropdown = ({ currentStatus, onStatusChange, onDelete }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [position, setPosition] = useState({ top: 0, left: 0 });
  const buttonRef = useRef(null);

  const statusConfig = {
    PENDING: {
      bg: "bg-yellow-50",
      text: "text-yellow-700",
      border: "border-yellow-200",
      dot: "bg-yellow-500",
      hoverBg: "hover:bg-yellow-100",
      next: ["CONFIRM", "CANCEL"],
    },
    CONFIRMED: {
      bg: "bg-blue-50",
      text: "text-blue-700",
      border: "border-blue-200",
      dot: "bg-blue-500",
      hoverBg: "hover:bg-blue-100",
      next: ["SHIP", "CANCEL"],
    },
    SHIPPED: {
      bg: "bg-indigo-50",
      text: "text-indigo-700",
      border: "border-indigo-200",
      dot: "bg-indigo-500",
      hoverBg: "hover:bg-indigo-100",
      next: ["DELIVER"],
    },
    DELIVERED: {
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

  useEffect(() => {
    if (isOpen && buttonRef.current) {
      const rect = buttonRef.current.getBoundingClientRect();
      setPosition({
        top: rect.bottom + window.scrollY + 4,
        left: rect.left + window.scrollX,
      });
    }
  }, [isOpen]);

  const dropdownContent = isOpen && (
    <>
      <div className="fixed inset-0 z-40" onClick={() => setIsOpen(false)} />
      <div
        className="fixed bg-white rounded-lg shadow-xl border border-gray-200 py-1 min-w-[140px] z-50 overflow-hidden"
        style={{ top: `${position.top}px`, left: `${position.left}px` }}
      >
        <div className="px-3 py-1.5 text-[10px] font-bold text-gray-400 uppercase tracking-wider border-b border-gray-50">
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
              className="w-full flex items-center gap-2 px-3 py-2 text-xs font-medium text-gray-700 hover:bg-gray-50 transition-colors text-left"
            >
              {action === "CONFIRM" && (
                <Check size={14} className="text-blue-500" />
              )}
              {action === "SHIP" && (
                <Truck size={14} className="text-indigo-500" />
              )}
              {action === "DELIVER" && (
                <PackageCheck size={14} className="text-green-500" />
              )}
              {action === "CANCEL" && (
                <XCircle size={14} className="text-red-500" />
              )}
              {action.charAt(0) + action.slice(1).toLowerCase()}
            </button>
          ))
        ) : (
          <div className="px-3 py-2 text-xs text-gray-400 italic">
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
            className="w-full flex items-center gap-2 px-3 py-2 text-xs font-medium text-red-600 hover:bg-red-50 border-t border-gray-100 transition-colors text-left"
          >
            <Trash2 size={14} />
            Delete Order
          </button>
        )}
      </div>
    </>
  );

  return (
    <>
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
      {typeof document !== "undefined" &&
        createPortal(dropdownContent, document.body)}
    </>
  );
};

const SalesOrders = () => {
  const { execute, loading } = useApi();
  const toast = useToast();

  const [orders, setOrders] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const [formData, setFormData] = useState({
    orderNumber: "",
    customerId: "",
    orderDate: new Date().toISOString().split("T")[0],
    deliveryDate: "",
    items: [{ productId: "", quantity: 1, unitPrice: "" }],
  });

  useEffect(() => {
    fetchOrders();
    fetchCustomers();
    fetchProducts();
  }, []);

  const fetchOrders = async () => {
    try {
      const data = await execute(salesApi.getSalesOrders);
      setOrders(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching orders", error);
      toast.error("Failed to load sales orders");
      setOrders([]);
    }
  };

  const fetchCustomers = async () => {
    try {
      const data = await execute(salesApi.getCustomers);
      setCustomers(Array.isArray(data) ? data : []);
    } catch (error) {
      setCustomers([]);
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

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const handleItemChange = (index, field, value) => {
    setFormData((prev) => {
      const items = [...prev.items];
      items[index] = { ...items[index], [field]: value };

      // Auto-fill price when product selected
      if (field === "productId" && value) {
        const product = products.find((p) => p.id === parseInt(value));
        if (product) {
          items[index].unitPrice = product.unitPrice || "";
        }
      }

      return { ...prev, items };
    });
  };

  const addItem = () => {
    setFormData((prev) => ({
      ...prev,
      items: [...prev.items, { productId: "", quantity: 1, unitPrice: "" }],
    }));
  };

  const removeItem = (index) => {
    if (formData.items.length > 1) {
      setFormData((prev) => ({
        ...prev,
        items: prev.items.filter((_, i) => i !== index),
      }));
    }
  };

  const resetForm = () => {
    setFormData({
      orderNumber: "",
      customerId: "",
      orderDate: new Date().toISOString().split("T")[0],
      deliveryDate: "",
      items: [{ productId: "", quantity: 1, unitPrice: "" }],
    });
    setErrors({});
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.orderNumber.trim()) {
      newErrors.orderNumber = "Order number is required";
    }
    if (!formData.customerId) {
      newErrors.customerId = "Customer is required";
    }
    if (!formData.orderDate) {
      newErrors.orderDate = "Order date is required";
    }

    const hasValidItems = formData.items.some(
      (item) => item.productId && item.quantity > 0
    );
    if (!hasValidItems) {
      newErrors.items = "At least one item is required";
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
      orderNumber: formData.orderNumber,
      customerId: parseInt(formData.customerId),
      warehouseId: 1, // Default warehouse
      orderDate: formData.orderDate,
      deliveryDate: formData.deliveryDate || null,
      createdById: 1, // Default user
      items: formData.items
        .filter((item) => item.productId && item.quantity > 0)
        .map((item) => ({
          productId: parseInt(item.productId),
          quantity: parseInt(item.quantity),
          unitPrice: parseFloat(item.unitPrice) || 0,
        })),
    };

    try {
      await execute(salesApi.createSalesOrder, submitData);
      toast.success("Sales order created successfully");
      await fetchOrders();
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      logger.error("Error creating order", error);
      toast.error(error.response?.data?.message || "Error creating order");
    } finally {
      setFormLoading(false);
    }
  };

  const handleConfirm = async (id) => {
    try {
      await execute(salesApi.confirmSalesOrder, id);
      toast.success("Order confirmed successfully");
      await fetchOrders();
    } catch (error) {
      toast.error(error.response?.data?.message || "Error confirming order");
    }
  };

  const handleShip = async (id) => {
    try {
      await execute(salesApi.shipSalesOrder, id);
      toast.success("Order shipped successfully");
      await fetchOrders();
    } catch (error) {
      toast.error(error.response?.data?.message || "Error shipping order");
    }
  };

  const handleDeliver = async (id) => {
    try {
      await execute(salesApi.deliverSalesOrder, id);
      toast.success("Order delivered successfully");
      await fetchOrders();
    } catch (error) {
      toast.error(error.response?.data?.message || "Error delivering order");
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Are you sure you want to cancel this order?")) return;
    try {
      await execute(salesApi.cancelSalesOrder, id);
      toast.success("Order cancelled");
      await fetchOrders();
    } catch (error) {
      toast.error(error.response?.data?.message || "Error cancelling order");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this order?")) return;
    try {
      await execute(salesApi.deleteSalesOrder, id);
      toast.success("Order deleted");
      await fetchOrders();
    } catch (error) {
      toast.error(error.response?.data?.message || "Error deleting order");
    }
  };

  const filteredOrders = orders.filter(
    (o) => statusFilter === "ALL" || o.status === statusFilter
  );

  // Calculate totals
  const totalRevenue = orders
    .filter((o) => o.status === "DELIVERED" || o.status === "CONFIRMED")
    .reduce((sum, o) => sum + (parseFloat(o.totalAmount) || 0), 0);
  const pendingCount = orders.filter((o) => o.status === "PENDING").length;
  const deliveredCount = orders.filter((o) => o.status === "DELIVERED").length;

  const columns = [
    {
      accessorKey: "orderNumber",
      header: "Order #",
      cell: ({ getValue }) => <span className="font-mono font-medium">{getValue()}</span>,
    },
    {
      accessorKey: "customerName",
      header: "Customer",
      cell: ({ getValue }) => getValue() || "-",
    },
    {
      accessorKey: "orderDate",
      header: "Date",
      cell: ({ getValue }) => (getValue() ? new Date(getValue()).toLocaleDateString() : "-"),
    },
    {
      accessorKey: "totalAmount",
      header: "Total",
      cell: ({ getValue }) => (
        <span className="font-semibold">
          ₹{parseFloat(getValue() || 0).toLocaleString()}
        </span>
      ),
    },
    {
      accessorKey: "status",
      header: "Status",
      size: 150,
      cell: ({ getValue, row }) => (
        <StatusDropdown
          currentStatus={getValue()}
          onStatusChange={(action) => {
            if (action === "CONFIRM") handleConfirm(row.original.id);
            if (action === "SHIP") handleShip(row.original.id);
            if (action === "DELIVER") handleDeliver(row.original.id);
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
            Sales Orders
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage customer orders
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(true)}>
          <Plus size={20} />
          New Order
        </Button>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard
          title="Total Orders"
          value={orders.length}
          icon={ShoppingCart}
          accent="blue"
        />
        <MetricCard
          title="Pending"
          value={pendingCount}
          icon={Clock3}
          accent="amber"
        />
        <MetricCard
          title="Delivered"
          value={deliveredCount}
          icon={PackageCheck}
          accent="green"
        />
        <MetricCard
          title="Revenue"
          value={`₹${totalRevenue.toLocaleString()}`}
          icon={Banknote}
          accent="purple"
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
            <option value="PENDING">Pending</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="SHIPPED">Shipped</option>
            <option value="DELIVERED">Delivered</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </Card>

      {/* Table */}
      <DataTable
        columns={columns}
        data={filteredOrders}
        loading={loading}
        emptyMessage="No orders found"
        enableRowSelection
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Create Sales Order"
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
            <Input
              label="Order Number *"
              name="orderNumber"
              value={formData.orderNumber}
              onChange={handleChange}
              placeholder="SO-001"
              error={errors.orderNumber}
            />
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Customer *
              </label>
              <select
                name="customerId"
                value={formData.customerId}
                onChange={handleChange}
                className={`w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 ${
                  errors.customerId ? "border-red-500" : "border-gray-300"
                }`}
              >
                <option value="">Select Customer</option>
                {customers.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} ({c.customerCode})
                  </option>
                ))}
              </select>
              {errors.customerId && (
                <p className="text-red-500 text-xs mt-1">{errors.customerId}</p>
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
              label="Delivery Date"
              name="deliveryDate"
              type="date"
              value={formData.deliveryDate}
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
                    className="w-20 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                  <input
                    type="number"
                    placeholder="Price"
                    value={item.unitPrice}
                    onChange={(e) =>
                      handleItemChange(index, "unitPrice", e.target.value)
                    }
                    className="w-24 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                  <button
                    type="button"
                    onClick={() => removeItem(index)}
                    className="p-2 text-red-500 hover:bg-red-50 rounded-lg"
                  >
                    ×
                  </button>
                </div>
              ))}
            </div>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default SalesOrders;
