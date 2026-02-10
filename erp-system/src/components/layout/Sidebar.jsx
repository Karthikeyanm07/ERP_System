/**
 * Sidebar Component
 *
 * Features:
 * - Collapsible sidebar showing icons when minimized
 * - Full sidebar with labels when expanded
 * - Preserves logo when collapsed
 * - Tooltip on hover for collapsed icons
 */

import { useState } from "react";
import { NavLink } from "react-router-dom";
import { useSidebar } from "../../context/SidebarContext";
import {
  LayoutDashboard,
  Users,
  Building2,
  Calendar,
  FileText,
  DollarSign,
  Receipt,
  CreditCard,
  Package,
  Warehouse,
  ShoppingCart,
  Truck,
  UserCircle,
  ClipboardList,
  FileSpreadsheet,
  Wallet,
  ChevronDown,
  ChevronRight,
} from "lucide-react";

/**
 * Navigation menu structure
 */
const menuItems = [
  {
    name: "Dashboard",
    icon: LayoutDashboard,
    path: "/",
  },
  {
    name: "HR Management",
    icon: Users,
    subItems: [
      { name: "Employees", icon: UserCircle, path: "/hr/employees" },
      { name: "Departments", icon: Building2, path: "/hr/departments" },
      { name: "Attendance", icon: Calendar, path: "/hr/attendance" },
      { name: "Leave Requests", icon: FileText, path: "/hr/leave" },
    ],
  },
  {
    name: "Finance",
    icon: DollarSign,
    subItems: [
      { name: "Accounts", icon: Wallet, path: "/finance/accounts" },
      { name: "Transactions", icon: Receipt, path: "/finance/transactions" },
      { name: "Expenses", icon: CreditCard, path: "/finance/expenses" },
    ],
  },
  {
    name: "Inventory",
    icon: Package,
    subItems: [
      { name: "Products", icon: Package, path: "/inventory/products" },
      { name: "Stock", icon: Warehouse, path: "/inventory/stock" },
      {
        name: "Purchase Orders",
        icon: ClipboardList,
        path: "/inventory/purchase-orders",
      },
      { name: "Suppliers", icon: Truck, path: "/inventory/suppliers" },
    ],
  },
  {
    name: "Sales",
    icon: ShoppingCart,
    subItems: [
      { name: "Customers", icon: UserCircle, path: "/sales/customers" },
      { name: "Sales Orders", icon: ClipboardList, path: "/sales/orders" },
      { name: "Invoices", icon: FileSpreadsheet, path: "/sales/invoices" },
      { name: "Payments", icon: CreditCard, path: "/sales/payments" },
    ],
  },
];

/**
 * MenuItem Component with collapsed/expanded modes
 */
