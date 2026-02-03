/**
 * Input Component
 *
 * Explanation:
 * - Form input with label, error state, and helper text
 * - Supports various input types: text, email, password, number, date, etc.
 * - Accessible with proper label association via id
 *
 * Features:
 * - Error state with red border and error message
 * - Disabled state with muted colors
 * - Optional icon prefix
 * - Full-width by default for form layouts
 */

import { forwardRef } from "react";

const Input = forwardRef(
  (
    {
      label,
      type = "text",
      error,
      helperText,
      icon: Icon,
      className = "",
      disabled = false,
      required = false,
      id,
      ...props
    },
    ref
  ) => {
    // Generate unique id if not provided
    const inputId = id || `input-${label?.toLowerCase().replace(/\s+/g, "-")}`;

    return (
      <div className={`w-full ${className}`}>
        {/* Label */}
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1"
          >
            {label}
            {required && (
              <span className="text-red-500 dark:text-red-400 ml-1">*</span>
            )}
          </label>
        )}

        {/* Input wrapper with optional icon */}
        <div className="relative">
          {/* Icon prefix */}
          {Icon && (
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Icon className="h-5 w-5 text-gray-400 dark:text-gray-500" />
            </div>
          )}

          {/* Input field */}
          <input
            ref={ref}
            id={inputId}
            type={type}
            disabled={disabled}
            className={`
            block w-full rounded-lg border bg-white dark:bg-gray-800 transition-colors duration-200
            focus:outline-none focus:ring-2 focus:ring-offset-0 dark:focus:ring-offset-gray-800
            disabled:bg-gray-100 dark:disabled:bg-gray-700 disabled:cursor-not-allowed
            ${Icon ? "pl-10" : "pl-4"} pr-4 py-2.5
            ${
              error
                ? "border-red-300 dark:border-red-500 text-red-900 dark:text-red-200 placeholder-red-300 focus:border-red-500 focus:ring-red-500"
                : "border-gray-300 dark:border-gray-600 text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:border-blue-500 focus:ring-blue-500 dark:focus:border-blue-400 dark:focus:ring-blue-400"
            }
          `}
            {...props}
          />
        </div>

        {/* Error message or helper text */}
        {(error || helperText) && (
          <p
            className={`mt-1 text-sm ${
              error
                ? "text-red-600 dark:text-red-400"
                : "text-gray-500 dark:text-gray-400"
            }`}
          >
            {error || helperText}
          </p>
        )}
      </div>
    );
  }
);

// Display name for React DevTools
Input.displayName = "Input";

export default Input;
