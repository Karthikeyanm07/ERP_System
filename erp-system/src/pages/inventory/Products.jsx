/**
 * Products Page - Inventory Module
 *
 * Backend DTO: ProductDTO
 * Required fields: productCode, name, unit
 *
 * Features:
 * - Search functionality
 * - Stock status filter (All/Low Stock/Out of Stock)
 * - Delete with confirmation
 * - Role-based access control
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useAuth } from "../../hooks/useAuth";
import { inventoryApi } from "../../api/inventoryApi";
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
  Package,
  Pencil,
  Trash2,
  RefreshCw,
  Filter,
  AlertTriangle,
  PackageX,
  Power,
  PowerOff,
} from "lucide-react";

const Products = () => {
  const { hasAnyRole } = useAuth();

  // Role-based permissions
  const canManageProducts = hasAnyRole(["ROLE_WAREHOUSE_STAFF", "ROLE_ADMIN"]);
  const canDeleteProducts = hasAnyRole(["ROLE_ADMIN"]);

  const [searchTerm, setSearchTerm] = useState("");
  const [stockFilter, setStockFilter] = useState("ALL");

  const initialData = {
    productCode: "",
    name: "",
    description: "",
    unit: "PCS",
    unitPrice: "",
    reorderLevel: "",
    categoryId: "",
    isActive: true,
  };

  const validate = (data) => {
    const errors = {};
    if (!data.productCode?.trim()) errors.productCode = "Product code is required";
    if (!data.name?.trim()) errors.name = "Product name is required";
    if (!data.unit?.trim()) errors.unit = "Unit is required";
    if (data.unitPrice && parseFloat(data.unitPrice) <= 0) {
      errors.unitPrice = "Price must be greater than 0";
    }
    return errors;
  };

  const {
    items: products,
    formData,
    errors,
    loading,
    formLoading,
    isModalOpen,
    setIsModalOpen,
    isDeleteDialogOpen,
    setIsDeleteDialogOpen,
    editingItem: editingProduct,
    itemToDelete: productToDelete,
    fetchItems: fetchProducts,
    handleChange,
    handleAdd,
    handleEdit,
    handleDelete,
    confirmDelete,
    handleSubmit: baseSubmit,
  } = useCrudForm({
    initialData,
    validate,
    api: inventoryApi,
    entityName: "Product",
  });

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleSubmit = (e) => {
    // Custom data transformation before submit
    const submitData = {
      ...formData,
      unitPrice: formData.unitPrice ? parseFloat(formData.unitPrice) : null,
      reorderLevel: formData.reorderLevel ? parseInt(formData.reorderLevel) : null,
      categoryId: formData.categoryId ? parseInt(formData.categoryId) : null,
    };
    // Sync back to internal hook state if needed, or pass directly
    // The hook uses formData internally. If we want to transform, we can either:
    // 1. Transform in the hook (added to hook)
    // 2. Wrap handleSubmit
    baseSubmit(e); 
  };

  const handleToggleStatus = async (product) => {
    try {
      await inventoryApi.updateProduct(product.id, {
        ...product,
        isActive: product.isActive === false,
      });
      fetchProducts();
    } catch (error) {
      logger.error("Error toggling status", error);
    }
  };

  const clearFilters = () => {
    setSearchTerm("");
    setStockFilter("ALL");
  };

  // Filter products
  const filteredProducts = products.filter((p) => {
    const matchesSearch =
      p.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.productCode?.toLowerCase().includes(searchTerm.toLowerCase());

    const stock = p.totalStock || 0;
    const reorder = p.reorderLevel || 10;

    let matchesStock = true;
    if (stockFilter === "LOW") {
      matchesStock = stock > 0 && stock <= reorder;
    } else if (stockFilter === "OUT") {
      matchesStock = stock <= 0;
    }

    return matchesSearch && matchesStock;
  });

  // Calculate stats
  const totalProducts = products.length;
  const lowStockCount = products.filter(
    (p) =>
      (p.totalStock || 0) > 0 && (p.totalStock || 0) <= (p.reorderLevel || 10)
  ).length;
  const outOfStockCount = products.filter(
    (p) => (p.totalStock || 0) <= 0
  ).length;
  const totalValue = products.reduce(
    (sum, p) => sum + (p.totalStock || 0) * (p.unitPrice || 0),
    0
  );

  const getStockBadge = (totalStock, reorderLevel) => {
    const stock = totalStock || 0;
    const level = reorderLevel || 10;
    if (stock <= 0) return <Badge variant="danger">Out of Stock</Badge>;
    if (stock <= level) return <Badge variant="warning">Low Stock</Badge>;
    return <Badge variant="success">In Stock</Badge>;
  };

  const columns = [
    {
      accessorKey: "name",
      header: "Product",
      size: 240,
      cell: ({ getValue, row }) => (
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-br from-blue-100 to-indigo-100 dark:from-blue-900/30 dark:to-indigo-900/30 rounded-lg flex items-center justify-center flex-shrink-0">
            <Package className="text-blue-600 dark:text-blue-400" size={20} />
          </div>
          <div className="min-w-0">
            <p className="font-medium text-gray-900 dark:text-gray-100 truncate">{getValue()}</p>
            <p className="text-xs text-gray-500 font-mono">{row.original.productCode}</p>
          </div>
        </div>
      ),
    },
    {
      accessorKey: "categoryName",
      header: "Category",
      size: 120,
      cell: ({ getValue }) =>
        getValue() ? (
          <Badge variant="default">{getValue()}</Badge>
        ) : (
          <span className="text-gray-400">—</span>
        ),
    },
    {
      accessorKey: "unit",
      header: "Unit",
      size: 80,
      cell: ({ getValue }) => <span className="text-gray-600 dark:text-gray-300">{getValue() || "—"}</span>,
    },
    {
      accessorKey: "unitPrice",
      header: "Price",
      size: 100,
      cell: ({ getValue }) => (
        <span className="font-semibold text-gray-900 dark:text-gray-100">
          ₹{parseFloat(getValue() || 0).toFixed(2)}
        </span>
      ),
    },
    {
      accessorKey: "totalStock",
      header: "Stock",
      size: 100,
      cell: ({ getValue, row }) => (
        <div>
          <span className="font-medium">{getValue() || 0}</span>
          <span className="text-gray-400 text-xs ml-1">{row.original.unit}</span>
        </div>
      ),
    },
    {
      accessorKey: "isActive",
      header: "Active",
      size: 100,
      cell: ({ getValue }) => (
        <Badge variant={getValue() !== false ? "success" : "default"} dot>
          {getValue() !== false ? "Active" : "Inactive"}
        </Badge>
      ),
    },
    {
      id: "stockStatus",
      header: "Status",
      size: 110,
      cell: ({ row }) => getStockBadge(row.original.totalStock, row.original.reorderLevel),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Products
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage your product catalog
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="outline" onClick={fetchProducts}>
            <RefreshCw size={18} />
          </Button>
          {canManageProducts && (
            <Button onClick={handleAdd}>
              <Plus size={20} />
              Add Product
            </Button>
          )}
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Card className="bg-gradient-to-br from-blue-50 to-indigo-50 border-blue-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">
                Total Products
              </p>
              <p className="text-2xl font-bold text-blue-600 mt-1">
                {totalProducts}
              </p>
            </div>
            <div className="p-3 bg-blue-100 rounded-xl">
              <Package className="text-blue-600" size={24} />
            </div>
          </div>
        </Card>
        <Card className="bg-gradient-to-br from-amber-50 to-yellow-50 border-amber-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">Low Stock</p>
              <p className="text-2xl font-bold text-amber-600 mt-1">
                {lowStockCount}
              </p>
            </div>
            <div className="p-3 bg-amber-100 rounded-xl">
              <AlertTriangle className="text-amber-600" size={24} />
            </div>
          </div>
        </Card>
        <Card className="bg-gradient-to-br from-red-50 to-rose-50 border-red-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">Out of Stock</p>
              <p className="text-2xl font-bold text-red-600 mt-1">
                {outOfStockCount}
              </p>
            </div>
            <div className="p-3 bg-red-100 rounded-xl">
              <PackageX className="text-red-600" size={24} />
            </div>
          </div>
        </Card>
        <Card className="bg-gradient-to-br from-green-50 to-emerald-50 border-green-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm font-medium">Stock Value</p>
              <p className="text-2xl font-bold text-green-600 mt-1">
                ₹{totalValue.toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-green-100 rounded-xl">
              <Package className="text-green-600" size={24} />
            </div>
          </div>
        </Card>
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={filteredProducts}
        loading={loading}
        emptyMessage="No products found"
        enableRowSelection
        searchPlaceholder="Search by name or code..."
        filters={
          <select
            value={stockFilter}
            onChange={(e) => setStockFilter(e.target.value)}
            className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm cursor-pointer focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200 appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%236b7280%22%20stroke-width%3D%222%22%3E%3Cpath%20d%3D%22m6%209%206%206%206-6%22%2F%3E%3C%2Fsvg%3E')] bg-[length:16px] bg-[right_8px_center] bg-no-repeat pr-8"
          >
            <option value="ALL">All Stock Levels</option>
            <option value="LOW">Low Stock Only</option>
            <option value="OUT">Out of Stock Only</option>
          </select>
        }
        actions={
          canManageProducts
            ? (row) => (
                <DropdownActions
                  actions={[
                    { label: "Edit Product", icon: Pencil, onClick: () => handleEdit(row) },
                    { 
                      label: row.isActive === false ? "Activate Product" : "Deactivate Product", 
                      icon: row.isActive === false ? Power : PowerOff, 
                      onClick: () => handleToggleStatus(row) 
                    },
                    ...(canDeleteProducts
                      ? [
                          { divider: true },
                          { label: "Delete Product", icon: Trash2, onClick: () => handleDelete(row), variant: "danger" },
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
        title="Delete Product"
        message={`Are you sure you want to delete "${productToDelete?.name}"? This action cannot be undone.`}
        confirmText="Delete"
        variant="danger"
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingProduct ? "Edit Product" : "Add Product"}
        size="lg"
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              {editingProduct ? "Update" : "Create"}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Product Code *"
              name="productCode"
              value={formData.productCode}
              onChange={handleChange}
              placeholder="PRD-001"
              error={errors.productCode}
            />
            <Input
              label="Product Name *"
              name="name"
              value={formData.name}
              onChange={handleChange}
              error={errors.name}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Unit *
              </label>
              <select
                name="unit"
                value={formData.unit}
                onChange={handleChange}
                className={`w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 ${
                  errors.unit ? "border-red-500" : "border-gray-300"
                }`}
              >
                <option value="PCS">PCS (Pieces)</option>
                <option value="KG">KG (Kilograms)</option>
                <option value="LTR">LTR (Liters)</option>
                <option value="MTR">MTR (Meters)</option>
                <option value="BOX">BOX</option>
                <option value="SET">SET</option>
              </select>
              {errors.unit && (
                <p className="text-red-500 text-xs mt-1">{errors.unit}</p>
              )}
            </div>
            <Input
              label="Unit Price"
              name="unitPrice"
              type="number"
              step="0.01"
              value={formData.unitPrice}
              onChange={handleChange}
              placeholder="99.99"
              error={errors.unitPrice}
            />
          </div>

          <Input
            label="Reorder Level"
            name="reorderLevel"
            type="number"
            value={formData.reorderLevel}
            onChange={handleChange}
            placeholder="10"
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={2}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {editingProduct && (
            <div className="flex items-center gap-2 pt-2">
              <input
                type="checkbox"
                name="isActive"
                id="isActive"
                checked={formData.isActive}
                onChange={handleChange}
                className="w-4 h-4 text-blue-600 rounded focus:ring-blue-500 dark:bg-gray-800 dark:border-gray-600"
              />
              <label
                htmlFor="isActive"
                className="text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                Active Product
              </label>
            </div>
          )}
        </form>
      </Modal>
    </div>
  );
};

export default Products;
