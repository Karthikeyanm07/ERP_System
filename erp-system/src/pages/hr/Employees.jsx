/**
 * Employees Page - HR Module
 *
 * Backend DTO: EmployeeCreateRequest
 * Required fields: employeeCode, firstName, lastName, email, dateOfJoining
 *
 * Features:
 * - Cached data fetching (prevents duplicate API calls on tab switch)
 * - Inline form validation with error messages
 * - Toast notifications for success/error feedback
 * - Filter by department and status
 * - Change employee status (ACTIVE/INACTIVE/TERMINATED)
 */

import { useState, useCallback } from "react";
import { hrApi } from "../../api/hrApi";
import { useToast } from "../../components/common/Toast";
import { useAuth } from "../../hooks/useAuth";
import { useCachedData } from "../../hooks/useCachedData";
import { useDataCache } from "../../context/DataCacheContext";
import DataTable from "../../components/common/DataTable";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Card from "../../components/common/Card";
import Badge from "../../components/common/Badge";
import ConfirmDialog from "../../components/common/ConfirmDialog";
import SearchBar from "../../components/common/SearchBar";
import DropdownActions from "../../components/common/DropdownActions";
import {
  Plus,
  Pencil,
  Trash2,
  Filter,
  ChevronDown,
} from "lucide-react";
import { createPortal } from "react-dom";
import { useRef, useEffect } from "react";
import { Users, UserCheck, UserMinus, UserX } from "lucide-react";
import MetricCard from "../../components/common/MetricCard";

/**
 * Custom Status Dropdown with styled options and hover effects
 * Uses React Portal to render outside table DOM and avoid overflow clipping
 */
