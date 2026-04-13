import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { authAPI } from '../../api/auth';
import Header from '../../components/layout/Header';
import './Settings.css';

export default function SettingsPage() {
  const { user, updateUser } = useAuth();
  const [profileForm, setProfileForm] = useState({
    username: user?.username || '',
    email: user?.email || '',
  });
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [profileMsg, setProfileMsg] = useState({ type: '', text: '' });
  const [passwordMsg, setPasswordMsg] = useState({ type: '', text: '' });
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setSavingProfile(true);
    setProfileMsg({ type: '', text: '' });
    try {
      const res = await authAPI.updateProfile(profileForm);
      const updated = res.data.data;
      updateUser({ ...user, username: updated.username, email: updated.email });
      setProfileMsg({ type: 'success', text: 'Profile updated successfully' });
    } catch (err) {
      setProfileMsg({ type: 'error', text: err.response?.data?.message || 'Failed to update profile' });
    } finally {
      setSavingProfile(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setPasswordMsg({ type: '', text: '' });

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setPasswordMsg({ type: 'error', text: 'New passwords do not match' });
      return;
    }

    if (passwordForm.newPassword.length < 6) {
      setPasswordMsg({ type: 'error', text: 'Password must be at least 6 characters' });
      return;
    }

    setSavingPassword(true);
    try {
      await authAPI.changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      });
      setPasswordForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
      setPasswordMsg({ type: 'success', text: 'Password changed successfully' });
    } catch (err) {
      setPasswordMsg({ type: 'error', text: err.response?.data?.message || 'Failed to change password' });
    } finally {
      setSavingPassword(false);
    }
  };

  return (
    <div className="settings-page">
      <Header title="Settings" subtitle="Manage your account" />

      <div className="settings-grid">
        <div className="settings-card">
          <div className="settings-card-header">
            <div className="settings-card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <div>
              <h3>Profile</h3>
              <p>Update your personal information</p>
            </div>
          </div>

          {profileMsg.text && (
            <div className={`settings-msg ${profileMsg.type}`}>
              {profileMsg.type === 'success' ? (
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              ) : (
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12"/>
                  <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
              )}
              {profileMsg.text}
            </div>
          )}

          <form onSubmit={handleProfileSubmit} className="settings-form">
            <div className="settings-form-group">
              <label>Username</label>
              <input
                type="text"
                value={profileForm.username}
                onChange={(e) => setProfileForm({ ...profileForm, username: e.target.value })}
                required
              />
            </div>
            <div className="settings-form-group">
              <label>Email</label>
              <input
                type="email"
                value={profileForm.email}
                onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
                required
              />
            </div>
            <div className="settings-form-group">
              <label>Role</label>
              <div className="settings-role-display">
                <span className={`settings-role-badge role-${user?.role?.toLowerCase()}`}>{user?.role}</span>
                <span className="settings-role-hint">Contact admin to change your role</span>
              </div>
            </div>
            <div className="settings-form-actions">
              <button type="submit" className="settings-save-btn" disabled={savingProfile}>
                {savingProfile ? 'Saving...' : 'Save changes'}
              </button>
            </div>
          </form>
        </div>

        <div className="settings-card">
          <div className="settings-card-header">
            <div className="settings-card-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
            </div>
            <div>
              <h3>Password</h3>
              <p>Change your account password</p>
            </div>
          </div>

          {passwordMsg.text && (
            <div className={`settings-msg ${passwordMsg.type}`}>
              {passwordMsg.type === 'success' ? (
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              ) : (
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12"/>
                  <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
              )}
              {passwordMsg.text}
            </div>
          )}

          <form onSubmit={handlePasswordSubmit} className="settings-form">
            <div className="settings-form-group">
              <label>Current password</label>
              <input
                type="password"
                value={passwordForm.oldPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, oldPassword: e.target.value })}
                placeholder="Enter current password"
                required
              />
            </div>
            <div className="settings-form-group">
              <label>New password</label>
              <input
                type="password"
                value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                placeholder="Min 6 characters"
                required
              />
            </div>
            <div className="settings-form-group">
              <label>Confirm new password</label>
              <input
                type="password"
                value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                placeholder="Repeat new password"
                required
              />
            </div>
            <div className="settings-form-actions">
              <button type="submit" className="settings-save-btn" disabled={savingPassword}>
                {savingPassword ? 'Changing...' : 'Change password'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}