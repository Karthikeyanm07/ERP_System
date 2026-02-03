/**
 * Table Component
 *
 * Features:
 * - Reusable data table with configurable columns
 * - Fixed column widths for better alignment
 * - Handles loading state with skeleton
 * - Shows empty state when no data
 * - Supports row actions (edit, delete, etc.)
 *
 * Props:
 * - columns: Array of column definitions { key, header, render?, width? }
 * - data: Array of data objects
 * - loading: Boolean to show loading skeleton
 * - onRowClick: Optional callback when row is clicked
 * - actions: Optional function to render action buttons
 */

import { Loader2 } from "lucide-react";

const Table = ({
  columns = [],
  data = [],
  loading = false,
  onRowClick,
  actions,
  emptyMessage = "No data available",
}) => {
  // Loading skeleton rows
  if (loading) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full table-fixed">
            <thead className="bg-gray-50 dark:bg-gray-700/50">
              <tr>
                {columns.map((column) => (
                  <th
                    key={column.key}
                    className="px-4 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider"
                    style={{ width: column.width || "auto" }}
                  >
                    {column.header}
                  </th>
                ))}
                {actions && (
                  <th
                    className="px-4 py-3 text-center text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider"
                    style={{ width: "100px" }}
                  >
                    Actions
                  </th>
                )}
              </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
              {[1, 2, 3, 4, 5].map((i) => (
                <tr key={i} className="animate-pulse">
                  {columns.map((column) => (
                    <td key={column.key} className="px-4 py-4">
                      <div className="h-4 bg-gray-200 dark:bg-gray-600 rounded w-3/4"></div>
                    </td>
                  ))}
                  {actions && (
                    <td className="px-4 py-4 text-center">
                      <div className="h-4 bg-gray-200 dark:bg-gray-600 rounded w-16 mx-auto"></div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  // Empty state
  if (!data || data.length === 0) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full table-fixed">
            <thead className="bg-gray-50 dark:bg-gray-700/50">
              <tr>
                {columns.map((column) => (
                  <th
                    key={column.key}
                    className="px-4 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider"
                    style={{ width: column.width || "auto" }}
                  >
                    {column.header}
                  </th>
                ))}
                {actions && (
                  <th
                    className="px-4 py-3 text-center text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider"
                    style={{ width: "100px" }}
                  >
                    Actions
                  </th>
                )}
              </tr>
            </thead>
          </table>
        </div>
        <div className="px-6 py-12 text-center">
          <p className="text-gray-500 dark:text-gray-400">{emptyMessage}</p>
        </div>
      </div>
    );
  }

  // Data table
  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
      <div className="overflow-x-auto">
        <table className="w-full table-fixed">
          <thead className="bg-gray-50 dark:bg-gray-700/50">
            <tr>
              {columns.map((column) => (
                <th
                  key={column.key}
                  className="px-4 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider"
                  style={{ width: column.width || "auto" }}
                >
                  {column.header}
                </th>
              ))}
              {actions && (
                <th
                  className="px-4 py-3 text-center text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider"
                  style={{ width: "100px" }}
                >
                  Actions
                </th>
              )}
            </tr>
          </thead>
          <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            {data.map((row, rowIndex) => (
              <tr
                key={row.id || rowIndex}
                onClick={() => onRowClick && onRowClick(row)}
                className={`${
                  onRowClick
                    ? "cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700/50"
                    : ""
                } transition-colors`}
              >
                {columns.map((column) => (
                  <td
                    key={column.key}
                    className="px-4 py-3 text-sm text-gray-700 dark:text-gray-100 overflow-hidden text-ellipsis"
                    style={{ width: column.width || "auto" }}
                  >
                    {/* Use custom render if provided, otherwise get value by key */}
                    {column.render
                      ? column.render(row[column.key], row)
                      : row[column.key] || "—"}
                  </td>
                ))}
                {actions && (
                  <td className="px-4 py-3 text-center">
                    <div className="flex items-center justify-center gap-1">
                      {actions(row)}
                    </div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Table;
