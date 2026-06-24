/**
 * Reset Password Page
 *
 * User lands here from the email link with ?token=...
 * Validates the token, then lets them set a new password.
 */

import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { authApi } from "../../api/authApi";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import {
  Lock,
  Package,
  AlertCircle,
  CheckCircle,
  Sun,
  Moon,
  ArrowLeft,
} from "lucide-react";
import { useTheme } from "../../context/ThemeContext";

const ResetPassword = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") || "";
  const { theme, toggleTheme } = useTheme();

  const [validating, setValidating] = useState(true);
  const [tokenValid, setTokenValid] = useState(false);
  const [tokenError, setTokenError] = useState("");
  const [formData, setFormData] = useState({
    newPassword: "",
    confirmPassword: "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const validate = async () => {
      if (!token) {
        setTokenError("Invalid or missing reset link.");
        setValidating(false);
        return;
      }

      try {
        await authApi.validateResetToken(token);
        setTokenValid(true);
      } catch (err) {
        setTokenError(
          err.response?.data?.message ||
            "This reset link is invalid or has expired."
        );
      } finally {
        setValidating(false);
      }
    };

    validate();
  }, [token]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (error) setError("");
  };

  const validateForm = () => {
    if (formData.newPassword.length < 6) {
      setError("Password must be at least 6 characters");
      return false;
    }
    if (formData.newPassword !== formData.confirmPassword) {
      setError("Passwords do not match");
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    setError("");

    try {
      await authApi.resetPassword(token, formData.newPassword);
      setSuccess(true);
      setTimeout(() => navigate("/login"), 2500);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Unable to reset password. Please request a new link."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 dark:from-gray-950 dark:via-gray-900 dark:to-gray-950 flex items-center justify-center p-4 relative">
      <button
        type="button"
        onClick={toggleTheme}
        className="absolute top-4 right-4 p-2 rounded-lg text-gray-500 hover:text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:text-gray-200 dark:hover:bg-gray-800 transition-colors"
        title={theme === "dark" ? "Light mode" : "Dark mode"}
      >
        {theme === "dark" ? <Sun size={22} /> : <Moon size={22} />}
      </button>

      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-600 to-indigo-600 dark:from-blue-500 dark:to-indigo-500 rounded-2xl shadow-lg shadow-blue-600/30 dark:shadow-blue-500/30 mb-4">
            <Package className="text-white" size={32} />
          </div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Reset Password
          </h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">
            Choose a new password for your account
          </p>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl shadow-gray-200/50 dark:shadow-none dark:border dark:border-gray-700 p-8">
          {validating ? (
            <div className="flex justify-center py-12">
              <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600 dark:border-blue-400" />
            </div>
          ) : !tokenValid ? (
            <div className="text-center py-4">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-red-100 dark:bg-red-900/30 rounded-full mb-4">
                <AlertCircle
                  className="text-red-600 dark:text-red-400"
                  size={32}
                />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
                Link expired or invalid
              </h3>
              <p className="text-gray-500 dark:text-gray-400 text-sm mb-6">
                {tokenError}
              </p>
              <Link
                to="/forgot-password"
                className="inline-flex items-center gap-2 text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 font-medium text-sm"
              >
                Request a new reset link
              </Link>
            </div>
          ) : success ? (
            <div className="text-center py-4">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 dark:bg-green-500/20 rounded-full mb-4">
                <CheckCircle
                  className="text-green-600 dark:text-green-400"
                  size={32}
                />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
                Password updated
              </h3>
              <p className="text-gray-500 dark:text-gray-400 text-sm">
                Redirecting you to sign in...
              </p>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-5">
              {error && (
                <div className="flex items-center gap-3 p-4 bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800 rounded-lg text-red-700 dark:text-red-300">
                  <AlertCircle size={20} />
                  <span className="text-sm">{error}</span>
                </div>
              )}

              <Input
                label="New password"
                name="newPassword"
                type="password"
                icon={Lock}
                placeholder="Enter new password"
                value={formData.newPassword}
                onChange={handleChange}
                helperText="Minimum 6 characters"
                required
              />

              <Input
                label="Confirm new password"
                name="confirmPassword"
                type="password"
                icon={Lock}
                placeholder="Confirm new password"
                value={formData.confirmPassword}
                onChange={handleChange}
                required
              />

              <Button
                type="submit"
                variant="primary"
                size="lg"
                loading={loading}
                className="w-full"
              >
                Reset password
              </Button>

              <div className="text-center">
                <Link
                  to="/login"
                  className="inline-flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400 hover:text-blue-600 dark:hover:text-blue-400"
                >
                  <ArrowLeft size={16} />
                  Back to sign in
                </Link>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResetPassword;
