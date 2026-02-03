/**
 * useAuth Hook
 * 
 * Explanation:
 * - Custom hook to access auth context easily
 * - Returns user, login, logout, hasRole functions
 */

import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

export const useAuth = () => {
  const context = useContext(AuthContext);
  
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  
  return context;
};