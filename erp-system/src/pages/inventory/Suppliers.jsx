/**
 * Suppliers Page - Inventory Module
 *
 * Backend DTO: SupplierDTO
 * Required: supplierCode, name
 * Optional: contactPerson, email, phone, address, city, country
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { inventoryApi } from "../../api/inventoryApi";
import { useToast } from "../../components/common/Toast";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Card from "../../components/common/Card";
import Badge from "../../components/common/Badge";
import {
  Plus,
  Truck,
  Mail,
  Phone,
  MapPin,
  Pencil,
  CheckCircle,
  Globe2,
} from "lucide-react";
import MetricCard from "../../components/common/MetricCard";
import DropdownActions from "../../components/common/DropdownActions";

const Suppliers = () => {
  const { execute, loading } = useApi();
  const toast = useToast();

  const [suppliers, setSuppliers] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Form data matching SupplierDTO
  const [formData, setFormData] = useState({
    supplierCode: "",
    name: "",
    contactPerson: "",
    email: "",
    phone: "",
    address: "",
    city: "",
    country: "",
  });

  useEffect(() => {
    fetchSuppliers();
  }, []);

  const fetchSuppliers = async () => {
    try {
      const data = await execute(inventoryApi.getSuppliers);
      setSuppliers(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching suppliers", error);
      toast.error("Failed to load suppliers");
      setSuppliers([]);
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
      supplierCode: "",
      name: "",
      contactPerson: "",
      email: "",
      phone: "",
      address: "",
      city: "",
      country: "",
    });
    setErrors({});
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.supplierCode.trim()) {
      newErrors.supplierCode = "Supplier code is required";
    }
    if (!formData.name.trim()) {
      newErrors.name = "Supplier name is required";
    }
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Please enter a valid email";
    }
    if (
      formData.phone &&
      !/^[0-9]{10,20}$/.test(formData.phone.replace(/\D/g, ""))
    ) {
      newErrors.phone = "Phone must be 10-20 digits";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleAdd = () => {
    setEditingSupplier(null);
    resetForm();
    setIsModalOpen(true);
  };

  const handleEdit = (supplier) => {
    setEditingSupplier(supplier);
    setFormData({
      supplierCode: supplier.supplierCode || "",
      name: supplier.name || "",
      contactPerson: supplier.contactPerson || "",
      email: supplier.email || "",
      phone: supplier.phone || "",
      address: supplier.address || "",
      city: supplier.city || "",
      country: supplier.country || "",
    });
    setErrors({});
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.warning("Please fix the form errors");
      return;
    }

    setFormLoading(true);

    try {
      if (editingSupplier) {
        await execute(
          inventoryApi.updateSupplier,
          editingSupplier.id,
          formData
        );
        toast.success("Supplier updated successfully");
      } else {
        await execute(inventoryApi.createSupplier, formData);
        toast.success("Supplier created successfully");
      }
      await fetchSuppliers();
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      logger.error("Error saving supplier", error);
      toast.error(error.response?.data?.message || "Error saving supplier");
    } finally {
      setFormLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Suppliers
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage your suppliers and vendors
          </p>
        </div>
        <Button onClick={handleAdd}>
          <Plus size={20} />
          Add Supplier
        </Button>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <MetricCard
          title="Total Suppliers"
          value={suppliers.length}
          icon={Truck}
          accent="blue"
        />
        <MetricCard
          title="Active"
          value={suppliers.filter((s) => s.isActive !== false).length}
          icon={CheckCircle}
          accent="green"
        />
        <MetricCard
          title="Countries"
          value={new Set(suppliers.map((s) => s.country).filter(Boolean)).size}
          icon={Globe2}
          accent="purple"
        />
      </div>

      {/* Supplier Cards */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div
              key={i}
              className="bg-white dark:bg-gray-800 rounded-xl p-6 animate-pulse"
            >
              <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-4"></div>
              <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/2"></div>
            </div>
          ))}
        </div>
      ) : suppliers.length === 0 ? (
        <Card className="text-center py-12">
          <Truck className="mx-auto text-gray-400 mb-4" size={48} />
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
            No suppliers yet
          </h3>
          <p className="text-gray-500 dark:text-gray-400 mb-4">
            Add suppliers to start ordering inventory
          </p>
          <Button onClick={handleAdd}>
            <Plus size={20} />
            Add Supplier
          </Button>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {suppliers.map((supplier) => (
            <div
              key={supplier.id}
              className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className="p-3 bg-blue-100 dark:bg-blue-500/20 rounded-xl">
                    <Truck
                      className="text-blue-600 dark:text-blue-300"
                      size={24}
                    />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900 dark:text-gray-100">
                      {supplier.name}
                    </h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      {supplier.supplierCode}
                    </p>
                  </div>
                </div>
                <DropdownActions
                  actions={[
                    { label: "Edit Supplier", icon: Pencil, onClick: () => handleEdit(supplier) },
                  ]}
                />
              </div>

              <div className="space-y-2 text-sm text-gray-600 dark:text-gray-300">
                {supplier.contactPerson && (
                  <p>Contact: {supplier.contactPerson}</p>
                )}
                {supplier.email && (
                  <div className="flex items-center gap-2">
                    <Mail size={14} />
                    <span>{supplier.email}</span>
                  </div>
                )}
                {supplier.phone && (
                  <div className="flex items-center gap-2">
                    <Phone size={14} />
                    <span>{supplier.phone}</span>
                  </div>
                )}
                {(supplier.city || supplier.country) && (
                  <div className="flex items-center gap-2">
                    <MapPin size={14} />
                    <span>
                      {[supplier.city, supplier.country]
                        .filter(Boolean)
                        .join(", ")}
                    </span>
                  </div>
                )}
              </div>

              <div className="mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
                <Badge
                  variant={supplier.isActive !== false ? "success" : "default"}
                  dot
                >
                  {supplier.isActive !== false ? "Active" : "Inactive"}
                </Badge>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingSupplier ? "Edit Supplier" : "Add Supplier"}
        size="lg"
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              {editingSupplier ? "Update" : "Create"}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Supplier Code *"
              name="supplierCode"
              value={formData.supplierCode}
              onChange={handleChange}
              placeholder="SUP-001"
              error={errors.supplierCode}
            />
            <Input
              label="Supplier Name *"
              name="name"
              value={formData.name}
              onChange={handleChange}
              error={errors.name}
            />
          </div>

          <Input
            label="Contact Person"
            name="contactPerson"
            value={formData.contactPerson}
            onChange={handleChange}
          />

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              error={errors.email}
            />
            <Input
              label="Phone"
              name="phone"
              value={formData.phone}
              onChange={handleChange}
              placeholder="10-20 digits"
              error={errors.phone}
            />
          </div>

          <Input
            label="Address"
            name="address"
            value={formData.address}
            onChange={handleChange}
          />

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="City"
              name="city"
              value={formData.city}
              onChange={handleChange}
            />
            <Input
              label="Country"
              name="country"
              value={formData.country}
              onChange={handleChange}
            />
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Suppliers;
