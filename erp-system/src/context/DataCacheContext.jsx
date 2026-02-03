/**
 * Data Cache Context
 * 
 * Purpose:
 * - Caches frequently accessed data (employees, departments, products, etc.)
 * - Prevents duplicate API calls when switching between tabs
 * - Provides cache invalidation when data is modified
 * 
 * How it works:
 * - Data is cached in memory with a timestamp
 * - Cache is valid for a configurable TTL (Time To Live)
 * - After CRUD operations, relevant cache is invalidated
 */

import React, { createContext, useContext, useState, useCallback, useRef } from 'react';

// Cache configuration
const CACHE_TTL = 5 * 60 * 1000; // 5 minutes in milliseconds

const DataCacheContext = createContext(null);

export const DataCacheProvider = ({ children }) => {
  const [cache, setCache] = useState({});
  const pendingRequests = useRef({}); // Track in-flight requests to prevent duplicates

  /**
   * Get cached data if still valid
   */
  const getCached = useCallback((key) => {
    const entry = cache[key];
    if (!entry) return null;
    
    const isExpired = Date.now() - entry.timestamp > CACHE_TTL;
    if (isExpired) {
      return null;
    }
    
    return entry.data;
  }, [cache]);

  /**
   * Set data in cache
   */
  const setCached = useCallback((key, data) => {
    setCache(prev => ({
      ...prev,
      [key]: {
        data,
        timestamp: Date.now(),
      }
    }));
  }, []);

  /**
   * Invalidate specific cache key or pattern
   * @param keyOrPattern - Exact key or regex pattern to match
   */
  const invalidate = useCallback((keyOrPattern) => {
    setCache(prev => {
      const newCache = { ...prev };
      
      if (typeof keyOrPattern === 'string') {
        // Invalidate exact key and related keys
        Object.keys(newCache).forEach(key => {
          if (key === keyOrPattern || key.startsWith(keyOrPattern)) {
            delete newCache[key];
          }
        });
      } else if (keyOrPattern instanceof RegExp) {
        // Invalidate by pattern
        Object.keys(newCache).forEach(key => {
          if (keyOrPattern.test(key)) {
            delete newCache[key];
          }
        });
      }
      
      return newCache;
    });
  }, []);

  /**
   * Clear all cache
   */
  const clearAll = useCallback(() => {
    setCache({});
    pendingRequests.current = {};
  }, []);

  /**
   * Fetch with caching - prevents duplicate requests and caches results
   * @param key - Cache key
   * @param fetchFn - Async function to fetch data
   * @param forceRefresh - Skip cache and fetch fresh data
   */
  const fetchWithCache = useCallback(async (key, fetchFn, forceRefresh = false) => {
    // Return cached data if valid and not forcing refresh
    if (!forceRefresh) {
      const cached = getCached(key);
      if (cached !== null) {
        return cached;
      }
    }

    // Check if request is already in flight (prevent duplicate calls)
    if (pendingRequests.current[key]) {
      return pendingRequests.current[key];
    }

    // Create the fetch promise
    const fetchPromise = (async () => {
      try {
        const data = await fetchFn();
        setCached(key, data);
        return data;
      } finally {
        // Clear pending request
        delete pendingRequests.current[key];
      }
    })();

    // Store the pending request
    pendingRequests.current[key] = fetchPromise;
    
    return fetchPromise;
  }, [getCached, setCached]);

  /**
   * Cache keys for common data types
   */
  const CACHE_KEYS = {
    // HR
    EMPLOYEES: 'hr:employees',
    DEPARTMENTS: 'hr:departments',
    LEAVE_TYPES: 'hr:leaveTypes',
    
    // Inventory
    PRODUCTS: 'inventory:products',
    SUPPLIERS: 'inventory:suppliers',
    WAREHOUSES: 'inventory:warehouses',
    
    // Sales
    CUSTOMERS: 'sales:customers',
    
    // Finance
    ACCOUNTS: 'finance:accounts',
  };

  return (
    <DataCacheContext.Provider value={{
      getCached,
      setCached,
      invalidate,
      clearAll,
      fetchWithCache,
      CACHE_KEYS,
    }}>
      {children}
    </DataCacheContext.Provider>
  );
};

/**
 * Hook to use data cache
 */
export const useDataCache = () => {
  const context = useContext(DataCacheContext);
  if (!context) {
    throw new Error('useDataCache must be used within a DataCacheProvider');
  }
  return context;
};

export default DataCacheContext;
