/**
 * useApi Hook
 * 
 * Explanation:
 * - Generic hook for API calls
 * - Manages loading, error, and data states
 * - Automatically handles errors
 */

import { useState, useCallback } from 'react';

export const useApi = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const execute = useCallback(async (apiFunction, ...args) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await apiFunction(...args);
      setLoading(false);
      return result;
    } catch (err) {
      setError(err.response?.data?.message || 'An error occurred');
      setLoading(false);
      throw err;
    }
  }, []);

  return { execute, loading, error };
};