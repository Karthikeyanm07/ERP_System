/**
 * Main Entry Point
 * 
 * Explanation:
 * - BrowserRouter: Wraps the app to enable React Router functionality
 *   - Uses HTML5 History API (pushState, replaceState, popstate)
 *   - Allows navigation without page refresh
 * - StrictMode: Development tool that highlights potential problems
 *   - Double-renders components to detect side effects
 *   - Only active in development, no impact on production
 */

import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './index.css';
import App from './App.jsx';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
);
