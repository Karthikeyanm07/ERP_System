/**
 * SearchBar Component
 *
 * Reusable search input with debounced onChange, clear button, and accessibility.
 *
 * Props:
 * - value: Current search string (controlled)
 * - onChange: Callback receiving the debounced value
 * - placeholder: Input placeholder text
 * - debounceMs: Debounce delay in ms (default 300)
 * - className: Additional wrapper classes
 */

import { useState, useEffect, useRef, useCallback } from "react";
import { Search, X } from "lucide-react";

const SearchBar = ({
  value = "",
  onChange,
  placeholder = "Search...",
  debounceMs = 300,
  className = "",
}) => {
  const [localValue, setLocalValue] = useState(value);
  const inputRef = useRef(null);
  const debounceTimer = useRef(null);

  // Sync external value changes
  useEffect(() => {
    setLocalValue(value);
  }, [value]);

  const debouncedOnChange = useCallback(
    (val) => {
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }
      debounceTimer.current = setTimeout(() => {
        onChange?.(val);
      }, debounceMs);
    },
    [onChange, debounceMs]
  );

  // Cleanup timer on unmount
  useEffect(() => {
    return () => {
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }
    };
  }, []);

  const handleChange = (e) => {
    const val = e.target.value;
    setLocalValue(val);
    debouncedOnChange(val);
  };

  const handleClear = () => {
    setLocalValue("");
    onChange?.("");
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }
    inputRef.current?.focus();
  };

  const handleKeyDown = (e) => {
    if (e.key === "Escape") {
      if (localValue) {
        e.preventDefault();
        handleClear();
      }
    }
  };

  return (
    <div className={`relative group ${className}`}>
      {/* Search Icon */}
      <Search
        className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500 group-focus-within:text-gray-500 
        dark:group-focus-within:text-gray-400 transition-colors duration-200 pointer-events-none"
        size={18}
      />

      {/* Input */}
      <input
        ref={inputRef}
        type="text"
        value={localValue}
        onChange={handleChange}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        aria-label={placeholder}
        className="w-full pl-10 pr-9 py-2.5 bg-white dark:bg-gray-800 border border-gray-300 
        dark:border-gray-600 text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 
        rounded-md focus:outline-none  
        focus:border-gray-500 dark:focus:border-gray-400 transition-all duration-200 text-sm"
      />

      {/* Clear Button */}
      <button
        onClick={handleClear}
        className={`absolute right-2.5 top-1/2 -translate-y-1/2 p-0.5 rounded-md text-gray-400 dark:text-gray-500 
            hover:text-gray-600 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-all duration-200 ${
          localValue
            ? "opacity-100 scale-100"
            : "opacity-0 scale-75 pointer-events-none"
        }`}
        aria-label="Clear search"
        tabIndex={localValue ? 0 : -1}
      >
        <X size={15} />
      </button>
    </div>
  );
};

export default SearchBar;
