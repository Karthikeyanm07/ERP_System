/**
 * Loader Component
 * 
 * Explanation:
 * - Spinning loader for async operations
 * - Multiple sizes for different contexts
 * - Optional text label
 * - Can be used as full-page or inline loader
 * 
 * Usage:
 * - Page loading: <Loader fullScreen />
 * - Button loading: <Loader size="sm" />
 * - Section loading: <Loader text="Loading data..." />
 */

const Loader = ({
  size = 'md',
  text,
  fullScreen = false,
  className = '',
}) => {
  // Size variants for the spinner
  const sizes = {
    sm: 'w-4 h-4 border-2',
    md: 'w-8 h-8 border-3',
    lg: 'w-12 h-12 border-4',
    xl: 'w-16 h-16 border-4',
  };

  const spinner = (
    <div className={`flex flex-col items-center justify-center ${className}`}>
      {/* Spinning circle */}
      <div
        className={`
          ${sizes[size]}
          border-blue-200
          border-t-blue-600
          rounded-full
          animate-spin
        `}
      />
      
      {/* Optional loading text */}
      {text && (
        <p className="mt-3 text-sm text-gray-500">{text}</p>
      )}
    </div>
  );

  // Full screen overlay loader
  if (fullScreen) {
    return (
      <div className="fixed inset-0 bg-white/80 backdrop-blur-sm flex items-center justify-center z-50">
        {spinner}
      </div>
    );
  }

  return spinner;
};

export default Loader;
