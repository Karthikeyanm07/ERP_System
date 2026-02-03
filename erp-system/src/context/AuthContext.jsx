/**
 * Authentication Context
 *
 * Explanation:
 * - Provides authentication state across the app
 * - Stores current user info (not token - token is in HttpOnly cookie)
 * - Provides login/logout functions
 * - Persists user info in localStorage (token is in secure cookie)
 *
 * Security:
 * - JWT token stored in HttpOnly cookie (cannot be accessed by JavaScript)
 * - Only user metadata stored in localStorage (not sensitive)
 */

import React, { createContext, useState, useEffect } from "react";
import { authApi } from "../api/authApi";

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Load user from localStorage, then re-validate with backend (prevents tampered localStorage)
  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (_) {
        localStorage.removeItem("user");
      }
    }

    const init = async () => {
      const stored = localStorage.getItem("user");
      if (!stored) {
        setLoading(false);
        return;
      }
      try {
        const data = await authApi.getMe();
        const userData = {
          id: data.id,
          username: data.username,
          email: data.email,
          roles: data.roles,
        };
        setUser(userData);
        localStorage.setItem("user", JSON.stringify(userData));
      } catch (_) {
        setUser(null);
        localStorage.removeItem("user");
      } finally {
        setLoading(false);
      }
    };
    init();
  }, []);

  const login = async (username, password) => {
    const data = await authApi.login(username, password);

    // Extract user data from response
    // Note: Token is NOT in response - it's set as HttpOnly cookie by server
    const userData = {
      id: data.id,
      username: data.username,
      email: data.email,
      roles: data.roles,
    };

    // Store user info (not token) in localStorage
    localStorage.setItem("user", JSON.stringify(userData));
    setUser(userData);

    return data;
  };

  const logout = async () => {
    // Call server to clear HttpOnly cookie
    await authApi.logout();

    // Clear local user data
    localStorage.removeItem("user");
    setUser(null);
  };

  /**
   * Update local user data (e.g. after profile edit).
   * Call this after saving profile so navbar and other components reflect changes.
   */
  const updateUser = (updates) => {
    if (!user) return;
    const updated = { ...user, ...updates };
    setUser(updated);
    localStorage.setItem("user", JSON.stringify(updated));
  };

  const hasRole = (role) => {
    return user?.roles?.includes(role) || false;
  };

  /**
   * Check if user has any of the specified roles
   */
  const hasAnyRole = (roles) => {
    return roles.some((role) => user?.roles?.includes(role));
  };

  return (
    <AuthContext.Provider
      value={{ user, login, logout, updateUser, hasRole, hasAnyRole, loading }}
    >
      {children}
    </AuthContext.Provider>
  );
};
