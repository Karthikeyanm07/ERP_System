/**
 * Theme Context
 *
 * Provides dark/light theme state across the app.
 * - Persists preference to localStorage
 * - Applies `dark` class to document.documentElement for Tailwind dark mode
 * - Supports system preference on first load
 */

import { createContext, useContext, useEffect, useState } from "react";

const ThemeContext = createContext(null);

const STORAGE_KEY = "erp-theme";

function getInitialTheme() {
  if (typeof window === "undefined") return "light";
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === "dark" || stored === "light" || stored === "system")
    return stored;
  return "system";
}

function getSystemDark() {
  return (
    typeof window !== "undefined" &&
    window.matchMedia?.("(prefers-color-scheme: dark)")?.matches
  );
}

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(getInitialTheme);

  useEffect(() => {
    const root = document.documentElement;
    const applyDark = (isDark) => {
      if (isDark) root.classList.add("dark");
      else root.classList.remove("dark");
    };
    if (theme === "dark") {
      applyDark(true);
    } else if (theme === "light") {
      applyDark(false);
    } else {
      applyDark(getSystemDark());
      localStorage.setItem(STORAGE_KEY, theme);
      const mq = window.matchMedia?.("(prefers-color-scheme: dark)");
      const listener = () => applyDark(getSystemDark());
      mq?.addEventListener?.("change", listener);
      return () => mq?.removeEventListener?.("change", listener);
    }
    localStorage.setItem(STORAGE_KEY, theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === "light" ? "dark" : "light"));
  };

  return (
    <ThemeContext.Provider value={{ theme, setTheme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error("useTheme must be used within ThemeProvider");
  return ctx;
}
