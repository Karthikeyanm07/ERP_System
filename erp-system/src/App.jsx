/**
 * Main App Component - Routing Configuration
 *
 * Explanation:
 * - AuthProvider: Wraps all components to provide authentication context
 * - Routes: Defines URL-to-component mapping
 * - ProtectedRoute: Wrapper that redirects to login if not authenticated
 *
 * Route Structure:
 * - /login, /register → Public (no auth needed)
 * - / → Dashboard (protected)
 * - /hr/* → HR module pages (protected)
 * - /finance/* → Finance module pages (protected)
 * - /inventory/* → Inventory module pages (protected)
 * - /sales/* → Sales module pages (protected)
 */

import { Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { DataCacheProvider } from "./context/DataCacheContext";
import { SidebarProvider } from "./context/SidebarContext";
import { ThemeProvider } from "./context/ThemeContext";
import { useAuth } from "./hooks/useAuth";
import { ToastProvider } from "./components/common/Toast";

// Layout
import Layout from "./components/layout/Layout";

// Auth Pages
import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";
import ForgotPassword from "./pages/auth/ForgotPassword";
import ResetPassword from "./pages/auth/ResetPassword";

// Dashboard
import Dashboard from "./pages/dashboard/Dashboard";

// HR Module
import Employees from "./pages/hr/Employees";
import Departments from "./pages/hr/Departments";
import Attendance from "./pages/hr/Attendance";
import LeaveManagement from "./pages/hr/LeaveManagement";

// Finance Module
import Accounts from "./pages/finance/Accounts";
import Transactions from "./pages/finance/Transactions";
import Expenses from "./pages/finance/Expenses";

// Inventory Module
import Products from "./pages/inventory/Products";
import Stock from "./pages/inventory/Stock";
import PurchaseOrders from "./pages/inventory/PurchaseOrders";
import Suppliers from "./pages/inventory/Suppliers";

// Sales Module
import Customers from "./pages/sales/Customers";
import SalesOrders from "./pages/sales/SalesOrders";
import Invoices from "./pages/sales/Invoices";
import Payments from "./pages/sales/Payments";

// Profile & Settings
import Profile from "./pages/profile/Profile";
import Settings from "./pages/settings/Settings";
import AuditLogs from "./pages/settings/AuditLogs";

// Legal
import PrivacyPolicy from "./pages/legal/PrivacyPolicy";

/**
 * ProtectedRoute Component
 *
 * How it works:
 * 1. Checks if user is logged in via AuthContext
 * 2. If loading (checking localStorage) → shows loading spinner
 * 3. If not logged in → redirects to /login
 * 4. If logged in → renders the child component
 *
 * @param {ReactNode} children - Component to render if authenticated
 */
const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();

  // While checking authentication status
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-950">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 dark:border-blue-400"></div>
      </div>
    );
  }

  // Not authenticated - redirect to login
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Authenticated - render the protected content
  return children;
};

/**
 * AppRoutes Component
 *
 * Separated from App because useAuth hook must be used inside AuthProvider.
 * This component contains all the route definitions.
 */
const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Routes - No authentication required */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/privacy-policy" element={<PrivacyPolicy />} />

      {/* Protected Routes - Wrapped in Layout with Sidebar/Navbar */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        {/* Dashboard - Default route */}
        <Route index element={<Dashboard />} />

        {/* Profile & Settings */}
        <Route path="profile" element={<Profile />} />
        <Route path="settings" element={<Settings />} />

        {/* HR Module Routes */}
        <Route path="hr">
          <Route path="employees" element={<Employees />} />
          <Route path="departments" element={<Departments />} />
          <Route path="attendance" element={<Attendance />} />
          <Route path="leave" element={<LeaveManagement />} />
        </Route>

        {/* Finance Module Routes */}
        <Route path="finance">
          <Route path="accounts" element={<Accounts />} />
          <Route path="transactions" element={<Transactions />} />
          <Route path="expenses" element={<Expenses />} />
        </Route>

        {/* Inventory Module Routes */}
        <Route path="inventory">
          <Route path="products" element={<Products />} />
          <Route path="stock" element={<Stock />} />
          <Route path="purchase-orders" element={<PurchaseOrders />} />
          <Route path="suppliers" element={<Suppliers />} />
        </Route>

        {/* Sales Module Routes */}
        <Route path="sales">
          <Route path="customers" element={<Customers />} />
          <Route path="orders" element={<SalesOrders />} />
          <Route path="invoices" element={<Invoices />} />
          <Route path="payments" element={<Payments />} />
        </Route>

        {/* Admin/System Routes */}
        <Route path="admin">
          <Route path="audit-logs" element={<AuditLogs />} />
        </Route>
      </Route>

      {/* Catch-all - Redirect unknown routes to dashboard */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

/**
 * Main App Component
 *
 * Structure:
 * - AuthProvider at the top level provides user state to entire app
 * - AppRoutes defines all the navigation paths
 */
function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <DataCacheProvider>
          <SidebarProvider>
            <ToastProvider>
              <AppRoutes />
            </ToastProvider>
          </SidebarProvider>
        </DataCacheProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
