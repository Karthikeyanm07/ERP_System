/**
 * CommandPalette Component
 *
 * Spotlight-style global search activated by Ctrl+K / ⌘+K.
 * Features:
 * - Fuzzy search across all pages/modules
 * - Keyboard navigation (↑/↓ to move, Enter to select, Escape to close)
 * - Quick actions (toggle theme)
 * - Premium dark mode support with backdrop blur
 */

import { useState, useEffect, useRef, useCallback } from "react";
import { createPortal } from "react-dom";
import { useNavigate } from "react-router-dom";
import { useTheme } from "../../context/ThemeContext";
import {
  Search,
  LayoutDashboard,
  Users,
  UserCircle,
  Building2,
  Calendar,
  FileText,
  DollarSign,
  Receipt,
  CreditCard,
  Package,
  Warehouse,
  ClipboardList,
  Truck,
  ShoppingCart,
  FileSpreadsheet,
  Wallet,
  Sun,
  Moon,
  ArrowRight,
  Command,
  CornerDownLeft,
} from "lucide-react";

/* ── Searchable items ── */
const NAV_ITEMS = [
  {
    id: "dashboard",
    name: "Dashboard",
    category: "Navigation",
    icon: LayoutDashboard,
    path: "/",
    keywords: ["home", "overview", "main"],
  },
  // HR
  {
    id: "employees",
    name: "Employees",
    category: "HR Management",
    icon: UserCircle,
    path: "/hr/employees",
    keywords: ["staff", "people", "team", "workers"],
  },
  {
    id: "departments",
    name: "Departments",
    category: "HR Management",
    icon: Building2,
    path: "/hr/departments",
    keywords: ["teams", "divisions", "groups"],
  },
  {
    id: "attendance",
    name: "Attendance",
    category: "HR Management",
    icon: Calendar,
    path: "/hr/attendance",
    keywords: ["checkin", "clock", "present", "absent"],
  },
  {
    id: "leave",
    name: "Leave Requests",
    category: "HR Management",
    icon: FileText,
    path: "/hr/leave",
    keywords: ["vacation", "time off", "holiday", "sick"],
  },
  // Finance
  {
    id: "accounts",
    name: "Accounts",
    category: "Finance",
    icon: Wallet,
    path: "/finance/accounts",
    keywords: ["ledger", "chart of accounts", "balance"],
  },
  {
    id: "transactions",
    name: "Transactions",
    category: "Finance",
    icon: Receipt,
    path: "/finance/transactions",
    keywords: ["journal", "entry", "debit", "credit"],
  },
  {
    id: "expenses",
    name: "Expenses",
    category: "Finance",
    icon: CreditCard,
    path: "/finance/expenses",
    keywords: ["cost", "reimbursement", "spend"],
  },
  // Inventory
  {
    id: "products",
    name: "Products",
    category: "Inventory",
    icon: Package,
    path: "/inventory/products",
    keywords: ["items", "goods", "catalog"],
  },
  {
    id: "stock",
    name: "Stock",
    category: "Inventory",
    icon: Warehouse,
    path: "/inventory/stock",
    keywords: ["inventory", "quantity", "warehouse"],
  },
  {
    id: "purchase-orders",
    name: "Purchase Orders",
    category: "Inventory",
    icon: ClipboardList,
    path: "/inventory/purchase-orders",
    keywords: ["po", "buy", "procurement", "order"],
  },
  {
    id: "suppliers",
    name: "Suppliers",
    category: "Inventory",
    icon: Truck,
    path: "/inventory/suppliers",
    keywords: ["vendor", "provider", "manufacturer"],
  },
  // Sales
  {
    id: "customers",
    name: "Customers",
    category: "Sales",
    icon: UserCircle,
    path: "/sales/customers",
    keywords: ["client", "buyer", "contact"],
  },
  {
    id: "sales-orders",
    name: "Sales Orders",
    category: "Sales",
    icon: ClipboardList,
    path: "/sales/orders",
    keywords: ["so", "sell", "order"],
  },
  {
    id: "invoices",
    name: "Invoices",
    category: "Sales",
    icon: FileSpreadsheet,
    path: "/sales/invoices",
    keywords: ["bill", "receipt", "payment due"],
  },
  {
    id: "payments",
    name: "Payments",
    category: "Sales",
    icon: CreditCard,
    path: "/sales/payments",
    keywords: ["pay", "collection", "receive"],
  },
  // Legal
  {
    id: "privacy-policy",
    name: "Privacy & Policy",
    category: "Navigation",
    icon: CreditCard,
    path: "/privacy-policy",
    keywords: ["privacy", "policy", "legal", "terms", "data"],
  },
];

