/**
 * DataTable Component — Powered by @tanstack/react-table v8
 *
 * Features:
 *  - Sortable columns (click header)
 *  - Global text filter (search across all columns)
 *  - Pagination with page-size selector
 *  - Row selection with checkboxes
 *  - Column resizing via drag handles
 *  - Loading skeleton & empty state
 *  - Dark mode via Tailwind
 *  - Actions column (DropdownActions compatible)
 */

import { useState, useMemo } from "react";
import {
  useReactTable,
  getCoreRowModel,
  getSortedRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  flexRender,
} from "@tanstack/react-table";
import {
  ChevronUp,
  ChevronDown,
  ChevronsUpDown,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  Search,
  X,
} from "lucide-react";

/* ─── Indeterminate Checkbox ─── */
function IndeterminateCheckbox({ indeterminate, className = "", ...rest }) {
  const ref = (el) => {
    if (el) el.indeterminate = indeterminate;
  };
  return (
    <input
      type="checkbox"
      ref={ref}
      className={`w-4 h-4 rounded border-gray-300 dark:border-gray-600 text-blue-600 
        focus:ring-blue-500 focus:ring-offset-0 dark:bg-gray-700 cursor-pointer 
        accent-blue-600 ${className}`}
      {...rest}
    />
  );
}

