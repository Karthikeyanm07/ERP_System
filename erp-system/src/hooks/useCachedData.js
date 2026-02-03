/**
 * useCachedData Hook
 * 
 * Purpose:
 * - Simplifies fetching and caching data in components
 * - Prevents duplicate API calls on tab switches
 * - Automatically uses cache if data is fresh
 * 
 * Usage:
 * const { data, loading, refetch } = useCachedData('hr:employees', hrApi.getEmployees);
 */

import { useState, useEffect, useCallback, useRef } from 'react';
import { useDataCache } from '../context/DataCacheContext';

export const useCachedData = (cacheKey, fetchFn, options = {}) => {
  const { fetchWithCache, invalidate } = useDataCache();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const mountedRef = useRef(true);

  const {
    autoFetch = true,
    dependencies = [],
    initialData = null,
  } = options;

  /**
   * Fetch data with caching
   */
  const fetch = useCallback(async (forceRefresh = false) => {
    if (!cacheKey || !fetchFn) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const result = await fetchWithCache(cacheKey, fetchFn, forceRefresh);
      if (mountedRef.current) {
        setData(result);
      }
    } catch (err) {
      if (mountedRef.current) {
        setError(err);
      }
    } finally {
      if (mountedRef.current) {
        setLoading(false);
      }
    }
  }, [cacheKey, fetchFn, fetchWithCache]);

  /**
   * Force refresh - bypasses cache
   */
  const refetch = useCallback(() => {
    return fetch(true);
  }, [fetch]);

  /**
   * Invalidate cache and refetch
   */
  const invalidateAndRefetch = useCallback(() => {
    invalidate(cacheKey);
    return fetch(true);
  }, [invalidate, cacheKey, fetch]);

  // Initial fetch on mount
  useEffect(() => {
    mountedRef.current = true;
    
    if (autoFetch) {
      fetch(false);
    } else if (initialData !== null) {
      setData(initialData);
      setLoading(false);
    }

    return () => {
      mountedRef.current = false;
    };
  }, [cacheKey, ...dependencies]); // eslint-disable-line react-hooks/exhaustive-deps

  return {
    data,
    loading,
    error,
    refetch,
    invalidateAndRefetch,
    setData, // For optimistic updates
  };
};

export default useCachedData;