const StatusDropdown = ({ currentStatus, statusConfig, onStatusChange }) => {
  const [isOpen, setIsOpen] = useState(false);
  const buttonRef = useRef(null);
  const dropdownRef = useRef(null);
  const currentConfig = statusConfig[currentStatus];

  const statuses = ["ACTIVE", "INACTIVE", "TERMINATED"];

  // Calculate dropdown position dynamically on each render
  const getDropdownStyle = () => {
    if (!buttonRef.current) return {};

    const rect = buttonRef.current.getBoundingClientRect();
    const dropdownHeight = 130;
    const viewportHeight = window.innerHeight;
    const viewportWidth = window.innerWidth;

    // Check if dropdown would overflow bottom
    const wouldOverflowBottom = rect.bottom + dropdownHeight > viewportHeight;
    // Check if dropdown would overflow right
    const wouldOverflowRight = rect.left + 140 > viewportWidth;

    return {
      position: "fixed",
      top: wouldOverflowBottom
        ? `${rect.top - dropdownHeight - 4}px`
        : `${rect.bottom + 4}px`,
      left: wouldOverflowRight ? `${rect.right - 140}px` : `${rect.left}px`,
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
      {/* Trigger Button */}
      <button
        ref={buttonRef}
        onClick={(e) => {
          e.stopPropagation();
          setIsOpen(!isOpen);
        }}
        className={`inline-flex items-center gap-1.5 text-xs font-medium border rounded-md px-2.5 py-1 cursor-pointer transition-colors ${currentConfig.bg} ${currentConfig.text} ${currentConfig.border} ${currentConfig.hoverBg}`}
      >
        <span
          className={`w-1.5 h-1.5 rounded-full ${currentConfig.dot}`}
        ></span>
        {currentStatus}
        <ChevronDown
          size={12}
          className={`transition-transform ${isOpen ? "rotate-180" : ""}`}
        />
      </button>

      {/* Render dropdown in portal at document body level */}
      {isOpen &&
        createPortal(
          <>
            <div
              className="fixed inset-0"
              style={{ zIndex: 9998 }}
              onClick={(e) => {
                e.stopPropagation();
                setIsOpen(false);
              }}
            />

            <div
              ref={dropdownRef}
              className="bg-white dark:bg-gray-800 rounded-lg shadow-xl border border-gray-200 dark:border-gray-700 py-1 min-w-[130px]"
              style={getDropdownStyle()}
            >
              {statuses.map((status) => {
                const config = statusConfig[status];
                const isSelected = status === currentStatus;

                return (
                  <button
                    key={status}
                    onClick={(e) => {
                      e.stopPropagation();
                      onStatusChange(status);
                      setIsOpen(false);
                    }}
                    className={`w-full flex items-center gap-2 px-3 py-2 text-xs font-medium transition-colors text-left ${
                      isSelected
                        ? `${config.bg} ${config.text} dark:bg-gray-600 dark:text-white`
                        : `text-gray-700 dark:text-gray-200 ${config.hoverBg} dark:hover:bg-gray-700`
                    }`}
                  >
                    <span
                      className={`w-2 h-2 rounded-full ${config.dot}`}
                    ></span>
                    {status}
                    {isSelected && (
                      <span className="ml-auto text-current">✓</span>
                    )}
                  </button>
                );
              })}
            </div>
          </>,
          document.body
        )}
    </div>
  );
};

const Employees = () => {
  const toast = useToast();
  const { hasAnyRole } = useAuth();
  const { CACHE_KEYS, invalidate } = useDataCache();

  // Role-based permissions
  const canManageEmployees = hasAnyRole(["ROLE_HR", "ROLE_ADMIN"]);

  // Use cached data - prevents API calls on every tab switch
  const {
    data: employeesData,
    loading,
    refetch: refetchEmployees,
  } = useCachedData(CACHE_KEYS.EMPLOYEES, hrApi.getEmployees);

  const { data: departmentsData } = useCachedData(
    CACHE_KEYS.DEPARTMENTS,
    hrApi.getDepartments
  );

  // Handle null values from cache
  const employees = employeesData ?? [];
  const departments = departmentsData ?? [];

  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [departmentFilter, setDepartmentFilter] = useState("ALL");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [employeeToDelete, setEmployeeToDelete] = useState(null);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Form data matching EmployeeCreateRequest DTO
  const [formData, setFormData] = useState({
    employeeCode: "",
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    departmentId: "",
    designation: "",
    dateOfJoining: new Date().toISOString().split("T")[0],
    salary: "",
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
      employeeCode: "",
      firstName: "",
      lastName: "",
      email: "",
      phone: "",
      departmentId: "",
      designation: "",
      dateOfJoining: new Date().toISOString().split("T")[0],
      salary: "",
    });
    setErrors({});
  }, []);

  const validateForm = () => {
    const newErrors = {};

    if (!formData.employeeCode.trim()) {
      newErrors.employeeCode = "Employee code is required";
    }
    if (!formData.firstName.trim()) {
      newErrors.firstName = "First name is required";
    } else if (formData.firstName.length < 2) {
      newErrors.firstName = "First name must be at least 2 characters";
    }
    if (!formData.lastName.trim()) {
      newErrors.lastName = "Last name is required";
    } else if (formData.lastName.length < 2) {
      newErrors.lastName = "Last name must be at least 2 characters";
    }
    if (!formData.email.trim()) {
      newErrors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Please enter a valid email address";
    }
    if (formData.phone && !/^[0-9]{10,20}$/.test(formData.phone)) {
      newErrors.phone = "Phone must be 10-20 digits";
    }
    if (!formData.dateOfJoining) {
      newErrors.dateOfJoining = "Date of joining is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleAdd = () => {
    setEditingEmployee(null);
    resetForm();
    setIsModalOpen(true);
  };

  const handleEdit = (employee) => {
    setEditingEmployee(employee);
    setFormData({
      employeeCode: employee.employeeCode || "",
      firstName: employee.firstName || "",
      lastName: employee.lastName || "",
      email: employee.email || "",
      phone: employee.phone || "",
      departmentId: employee.departmentId || "",
      designation: employee.designation || "",
      dateOfJoining:
        employee.dateOfJoining || new Date().toISOString().split("T")[0],
      salary: employee.salary || "",
    });
    setErrors({});
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.warning("Please fix the form errors before submitting");
      return;
    }

    setFormLoading(true);

    const submitData = {
      ...formData,
      departmentId: formData.departmentId
        ? parseInt(formData.departmentId)
        : null,
      salary: formData.salary ? parseFloat(formData.salary) : null,
    };

    try {
      if (editingEmployee) {
        await hrApi.updateEmployee(editingEmployee.id, submitData);
        toast.success("Employee updated successfully");
      } else {
        await hrApi.createEmployee(submitData);
        toast.success("Employee created successfully");
      }
      invalidate(CACHE_KEYS.EMPLOYEES);
      await refetchEmployees();
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      const message = error.response?.data?.message || "Error saving employee";
      toast.error(message);
    } finally {
      setFormLoading(false);
    }
  };

  /**
   * Open delete confirmation dialog
   */
  const handleDelete = (employee) => {
    setEmployeeToDelete(employee);
    setIsDeleteDialogOpen(true);
  };

  /**
   * Confirm and execute delete
   */
  const confirmDelete = async () => {
    if (!employeeToDelete) return;

    try {
      await hrApi.deleteEmployee(employeeToDelete.id);
      toast.success("Employee marked as terminated");
      invalidate(CACHE_KEYS.EMPLOYEES);
      refetchEmployees();
      setIsDeleteDialogOpen(false);
      setEmployeeToDelete(null);
    } catch (error) {
      toast.error(error.response?.data?.message || "Error deleting employee");
    }
  };

  /**
   * Change employee status
   */
  const handleStatusChange = async (employeeId, newStatus) => {
    try {
      await hrApi.changeEmployeeStatus(employeeId, newStatus);
      toast.success(`Employee status changed to ${newStatus}`);
      invalidate(CACHE_KEYS.EMPLOYEES);
      refetchEmployees();
    } catch (error) {
      toast.error(error.response?.data?.message || "Error changing status");
    }
  };

  /**
   * Filter employees by search, status, and department
   */
  const filteredEmployees = employees.filter((e) => {
    // Search filter
    const fullName = `${e.firstName || ""} ${e.lastName || ""}`.toLowerCase();
    const matchesSearch =
      fullName.includes(searchTerm.toLowerCase()) ||
      e.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      e.employeeCode?.toLowerCase().includes(searchTerm.toLowerCase());

    // Status filter
    const matchesStatus = statusFilter === "ALL" || e.status === statusFilter;

    // Department filter
    const matchesDepartment =
      departmentFilter === "ALL" ||
      e.departmentId?.toString() === departmentFilter;

    return matchesSearch && matchesStatus && matchesDepartment;
  });

  const columns = [
    {
      accessorKey: "firstName",
      header: "Employee",
      size: 220,
      cell: ({ row }) => (
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 bg-gradient-to-br from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white font-medium text-sm flex-shrink-0">
            {row.original.firstName?.[0]?.toUpperCase() || "E"}
          </div>
          <div className="min-w-0">
            <p className="font-medium text-gray-900 dark:text-gray-100 truncate">
              {row.original.firstName} {row.original.lastName}
            </p>
            <p className="text-xs text-gray-500">{row.original.employeeCode}</p>
          </div>
        </div>
      ),
    },
    {
      accessorKey: "email",
      header: "Email",
      size: 200,
      cell: ({ getValue }) => (
        <span className="truncate block" title={getValue()}>
          {getValue() || "—"}
        </span>
      ),
    },
    {
      accessorKey: "designation",
      header: "Designation",
      size: 160,
      cell: ({ getValue }) => (
        <span className="truncate block" title={getValue()}>
          {getValue() || "—"}
        </span>
      ),
    },
    {
      accessorKey: "departmentName",
      header: "Department",
      size: 150,
      cell: ({ getValue }) => (
        <span className="truncate block" title={getValue()}>
          {getValue() || "—"}
        </span>
      ),
    },
    {
      accessorKey: "status",
      header: "Status",
      size: 140,
      cell: ({ getValue, row }) => {
        const status = getValue() || "ACTIVE";
        const statusConfig = {
          ACTIVE: {
            bg: "bg-green-100",
            text: "text-green-700",
            border: "border-green-300",
            hoverBg: "hover:bg-green-200",
            dot: "bg-green-500",
          },
          INACTIVE: {
            bg: "bg-yellow-100",
            text: "text-yellow-700",
            border: "border-yellow-300",
            hoverBg: "hover:bg-yellow-200",
            dot: "bg-yellow-500",
          },
          TERMINATED: {
            bg: "bg-red-100",
            text: "text-red-700",
            border: "border-red-300",
            hoverBg: "hover:bg-red-200",
            dot: "bg-red-500",
          },
        };

        const currentConfig = statusConfig[status];

        if (canManageEmployees) {
          return (
            <StatusDropdown
              currentStatus={status}
              statusConfig={statusConfig}
              onStatusChange={(newStatus) =>
                handleStatusChange(row.original.id, newStatus)
              }
            />
          );
        }

        return (
          <span
            className={`inline-flex items-center gap-1.5 text-xs font-medium border rounded-md px-2.5 py-1 ${currentConfig.bg} ${currentConfig.text} ${currentConfig.border}`}
          >
            <span className={`w-1.5 h-1.5 rounded-full ${currentConfig.dot}`}></span>
            {status}
          </span>
        );
      },
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Employees
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage employee records
          </p>
        </div>
        {canManageEmployees && (
          <Button onClick={handleAdd}>
            <Plus size={20} />
            Add Employee
          </Button>
        )}
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard
          title="Total Employees"
          value={employees.length}
          icon={Users}
          accent="blue"
        />
        <MetricCard
          title="Active"
          value={employees.filter((e) => e.status === "ACTIVE").length}
          icon={UserCheck}
          accent="green"
        />
        <MetricCard
          title="Inactive"
          value={employees.filter((e) => e.status === "INACTIVE").length}
          icon={UserMinus}
          accent="amber"
        />
        <MetricCard
          title="Terminated"
          value={employees.filter((e) => e.status === "TERMINATED").length}
          icon={UserX}
          accent="rose"
        />
      </div>

      {/* Filters */}
      <Card padding={false} className="p-4">
        <div className="flex items-center gap-2 mb-3">
          <Filter size={18} className="text-gray-500" />
          <span className="font-medium text-gray-700">Filters</span>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Search */}
          <SearchBar
            value={searchTerm}
            onChange={setSearchTerm}
            placeholder="Search by name, email, or code..."
          />

          {/* Status Filter */}
          <div>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-md focus:outline-none focus:border-gray-500 dark:focus:border-gray-400 transition-all duration-200 text-sm cursor-pointer"
            >
              <option value="ALL">All Statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
              <option value="TERMINATED">Terminated</option>
            </select>
          </div>

          {/* Department Filter */}
          <div>
            <select
              value={departmentFilter}
              onChange={(e) => setDepartmentFilter(e.target.value)}
              className="w-full px-4 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-md focus:outline-none focus:border-gray-500 dark:focus:border-gray-400 transition-all duration-200 text-sm cursor-pointer"
            >
              <option value="ALL">All Departments</option>
              {departments.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </div>
        </div>
      </Card>

      {/* Table */}
      <DataTable
        columns={columns}
        data={filteredEmployees}
        loading={loading}
        emptyMessage="No employees found"
        enableRowSelection
        actions={
          canManageEmployees
            ? (row) => (
                <DropdownActions
                  actions={[
                    { label: "Edit Employee", icon: Pencil, onClick: () => handleEdit(row) },
                    { divider: true },
                    { label: "Delete Employee", icon: Trash2, onClick: () => handleDelete(row), variant: "danger" },
                  ]}
                />
              )
            : null
        }
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingEmployee ? "Edit Employee" : "Add Employee"}
        size="lg"
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              {editingEmployee ? "Update" : "Create"}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Employee Code *"
            name="employeeCode"
            value={formData.employeeCode}
            onChange={handleChange}
            placeholder="EMP001"
            error={errors.employeeCode}
          />

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="First Name *"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              error={errors.firstName}
            />
            <Input
              label="Last Name *"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              error={errors.lastName}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Email *"
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

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Department
              </label>
              <select
                name="departmentId"
                value={formData.departmentId}
                onChange={handleChange}
                className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
              >
                <option value="">Select Department</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name}
                  </option>
                ))}
              </select>
            </div>
            <Input
              label="Designation"
              name="designation"
              value={formData.designation}
              onChange={handleChange}
              placeholder="Software Engineer"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Date of Joining *"
              name="dateOfJoining"
              type="date"
              value={formData.dateOfJoining}
              onChange={handleChange}
              error={errors.dateOfJoining}
            />
            <Input
              label="Salary"
              name="salary"
              type="number"
              value={formData.salary}
              onChange={handleChange}
              placeholder="50000"
            />
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Dialog */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => {
          setIsDeleteDialogOpen(false);
          setEmployeeToDelete(null);
        }}
        onConfirm={confirmDelete}
        title="Delete Employee"
        message={
          employeeToDelete
            ? `Are you sure you want to delete ${employeeToDelete.firstName} ${employeeToDelete.lastName}? This will mark them as TERMINATED. This action cannot be undone.`
            : "Are you sure you want to delete this employee?"
        }
        confirmText="Delete Employee"
        cancelText="Cancel"
        variant="danger"
      />
    </div>
  );
};

export default Employees;
