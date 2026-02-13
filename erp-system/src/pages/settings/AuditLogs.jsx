/**
 * Audit Logs Page
 * 
 * Administration tool to monitor system actions.
 * Displays logs from the backend audit_logs table.
 */
import { useState, useEffect } from "react";
import { auditApi } from "../../api/auditApi";
import DataTable from "../../components/common/DataTable";
import Button from "../../components/common/Button";
import Card from "../../components/common/Card";
import Badge from "../../components/common/Badge";
import Input from "../../components/common/Input";
import { 
  ShieldCheck, 
  Search, 
  RefreshCw, 
  Filter,
  Clock,
  User,
  Activity,
  ChevronRight
} from "lucide-react";

const AuditLogs = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [actionFilter, setActionFilter] = useState("ALL");

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const data = await auditApi.getRecentLogs();
      setLogs(data);
    } catch (error) {
      console.error("Failed to fetch audit logs", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const filteredLogs = logs.filter(log => {
    const matchesSearch = 
      log.entityName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.actorUsername?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      log.action?.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesAction = actionFilter === "ALL" || log.action === actionFilter;
    
    return matchesSearch && matchesAction;
  });

  const columns = [
    {
      accessorKey: "timestamp",
      header: "Timestamp",
      cell: ({ getValue }) => {
        const date = new Date(getValue());
        return (
          <div className="flex flex-col">
            <span className="text-sm font-medium text-gray-900 dark:text-gray-100">
              {date.toLocaleDateString()}
            </span>
            <span className="text-xs text-gray-500">
              {date.toLocaleTimeString()}
            </span>
          </div>
        );
      },
      size: 160,
    },
    {
      accessorKey: "actorUsername",
      header: "User",
      cell: ({ getValue }) => (
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center text-gray-600 dark:text-gray-400">
            <User size={14} />
          </div>
          <span className="font-medium text-gray-700 dark:text-gray-300">{getValue() || "SYSTEM"}</span>
        </div>
      ),
      size: 150,
    },
    {
      accessorKey: "action",
      header: "Action",
      cell: ({ getValue }) => {
        const action = getValue();
        let color = "gray";
        if (action === "CREATE") color = "green";
        if (action === "UPDATE") color = "blue";
        if (action === "DELETE") color = "red";
        if (action === "AUTO_CREATE") color = "purple";

        return (
          <Badge color={color} variant="flat" size="sm">
            {action}
          </Badge>
        );
      },
      size: 120,
    },
    {
      accessorKey: "entityName",
      header: "Entity",
      cell: ({ getValue, row }) => (
        <div className="flex items-center gap-2">
          <span className="font-semibold text-gray-900 dark:text-gray-100">{getValue()}</span>
          <span className="text-xs text-gray-500 font-mono bg-gray-50 dark:bg-gray-800 px-1.5 py-0.5 rounded">
            #{row.original.entityId}
          </span>
        </div>
      ),
      size: 180,
    },
    {
      accessorKey: "newValue",
      header: "Details",
      cell: ({ getValue }) => (
        <span className="text-sm text-gray-600 dark:text-gray-400 line-clamp-1" title={getValue()}>
          {getValue() || "—"}
        </span>
      ),
      size: 300,
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-blue-600 dark:text-blue-400 mb-1">
            <ShieldCheck size={18} />
            <span className="text-xs font-bold uppercase tracking-wider">Security & Compliance</span>
          </div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Audit Logs</h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">Monitor all systematic financial and administrative actions</p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="outline" onClick={fetchLogs}>
            <RefreshCw size={18} />
            Refresh
          </Button>
        </div>
      </div>

      {/* Stats/Metrics Overlay (Optional future use) */}

      {/* Main Table Card */}
      <Card className="overflow-hidden border-none shadow-sm">
        <DataTable
          columns={columns}
          data={filteredLogs}
          loading={loading}
          enableRowSelection={false}
          searchPlaceholder="Search logs..."
          filters={
            <>
              <select
                value={actionFilter}
                onChange={(e) => setActionFilter(e.target.value)}
                className="px-3 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 text-gray-900 dark:text-gray-100 rounded-lg text-sm cursor-pointer focus:outline-none focus:border-blue-400 dark:focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20 transition-all duration-200 appearance-none bg-[url('data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2024%2024%22%20fill%3D%22none%22%20stroke%3D%22%236b7280%22%20stroke-width%3D%222%22%3E%3Cpath%20d%3D%22m6%209%206%206%206-6%22%2F%3E%3C%2Fsvg%3E')] bg-[length:16px] bg-[right_8px_center] bg-no-repeat pr-8"
              >
                <option value="ALL">All Actions</option>
                <option value="CREATE">Create</option>
                <option value="UPDATE">Update</option>
                <option value="DELETE">Delete</option>
                <option value="AUTO_CREATE">Automated</option>
                <option value="UPDATE_BALANCE">Balance Changes</option>
              </select>
            </>
          }
        />
      </Card>
    </div>
  );
};

export default AuditLogs;
