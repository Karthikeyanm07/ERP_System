/**
 * Layout Component
 *
 * Features:
 * - Sidebar shows icons when collapsed (w-16)
 * - Sidebar shows full menu when expanded (w-64)
 * - Content area adjusts accordingly
 */

import { Outlet } from "react-router-dom";
import { useSidebar } from "../../context/SidebarContext";
import Sidebar from "./Sidebar";
import Navbar from "./Navbar";

const Layout = () => {
  const { isCollapsed } = useSidebar();

  return (
    <div className="flex min-h-screen bg-gray-50 dark:bg-gray-950">
      {/* Sidebar */}
      <Sidebar />

      {/* Main Content Area - adjusts margin based on sidebar state */}
      <div
        className={`flex-1 flex flex-col transition-all duration-300 ease-in-out ${
          isCollapsed ? "ml-16" : "ml-64"
        }`}
      >
        {/* Top Navbar */}
        <Navbar />

        {/* Page Content */}
        <main className="flex-1 p-6 overflow-auto dark:bg-gray-950">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Layout;
