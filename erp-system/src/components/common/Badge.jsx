/**
 * Badge Component
 *
 * Explanation:
 * - Small status indicator for tags, labels, statuses
 * - Color variants for different meanings
 * - Optional dot indicator for emphasis
 *
 * Use Cases:
 * - Order status: Pending (yellow), Confirmed (green), Cancelled (red)
 * - User roles: Admin (purple), User (gray)
 * - Stock levels: In Stock (green), Low Stock (yellow), Out of Stock (red)
 */

const Badge = ({
  children,
  variant = "default",
  size = "md",
  dot = false,
  className = "",
}) => {
  // Color variants (with dark mode)
  const variants = {
    default: "bg-gray-100 text-gray-700 dark:bg-gray-600 dark:text-gray-200",
    primary: "bg-blue-100 text-blue-700 dark:bg-blue-500/30 dark:text-blue-200",
    success:
      "bg-green-100 text-green-700 dark:bg-green-500/30 dark:text-green-200",
    warning:
      "bg-yellow-100 text-yellow-700 dark:bg-yellow-500/30 dark:text-yellow-200",
    danger: "bg-red-100 text-red-700 dark:bg-red-500/30 dark:text-red-200",
    info: "bg-cyan-100 text-cyan-700 dark:bg-cyan-500/30 dark:text-cyan-200",
    purple:
      "bg-purple-100 text-purple-700 dark:bg-purple-500/30 dark:text-purple-200",
  };

  // Size variants
  const sizes = {
    sm: "px-2 py-0.5 text-xs",
    md: "px-2.5 py-1 text-xs",
    lg: "px-3 py-1 text-sm",
  };

  // Dot color matching variants
  const dotColors = {
    default: "bg-gray-500",
    primary: "bg-blue-500",
    success: "bg-green-500",
    warning: "bg-yellow-500",
    danger: "bg-red-500",
    info: "bg-cyan-500",
    purple: "bg-purple-500",
  };

  return (
    <span
      className={`
        inline-flex items-center font-medium rounded-full
        ${variants[variant]}
        ${sizes[size]}
        ${className}
      `}
    >
      {/* Optional dot indicator */}
      {dot && (
        <span
          className={`w-1.5 h-1.5 rounded-full mr-1.5 ${dotColors[variant]}`}
        />
      )}
      {children}
    </span>
  );
};

export default Badge;
