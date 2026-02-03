/**
 * StatsCard Component
 *
 * Explanation:
 * - Card displaying a single metric with icon and trend
 * - Used on dashboard for key statistics
 * - Shows value, label, and optional change percentage
 *
 * Props:
 * - title: Metric name (e.g., "Total Employees")
 * - value: Current value (e.g., "145")
 * - icon: Lucide icon component
 * - trend: Change indicator { value: "+12%", isPositive: true }
 * - color: Theme color (blue, green, purple, orange)
 */

const StatsCard = ({
  title,
  value,
  icon: Icon,
  trend,
  color = "blue",
  className = "",
}) => {
  // Color variants for icon background (light and dark)
  const colors = {
    blue: "bg-blue-100 text-blue-600 dark:bg-blue-500/20 dark:text-blue-400",
    green:
      "bg-green-100 text-green-600 dark:bg-green-500/20 dark:text-green-400",
    purple:
      "bg-purple-100 text-purple-600 dark:bg-purple-500/20 dark:text-purple-400",
    orange:
      "bg-orange-100 text-orange-600 dark:bg-orange-500/20 dark:text-orange-400",
    red: "bg-red-100 text-red-600 dark:bg-red-500/20 dark:text-red-400",
    cyan: "bg-cyan-100 text-cyan-600 dark:bg-cyan-500/20 dark:text-cyan-400",
  };

  return (
    <div
      className={`bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 p-6 ${className}`}
    >
      <div className="flex items-start justify-between">
        {/* Left side - Title and Value */}
        <div>
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">
            {title}
          </p>
          <p className="text-3xl font-bold text-gray-900 dark:text-gray-100 mt-2">
            {value}
          </p>

          {/* Trend indicator */}
          {trend && (
            <div className="flex items-center gap-1 mt-2">
              <span
                className={`text-sm font-medium ${
                  trend.isPositive
                    ? "text-green-600 dark:text-green-400"
                    : "text-red-600 dark:text-red-400"
                }`}
              >
                {trend.isPositive ? "↑" : "↓"} {trend.value}
              </span>
              <span className="text-xs text-gray-400 dark:text-gray-500">
                vs last month
              </span>
            </div>
          )}
        </div>

        {/* Right side - Icon */}
        {Icon && (
          <div className={`p-3 rounded-lg ${colors[color]}`}>
            <Icon size={24} />
          </div>
        )}
      </div>
    </div>
  );
};

export default StatsCard;