const MenuItem = ({
  item,
  isExpanded,
  onToggle,
  isCollapsed,
  onExpandSidebar,
}) => {
  const Icon = item.icon;

  // Collapsed mode - show only icon with tooltip, click to expand sidebar
  if (isCollapsed) {
    if (item.subItems) {
      return (
        <div className="relative group mb-1">
          <button
            onClick={onExpandSidebar}
            className="w-full flex items-center justify-center p-3 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100 rounded-lg transition-all duration-200"
          >
            <Icon size={20} />
          </button>
          {/* Tooltip on hover */}
          <div className="absolute left-full ml-2 top-1/2 -translate-y-1/2 bg-gray-800 dark:bg-gray-700 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 pointer-events-none whitespace-nowrap z-50 transition-opacity">
            {item.name}
          </div>
        </div>
      );
    }

    return (
      <div className="relative group">
        <NavLink
          to={item.path}
          end
          onClick={onExpandSidebar}
          className={({ isActive }) =>
            `flex items-center justify-center p-3 rounded-lg transition-all duration-200 ${
              isActive
                ? "bg-blue-600 dark:bg-blue-500 text-white shadow-lg shadow-blue-600/30 dark:shadow-blue-500/30"
                : "text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100"
            }`
          }
        >
          <Icon size={20} />
        </NavLink>
        {/* Tooltip on hover */}
        <div className="absolute left-full ml-2 top-1/2 -translate-y-1/2 bg-gray-800 dark:bg-gray-700 text-white text-sm px-2 py-1 rounded opacity-0 group-hover:opacity-100 pointer-events-none whitespace-nowrap z-50 transition-opacity">
          {item.name}
        </div>
      </div>
    );
  }

  // Expanded mode - full menu items
  if (item.subItems) {
    return (
      <div className="mb-1">
        <button
          onClick={onToggle}
          className="w-full flex items-center justify-between px-4 py-3 text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100 rounded-lg transition-all duration-200 overflow-hidden"
        >
          <div className="flex items-center gap-3 overflow-hidden">
            <Icon size={20} className="shrink-0" />
            <span className="font-medium whitespace-nowrap overflow-hidden text-ellipsis">{item.name}</span>
          </div>
          {isExpanded ? <ChevronDown size={18} className="shrink-0" /> : <ChevronRight size={18} className="shrink-0" />}
        </button>

        {isExpanded && (
          <div className="ml-4 mt-1 space-y-1">
            {item.subItems.map((subItem) => {
              const SubIcon = subItem.icon;
              return (
                <NavLink
                  key={subItem.path}
                  to={subItem.path}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-4 py-2 rounded-lg transition-all duration-200 overflow-hidden ${
                      isActive
                        ? "bg-blue-50 dark:bg-blue-500/20 text-blue-700 dark:text-blue-300 font-medium"
                        : "text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-700 dark:hover:text-gray-200"
                    }`
                  }
                >
                  <SubIcon size={18} className="shrink-0" />
                  <span className="whitespace-nowrap overflow-hidden text-ellipsis">{subItem.name}</span>
                </NavLink>
              );
            })}
          </div>
        )}
      </div>
    );
  }

  return (
    <NavLink
      to={item.path}
      end
      className={({ isActive }) =>
        `flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 overflow-hidden ${
          isActive
            ? "bg-blue-600 dark:bg-blue-500 text-white shadow-lg shadow-blue-600/30 dark:shadow-blue-500/30"
            : "text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-100"
        }`
      }
    >
      <Icon size={20} className="shrink-0" />
      <span className="font-medium whitespace-nowrap overflow-hidden text-ellipsis">{item.name}</span>
    </NavLink>
  );
};

/**
 * Sidebar Component
 */
const Sidebar = () => {
  const { isCollapsed, toggleSidebar } = useSidebar();
  const [expandedSections, setExpandedSections] = useState({});

  const toggleSection = (sectionName) => {
    setExpandedSections((prev) => ({
      ...prev,
      [sectionName]: !prev[sectionName],
    }));
  };

  // Expand sidebar when clicking an icon in collapsed mode
  const handleExpandSidebar = () => {
    if (isCollapsed) {
      toggleSidebar();
    }
  };

  return (
    <aside
      className={`fixed left-0 top-0 h-screen bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-800 flex flex-col z-40 transition-all duration-300 ease-in-out overflow-hidden ${
        isCollapsed ? "w-16" : "w-64"
      }`}
    >
      {/* Logo/Brand */}
      <div className="h-16 flex items-center justify-center border-b border-gray-200 dark:border-gray-800">
        {isCollapsed ? (
          <div
            className="w-10 h-10 bg-gradient-to-br from-blue-600 to-indigo-600 dark:from-blue-500 dark:to-indigo-500 rounded-xl flex items-center justify-center cursor-pointer"
            onClick={handleExpandSidebar}
          >
            <Package className="text-white" size={24} />
          </div>
        ) : (
          <div className="flex items-center gap-2 overflow-hidden">
            <div className="w-10 h-10 bg-gradient-to-br from-blue-600 to-indigo-600 dark:from-blue-500 dark:to-indigo-500 rounded-xl flex items-center justify-center shrink-0">
              <Package className="text-white" size={24} />
            </div>
            <div className="overflow-hidden">
              <h1 className="text-xl font-bold text-gray-800 dark:text-gray-100 whitespace-nowrap">
                ERP System
              </h1>
              <p className="text-xs text-gray-500 dark:text-gray-400 whitespace-nowrap">
                Enterprise Suite
              </p>
            </div>
          </div>
        )}
      </div>

      {/* Navigation Menu */}
      <nav
        className={`flex-1 ${
          isCollapsed ? "p-2" : "p-4"
        } space-y-1 overflow-y-auto overflow-x-hidden`}
      >
        {menuItems.map((item) => (
          <MenuItem
            key={item.name}
            item={item}
            isExpanded={expandedSections[item.name]}
            onToggle={() => toggleSection(item.name)}
            isCollapsed={isCollapsed}
            onExpandSidebar={handleExpandSidebar}
          />
        ))}
      </nav>

      {/* Version info at bottom */}
      {!isCollapsed && (
        <div className="p-4 border-t border-gray-200 dark:border-gray-800">
          <p className="text-xs text-gray-400 dark:text-gray-500 text-center">
            v1.0.0
          </p>
        </div>
      )}
    </aside>
  );
};

export default Sidebar;
