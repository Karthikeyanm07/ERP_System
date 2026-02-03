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
import { useApi } from "../../hooks/useApi";
import { useAuth } from "../../hooks/useAuth";
import { inventoryApi } from "../../api/inventoryApi";
import { useToast } from "../../components/common/Toast";
import Table from "../../components/common/Table";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Card from "../../components/common/Card";
import Badge from "../../components/common/Badge";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import {
  Plus,
  Package,
  Pencil,
  Search,
  Trash2,
  RefreshCw,
  Filter,
  AlertTriangle,
  PackageX,
} from "lucide-react";

const Products = () => {
  const { execute, loading } = useApi();
  const { hasAnyRole } = useAuth();
  const toast = useToast();

  // Role-based permissions
  const canManageProducts = hasAnyRole(["ROLE_WAREHOUSE_STAFF", "ROLE_ADMIN"]);
  const canDeleteProducts = hasAnyRole(["ROLE_ADMIN"]);

  const [products, setProducts] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [stockFilter, setStockFilter] = useState("ALL");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [productToDelete, setProductToDelete] = useState(null);
  const [editingProduct, setEditingProduct] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Form data matching ProductDTO
  const [formData, setFormData] = useState({
    productCode: "",
    name: "",
    description: "",
    unit: "PCS",
    unitPrice: "",
    reorderLevel: "",
    categoryId: "",
  });

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      const data = await execute(inventoryApi.getProducts);
      setProducts(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching products", error);
      toast.error("Failed to load products");
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

  const resetForm = () => {
    setFormData({
      productCode: "",
      name: "",
      description: "",
      unit: "PCS",
      unitPrice: "",
      reorderLevel: "",
      categoryId: "",
    });
    setErrors({});
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.productCode.trim()) {
      newErrors.productCode = "Product code is required";
    }
    if (!formData.name.trim()) {
      newErrors.name = "Product name is required";
    }
    if (!formData.unit.trim()) {
      newErrors.unit = "Unit is required";
    }
    if (formData.unitPrice && parseFloat(formData.unitPrice) <= 0) {
      newErrors.unitPrice = "Price must be greater than 0";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleAdd = () => {
    setEditingProduct(null);
    resetForm();
    setIsModalOpen(true);
  };

  const handleEdit = (product) => {
    setEditingProduct(product);
    setFormData({
      productCode: product.productCode || "",
      name: product.name || "",
      description: product.description || "",
      unit: product.unit || "PCS",
      unitPrice: product.unitPrice || "",
      reorderLevel: product.reorderLevel || "",
      categoryId: product.categoryId || "",
    });
    setErrors({});
    setIsModalOpen(true);
  };

  const handleDelete = (product) => {
    setProductToDelete(product);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!productToDelete) return;

    try {
      await inventoryApi.deleteProduct(productToDelete.id);
      toast.success("Product deleted successfully");
      await fetchProducts();
      setIsDeleteDialogOpen(false);
      setProductToDelete(null);
    } catch (error) {
      toast.error(error.response?.data?.message || "Error deleting product");
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
      unitPrice: formData.unitPrice ? parseFloat(formData.unitPrice) : null,
      reorderLevel: formData.reorderLevel
        ? parseInt(formData.reorderLevel)
        : null,
      categoryId: formData.categoryId ? parseInt(formData.categoryId) : null,
    };

    try {
      if (editingProduct) {
        await inventoryApi.updateProduct(editingProduct.id, submitData);
        toast.success("Product updated successfully");
      } else {
        await inventoryApi.createProduct(submitData);
        toast.success("Product created successfully");
      }
      await fetchProducts();
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      logger.error("Error saving product", error);
      toast.error(error.response?.data?.message || "Error saving product");
    } finally {
      setFormLoading(false);
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
      key: "name",
      header: "Product",
      width: "240px",
      render: (value, row) => (
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-br from-blue-100 to-indigo-100 rounded-lg flex items-center justify-center flex-shrink-0">
            <Package className="text-blue-600" size={20} />
          </div>
          <div className="min-w-0">
            <p className="font-medium text-gray-900 truncate">{value}</p>
            <p className="text-xs text-gray-500 font-mono">{row.productCode}</p>
          </div>
        </div>
      ),
    },
    {
      key: "categoryName",
      header: "Category",
      width: "120px",
      render: (value) =>
        value ? (
          <Badge variant="default">{value}</Badge>
        ) : (
          <span className="text-gray-400">—</span>
        ),
    },
    {
      key: "unit",
      header: "Unit",
      width: "80px",
      render: (value) => <span className="text-gray-600">{value || "—"}</span>,
    },
    {
      key: "unitPrice",
      header: "Price",
      width: "100px",
      render: (value) => (
        <span className="font-semibold text-gray-900">
          ${parseFloat(value || 0).toFixed(2)}
        </span>
      ),
    },
    {
      key: "totalStock",
      header: "Stock",
      width: "100px",
      render: (value, row) => (
        <div>
          <span className="font-medium">{value || 0}</span>
          <span className="text-gray-400 text-xs ml-1">{row.unit}</span>
        </div>
      ),
    },
    {
      key: "status",
      header: "Status",
      width: "110px",
      render: (_, row) => getStockBadge(row.totalStock, row.reorderLevel),
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
                ${totalValue.toLocaleString()}
              </p>
            </div>
            <div className="p-3 bg-green-100 rounded-xl">
              <Package className="text-green-600" size={24} />
            </div>
          </div>
        </Card>
      </div>

      {/* Filters */}
      <Card padding={false} className="p-4">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Filter size={18} className="text-gray-500" />
            <span className="font-medium text-gray-700 dark:text-gray-300">
              Filters
            </span>
          </div>
          <Button variant="ghost" size="sm" onClick={clearFilters}>
            Clear All
          </Button>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Search */}
          <div className="relative">
            <Search
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500"
              size={20}
            />
            <input
              type="text"
              placeholder="Search by name or code..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 dark:placeholder-gray-400 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
            />
          </div>
          {/* Stock Filter */}
          <select
            value={stockFilter}
            onChange={(e) => setStockFilter(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
          >
            <option value="ALL">All Stock Levels</option>
            <option value="LOW">Low Stock Only</option>
            <option value="OUT">Out of Stock Only</option>
          </select>
        </div>
      </Card>

      {/* Table */}
      <Table
        columns={columns}
        data={filteredProducts}
        loading={loading}
        emptyMessage="No products found"
        actions={
          canManageProducts
            ? (row) => (
                <div className="flex items-center gap-1">
                  <button
                    onClick={() => handleEdit(row)}
                    className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                    title="Edit"
                  >
                    <Pencil size={18} />
                  </button>
                  {canDeleteProducts && (
                    <button
                      onClick={() => handleDelete(row)}
                      className="p-1.5 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Delete"
                    >
                      <Trash2 size={18} />
                    </button>
                  )}
                </div>
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
        </form>
      </Modal>
    </div>
  );
};

export default Products;
