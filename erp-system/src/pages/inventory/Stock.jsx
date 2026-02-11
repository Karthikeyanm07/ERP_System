/**
 * Stock Overview Page - Inventory Module
 *
 * Displays current stock levels across all warehouses
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { inventoryApi } from "../../api/inventoryApi";
import { useToast } from "../../components/common/Toast";
import DataTable from "../../components/common/DataTable";
import Card from "../../components/common/Card";
import MetricCard from "../../components/common/MetricCard";
import Badge from "../../components/common/Badge";
import {
  Package,
  AlertTriangle,
  TrendingUp,
  Warehouse,
  Search,
  Filter,
} from "lucide-react";

const Stock = () => {
  const { execute, loading } = useApi();
  const toast = useToast();

  const [stock, setStock] = useState([]);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);

  // Filters
  const [searchTerm, setSearchTerm] = useState("");
  const [warehouseFilter, setWarehouseFilter] = useState("ALL");
  const [showLowStock, setShowLowStock] = useState(false);

  useEffect(() => {
    fetchStock();
    fetchProducts();
    fetchWarehouses();
  }, []);

  const fetchStock = async () => {
    try {
      const data = await execute(inventoryApi.getStock);
      setStock(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching stock", error);
      toast.error("Failed to load stock data");
      setStock([]);
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
    setWarehouseFilter("ALL");
    setShowLowStock(false);
  };

  // Use products data if stock endpoint returns empty (show product inventory)
  const baseData =
    stock.length > 0
      ? stock
      : products.map((p) => ({
          id: p.id,
          productName: p.name,
          productCode: p.productCode,
          quantity: p.totalStock || 0,
          unit: p.unit,
          reorderLevel: p.reorderLevel || 10,
          unitPrice: p.unitPrice,
          warehouseId: p.warehouseId,
          warehouseName: p.warehouseName,
        }));

  // Client-side combined filtering - allows combining multiple filters
  const displayData = baseData.filter((item) => {
    // Search filter (product name, code)
    if (searchTerm) {
      const name = (item.productName || item.name || "").toLowerCase();
      const code = (item.productCode || "").toLowerCase();
      if (
        !name.includes(searchTerm.toLowerCase()) &&
        !code.includes(searchTerm.toLowerCase())
      ) {
        return false;
      }
    }

    // Warehouse filter
    if (
      warehouseFilter !== "ALL" &&
      item.warehouseId !== parseInt(warehouseFilter)
    ) {
      return false;
    }

    // Low stock filter
    if (showLowStock) {
      const quantity = item.quantity || 0;
      const reorderLevel = item.reorderLevel || 10;
      if (quantity > reorderLevel) {
        return false;
      }
    }

    return true;
  });

  const getStockStatus = (quantity, reorderLevel) => {
    if (quantity <= 0) return { variant: "danger", label: "Out of Stock" };
    if (quantity <= reorderLevel)
      return { variant: "warning", label: "Low Stock" };
    return { variant: "success", label: "In Stock" };
  };

  // Calculate stats
  const totalItems = displayData.length;
  const lowStock = displayData.filter(
    (s) => (s.quantity || 0) <= (s.reorderLevel || 10) && (s.quantity || 0) > 0
  ).length;
  const outOfStock = displayData.filter((s) => (s.quantity || 0) <= 0).length;
  const totalValue = displayData.reduce(
    (sum, s) => sum + (s.quantity || 0) * (s.unitPrice || 0),
    0
  );

  const columns = [
    {
      id: "product",
      header: "Product",
      cell: ({ row }) => (
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gray-100 dark:bg-gray-700 rounded-lg flex items-center justify-center">
            <Package className="text-gray-400 dark:text-gray-500" size={20} />
          </div>
          <div>
            <p className="font-medium text-gray-900 dark:text-gray-100">
              {row.original.productName || row.original.name}
            </p>
            <p className="text-sm text-gray-500">{row.original.productCode}</p>
          </div>
        </div>
      ),
    },
    {
      accessorKey: "warehouseName",
      header: "Warehouse",
      cell: ({ getValue }) => getValue() || "-",
    },
    {
      accessorKey: "quantity",
      header: "Quantity",
      cell: ({ getValue, row }) => (
        <div>
          <span className="font-semibold text-gray-900 dark:text-gray-100">{getValue() || 0}</span>
          <span className="text-gray-400 ml-1">{row.original.unit || "PCS"}</span>
        </div>
      ),
    },
    {
      accessorKey: "reorderLevel",
      header: "Reorder Level",
      cell: ({ getValue }) => getValue() ?? "-",
    },
    {
      id: "value",
      header: "Value",
      cell: ({ row }) => {
        const value = (row.original.quantity || 0) * (row.original.unitPrice || 0);
        return <span className="font-medium">₹{value.toLocaleString()}</span>;
      },
    },
    {
      id: "stockStatus",
      header: "Status",
      cell: ({ row }) => {
        const { variant, label } = getStockStatus(
          row.original.quantity,
          row.original.reorderLevel
        );
        return <Badge variant={variant}>{label}</Badge>;
      },
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
          Stock Overview
        </h1>
        <p className="text-gray-500 dark:text-gray-400 mt-1">
          Monitor inventory levels across all locations
        </p>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard
          title="Total Items"
          value={totalItems}
          icon={Package}
          accent="blue"
        />
        <MetricCard
          title="Low Stock"
          value={lowStock}
          icon={AlertTriangle}
          accent="amber"
        />
        <MetricCard
          title="Out of Stock"
          value={outOfStock}
          icon={Package}
          accent="rose"
        />
        <MetricCard
          title="Inventory Value"
          value={`₹${totalValue.toLocaleString()}`}
          icon={TrendingUp}
          accent="green"
        />
      </div>

      {/* Stock Table */}
      <DataTable
        columns={columns}
        data={displayData}
        loading={loading}
        emptyMessage="No stock records found"
        searchPlaceholder="Search by product name or code..."
        filters={
          <>
            <select
              value={warehouseFilter}
              onChange={(e) => setWarehouseFilter(e.target.value)}
              className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm cursor-pointer focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200 appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%236b7280%22%20stroke-width%3D%222%22%3E%3Cpath%20d%3D%22m6%209%206%206%206-6%22%2F%3E%3C%2Fsvg%3E')] bg-[length:16px] bg-[right_8px_center] bg-no-repeat pr-8"
            >
              <option value="ALL">All Warehouses</option>
              {warehouses.map((w) => (
                <option key={w.id} value={w.id}>
                  {w.name}
                </option>
              ))}
            </select>
            <button
              onClick={() => setShowLowStock(!showLowStock)}
              className={`flex items-center gap-2 px-3 py-2 rounded-lg border text-sm transition-all duration-200 ${
                showLowStock
                  ? "bg-yellow-50 dark:bg-yellow-900/20 border-yellow-300 dark:border-yellow-600 text-yellow-700 dark:text-yellow-400"
                  : "bg-gray-50 dark:bg-gray-700/50 border-gray-200 dark:border-gray-600 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
              }`}
            >
              <AlertTriangle size={16} />
              {showLowStock ? "Low Stock" : "Low Stock"}
            </button>
          </>
        }
        enableRowSelection
      />
    </div>
  );
};

export default Stock;
