import axiosInstance from "./axiosConfig";
import { logger } from "../utils/logger";

/**
 * Audit Log API Service
 */
export const auditApi = {
  /**
   * Get recent audit logs (Admin only)
   */
  getRecentLogs: async () => {
    try {
      const response = await axiosInstance.get("/audit/recent");
      return response.data;
    } catch (error) {
      logger.error("Error fetching recent audit logs:", error);
      throw error;
    }
  },

  /**
   * Get logs for a specific entity
   */
  getLogsByEntity: async (entityName, entityId) => {
    try {
      const response = await axiosInstance.get(`/audit/entity/${entityName}/${entityId}`);
      return response.data;
    } catch (error) {
      logger.error(`Error fetching logs for ${entityName} ${entityId}:`, error);
      throw error;
    }
  },
};
