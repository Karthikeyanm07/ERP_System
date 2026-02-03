/**
 * MetricCard
 *
 * Consistent KPI card used across modules.
 * - Light mode: subtle pastel background
 * - Dark mode: soft slate background with accent highlights
 *
 * Props:
 * - title: string (label)
 * - value: string | number
 * - icon: Lucide icon component (optional)
 * - accent: "blue" | "green" | "amber" | "purple" | "cyan" | "rose"
 * - hint: optional small text beneath value
 */

const ACCENTS = {
  blue: {
    value: "text-blue-600 dark:text-blue-200",
    icon: "bg-blue-100 text-blue-600 dark:bg-blue-500/25 dark:text-blue-200",
  },
  green: {
    value: "text-green-600 dark:text-green-200",
    icon: "bg-green-100 text-green-600 dark:bg-green-500/25 dark:text-green-200",
  },
  amber: {
    value: "text-amber-600 dark:text-amber-200",
    icon: "bg-amber-100 text-amber-600 dark:bg-amber-500/25 dark:text-amber-200",
  },
  purple: {
    value: "text-purple-600 dark:text-purple-200",
    icon: "bg-purple-100 text-purple-600 dark:bg-purple-500/25 dark:text-purple-200",
  },
  cyan: {
    value: "text-cyan-600 dark:text-cyan-200",
    icon: "bg-cyan-100 text-cyan-600 dark:bg-cyan-500/25 dark:text-cyan-200",
  },
  rose: {
    value: "text-rose-600 dark:text-rose-200",
    icon: "bg-rose-100 text-rose-600 dark:bg-rose-500/25 dark:text-rose-200",
  },
};

const MetricCard = ({
  title,
  value,
  icon: Icon,
  accent = "blue",
  hint,
  className = "",
}) => {
  const palette = ACCENTS[accent] ?? ACCENTS.blue;

  return (
    <div
      className={`rounded-2xl border border-gray-200 dark:border-slate-700 bg-white/90 dark:bg-slate-900/75 backdrop-blur shadow-lg shadow-slate-900/5 dark:shadow-black/30 p-6 transition-colors ${className}`}
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">
            {title}
          </p>
          <p
            className={`mt-2 text-3xl font-semibold leading-none ${palette.value}`}
          >
            {value}
          </p>
          {hint && (
            <p className="mt-2 text-xs text-gray-400 dark:text-gray-500">
              {hint}
            </p>
          )}
        </div>
        {Icon && (
          <div
            className={`p-3 rounded-xl ring-4 ring-transparent dark:ring-white/5 ${palette.icon}`}
          >
            <Icon size={22} />
          </div>
        )}
      </div>
    </div>
  );
};

export default MetricCard;
