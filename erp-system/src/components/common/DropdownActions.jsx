/**
 * DropdownActions Component
 *
 * Context-aware 3-dot action menu for table rows.
 * Uses React Portal to avoid table overflow clipping.
 *
 * Props:
 * - actions: Array of action objects
 *   { label, icon, onClick, variant?, disabled? }
 *   or { divider: true } for a separator
 * - triggerClassName: Optional override for trigger button classes
 *
 * Features:
 * - Portal-based dropdown for overflow safety
 * - Auto-positioning (flips up/left at viewport edges)
 * - Smooth open/close animation
 * - Full keyboard nav (ArrowUp/Down, Enter, Space, Escape)
 * - ARIA: role="menu", role="menuitem", aria-haspopup, aria-expanded
 * - "danger" variant for destructive actions (renders in red)
 * - Close on outside click, Escape, or scroll
 */

import { useState, useRef, useEffect, useCallback } from "react";
import { createPortal } from "react-dom";
import { MoreVertical } from "lucide-react";

const DropdownActions = ({ actions = [], triggerClassName = "" }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [position, setPosition] = useState({ top: 0, left: 0 });
  const [focusIndex, setFocusIndex] = useState(-1);
  const triggerRef = useRef(null);
  const menuRef = useRef(null);
  const itemRefs = useRef([]);

  // Filter out dividers for keyboard navigation
  const actionableItems = actions.filter((a) => !a.divider && !a.disabled);

  const calculatePosition = useCallback(() => {
    if (!triggerRef.current) return;

    const rect = triggerRef.current.getBoundingClientRect();
    const menuWidth = 192; // w-48 = 12rem = 192px
    const menuHeight = actions.length * 40 + 8; // estimate
    const viewportHeight = window.innerHeight;
    const viewportWidth = window.innerWidth;
    const scrollY = window.scrollY;

    // Flip up if too close to bottom
    const wouldOverflowBottom = rect.bottom + menuHeight + 8 > viewportHeight;
    // Flip left if too close to right
    const wouldOverflowRight = rect.right + menuWidth > viewportWidth;

    setPosition({
      top: wouldOverflowBottom
        ? rect.top - menuHeight - 4
        : rect.bottom + 4,
      left: wouldOverflowRight
        ? rect.right - menuWidth
        : rect.left - menuWidth + rect.width,
    });
  }, [actions.length]);

  const openMenu = useCallback(() => {
    calculatePosition();
    setIsOpen(true);
    setFocusIndex(-1);
  }, [calculatePosition]);

  const closeMenu = useCallback(() => {
    setIsOpen(false);
    setFocusIndex(-1);
    triggerRef.current?.focus();
  }, []);

  // Close on outside click
  useEffect(() => {
    if (!isOpen) return;

    const handleClickOutside = (e) => {
      if (
        menuRef.current &&
        !menuRef.current.contains(e.target) &&
        triggerRef.current &&
        !triggerRef.current.contains(e.target)
      ) {
        closeMenu();
      }
    };

    const handleScroll = () => closeMenu();
    const handleResize = () => closeMenu();

    document.addEventListener("mousedown", handleClickOutside);
    window.addEventListener("scroll", handleScroll, true);
    window.addEventListener("resize", handleResize);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      window.removeEventListener("scroll", handleScroll, true);
      window.removeEventListener("resize", handleResize);
    };
  }, [isOpen, closeMenu]);

  // Focus management for keyboard nav
  useEffect(() => {
    if (focusIndex >= 0 && itemRefs.current[focusIndex]) {
      itemRefs.current[focusIndex].focus();
    }
  }, [focusIndex]);

  const handleTriggerKeyDown = (e) => {
    if (e.key === "Enter" || e.key === " ") {
      e.preventDefault();
      if (isOpen) {
        closeMenu();
      } else {
        openMenu();
        setTimeout(() => setFocusIndex(0), 50);
      }
    } else if (e.key === "ArrowDown") {
      e.preventDefault();
      if (!isOpen) {
        openMenu();
        setTimeout(() => setFocusIndex(0), 50);
      }
    } else if (e.key === "Escape") {
      if (isOpen) {
        e.preventDefault();
        closeMenu();
      }
    }
  };

  const handleMenuKeyDown = (e) => {
    const actionCount = actionableItems.length;

    switch (e.key) {
      case "ArrowDown":
        e.preventDefault();
        setFocusIndex((prev) => (prev + 1) % actionCount);
        break;
      case "ArrowUp":
        e.preventDefault();
        setFocusIndex((prev) => (prev - 1 + actionCount) % actionCount);
        break;
      case "Escape":
        e.preventDefault();
        closeMenu();
        break;
      case "Tab":
        closeMenu();
        break;
      default:
        break;
    }
  };

  const handleItemClick = (action, e) => {
    e.stopPropagation();
    if (action.disabled) return;
    closeMenu();
    action.onClick?.();
  };

  const handleItemKeyDown = (action, e) => {
    if (e.key === "Enter" || e.key === " ") {
      e.preventDefault();
      handleItemClick(action, e);
    }
  };

  if (!actions.length) return null;

  let actionableIndex = -1;

  return (
    <>
      {/* Trigger Button */}
      <button
        ref={triggerRef}
        onClick={(e) => {
          e.stopPropagation();
          isOpen ? closeMenu() : openMenu();
        }}
        onKeyDown={handleTriggerKeyDown}
        className={
          triggerClassName ||
          `p-1.5 rounded-lg text-gray-400 dark:text-gray-500 hover:text-gray-600 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-all duration-200 ${
            isOpen
              ? "bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300"
              : ""
          }`
        }
        aria-haspopup="menu"
        aria-expanded={isOpen}
        aria-label="Row actions"
        title="Actions"
      >
        <MoreVertical size={18} />
      </button>

      {/* Dropdown Menu (Portal) */}
      {isOpen &&
        createPortal(
          <>
            {/* Invisible backdrop */}
            <div
              className="fixed inset-0"
              style={{ zIndex: 9998 }}
              onClick={(e) => {
                e.stopPropagation();
                closeMenu();
              }}
            />

            {/* Menu Panel */}
            <div
              ref={menuRef}
              role="menu"
              aria-orientation="vertical"
              onKeyDown={handleMenuKeyDown}
              className="fixed w-48 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-200 dark:border-gray-700 py-1.5 overflow-hidden animate-dropdown-in"
              style={{
                top: `${position.top}px`,
                left: `${position.left}px`,
                zIndex: 9999,
              }}
            >
              {actions.map((action, idx) => {
                // Divider
                if (action.divider) {
                  return (
                    <div
                      key={`divider-${idx}`}
                      className="my-1.5 border-t border-gray-200 dark:border-gray-700"
                      role="separator"
                    />
                  );
                }

                // Track actionable index for ref assignment
                if (!action.disabled) {
                  actionableIndex++;
                }
                const currentRefIndex = actionableIndex;

                const Icon = action.icon;
                const isDanger = action.variant === "danger";
                const isDisabled = action.disabled;

                return (
                  <button
                    key={action.label || idx}
                    ref={(el) => {
                      if (!isDisabled) {
                        itemRefs.current[currentRefIndex] = el;
                      }
                    }}
                    role="menuitem"
                    tabIndex={-1}
                    disabled={isDisabled}
                    onClick={(e) => handleItemClick(action, e)}
                    onKeyDown={(e) => handleItemKeyDown(action, e)}
                    className={`w-full flex items-center gap-3 px-3.5 py-2.5 text-sm transition-colors duration-150 text-left ${
                      isDisabled
                        ? "text-gray-300 dark:text-gray-600 cursor-not-allowed"
                        : isDanger
                        ? "text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-500/10 focus:bg-red-50 dark:focus:bg-red-500/10"
                        : "text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700/70 focus:bg-gray-50 dark:focus:bg-gray-700/70"
                    } focus:outline-none`}
                  >
                    {Icon && (
                      <Icon
                        size={16}
                        className={`shrink-0 ${
                          isDisabled
                            ? "text-gray-300 dark:text-gray-600"
                            : isDanger
                            ? "text-red-500 dark:text-red-400"
                            : "text-gray-400 dark:text-gray-500"
                        }`}
                      />
                    )}
                    <span className="truncate">{action.label}</span>
                  </button>
                );
              })}
            </div>
          </>,
          document.body
        )}
    </>
  );
};

export default DropdownActions;
