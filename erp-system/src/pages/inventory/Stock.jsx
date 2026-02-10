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
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Search */}
          <div className="relative">
            <Search
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500"
              size={20}
            />
            <input
              type="text"
              placeholder="Search by product name or code..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 dark:placeholder-gray-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
            />
          </div>

          {/* Warehouse Filter */}
          <select
            value={warehouseFilter}
            onChange={(e) => setWarehouseFilter(e.target.value)}
            className="px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-md focus:outline-none focus:border-gray-500 dark:focus:border-gray-400 transition-all duration-200 text-sm cursor-pointer"
          >
            <option value="ALL">All Warehouses</option>
            {warehouses.map((w) => (
              <option key={w.id} value={w.id}>
                {w.name}
              </option>
            ))}
          </select>

          {/* Low Stock Toggle */}
          <button
            onClick={() => setShowLowStock(!showLowStock)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg border transition-colors ${
              showLowStock
                ? "bg-yellow-50 border-yellow-300 text-yellow-700"
                : "bg-white border-gray-300 text-gray-700 hover:bg-gray-50"
            }`}
          >
            <AlertTriangle size={18} />
            {showLowStock ? "Showing Low Stock Only" : "Show Low Stock Only"}
          </button>
        </div>
      </Card>

      {/* Stock Table */}
      <DataTable
        columns={columns}
        data={displayData}
        loading={loading}
        emptyMessage="No stock data available"
        enableRowSelection
      />
    </div>
  );
};

export default Stock;
