/**
 * Card Component
 *
 * Explanation:
 * - Container component for grouping content
 * - Optional header with title and actions
 * - Optional footer for additional actions
 * - Clean, modern design with subtle shadow
 *
 * Usage Examples:
 * - Stats cards on dashboard
 * - Form containers
 * - List containers
 */

const Card = ({
  children,
  title,
  subtitle,
  headerActions,
  footer,
  className = "",
  padding = true,
}) => {
  return (
    <div
      className={`bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 ${className}`}
    >
      {/* Header (optional) */}
      {(title || headerActions) && (
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
          <div>
            {title && (
              <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
                {title}
              </h3>
            )}
            {subtitle && (
              <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
                {subtitle}
              </p>
            )}
          </div>
          {headerActions && (
            <div className="flex items-center gap-2">{headerActions}</div>
          )}
        </div>
      )}

      {/* Body */}
      <div className={padding ? "p-6" : ""}>{children}</div>

      {/* Footer (optional) */}
      {footer && (
        <div className="px-6 py-4 border-t border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700/50 rounded-b-xl">
          {footer}
        </div>
      )}
    </div>
  );
};

export default Card;
