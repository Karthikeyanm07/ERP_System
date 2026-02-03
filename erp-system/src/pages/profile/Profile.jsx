/**
 * Profile Page
 *
 * Displays current user information and allows:
 * - View/edit profile (username, email)
 * - Change password
 * Uses AuthContext for user data; updates can sync to backend when API is available.
 */

import { useState, useEffect } from "react";
import { useAuth } from "../../hooks/useAuth";
import { authApi } from "../../api/authApi";
import Card from "../../components/common/Card";
import Button from "../../components/common/Button";
import Input from "../../components/common/Input";
import { useToast } from "../../components/common/Toast";
import { Mail, Shield, KeyRound } from "lucide-react";

const Profile = () => {
  const { user, updateUser } = useAuth();
  const toast = useToast();
  const [profileSaving, setProfileSaving] = useState(false);
  const [passwordSaving, setPasswordSaving] = useState(false);

  // Editable profile (synced from user)
  const [profile, setProfile] = useState({
    username: user?.username ?? "",
    email: user?.email ?? "",
  });
  useEffect(() => {
    if (user) {
      setProfile({ username: user.username ?? "", email: user.email ?? "" });
    }
  }, [user?.username, user?.email]);

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [profileErrors, setProfileErrors] = useState({});
  const [passwordErrors, setPasswordErrors] = useState({});

  const handleProfileChange = (e) => {
    const { name, value } = e.target;
    setProfile((prev) => ({ ...prev, [name]: value }));
    if (profileErrors[name])
      setProfileErrors((prev) => ({ ...prev, [name]: "" }));
  };

  const validateProfile = () => {
    const err = {};
    if (!profile.username?.trim()) err.username = "Username is required";
    const email = profile.email?.trim() || user?.email || "";
    if (!email) err.email = "Email is required";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email))
      err.email = "Enter a valid email";
    setProfileErrors(err);
    return Object.keys(err).length === 0;
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    if (!validateProfile()) return;
    setProfileSaving(true);
    setProfileErrors({});
    try {
      const email = profile.email?.trim() || user?.email || "";
      const data = await authApi.updateProfile(profile.username.trim(), email);
      updateUser({ username: data.username, email: data.email });
      toast.success("Profile updated successfully");
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to update profile");
    } finally {
      setProfileSaving(false);
    }
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordForm((prev) => ({ ...prev, [name]: value }));
    if (passwordErrors[name])
      setPasswordErrors((prev) => ({ ...prev, [name]: "" }));
  };

  const validatePassword = () => {
    const err = {};
    if (!passwordForm.currentPassword)
      err.currentPassword = "Current password is required";
    if (!passwordForm.newPassword) err.newPassword = "New password is required";
    else if (passwordForm.newPassword.length < 6)
      err.newPassword = "At least 6 characters";
    if (passwordForm.newPassword !== passwordForm.confirmPassword)
      err.confirmPassword = "Passwords do not match";
    setPasswordErrors(err);
    return Object.keys(err).length === 0;
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (!validatePassword()) return;
    setPasswordSaving(true);
    setPasswordErrors({});
    try {
      await authApi.changePassword(
        passwordForm.currentPassword,
        passwordForm.newPassword
      );
      toast.success("Password changed successfully");
      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to change password");
    } finally {
      setPasswordSaving(false);
    }
  };

  const primaryRole = user?.roles?.[0]?.replace("ROLE_", "") ?? "User";

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
          Profile
        </h1>
        <p className="text-gray-500 dark:text-gray-400 mt-1">
          Manage your account information and security
        </p>
      </div>

      {/* Profile overview card */}
      <Card title="Account overview" subtitle="Your basic information">
        <div className="flex flex-col sm:flex-row items-start gap-6">
          <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center flex-shrink-0">
            <span className="text-3xl font-bold text-white">
              {user?.username?.charAt(0).toUpperCase() || "U"}
            </span>
          </div>
          <div className="space-y-1">
            <p className="text-lg font-semibold text-gray-900 dark:text-gray-100">
              {user?.username || "—"}
            </p>
            <p className="text-sm text-gray-500 dark:text-gray-400 flex items-center gap-2">
              <Mail size={14} />
              {user?.email || "No email set"}
            </p>
            <p className="text-sm text-gray-500 dark:text-gray-400 flex items-center gap-2">
              <Shield size={14} />
              Role: {primaryRole}
            </p>
          </div>
        </div>
      </Card>

      {/* Edit profile form */}
      <Card title="Edit profile" subtitle="Update your display name and email">
        <form onSubmit={handleSaveProfile} className="space-y-4 max-w-md">
          <Input
            label="Username"
            name="username"
            value={profile.username}
            onChange={handleProfileChange}
            error={profileErrors.username}
            required
          />
          <Input
            label="Email"
            name="email"
            type="email"
            value={profile.email}
            onChange={handleProfileChange}
            error={profileErrors.email}
            required
          />
          <Button type="submit" loading={profileSaving}>
            Save changes
          </Button>
        </form>
      </Card>

      {/* Change password */}
      <Card
        title="Change password"
        subtitle="Choose a strong password to keep your account secure"
      >
        <form onSubmit={handleChangePassword} className="space-y-4 max-w-md">
          <Input
            label="Current password"
            name="currentPassword"
            type="password"
            value={passwordForm.currentPassword}
            onChange={handlePasswordChange}
            error={passwordErrors.currentPassword}
            required
          />
          <Input
            label="New password"
            name="newPassword"
            type="password"
            value={passwordForm.newPassword}
            onChange={handlePasswordChange}
            error={passwordErrors.newPassword}
            helperText="Minimum 6 characters"
            required
          />
          <Input
            label="Confirm new password"
            name="confirmPassword"
            type="password"
            value={passwordForm.confirmPassword}
            onChange={handlePasswordChange}
            error={passwordErrors.confirmPassword}
            required
          />
          <Button type="submit" loading={passwordSaving} variant="secondary">
            <KeyRound size={18} className="mr-2" />
            Update password
          </Button>
        </form>
      </Card>
    </div>
  );
};

export default Profile;
