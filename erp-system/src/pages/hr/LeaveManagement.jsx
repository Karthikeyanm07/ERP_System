/**
 * Leave Management Page - HR Module
 *
 * Backend DTO: LeaveRequestCreateRequest
 * Required: employeeId, leaveTypeId, startDate, endDate, daysCount
 *
 * Features:
 * - Inline form validation
 * - Toast notifications
 * - Auto-calculate days from dates
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { useAuth } from "../../hooks/useAuth";
import { hrApi } from "../../api/hrApi";
import { useToast } from "../../components/common/Toast";
import DataTable from "../../components/common/DataTable";
import Card from "../../components/common/Card";
import Button from "../../components/common/Button";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import Badge from "../../components/common/Badge";
import DropdownActions from "../../components/common/DropdownActions";
import { Plus, Calendar, Check, X } from "lucide-react";

const LeaveManagement = () => {
  const { execute, loading } = useApi();
  const { user, hasRole } = useAuth();
  const toast = useToast();

  const [leaveRequests, setLeaveRequests] = useState([]);
  const [leaveTypes, setLeaveTypes] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Form data matching LeaveRequestCreateRequest
  const [formData, setFormData] = useState({
    employeeId: "",
    leaveTypeId: "",
    startDate: "",
    endDate: "",
    daysCount: 1,
    reason: "",
  });

  // Status filter
  const [statusFilter, setStatusFilter] = useState("ALL");

  // Confirmation state for approve/reject
  const [confirmAction, setConfirmAction] = useState(null); // { id, action: 'approve' | 'reject', employeeName }

  const canApprove =
    hasRole("ROLE_ADMIN") || hasRole("ROLE_MANAGER") || hasRole("ROLE_HR");

  useEffect(() => {
    fetchLeaveRequests();
    fetchLeaveTypes();
    fetchEmployees();
  }, []);

  const fetchLeaveRequests = async () => {
    try {
      const data = await execute(hrApi.getLeaveRequests);
      setLeaveRequests(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching leave requests", error);
      toast.error("Failed to load leave requests");
      setLeaveRequests([]);
    }
  };

  const fetchLeaveTypes = async () => {
    try {
      const data = await execute(hrApi.getLeaveTypes);
      setLeaveTypes(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching leave types", error);
      setLeaveTypes([]);
    }
  };

  const fetchEmployees = async () => {
    try {
      const data = await execute(hrApi.getEmployees);
      setEmployees(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching employees", error);
      setEmployees([]);
    }
  };

  const calculateDays = (startDate, endDate) => {
    if (!startDate || !endDate) return 1;
    const start = new Date(startDate);
    const end = new Date(endDate);
    const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1;
    return diff > 0 ? diff : 1;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => {
      const updated = { ...prev, [name]: value };

      if (name === "startDate" || name === "endDate") {
        const startDt = name === "startDate" ? value : prev.startDate;
        const endDt = name === "endDate" ? value : prev.endDate;
        updated.daysCount = calculateDays(startDt, endDt);
      }

      return updated;
    });

    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  const resetForm = () => {
    setFormData({
      employeeId: "",
      leaveTypeId: "",
      startDate: "",
      endDate: "",
      daysCount: 1,
      reason: "",
    });
    setErrors({});
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.employeeId) {
      newErrors.employeeId = "Please select an employee";
    }
    if (!formData.leaveTypeId) {
      newErrors.leaveTypeId = "Please select a leave type";
    }
    if (!formData.startDate) {
      newErrors.startDate = "Start date is required";
    }
    if (!formData.endDate) {
      newErrors.endDate = "End date is required";
    }
    if (
      formData.startDate &&
      formData.endDate &&
      new Date(formData.endDate) < new Date(formData.startDate)
    ) {
      newErrors.endDate = "End date cannot be before start date";
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
      employeeId: parseInt(formData.employeeId),
      leaveTypeId: parseInt(formData.leaveTypeId),
      startDate: formData.startDate,
      endDate: formData.endDate,
      daysCount: parseInt(formData.daysCount),
      reason: formData.reason || null,
    };

    try {
      await execute(hrApi.submitLeaveRequest, submitData);
      toast.success("Leave request submitted successfully");
      await fetchLeaveRequests();
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      logger.error("Error submitting leave request", error);
      toast.error(
        error.response?.data?.message || "Error submitting leave request"
      );
    } finally {
      setFormLoading(false);
    }
  };

  const getCurrentEmployeeId = () => {
    const currentEmployee = employees.find((e) => e.email === user?.email);
    return currentEmployee?.id;
  };

  // Show confirmation before approve/reject
  const showApprovalConfirm = (row, action) => {
    setConfirmAction({
      id: row.id,
      action,
      employeeName:
        row.employeeName ||
        `${row.employee?.firstName || ""} ${
          row.employee?.lastName || ""
        }`.trim() ||
        "Unknown",
      leaveType: row.leaveTypeName || row.leaveType?.name || "Leave",
      dates: `${
        row.startDate ? new Date(row.startDate).toLocaleDateString() : ""
      } - ${row.endDate ? new Date(row.endDate).toLocaleDateString() : ""}`,
    });
  };

  // Execute the confirmed approval/rejection
  const confirmApproval = async () => {
    if (!confirmAction) return;

    const { id, action } = confirmAction;
    const approved = action === "approve";

    // Try to get employee ID, fall back to user ID for HR users without employee record
    let approverEmployeeId = getCurrentEmployeeId();

    // For HR users without employee records, use a fallback (user id)
    if (!approverEmployeeId) {
      approverEmployeeId = user?.id;

      if (!approverEmployeeId) {
        toast.error(
          "Cannot approve: Unable to identify approver. Please contact admin."
        );
        setConfirmAction(null);
        return;
      }
    }

    try {
      await execute(hrApi.approveLeave, id, {
        approvedById: approverEmployeeId,
        status: approved ? "APPROVED" : "REJECTED",
        remarks: null,
      });
      toast.success(
        `Leave request ${approved ? "approved" : "rejected"} successfully`
      );
      await fetchLeaveRequests();
    } catch (error) {
      logger.error("Error processing leave request", error);
      const errorMsg =
        error.response?.data?.message || "Error processing leave request";
      toast.error(errorMsg);
    } finally {
      setConfirmAction(null);
    }
  };

  const getStatusBadge = (status) => {
    const variants = {
      PENDING: { variant: "warning", label: "Pending" },
      APPROVED: { variant: "success", label: "Approved" },
      REJECTED: { variant: "danger", label: "Rejected" },
    };
    const { variant, label } = variants[status] || variants.PENDING;
    return <Badge variant={variant}>{label}</Badge>;
  };

  const columns = [
    {
      id: "employee",
      header: "Employee",
      cell: ({ row }) => (
        <span className="font-medium">
          {row.original.employeeName ||
            `${row.original.employee?.firstName || ""} ${
              row.original.employee?.lastName || ""
            }`.trim() ||
            "Unknown"}
        </span>
      ),
    },
    {
      accessorKey: "leaveTypeName",
      header: "Type",
      cell: ({ getValue, row }) => (
        <Badge variant="primary">
          {getValue() || row.original.leaveType?.name || "Leave"}
        </Badge>
      ),
    },
    {
      accessorKey: "startDate",
      header: "From",
      cell: ({ getValue }) => (getValue() ? new Date(getValue()).toLocaleDateString() : "-"),
    },
    {
      accessorKey: "endDate",
      header: "To",
      cell: ({ getValue }) => (getValue() ? new Date(getValue()).toLocaleDateString() : "-"),
    },
    {
      accessorKey: "daysCount",
      header: "Days",
      cell: ({ getValue }) => (getValue() ? `${getValue()} day${getValue() > 1 ? "s" : ""}` : "-"),
    },
    {
      accessorKey: "reason",
      header: "Reason",
      cell: ({ getValue }) => (
        <span className="text-gray-600 dark:text-gray-300 truncate max-w-[200px] block">
          {getValue() || "-"}
        </span>
      ),
    },
    {
      accessorKey: "status",
      header: "Status",
      cell: ({ getValue }) => getStatusBadge(getValue()),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Leave Management
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Manage employee leave requests
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button onClick={() => setIsModalOpen(true)}>
            <Plus size={20} />
            Request Leave
          </Button>
        </div>
      </div>

      {/* Leave Stats - Clickable to filter */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card
          className={`text-center cursor-pointer transition-all hover:shadow-md ${
            statusFilter === "PENDING" ? "ring-2 ring-yellow-500" : ""
          }`}
          onClick={() =>
            setStatusFilter(statusFilter === "PENDING" ? "ALL" : "PENDING")
          }
        >
          <p className="text-3xl font-bold text-yellow-600">
            {leaveRequests.filter((r) => r.status === "PENDING").length}
          </p>
          <p className="text-sm text-gray-500 dark:text-gray-400">Pending</p>
        </Card>
        <Card
          className={`text-center cursor-pointer transition-all hover:shadow-md ${
            statusFilter === "APPROVED" ? "ring-2 ring-green-500" : ""
          }`}
          onClick={() =>
            setStatusFilter(statusFilter === "APPROVED" ? "ALL" : "APPROVED")
          }
        >
          <p className="text-3xl font-bold text-green-600">
            {leaveRequests.filter((r) => r.status === "APPROVED").length}
          </p>
          <p className="text-sm text-gray-500 dark:text-gray-400">Approved</p>
        </Card>
        <Card
          className={`text-center cursor-pointer transition-all hover:shadow-md ${
            statusFilter === "REJECTED" ? "ring-2 ring-red-500" : ""
          }`}
          onClick={() =>
            setStatusFilter(statusFilter === "REJECTED" ? "ALL" : "REJECTED")
          }
        >
          <p className="text-3xl font-bold text-red-600">
            {leaveRequests.filter((r) => r.status === "REJECTED").length}
          </p>
          <p className="text-sm text-gray-500 dark:text-gray-400">Rejected</p>
        </Card>
        <Card
          className={`text-center cursor-pointer transition-all hover:shadow-md ${
            statusFilter === "ALL" ? "ring-2 ring-blue-500" : ""
          }`}
          onClick={() => setStatusFilter("ALL")}
        >
          <p className="text-3xl font-bold text-blue-600">
            {leaveRequests.length}
          </p>
          <p className="text-sm text-gray-500 dark:text-gray-400">Total</p>
        </Card>
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={
          statusFilter === "ALL"
            ? leaveRequests
            : leaveRequests.filter((r) => r.status === statusFilter)
        }
        loading={loading}
        emptyMessage={
          statusFilter === "ALL"
            ? "No leave requests found"
            : `No ${statusFilter.toLowerCase()} requests`
        }
        enableRowSelection
        searchPlaceholder="Search leave requests..."
        filters={
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm cursor-pointer focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200 appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%236b7280%22%20stroke-width%3D%222%22%3E%3Cpath%20d%3D%22m6%209%206%206%206-6%22%2F%3E%3C%2Fsvg%3E')] bg-[length:16px] bg-[right_8px_center] bg-no-repeat pr-8"
          >
            <option value="ALL">All Requests</option>
            <option value="PENDING">Pending Only</option>
            <option value="APPROVED">Approved Only</option>
            <option value="REJECTED">Rejected Only</option>
          </select>
        }
        actions={
          canApprove
            ? (row) =>
                row.status === "PENDING" ? (
                  <DropdownActions
                    actions={[
                      { label: "Approve", icon: Check, onClick: () => showApprovalConfirm(row, "approve") },
                      { label: "Reject", icon: X, onClick: () => showApprovalConfirm(row, "reject"), variant: "danger" },
                    ]}
                  />
                ) : null
            : undefined
        }
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Request Leave"
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              Submit Request
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Employee */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Employee *
            </label>
            <select
              name="employeeId"
              value={formData.employeeId}
              onChange={handleChange}
              className={`w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 ${
                errors.employeeId ? "border-red-500" : "border-gray-300"
              }`}
            >
              <option value="">Select Employee</option>
              {employees.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.firstName} {e.lastName} ({e.employeeCode})
                </option>
              ))}
            </select>
            {errors.employeeId && (
              <p className="text-red-500 text-xs mt-1">{errors.employeeId}</p>
            )}
          </div>

          {/* Leave Type */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Leave Type *
            </label>
            <select
              name="leaveTypeId"
              value={formData.leaveTypeId}
              onChange={handleChange}
              className={`w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 ${
                errors.leaveTypeId ? "border-red-500" : "border-gray-300"
              }`}
            >
              <option value="">Select Leave Type</option>
              {leaveTypes.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name} ({t.daysAllowed} days/year)
                </option>
              ))}
            </select>
            {errors.leaveTypeId && (
              <p className="text-red-500 text-xs mt-1">{errors.leaveTypeId}</p>
            )}
            {leaveTypes.length === 0 && (
              <p className="text-amber-600 text-xs mt-1">
                No leave types available. Please contact admin.
              </p>
            )}
          </div>

          {/* Dates */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Input
                label="Start Date *"
                name="startDate"
                type="date"
                value={formData.startDate}
                onChange={handleChange}
                error={errors.startDate}
              />
            </div>
            <div>
              <Input
                label="End Date *"
                name="endDate"
                type="date"
                value={formData.endDate}
                onChange={handleChange}
                error={errors.endDate}
              />
            </div>
          </div>

          {/* Days Count Display */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
            <p className="text-sm text-blue-800">
              <strong>Number of Days:</strong> {formData.daysCount} day
              {formData.daysCount !== 1 ? "s" : ""}
            </p>
            <p className="text-xs text-blue-600 mt-1">
              Auto-calculated from selected dates
            </p>
          </div>

          {/* Reason */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Reason
            </label>
            <textarea
              name="reason"
              value={formData.reason}
              onChange={handleChange}
              rows={3}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Reason for leave request"
            />
          </div>
        </form>
      </Modal>

      {/* Confirmation Modal for Approve/Reject */}
      <Modal
        isOpen={!!confirmAction}
        onClose={() => setConfirmAction(null)}
        title={
          confirmAction?.action === "approve"
            ? "✓ Confirm Approval"
            : "✗ Confirm Rejection"
        }
        footer={
          <>
            <Button variant="outline" onClick={() => setConfirmAction(null)}>
              Cancel
            </Button>
            <Button
              variant={
                confirmAction?.action === "approve" ? "primary" : "danger"
              }
              onClick={confirmApproval}
              loading={loading}
            >
              {confirmAction?.action === "approve"
                ? "Yes, Approve"
                : "Yes, Reject"}
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <div
            className={`p-4 rounded-lg ${
              confirmAction?.action === "approve"
                ? "bg-green-50 border border-green-200"
                : "bg-red-50 border border-red-200"
            }`}
          >
            <p
              className={`font-medium ${
                confirmAction?.action === "approve"
                  ? "text-green-800"
                  : "text-red-800"
              }`}
            >
              Are you sure you want to {confirmAction?.action} this leave
              request?
            </p>
            <p className="text-sm text-gray-600 mt-1">
              This action cannot be undone.
            </p>
          </div>

          <div className="bg-gray-50 rounded-lg p-4 space-y-2">
            <div className="flex justify-between">
              <span className="text-gray-500">Employee:</span>
              <span className="font-medium">{confirmAction?.employeeName}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Leave Type:</span>
              <span className="font-medium">{confirmAction?.leaveType}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Dates:</span>
              <span className="font-medium">{confirmAction?.dates}</span>
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default LeaveManagement;