/* ── Category colours ── */
const CATEGORY_COLORS = {
  Navigation:
    "bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300",
  "HR Management":
    "bg-blue-50 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400",
  Finance:
    "bg-emerald-50 text-emerald-600 dark:bg-emerald-900/30 dark:text-emerald-400",
  Inventory:
    "bg-amber-50 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400",
  Sales:
    "bg-purple-50 text-purple-600 dark:bg-purple-900/30 dark:text-purple-400",
  Action:
    "bg-pink-50 text-pink-600 dark:bg-pink-900/30 dark:text-pink-400",
};

/* ── Fuzzy match helper ── */
const fuzzyMatch = (query, text) => {
  const q = query.toLowerCase();
  const t = text.toLowerCase();
  if (t.includes(q)) return true;
  // simple char-by-char fuzzy
  let qi = 0;
  for (let i = 0; i < t.length && qi < q.length; i++) {
    if (t[i] === q[qi]) qi++;
  }
  return qi === q.length;
};

const CommandPalette = ({ isOpen = false, onClose }) => {
  const [query, setQuery] = useState("");
  const [activeIndex, setActiveIndex] = useState(0);
  const inputRef = useRef(null);
  const listRef = useRef(null);
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();

  // Build action items (theme changes per current state)
  const actionItems = [
    {
      id: "toggle-theme",
      name: theme === "dark" ? "Switch to Light Mode" : "Switch to Dark Mode",
      category: "Action",
      icon: theme === "dark" ? Sun : Moon,
      action: () => toggleTheme(),
      keywords: ["theme", "dark", "light", "mode", "appearance"],
    },
  ];

  const allItems = [...NAV_ITEMS, ...actionItems];

  // Filtered results
  const results = query.trim()
    ? allItems.filter(
        (item) =>
          fuzzyMatch(query, item.name) ||
          fuzzyMatch(query, item.category) ||
          item.keywords.some((k) => fuzzyMatch(query, k))
      )
    : allItems;

  // Focus input when opened
  useEffect(() => {
    if (isOpen) {
      setQuery("");
      setActiveIndex(0);
      // Small delay to let the portal mount
      requestAnimationFrame(() => inputRef.current?.focus());
    }
  }, [isOpen]);

  // Reset active index when query changes
  useEffect(() => {
    setActiveIndex(0);
  }, [query]);

  // Scroll active item into view
  useEffect(() => {
    if (!listRef.current) return;
    const active = listRef.current.children[activeIndex];
    if (active) active.scrollIntoView({ block: "nearest" });
  }, [activeIndex]);

  // ── Select an item ──
  const selectItem = useCallback(
    (item) => {
      onClose?.();
      if (item.action) {
        item.action();
      } else if (item.path) {
        navigate(item.path);
      }
    },
    [navigate, onClose]
  );

  // ── Keyboard navigation inside palette ──
  const handleKeyDown = (e) => {
    switch (e.key) {
      case "ArrowDown":
        e.preventDefault();
        setActiveIndex((i) => (i + 1) % results.length);
        break;
      case "ArrowUp":
        e.preventDefault();
        setActiveIndex((i) => (i - 1 + results.length) % results.length);
        break;
      case "Enter":
        e.preventDefault();
        if (results[activeIndex]) selectItem(results[activeIndex]);
        break;
      case "Escape":
        e.preventDefault();
        onClose?.();
        break;
    }
  };

  if (!isOpen) return null;

  return createPortal(
    <div className="fixed inset-0 z-[9999] flex items-start justify-center pt-[15vh]">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/40 dark:bg-black/60 backdrop-blur-sm animate-[fadeIn_150ms_ease-out]"
        onClick={() => onClose?.()}
      />

      {/* Palette */}
      <div
        className="relative w-full max-w-lg mx-4 bg-white dark:bg-gray-900 rounded-2xl shadow-2xl dark:shadow-black/40 border border-gray-200 dark:border-gray-700 overflow-hidden animate-[slideDown_200ms_ease-out]"
        onKeyDown={handleKeyDown}
        role="dialog"
        aria-label="Command palette"
      >
        {/* Search input */}
        <div className="flex items-center gap-3 px-4 py-3.5 border-b border-gray-100 dark:border-gray-800">
          <Search
            size={18}
            className="text-gray-400 dark:text-gray-500 flex-shrink-0"
          />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search pages, actions..."
            className="flex-1 bg-transparent text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 text-sm outline-none"
          />
          <kbd className="hidden sm:inline-flex items-center gap-1 px-1.5 py-0.5 text-[10px] font-medium text-gray-400 dark:text-gray-500 bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded">
            ESC
          </kbd>
        </div>

        {/* Results list */}
        <div
          ref={listRef}
          className="max-h-[340px] overflow-y-auto overscroll-contain py-2 px-2"
          role="listbox"
        >
          {results.length > 0 ? (
            results.map((item, index) => {
              const Icon = item.icon;
              const isActive = index === activeIndex;
              return (
                <button
                  key={item.id}
                  role="option"
                  aria-selected={isActive}
                  onClick={() => selectItem(item)}
                  onMouseEnter={() => setActiveIndex(index)}
                  className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-left transition-colors duration-100 group ${
                    isActive
                      ? "bg-blue-50 dark:bg-blue-900/20"
                      : "hover:bg-gray-50 dark:hover:bg-gray-800/50"
                  }`}
                >
                  {/* Icon */}
                  <div
                    className={`flex items-center justify-center w-8 h-8 rounded-lg flex-shrink-0 transition-colors ${
                      isActive
                        ? "bg-blue-100 dark:bg-blue-900/40 text-blue-600 dark:text-blue-400"
                        : "bg-gray-100 dark:bg-gray-800 text-gray-500 dark:text-gray-400"
                    }`}
                  >
                    <Icon size={16} />
                  </div>

                  {/* Text */}
                  <div className="flex-1 min-w-0">
                    <p
                      className={`text-sm font-medium truncate ${
                        isActive
                          ? "text-blue-700 dark:text-blue-300"
                          : "text-gray-700 dark:text-gray-200"
                      }`}
                    >
                      {item.name}
                    </p>
                  </div>

                  {/* Category badge */}
                  <span
                    className={`hidden sm:inline-flex text-[10px] font-medium px-2 py-0.5 rounded-full flex-shrink-0 ${
                      CATEGORY_COLORS[item.category] || ""
                    }`}
                  >
                    {item.category}
                  </span>

                  {/* Arrow hint */}
                  <ArrowRight
                    size={14}
                    className={`flex-shrink-0 transition-all duration-150 ${
                      isActive
                        ? "opacity-100 text-blue-500 dark:text-blue-400 translate-x-0"
                        : "opacity-0 -translate-x-1"
                    }`}
                  />
                </button>
              );
            })
          ) : (
            <div className="px-4 py-8 text-center">
              <p className="text-sm text-gray-400 dark:text-gray-500">
                No results for "
                <span className="text-gray-600 dark:text-gray-300">
                  {query}
                </span>
                "
              </p>
            </div>
          )}
        </div>

        {/* Footer hints */}
        <div className="flex items-center gap-4 px-4 py-2.5 border-t border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
          <div className="flex items-center gap-1.5 text-[11px] text-gray-400 dark:text-gray-500">
            <kbd className="inline-flex items-center justify-center w-5 h-5 bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded text-[10px]">
              ↑
            </kbd>
            <kbd className="inline-flex items-center justify-center w-5 h-5 bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded text-[10px]">
              ↓
            </kbd>
            <span className="ml-0.5">Navigate</span>
          </div>
          <div className="flex items-center gap-1.5 text-[11px] text-gray-400 dark:text-gray-500">
            <kbd className="inline-flex items-center justify-center h-5 px-1 bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded text-[10px]">
              <CornerDownLeft size={10} />
            </kbd>
            <span className="ml-0.5">Select</span>
          </div>
          <div className="flex items-center gap-1.5 text-[11px] text-gray-400 dark:text-gray-500">
            <kbd className="inline-flex items-center justify-center h-5 px-1.5 bg-gray-100 dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded text-[10px]">
              Esc
            </kbd>
            <span className="ml-0.5">Close</span>
          </div>
        </div>
      </div>

      {/* Keyframe animations */}
      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; }
          to   { opacity: 1; }
        }
        @keyframes slideDown {
          from { opacity: 0; transform: scale(0.98) translateY(-8px); }
          to   { opacity: 1; transform: scale(1) translateY(0); }
        }
      `}</style>
    </div>,
    document.body
  );
};

export default CommandPalette;
