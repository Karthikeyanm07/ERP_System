/**
 * Authentication API
 *
 * - login, register, logout (public)
 * - getMe, updateProfile, changePassword, updateSessionTimeout (require auth, cookie sent automatically)
 */

import axios from "./axiosConfig";
import { logger } from "../utils/logger";

export const authApi = {
  login: async (username, password) => {
    const response = await axios.post("/auth/login", { username, password });
    return response.data;
  },

  register: async (userData) => {
    const response = await axios.post("/auth/register", userData);
    return response.data;
  },

  logout: async () => {
    try {
      await axios.post("/auth/logout");
    } catch (error) {
      logger.error("Logout error", error);
    }
  },

  /** GET /api/auth/me - current user profile (id, username, email, roles, sessionTimeoutMinutes) */
  getMe: async () => {
    const response = await axios.get("/auth/me");
    return response.data;
  },

  /** PATCH /api/auth/profile - update username and email */
  updateProfile: async (username, email) => {
    const response = await axios.patch("/auth/profile", { username, email });
    return response.data;
  },

  /** POST /api/auth/change-password */
  changePassword: async (currentPassword, newPassword) => {
    const response = await axios.post("/auth/change-password", {
      currentPassword,
      newPassword,
    });
    return response.data;
  },

  /** PATCH /api/auth/settings/session-timeout - save session timeout to DB */
  updateSessionTimeout: async (sessionTimeoutMinutes) => {
    const response = await axios.patch("/auth/settings/session-timeout", {
      sessionTimeoutMinutes: Number(sessionTimeoutMinutes),
    });
    return response.data;
  },
};