/* ─── Main DataTable ─── */
const DataTable = ({
  columns: userColumns = [],
  data = [],
  loading = false,
  emptyMessage = "No data available",
  actions,
  enableSorting = true,
  enableFiltering = true,
  enablePagination = true,
  enableRowSelection = false,
  enableColumnResizing = true,
  onRowSelectionChange,
  pageSize: defaultPageSize = 10,
  globalFilter: controlledGlobalFilter,
  onGlobalFilterChange: controlledOnGlobalFilterChange,
  searchPlaceholder = "Search all columns...",
  filters,
}) => {
  /* ── State ── */
  const [sorting, setSorting] = useState([]);
  const [internalGlobalFilter, setInternalGlobalFilter] = useState("");
  const [rowSelection, setRowSelection] = useState({});
  const [columnSizing, setColumnSizing] = useState({});

  // Support both controlled and uncontrolled global filter
  const globalFilter =
    controlledGlobalFilter !== undefined
      ? controlledGlobalFilter
      : internalGlobalFilter;
  const setGlobalFilter =
    controlledOnGlobalFilterChange || setInternalGlobalFilter;

  /* ── Build columns ── */
  const tableColumns = useMemo(() => {
    const cols = [];

    // Row selection checkbox column
    if (enableRowSelection) {
      cols.push({
        id: "select",
        size: 40,
        minSize: 40,
        maxSize: 40,
        enableResizing: false,
        enableSorting: false,
        header: ({ table }) => (
          <IndeterminateCheckbox
            checked={table.getIsAllRowsSelected()}
            indeterminate={table.getIsSomeRowsSelected()}
            onChange={table.getToggleAllRowsSelectedHandler()}
            aria-label="Select all rows"
          />
        ),
        cell: ({ row }) => (
          <IndeterminateCheckbox
            checked={row.getIsSelected()}
            disabled={!row.getCanSelect()}
            indeterminate={row.getIsSomeSelected()}
            onChange={row.getToggleSelectedHandler()}
            aria-label="Select row"
          />
        ),
      });
    }

    // User-defined columns
    cols.push(...userColumns);

    // Actions column
    if (actions) {
      cols.push({
        id: "actions",
        header: "Actions",
        size: 80,
        minSize: 80,
        maxSize: 80,
        enableResizing: false,
        enableSorting: false,
        cell: ({ row }) => (
          <div className="flex items-center justify-center">
            {actions(row.original)}
          </div>
        ),
      });
    }

    return cols;
  }, [userColumns, actions, enableRowSelection]);

  /* ── Table instance ── */
  const table = useReactTable({
    data,
    columns: tableColumns,
    state: {
      sorting,
      globalFilter,
      rowSelection,
      columnSizing,
    },
    onSortingChange: setSorting,
    onGlobalFilterChange: setGlobalFilter,
    onRowSelectionChange: (updater) => {
      const newSelection =
        typeof updater === "function" ? updater(rowSelection) : updater;
      setRowSelection(newSelection);
      if (onRowSelectionChange) {
        const selectedRows = Object.keys(newSelection)
          .filter((k) => newSelection[k])
          .map((k) => data[parseInt(k)]);
        onRowSelectionChange(selectedRows);
      }
    },
    onColumnSizingChange: setColumnSizing,
    columnResizeMode: "onChange",
    getCoreRowModel: getCoreRowModel(),
    ...(enableSorting && { getSortedRowModel: getSortedRowModel() }),
    ...(enableFiltering && { getFilteredRowModel: getFilteredRowModel() }),
    ...(enablePagination && { getPaginationRowModel: getPaginationRowModel() }),
    initialState: {
      pagination: { pageSize: defaultPageSize },
    },
    globalFilterFn: "includesString",
  });

  /* ── Helpers ── */
  const headerGroups = table.getHeaderGroups();
  const rows = table.getRowModel().rows;
  const hasToolbar = enableFiltering || enableRowSelection || filters;

  /* ─────────────── RENDER ─────────────── */

  // ── Loading Skeleton ──
  if (loading) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
        {/* Skeleton toolbar */}
        <div className="flex items-center gap-3 px-4 py-3 border-b border-gray-100 dark:border-gray-700/50">
          <div className="h-9 bg-gray-100 dark:bg-gray-700 rounded-lg animate-pulse w-56" />
          <div className="h-9 bg-gray-100 dark:bg-gray-700 rounded-lg animate-pulse w-32" />
          <div className="h-9 bg-gray-100 dark:bg-gray-700 rounded-lg animate-pulse w-32" />
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 dark:bg-gray-700/50">
              <tr>
                {tableColumns.map((col, i) => (
                  <th
                    key={col.id || col.accessorKey || i}
                    className="px-4 py-3.5 text-left text-xs font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider"
                  >
                    <div className="h-3 bg-gray-200 dark:bg-gray-600 rounded w-16 animate-pulse" />
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-700/50">
              {Array.from({ length: 5 }).map((_, i) => (
                <tr key={i}>
                  {tableColumns.map((col, j) => (
                    <td
                      key={j}
                      className="px-4 py-3.5"
                    >
                      <div className="h-4 bg-gray-100 dark:bg-gray-700 rounded animate-pulse" style={{ width: `${55 + Math.random() * 30}%` }} />
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
      {/* ── Toolbar: Search + Filters + Selection Info ── */}
      {hasToolbar && (
        <div className="flex flex-wrap items-center gap-3 px-4 py-3 border-b border-gray-100 dark:border-gray-700/50">
          {/* Global Search */}
          {enableFiltering && (
            <div className="relative group min-w-[200px] max-w-xs flex-shrink-0">
              <Search
                className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500 
                  group-focus-within:text-blue-500 dark:group-focus-within:text-blue-400 
                  transition-colors duration-200 pointer-events-none"
                size={16}
              />
              <input
                type="text"
                value={globalFilter ?? ""}
                onChange={(e) => setGlobalFilter(e.target.value)}
                placeholder={searchPlaceholder}
                className="w-full pl-9 pr-8 py-2 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 
                  dark:border-gray-600 text-gray-900 dark:text-gray-100 placeholder-gray-400 
                  dark:placeholder-gray-500 rounded-lg focus:outline-none focus:bg-white 
                  dark:focus:bg-gray-700 focus:border-blue-400 dark:focus:border-blue-500 
                  focus:ring-2 focus:ring-blue-500/20 dark:focus:ring-blue-400/20
                  transition-all duration-200 text-sm"
                aria-label="Search table"
              />
              {globalFilter && (
                <button
                  onClick={() => setGlobalFilter("")}
                  className="absolute right-2.5 top-1/2 -translate-y-1/2 p-0.5 rounded text-gray-400 
                    hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
                  aria-label="Clear search"
                >
                  <X size={14} />
                </button>
              )}
            </div>
          )}
          {/* Inline Filters */}
          {filters && (
            <div className="flex flex-wrap items-center gap-2 flex-1 min-w-0">
              {filters}
            </div>
          )}

          {/* Selection Info — pushed right */}
          {enableRowSelection && Object.keys(rowSelection).length > 0 && (
            <div className="flex items-center gap-2 text-sm text-blue-600 dark:text-blue-400 font-medium ml-auto">
              <span className="inline-flex items-center justify-center w-5 h-5 bg-blue-100 dark:bg-blue-900/40 rounded-full text-xs">
                {table.getSelectedRowModel().rows.length}
              </span>
              row{table.getSelectedRowModel().rows.length !== 1 ? "s" : ""} selected
            </div>
          )}
        </div>
      )}

      {/* ── Table ── */}
      <div className="overflow-x-auto">
        <table className="w-full" style={{ minWidth: table.getTotalSize() }}>
          {/* THEAD */}
          <thead className="bg-gray-50/80 dark:bg-gray-700/30">
            {headerGroups.map((headerGroup) => (
              <tr key={headerGroup.id}>
                {headerGroup.headers.map((header) => {
                  const canSort = header.column.getCanSort();
                  const sorted = header.column.getIsSorted();
                  return (
                    <th
                      key={header.id}
                      className={`relative px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider 
                        select-none
                        ${canSort
                          ? "cursor-pointer hover:bg-gray-100/80 dark:hover:bg-gray-600/30 transition-colors"
                          : ""
                        }
                        ${sorted
                          ? "text-gray-900 dark:text-gray-100"
                          : "text-gray-500 dark:text-gray-400"
                        }`}
                      style={{ width: header.getSize() }}
                      onClick={header.column.getToggleSortingHandler()}
                    >
                      <div className="flex items-center gap-1.5">
                        {header.isPlaceholder
                          ? null
                          : flexRender(
                              header.column.columnDef.header,
                              header.getContext()
                            )}

                        {/* Sort indicator */}
                        {canSort && (
                          <span className="inline-flex flex-col -space-y-1">
                            {sorted === "asc" ? (
                              <ChevronUp size={14} className="text-blue-600 dark:text-blue-400" />
                            ) : sorted === "desc" ? (
                              <ChevronDown size={14} className="text-blue-600 dark:text-blue-400" />
                            ) : (
                              <ChevronsUpDown size={14} className="text-gray-300 dark:text-gray-600" />
                            )}
                          </span>
                        )}
                      </div>

                      {/* Resize handle */}
                      {enableColumnResizing && header.column.getCanResize() && (
                        <div
                          onMouseDown={header.getResizeHandler()}
                          onTouchStart={header.getResizeHandler()}
                          onClick={(e) => e.stopPropagation()}
                          className={`absolute right-0 top-0 h-full w-1 cursor-col-resize select-none touch-none 
                            hover:bg-blue-500 dark:hover:bg-blue-400 transition-colors
                            ${header.column.getIsResizing() ? "bg-blue-600 dark:bg-blue-400" : "bg-transparent"}`}
                        />
                      )}
                    </th>
                  );
                })}
              </tr>
            ))}
          </thead>

          {/* TBODY */}
          <tbody className="divide-y divide-gray-100 dark:divide-gray-700/50">
            {rows.length === 0 ? (
              <tr>
                <td
                  colSpan={tableColumns.length}
                  className="px-6 py-16 text-center"
                >
                  <div className="flex flex-col items-center gap-2">
                    <div className="w-12 h-12 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center">
                      <Search size={20} className="text-gray-400 dark:text-gray-500" />
                    </div>
                    <p className="text-gray-500 dark:text-gray-400 text-sm font-medium">
                      {emptyMessage}
                    </p>
                    {globalFilter && (
                      <button
                        onClick={() => setGlobalFilter("")}
                        className="text-blue-600 dark:text-blue-400 text-xs hover:underline"
                      >
                        Clear search filter
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ) : (
              rows.map((row) => (
                <tr
                  key={row.id}
                  className={`transition-colors
                    ${row.getIsSelected()
                      ? "bg-blue-50/60 dark:bg-blue-900/15"
                      : "hover:bg-gray-50/70 dark:hover:bg-gray-700/30"
                    }`}
                >
                  {row.getVisibleCells().map((cell) => (
                    <td
                      key={cell.id}
                      className="px-4 py-3 text-sm text-gray-700 dark:text-gray-200"
                      style={{ width: cell.column.getSize() }}
                    >
                      {flexRender(
                        cell.column.columnDef.cell,
                        cell.getContext()
                      )}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* ── Pagination ── */}
      {enablePagination && data.length > 0 && (
        <div className="flex flex-wrap items-center justify-between gap-3 px-4 py-3 border-t border-gray-100 dark:border-gray-700/50 text-sm">
          {/* Left: row count */}
          <div className="text-gray-500 dark:text-gray-400">
            Showing{" "}
            <span className="font-semibold text-gray-700 dark:text-gray-200">
              {table.getState().pagination.pageIndex *
                table.getState().pagination.pageSize +
                1}
            </span>
            –
            <span className="font-semibold text-gray-700 dark:text-gray-200">
              {Math.min(
                (table.getState().pagination.pageIndex + 1) *
                  table.getState().pagination.pageSize,
                table.getFilteredRowModel().rows.length
              )}
            </span>{" "}
            of{" "}
            <span className="font-semibold text-gray-700 dark:text-gray-200">
              {table.getFilteredRowModel().rows.length}
            </span>{" "}
            results
          </div>

          {/* Center: page buttons */}
          <div className="flex items-center gap-1">
            <button
              onClick={() => table.setPageIndex(0)}
              disabled={!table.getCanPreviousPage()}
              className="p-1.5 rounded-lg text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 
                disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
              aria-label="First page"
            >
              <ChevronsLeft size={16} />
            </button>
            <button
              onClick={() => table.previousPage()}
              disabled={!table.getCanPreviousPage()}
              className="p-1.5 rounded-lg text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 
                disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
              aria-label="Previous page"
            >
              <ChevronLeft size={16} />
            </button>

            <div className="flex items-center gap-1 px-2">
              <span className="text-gray-500 dark:text-gray-400">Page</span>
              <span className="font-semibold text-gray-700 dark:text-gray-200 min-w-[2ch] text-center">
                {table.getState().pagination.pageIndex + 1}
              </span>
              <span className="text-gray-500 dark:text-gray-400">
                of {table.getPageCount()}
              </span>
            </div>

            <button
              onClick={() => table.nextPage()}
              disabled={!table.getCanNextPage()}
              className="p-1.5 rounded-lg text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 
                disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
              aria-label="Next page"
            >
              <ChevronRight size={16} />
            </button>
            <button
              onClick={() => table.setPageIndex(table.getPageCount() - 1)}
              disabled={!table.getCanNextPage()}
              className="p-1.5 rounded-lg text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 
                disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
              aria-label="Last page"
            >
              <ChevronsRight size={16} />
            </button>
          </div>

          {/* Right: page size */}
          <select
            value={table.getState().pagination.pageSize}
            onChange={(e) => table.setPageSize(Number(e.target.value))}
            className="px-3 py-1.5 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-600 
              text-gray-700 dark:text-gray-200 rounded-lg text-sm focus:outline-none 
              focus:border-gray-400 dark:focus:border-gray-500 cursor-pointer transition-colors"
          >
            {[10, 20, 50].map((size) => (
              <option key={size} value={size}>
                {size} / page
              </option>
            ))}
          </select>
        </div>
      )}
    </div>
  );
};

export default DataTable;
