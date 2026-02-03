/**
 * Settings Page
 *
 * ERP-wide preferences:
 * - Appearance (theme: light / dark / system)
 * - Notifications (email, in-app)
 * - Security (session timeout placeholder)
 * Persists where possible (localStorage) and uses ThemeContext for theme.
 */

import { useState, useEffect } from "react";
import { useTheme } from "../../context/ThemeContext";
import { authApi } from "../../api/authApi";
import Card from "../../components/common/Card";
import { useToast } from "../../components/common/Toast";
import { Sun, Moon, Monitor, Bell, Mail, Shield, Save } from "lucide-react";

const STORAGE_KEYS = {
  theme: "erp-theme",
  emailNotifications: "erp-settings-email-notifications",
  inAppNotifications: "erp-settings-in-app-notifications",
  sessionTimeout: "erp-settings-session-timeout",
};

const Settings = () => {
  const { theme, setTheme } = useTheme();
  const toast = useToast();
  const [saving, setSaving] = useState(false);

  const [emailNotifications, setEmailNotifications] = useState(true);
  const [inAppNotifications, setInAppNotifications] = useState(true);
  const [sessionTimeout, setSessionTimeout] = useState("30"); // minutes

  useEffect(() => {
    try {
      const email = localStorage.getItem(STORAGE_KEYS.emailNotifications);
      const inApp = localStorage.getItem(STORAGE_KEYS.inAppNotifications);
      const timeout = localStorage.getItem(STORAGE_KEYS.sessionTimeout);
      if (email !== null) setEmailNotifications(email === "true");
      if (inApp !== null) setInAppNotifications(inApp === "true");
      if (timeout) setSessionTimeout(timeout);
    } catch (_) {}
  }, []);

  // Load session timeout from backend (user preference in DB)
  useEffect(() => {
    authApi
      .getMe()
      .then((data) => {
        if (data.sessionTimeoutMinutes != null)
          setSessionTimeout(String(data.sessionTimeoutMinutes));
      })
      .catch(() => {});
  }, []);

  const handleThemeChange = (value) => {
    setTheme(value);
    toast.success(
      `Theme set to ${value === "system" ? "system default" : value}`
    );
  };

  const handleSaveNotifications = () => {
    setSaving(true);
    try {
      localStorage.setItem(
        STORAGE_KEYS.emailNotifications,
        String(emailNotifications)
      );
      localStorage.setItem(
        STORAGE_KEYS.inAppNotifications,
        String(inAppNotifications)
      );
      toast.success("Notification preferences saved");
    } catch (_) {
      toast.error("Failed to save preferences");
    }
    setSaving(false);
  };

  const handleSaveSecurity = async () => {
    setSaving(true);
    try {
      await authApi.updateSessionTimeout(sessionTimeout);
      localStorage.setItem(STORAGE_KEYS.sessionTimeout, sessionTimeout);
      toast.success("Session timeout saved. It will apply on your next login.");
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to save settings");
    }
    setSaving(false);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
          Settings
        </h1>
        <p className="text-gray-500 dark:text-gray-400 mt-1">
          Customize your ERP experience and preferences
        </p>
      </div>

      {/* Appearance */}
      <Card title="Appearance" subtitle="Choose how the app looks">
        <div className="space-y-3">
          <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
            Theme
          </p>
          <div className="flex flex-wrap gap-3">
            {[
              { value: "light", label: "Light", icon: Sun },
              { value: "dark", label: "Dark", icon: Moon },
              { value: "system", label: "System", icon: Monitor },
            ].map(({ value, label, icon: Icon }) => (
              <button
                key={value}
                type="button"
                onClick={() => handleThemeChange(value)}
                className={`flex items-center gap-2 px-4 py-2.5 rounded-lg border-2 transition-colors ${
                  theme === value
                    ? "border-blue-600 dark:border-blue-500 bg-blue-50 dark:bg-blue-500/20 text-blue-700 dark:text-blue-200"
                    : "border-gray-200 dark:border-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800"
                }`}
              >
                <Icon size={18} />
                {label}
              </button>
            ))}
          </div>
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-2">
            System uses your device preference (light or dark).
          </p>
        </div>
      </Card>

      {/* Notifications */}
      <Card title="Notifications" subtitle="How and when you receive updates">
        <div className="space-y-4 max-w-md">
          <label className="flex items-center justify-between gap-4 cursor-pointer">
            <span className="text-sm font-medium text-gray-700 dark:text-gray-300 flex items-center gap-2">
              <Mail size={18} />
              Email notifications
            </span>
            <input
              type="checkbox"
              checked={emailNotifications}
              onChange={(e) => setEmailNotifications(e.target.checked)}
              className="w-4 h-4 rounded border-gray-300 dark:border-gray-600 text-blue-600 focus:ring-blue-500 dark:bg-gray-700"
            />
          </label>
          <p className="text-xs text-gray-500 dark:text-gray-400">
            Receive alerts and summaries by email (e.g. order updates, low
            stock).
          </p>
          <label className="flex items-center justify-between gap-4 cursor-pointer">
            <span className="text-sm font-medium text-gray-700 dark:text-gray-300 flex items-center gap-2">
              <Bell size={18} />
              In-app notifications
            </span>
            <input
              type="checkbox"
              checked={inAppNotifications}
              onChange={(e) => setInAppNotifications(e.target.checked)}
              className="w-4 h-4 rounded border-gray-300 dark:border-gray-600 text-blue-600 focus:ring-blue-500 dark:bg-gray-700"
            />
          </label>
          <p className="text-xs text-gray-500 dark:text-gray-400">
            Show notification bell and toasts inside the application.
          </p>
          <button
            type="button"
            onClick={handleSaveNotifications}
            disabled={saving}
            className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600 rounded-lg disabled:opacity-50"
          >
            <Save size={16} />
            Save notification preferences
          </button>
        </div>
      </Card>

      {/* Security */}
      <Card title="Security" subtitle="Session and security options">
        <div className="space-y-4 max-w-md">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Session timeout (minutes)
            </label>
            <select
              value={sessionTimeout}
              onChange={(e) => setSessionTimeout(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-gray-100 rounded-lg focus:ring-2 focus:ring-blue-500 dark:focus:ring-blue-400"
            >
              <option value="15">15 minutes</option>
              <option value="30">30 minutes</option>
              <option value="60">1 hour</option>
              <option value="120">2 hours</option>
              <option value="480">8 hours</option>
            </select>
            <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
              After this period of inactivity you may be asked to sign in again.
              Saved to your account and applied on next login.
            </p>
          </div>
          <button
            type="button"
            onClick={handleSaveSecurity}
            disabled={saving}
            className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600 rounded-lg disabled:opacity-50"
          >
            <Shield size={16} />
            Save security settings
          </button>
        </div>
      </Card>
    </div>
  );
};

export default Settings;
