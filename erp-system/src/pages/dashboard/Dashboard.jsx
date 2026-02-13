/**
 * Dashboard Page
 *
 * Explanation:
 * - Main overview page after login
 * - Displays key metrics from all modules
 * - Uses StatsCard for quick statistics
 * - Fetches data from various APIs on mount
 *
 * Data Flow:
 * 1. Component mounts
 * 2. useEffect triggers API calls to fetch counts
 * 3. Data is displayed in StatsCard components
 *
 * Future Improvements:
 * - Add charts for trends (using recharts library)
 * - Add recent activity feed
 * - Add quick action buttons
 */

import { useState, useEffect } from "react";
import { logger } from "../../utils/logger";
import { useAuth } from "../../hooks/useAuth";
import { useApi } from "../../hooks/useApi";
import { hrApi } from "../../api/hrApi";
import { financeApi } from "../../api/financeApi";
import { inventoryApi } from "../../api/inventoryApi";
import { salesApi } from "../../api/salesApi";
import StatsCard from "../../components/charts/StatsCard";
import Card from "../../components/common/Card";
import {
  Package,
  ShoppingCart,
  TrendingUp,
  AlertTriangle,
  Clock,
  CheckCircle,
  IndianRupee,
} from "lucide-react";

const Dashboard = () => {
  const { user } = useAuth();
  const { execute, loading } = useApi();

  // Dashboard statistics
  const [stats, setStats] = useState({
    employees: 0,
    departments: 0,
    products: 0,
    lowStockProducts: 0,
    customers: 0,
    pendingOrders: 0,
    totalRevenue: 0,
    expenses: 0,
  });

  // Recent activities (placeholder for now)
  const [recentActivities] = useState([
    {
      id: 1,
      action: "New employee added",
      module: "HR",
      time: "2 hours ago",
      icon: Users,
    },
    {
      id: 2,
      action: "Invoice #INV-001 created",
      module: "Sales",
      time: "3 hours ago",
      icon: IndianRupee,
    },
    {
      id: 3,
      action: "Low stock alert: Product A",
      module: "Inventory",
      time: "5 hours ago",
      icon: AlertTriangle,
    },
    {
      id: 4,
      action: "Leave request approved",
      module: "HR",
      time: "1 day ago",
      icon: CheckCircle,
    },
  ]);

  /**
   * Fetch dashboard data on mount
   * We use Promise.allSettled to handle partial failures gracefully
   */
  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        // Fetch data from all modules in parallel
        const [
          employeesRes,
          departmentsRes,
          productsRes,
          lowStockRes,
          customersRes,
          ordersRes,
        ] = await Promise.allSettled([
          execute(hrApi.getEmployees),
          execute(hrApi.getDepartments),
          execute(inventoryApi.getProducts),
          execute(inventoryApi.getLowStockProducts),
          execute(salesApi.getCustomers),
          execute(salesApi.getSalesOrders),
        ]);

        // Update stats with fetched data (handle failures gracefully)
        setStats({
          employees:
            employeesRes.status === "fulfilled"
              ? employeesRes.value?.length || 0
              : 0,
          departments:
            departmentsRes.status === "fulfilled"
              ? departmentsRes.value?.length || 0
              : 0,
          products:
            productsRes.status === "fulfilled"
              ? productsRes.value?.length || 0
              : 0,
          lowStockProducts:
            lowStockRes.status === "fulfilled"
              ? lowStockRes.value?.length || 0
              : 0,
          customers:
            customersRes.status === "fulfilled"
              ? customersRes.value?.length || 0
              : 0,
          pendingOrders:
            ordersRes.status === "fulfilled"
              ? ordersRes.value?.filter((o) => o.status === "PENDING")
                  ?.length || 0
              : 0,
          totalRevenue: 0, // Would come from a dedicated endpoint
          expenses: 0, // Would come from a dedicated endpoint
        });
      } catch (error) {
        logger.error("Error fetching dashboard data", error);
      }
    };

    fetchDashboardData();
  }, []);

  // Get greeting based on time of day
  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return "Good morning";
    if (hour < 17) return "Good afternoon";
    return "Good evening";
  };

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="bg-gradient-to-r from-blue-600 to-indigo-600 dark:from-blue-600 dark:to-indigo-600 rounded-2xl p-6 text-white shadow-lg">
        <h1 className="text-2xl font-bold">
          {getGreeting()}, {user?.username || "User"}! 👋
        </h1>
        <p className="text-blue-100 mt-1">
          Here's what's happening with your business today.
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatsCard
          title="Total Employees"
          value={stats.employees}
          icon={Users}
          color="blue"
          trend={{ value: "12%", isPositive: true }}
        />
        <StatsCard
          title="Products"
          value={stats.products}
          icon={Package}
          color="purple"
          trend={{ value: "5%", isPositive: true }}
        />
        <StatsCard
          title="Customers"
          value={stats.customers}
          icon={ShoppingCart}
          color="green"
          trend={{ value: "8%", isPositive: true }}
        />
        <StatsCard
          title="Low Stock Items"
          value={stats.lowStockProducts}
          icon={AlertTriangle}
          color="orange"
        />
      </div>

      {/* Quick Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatsCard
          title="Departments"
          value={stats.departments}
          icon={Users}
          color="cyan"
        />
        <StatsCard
          title="Pending Orders"
          value={stats.pendingOrders}
          icon={Clock}
          color="orange"
        />
        <StatsCard
          title="Revenue This Month"
          value="₹0"
          icon={TrendingUp}
          color="green"
        />
      </div>

      {/* Recent Activity */}
      <Card
        title="Recent Activity"
        subtitle="Latest actions across all modules"
      >
        <div className="space-y-4">
          {recentActivities.map((activity) => {
            const Icon = activity.icon;
            return (
              <div
                key={activity.id}
                className="flex items-center gap-4 p-3 hover:bg-gray-50 dark:hover:bg-gray-700/50 rounded-lg transition-colors"
              >
                <div className="p-2 bg-blue-100 dark:bg-blue-500/20 rounded-lg">
                  <Icon
                    size={20}
                    className="text-blue-600 dark:text-blue-400"
                  />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                    {activity.action}
                  </p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">
                    {activity.module}
                  </p>
                </div>
                <span className="text-xs text-gray-400 dark:text-gray-500">
                  {activity.time}
                </span>
              </div>
            );
          })}
        </div>
      </Card>
    </div>
  );
};

export default Dashboard;
