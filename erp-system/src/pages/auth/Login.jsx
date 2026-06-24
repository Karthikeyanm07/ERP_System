/**
 * Login Page
 *
 * Explanation:
 * - Main authentication page for user login
 * - Uses AuthContext's login function which:
 *   1. Calls authApi.login() with credentials
 *   2. Stores JWT token and user info in localStorage
 *   3. Updates user state in context
 * - Redirects to dashboard on successful login
 * - Shows error message on failed login
 *
 * Flow:
 * 1. User enters username and password
 * 2. Form submits → calls login() from AuthContext
 * 3. On success → navigate to / (dashboard)
 * 4. On error → show error message
 */

import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import { Lock, User, Package, AlertCircle, Sun, Moon } from "lucide-react";
import { useTheme } from "../../context/ThemeContext";

const Login = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const { theme, toggleTheme } = useTheme();

  // Form state
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });

  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  /**
   * Handle input changes
   * Uses computed property names to update specific field
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error when user starts typing
    if (error) setError("");
  };

  /**
   * Handle form submission
   * 1. Prevent default form submit
   * 2. Call login from AuthContext
   * 3. Navigate on success or show error
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      // Call login from AuthContext
      // This will: call API → store token → update user state
      await login(formData.username, formData.password);

      // Redirect to previous page (e.g. after 401) or dashboard
      let returnTo = "/";
      try {
        const stored = sessionStorage.getItem("erp-return-to");
        if (stored && stored.startsWith("/") && stored !== "/login") {
          returnTo = stored;
          sessionStorage.removeItem("erp-return-to");
        }
      } catch (_) {}
      navigate(returnTo, { replace: true });
    } catch (err) {
      // Show error message from API or generic message
      setError(
        err.response?.data?.message ||
          "Login failed. Please check your credentials."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950 flex items-center justify-center p-4 relative">
      {/* Theme toggle - top right */}
      <button
        type="button"
        onClick={toggleTheme}
        className="absolute top-4 right-4 p-2 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:bg-gray-800 transition-colors"
        title={theme === "dark" ? "Light mode" : "Dark mode"}
      >
        {theme === "dark" ? <Sun size={22} /> : <Moon size={22} />}
      </button>

      {/* Login Card */}
      <div className="w-full max-w-md">
        {/* Logo/Brand */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-600 to-indigo-600 dark:from-blue-500 dark:to-indigo-500 rounded-2xl shadow-lg shadow-blue-600/30 dark:shadow-blue-500/30 mb-4">
            <Package className="text-white" size={32} />
          </div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Welcome Back
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Sign in to your ERP account
          </p>
        </div>

        {/* Login Form */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl shadow-gray-200/50 dark:shadow-none dark:border dark:border-gray-700 p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Error Alert */}
            {error && (
              <div className="flex items-center gap-3 p-4 bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-lg text-red-700 dark:text-red-300">
                <AlertCircle size={20} />
                <span className="text-sm">{error}</span>
              </div>
            )}

            {/* Username or Email Field */}
            <Input
              label="Username or Email"
              name="username"
              type="text"
              icon={User}
              placeholder="Enter your username or email"
              value={formData.username}
              onChange={handleChange}
              required
            />

            {/* Password Field */}
            <Input
              label="Password"
              name="password"
              type="password"
              icon={Lock}
              placeholder="Enter your password"
              value={formData.password}
              onChange={handleChange}
              required
            />

            {/* Remember Me & Forgot Password */}
            <div className="flex items-center justify-between text-sm">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  className="w-4 h-4 rounded border-gray-300 dark:border-gray-600 text-blue-600 focus:ring-blue-500 dark:bg-gray-700"
                />
                <span className="text-gray-600 dark:text-gray-400">
                  Remember me
                </span>
              </label>
              <Link
                to="/forgot-password"
                className="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-medium"
              >
                Forgot password?
              </Link>
            </div>

            {/* Submit Button */}
            <Button
              type="submit"
              variant="primary"
              size="lg"
              loading={loading}
              className="w-full"
            >
              Sign In
            </Button>
          </form>

          {/* Register Link */}
          <div className="mt-6 text-center text-sm text-gray-500 dark:text-gray-400">
            Don't have an account?{" "}
            <Link
              to="/register"
              className="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-medium"
            >
              Create account
            </Link>
          </div>
        </div>

        {/* Footer */}
        <div className="text-center mt-8 space-y-1">
          <p className="text-xs text-gray-400 dark:text-gray-500">
            © {new Date().getFullYear()} ERP System. All rights reserved.
          </p>
          <Link
            to="/privacy-policy"
            className="text-xs text-gray-400 dark:text-gray-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
          >
            Privacy & Policy
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
