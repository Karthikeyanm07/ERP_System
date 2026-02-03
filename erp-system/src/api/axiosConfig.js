/**
 * Axios Configuration
 *
 * Explanation:
 * - Creates axios instance with base URL
 * - Uses withCredentials for HttpOnly cookie authentication
 * - JWT token is sent automatically via cookie (XSS-safe)
 * - Handles token expiration (401 errors)
 * - Redirects to login on authentication failure
 *
 * Security:
 * - No localStorage token storage (XSS protection)
 * - Cookies sent automatically with credentials
 */

import axios from "axios";

// Use env in production (e.g. VITE_API_URL=https://api.yourerp.com/api); fallback for dev
const API_BASE_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // Required for HttpOnly cookie authentication
});

// Request interceptor - no longer needs to add Authorization header
// JWT is now sent automatically via HttpOnly cookie
axiosInstance.interceptors.request.use(
  (config) => {
    // Cookie is sent automatically when withCredentials is true
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handles errors globally
axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid - redirect to login; preserve return URL for post-login redirect
      try {
        const returnTo = window.location.pathname + window.location.search;
        if (returnTo && returnTo !== "/" && returnTo !== "/login")
          sessionStorage.setItem("erp-return-to", returnTo);
      } catch (_) {}
      window.location.href = "/login";
    } else if (error.response?.status === 403) {
      // Access denied - user doesn't have permission
      // Enhance error message for frontend
      const enhancedError = {
        ...error,
        response: {
          ...error.response,
          data: {
            ...error.response?.data,
            message:
              error.response?.data?.message ||
              "Access Denied: You don't have permission to perform this action.",
          },
        },
      };
      return Promise.reject(enhancedError);
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
