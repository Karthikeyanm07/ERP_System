/**
 * Attendance Page - HR Module
 *
 * Backend DTO: AttendanceCreateRequest
 * Required: employeeId, date, status
 * Optional: clockIn, clockOut, remarks
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useApi } from "../../hooks/useApi";
import { useAuth } from "../../hooks/useAuth";
import { hrApi } from "../../api/hrApi";
import { useToast } from "../../components/common/Toast";
import Table from "../../components/common/Table";
import Card from "../../components/common/Card";
import Button from "../../components/common/Button";
import Badge from "../../components/common/Badge";
import Modal from "../../components/common/Modal";
import Input from "../../components/common/Input";
import { Calendar, Clock, CheckCircle, Filter, Plus } from "lucide-react";

const Attendance = () => {
  const { execute, loading } = useApi();
  const { user, hasAnyRole } = useAuth();
  const toast = useToast();

  // Role-based permissions
  const canManageAttendance = hasAnyRole(["ROLE_HR", "ROLE_ADMIN"]);

  const [attendance, setAttendance] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split("T")[0]
  );
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Form data matching AttendanceCreateRequest
  const [formData, setFormData] = useState({
    employeeId: "",
    date: new Date().toISOString().split("T")[0],
    status: "PRESENT",
    clockIn: "",
    clockOut: "",
    remarks: "",
  });

  useEffect(() => {
    fetchAttendance();
    fetchEmployees();
  }, [selectedDate]);

  const fetchAttendance = async () => {
    try {
      const data = await execute(hrApi.getAttendance, selectedDate);
      setAttendance(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching attendance:", error);
      toast.error("Failed to load attendance records");
      setAttendance([]);
    }
  };

  const fetchEmployees = async () => {
    try {
      const data = await execute(hrApi.getEmployees);
      setEmployees(Array.isArray(data) ? data : []);
    } catch (error) {
      logger.error("Error fetching employees:", error);
      setEmployees([]);
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
      employeeId: "",
      date: new Date().toISOString().split("T")[0],
      status: "PRESENT",
      clockIn: "",
      clockOut: "",
      remarks: "",
    });
    setErrors({});
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.employeeId) {
      newErrors.employeeId = "Please select an employee";
    }
    if (!formData.date) {
      newErrors.date = "Date is required";
    }
    if (!formData.status) {
      newErrors.status = "Status is required";
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
      date: formData.date,
      status: formData.status,
      clockIn: formData.clockIn || null,
      clockOut: formData.clockOut || null,
      remarks: formData.remarks || null,
    };

    try {
      await execute(hrApi.markAttendance, submitData);
      toast.success("Attendance marked successfully");
      await fetchAttendance();
      setIsModalOpen(false);
      resetForm();
    } catch (error) {
      logger.error("Error marking attendance:", error);
      toast.error(error.response?.data?.message || "Error marking attendance");
    } finally {
      setFormLoading(false);
    }
  };

  const handleQuickCheckIn = async () => {
    const currentEmployee = employees.find((e) => e.email === user?.email);

    if (!currentEmployee) {
      // HR users may not have an employee record - show helpful message
      toast.info(
        `No employee record found matching your email (${
          user?.email || "unknown"
        }). ` + "Opening attendance form to mark for another employee."
      );
      setIsModalOpen(true);
      return;
    }

    const now = new Date();
    const submitData = {
      employeeId: currentEmployee.id,
      date: now.toISOString().split("T")[0],
      status: "PRESENT",
      clockIn: now.toTimeString().slice(0, 5),
    };

    try {
      await execute(hrApi.markAttendance, submitData);
      toast.success("Checked in successfully!");
      await fetchAttendance();
    } catch (error) {
      logger.error("Error checking in:", error);
      const errorMsg = error.response?.data?.message || "Error checking in";
      toast.error(errorMsg);
    }
  };

  const getStatusBadge = (status) => {
    const variants = {
      PRESENT: { variant: "success", label: "Present" },
      ABSENT: { variant: "danger", label: "Absent" },
      LATE: { variant: "warning", label: "Late" },
      HALF_DAY: { variant: "info", label: "Half Day" },
      LEAVE: { variant: "purple", label: "On Leave" },
    };
    const { variant, label } = variants[status] || {
      variant: "default",
      label: status,
    };
    return <Badge variant={variant}>{label}</Badge>;
  };

  const presentCount = attendance.filter((a) => a.status === "PRESENT").length;
  const absentCount = attendance.filter((a) => a.status === "ABSENT").length;

  const columns = [
    {
      key: "employee",
      header: "Employee",
      render: (_, row) => (
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-medium text-sm">
            {row.employeeName?.[0] || row.employee?.firstName?.[0] || "E"}
          </div>
          <span className="font-medium">
            {row.employeeName ||
              `${row.employee?.firstName || ""} ${
                row.employee?.lastName || ""
              }`.trim() ||
              "Unknown"}
          </span>
        </div>
      ),
    },
    {
      key: "date",
      header: "Date",
      render: (value) => (value ? new Date(value).toLocaleDateString() : "-"),
    },
    {
      key: "clockIn",
      header: "Clock In",
      render: (value) => (
        <span className="text-green-600 font-medium">{value || "-"}</span>
      ),
    },
    {
      key: "clockOut",
      header: "Clock Out",
      render: (value) => (
        <span className="text-red-600 font-medium">{value || "-"}</span>
      ),
    },
    {
      key: "status",
      header: "Status",
      render: (value) => getStatusBadge(value),
    },
    {
      key: "remarks",
      header: "Remarks",
      render: (value) => value || "-",
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Attendance
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Track employee attendance
          </p>
        </div>
        {canManageAttendance && (
          <Button onClick={() => setIsModalOpen(true)}>
            <Plus size={20} />
            Mark Attendance
          </Button>
        )}
      </div>

      {/* Quick Actions */}
      <Card title="Today's Attendance">
        <div className="flex flex-wrap items-center gap-4">
          <div className="flex items-center gap-2 text-gray-600">
            <Calendar size={20} />
            <span>
              {new Date().toLocaleDateString("en-US", {
                weekday: "long",
                year: "numeric",
                month: "long",
                day: "numeric",
              })}
            </span>
          </div>
          <div className="flex items-center gap-2 text-gray-600">
            <Clock size={20} />
            <span>{new Date().toLocaleTimeString()}</span>
          </div>
          <div className="flex-1" />
          {canManageAttendance && (
            <Button onClick={handleQuickCheckIn} variant="success">
              <CheckCircle size={18} />
              Quick Check In
            </Button>
          )}
        </div>
      </Card>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="text-center">
          <p className="text-3xl font-bold text-green-600">{presentCount}</p>
          <p className="text-sm text-gray-500">Present</p>
        </Card>
        <Card className="text-center">
          <p className="text-3xl font-bold text-red-600">{absentCount}</p>
          <p className="text-sm text-gray-500">Absent</p>
        </Card>
        <Card className="text-center">
          <p className="text-3xl font-bold text-blue-600">
            {attendance.length}
          </p>
          <p className="text-sm text-gray-500">Total Records</p>
        </Card>
      </div>

      {/* Filter */}
      <Card padding={false} className="p-4">
        <div className="flex items-center gap-4">
          <Filter size={20} className="text-gray-400" />
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </Card>

      {/* Table */}
      <Table
        columns={columns}
        data={attendance}
        loading={loading}
        emptyMessage="No attendance records for this date"
      />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Mark Attendance"
        footer={
          <>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              Save
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
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

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Date *"
              name="date"
              type="date"
              value={formData.date}
              onChange={handleChange}
              error={errors.date}
            />
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Status *
              </label>
              <select
                name="status"
                value={formData.status}
                onChange={handleChange}
                className={`w-full px-4 py-2.5 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400 dark:bg-gray-800 dark:text-gray-100 dark:border-gray-600 ${
                  errors.status ? "border-red-500" : "border-gray-300"
                }`}
              >
                <option value="PRESENT">Present</option>
                <option value="ABSENT">Absent</option>
                <option value="HALF_DAY">Half Day</option>
                <option value="LEAVE">On Leave</option>
              </select>
              {errors.status && (
                <p className="text-red-500 text-xs mt-1">{errors.status}</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Clock In"
              name="clockIn"
              type="time"
              value={formData.clockIn}
              onChange={handleChange}
            />
            <Input
              label="Clock Out"
              name="clockOut"
              type="time"
              value={formData.clockOut}
              onChange={handleChange}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Remarks
            </label>
            <textarea
              name="remarks"
              value={formData.remarks}
              onChange={handleChange}
              rows={2}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Any notes..."
            />
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Attendance;
