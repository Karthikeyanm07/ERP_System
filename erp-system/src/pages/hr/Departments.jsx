/**
 * Departments Page - HR Module
 *
 * Backend DTO: DepartmentDTO
 * Required: name
 * Optional: description, managerId
 *
 * Features:
 * - Cached data fetching (prevents duplicate API calls)
 * - ConfirmDialog for delete confirmation
 * - Toast notifications
 * - Employee count per department
 */

import { useState, useCallback } from "react";
import { hrApi } from "../../api/hrApi";
import { useToast } from "../../components/common/Toast";
import { useAuth } from "../../hooks/useAuth";
import { useCachedData } from "../../hooks/useCachedData";
import { useDataCache } from "../../context/DataCacheContext";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Card from "../../components/common/Card";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import {
  Plus,
  Building2,
  Users,
  Pencil,
  Trash2,
  BarChart3,
} from "lucide-react";
import MetricCard from "../../components/common/MetricCard";

const Departments = () => {
  const toast = useToast();
  const { hasAnyRole } = useAuth();
  const { CACHE_KEYS, invalidate } = useDataCache();

  // Role-based permissions
  const canManageDepartments = hasAnyRole(["ROLE_HR", "ROLE_ADMIN"]);

  // Use cached data
  const {
    data: departmentsData,
    loading,
    refetch: refetchDepartments,
  } = useCachedData(CACHE_KEYS.DEPARTMENTS, hrApi.getDepartments);

  const { data: employeesData } = useCachedData(
    CACHE_KEYS.EMPLOYEES,
    hrApi.getEmployees
  );

  const departments = departmentsData ?? [];
  const employees = employeesData ?? [];

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [departmentToDelete, setDepartmentToDelete] = useState(null);
  const [editingDepartment, setEditingDepartment] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Form data matching DepartmentDTO
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    managerId: "",
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const resetForm = useCallback(() => {
    setFormData({
      name: "",
      description: "",
      managerId: "",
    });
    setErrors({});
  }, []);

  const validateForm = () => {
    const newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = "Department name is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleAdd = () => {
    setEditingDepartment(null);
    resetForm();
    setIsModalOpen(true);
  };

  const handleEdit = (department) => {
    setEditingDepartment(department);
    setFormData({
      name: department.name || "",
      description: department.description || "",
      managerId: department.managerId || "",
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

    const submitData = {
      name: formData.name,
      description: formData.description || null,
      managerId: formData.managerId ? parseInt(formData.managerId) : null,
    };

    try {
      if (editingDepartment) {
        await hrApi.updateDepartment(editingDepartment.id, submitData);
        toast.success("Department updated successfully");
      } else {
        await hrApi.createDepartment(submitData);
        toast.success("Department created successfully");
      }
      invalidate(CACHE_KEYS.DEPARTMENTS);
      await refetchDepartments();
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      toast.error(error.response?.data?.message || "Error saving department");
    } finally {
      setFormLoading(false);
    }
  };

  /**
   * Open delete confirmation dialog
   */
  const handleDelete = (department) => {
    setDepartmentToDelete(department);
    setIsDeleteDialogOpen(true);
  };

  /**
   * Confirm and execute delete
   */
  const confirmDelete = async () => {
    if (!departmentToDelete) return;

    try {
      await hrApi.deleteDepartment(departmentToDelete.id);
      toast.success("Department deleted successfully");
      invalidate(CACHE_KEYS.DEPARTMENTS);
      refetchDepartments();
      setIsDeleteDialogOpen(false);
      setDepartmentToDelete(null);
    } catch (error) {
      toast.error(error.response?.data?.message || "Error deleting department");
    }
  };

  // Count employees per department
  const getEmployeeCount = (deptId) => {
    return employees.filter((e) => e.departmentId === deptId).length;
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Departments
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage organizational structure
          </p>
        </div>
        {canManageDepartments && (
          <Button onClick={handleAdd}>
            <Plus size={20} />
            Add Department
          </Button>
        )}
      </div>

      {/* Summary */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <MetricCard
          title="Total Departments"
          value={departments.length}
          icon={Building2}
          accent="blue"
        />
        <MetricCard
          title="Total Employees"
          value={employees.length}
          icon={Users}
          accent="green"
        />
        <MetricCard
          title="Avg per Dept"
          value={
            departments.length > 0
              ? Math.round(employees.length / departments.length)
              : 0
          }
          icon={BarChart3}
          accent="purple"
        />
      </div>

      {/* Department Cards */}
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
      ) : departments.length === 0 ? (
        <Card className="text-center py-12">
          <Building2 className="mx-auto text-gray-400 mb-4" size={48} />
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
            No departments yet
          </h3>
          <p className="text-gray-500 dark:text-gray-400 mb-4">
            Create departments to organize your employees
          </p>
          {canManageDepartments && (
            <Button onClick={handleAdd}>
              <Plus size={20} />
              Add Department
            </Button>
          )}
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {departments.map((dept) => (
            <div
              key={dept.id}
              className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className="p-3 bg-blue-100 dark:bg-blue-500/20 rounded-xl">
                    <Building2
                      className="text-blue-600 dark:text-blue-300"
                      size={24}
                    />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900 dark:text-gray-100">
                      {dept.name}
                    </h3>
                    {dept.managerName && (
                      <p className="text-sm text-gray-500 dark:text-gray-400">
                        Manager: {dept.managerName}
                      </p>
                    )}
                  </div>
                </div>
                {canManageDepartments && (
                  <div className="flex gap-1">
                    <button
                      onClick={() => handleEdit(dept)}
                      className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg"
                      title="Edit department"
                    >
                      <Pencil size={16} />
                    </button>
                    <button
                      onClick={() => handleDelete(dept)}
                      className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg"
                      title="Delete department"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                )}
              </div>

              {dept.description && (
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                  {dept.description}
                </p>
              )}

              <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-300">
                <Users size={16} />
                <span>{getEmployeeCount(dept.id)} employees</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingDepartment ? "Edit Department" : "Add Department"}
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              {editingDepartment ? "Update" : "Create"}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Department Name *"
            name="name"
            value={formData.name}
            onChange={handleChange}
            placeholder="e.g. Engineering, Sales, HR"
            error={errors.name}
          />

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Manager
            </label>
            <select
              name="managerId"
              value={formData.managerId}
              onChange={handleChange}
              className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
            >
              <option value="">No Manager Assigned</option>
              {employees.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.firstName} {e.lastName}
                </option>
              ))}
            </select>
          </div>

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
              placeholder="Brief description of this department"
            />
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => {
          setIsDeleteDialogOpen(false);
          setDepartmentToDelete(null);
        }}
        onConfirm={confirmDelete}
        title="Delete Department"
        message={
          departmentToDelete
            ? `Are you sure you want to delete the ${
                departmentToDelete.name
              } department? ${
                getEmployeeCount(departmentToDelete.id) > 0
                  ? `This department has ${getEmployeeCount(
                      departmentToDelete.id
                    )} employee(s). `
                  : ""
              }This action cannot be undone.`
            : "Are you sure you want to delete this department?"
        }
        confirmText="Delete Department"
        cancelText="Cancel"
        variant="danger"
      />
    </div>
  );
};

export default Departments;
