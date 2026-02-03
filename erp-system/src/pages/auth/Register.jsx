/**
 * Register Page
 *
 * Explanation:
 * - User registration form for new accounts
 * - Calls authApi.register() directly (not through context)
 * - On success, redirects to login page
 * - Validates password confirmation
 *
 * Backend expects:
 * {
 *   "username": "newuser",
 *   "email": "newuser@erp.com",
 *   "password": "password123"
 * }
 */

import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { authApi } from "../../api/authApi";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import {
  Lock,
  User,
  Mail,
  Package,
  AlertCircle,
  CheckCircle,
  Sun,
  Moon,
} from "lucide-react";
import { useTheme } from "../../context/ThemeContext";

const Register = () => {
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();

  // Form state
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  /**
   * Handle input changes
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
   * Validate form before submission
   */
  const validateForm = () => {
    // Check if passwords match
    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match");
      return false;
    }

    // Check password length
    if (formData.password.length < 6) {
      setError("Password must be at least 6 characters");
      return false;
    }

    // Check email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      setError("Please enter a valid email address");
      return false;
    }

    return true;
  };

  /**
   * Handle form submission
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validate form
    if (!validateForm()) return;

    setLoading(true);
    setError("");

    try {
      // Call register API
      await authApi.register({
        username: formData.username,
        email: formData.email,
        password: formData.password,
      });

      // Success! Show success message and redirect
      setSuccess(true);

      // Wait 2 seconds then redirect to login
      setTimeout(() => {
        navigate("/login");
      }, 2000);
    } catch (err) {
      // Show error message from API or generic message
      setError(
        err.response?.data?.message || "Registration failed. Please try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950 flex items-center justify-center p-4 relative">
      {/* Theme toggle */}
      <button
        type="button"
        onClick={toggleTheme}
        className="absolute top-4 right-4 p-2 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:bg-gray-800 transition-colors"
        title={theme === "dark" ? "Light mode" : "Dark mode"}
      >
        {theme === "dark" ? <Sun size={22} /> : <Moon size={22} />}
      </button>

      {/* Register Card */}
      <div className="w-full max-w-md">
        {/* Logo/Brand */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-600 to-indigo-600 dark:from-blue-500 dark:to-indigo-500 rounded-2xl shadow-lg shadow-blue-600/30 dark:shadow-blue-500/30 mb-4">
            <Package className="text-white" size={32} />
          </div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Create Account
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Join the ERP system
          </p>
        </div>

        {/* Register Form */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl shadow-gray-200/50 dark:shadow-none dark:border dark:border-gray-700 p-8">
          {/* Success Message */}
          {success ? (
            <div className="text-center py-8">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 dark:bg-green-500/20 rounded-full mb-4">
                <CheckCircle
                  className="text-green-600 dark:text-green-400"
                  size={32}
                />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
                Registration Successful!
              </h3>
              <p className="text-gray-500 dark:text-gray-400">
                Redirecting you to login page...
              </p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-5">
              {/* Error Alert */}
              {error && (
                <div className="flex items-center gap-3 p-4 bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-lg text-red-700 dark:text-red-300">
                  <AlertCircle size={20} />
                  <span className="text-sm">{error}</span>
                </div>
              )}

              {/* Username Field */}
              <Input
                label="Username"
                name="username"
                type="text"
                icon={User}
                placeholder="Choose a username"
                value={formData.username}
                onChange={handleChange}
                required
              />

              {/* Email Field */}
              <Input
                label="Email"
                name="email"
                type="email"
                icon={Mail}
                placeholder="Enter your email"
                value={formData.email}
                onChange={handleChange}
                required
              />

              {/* Password Field */}
              <Input
                label="Password"
                name="password"
                type="password"
                icon={Lock}
                placeholder="Create a password"
                value={formData.password}
                onChange={handleChange}
                helperText="Minimum 6 characters"
                required
              />

              {/* Confirm Password Field */}
              <Input
                label="Confirm Password"
                name="confirmPassword"
                type="password"
                icon={Lock}
                placeholder="Confirm your password"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />

              {/* Submit Button */}
              <Button
                type="submit"
                variant="primary"
                size="lg"
                loading={loading}
                className="w-full"
              >
                Create Account
              </Button>
            </form>
          )}

          {/* Login Link */}
          {!success && (
            <div className="mt-6 text-center text-sm text-gray-500 dark:text-gray-400">
              Already have an account?{" "}
              <Link
                to="/login"
                className="text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-medium"
              >
                Sign in
              </Link>
            </div>
          )}
        </div>

        {/* Footer */}
        <p className="text-center text-xs text-gray-400 dark:text-gray-500 mt-8">
          © 2025 ERP System. All rights reserved.
        </p>
      </div>
    </div>
  );
};

export default Register;
