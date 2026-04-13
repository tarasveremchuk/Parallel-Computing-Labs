import { useState, useEffect } from 'react';
import { usersAPI } from '../../api/users';
import Header from '../../components/layout/Header';
import './Users.css';

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [changeRole, setChangeRole] = useState(null);
  const [newRole, setNewRole] = useState('');
  const [resetPwd, setResetPwd] = useState(null);
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState('');

  const fetchUsers = async () => {
    try {
      const res = await usersAPI.getAll({ size: 50 });
      setUsers(res.data.content || []);
    } catch (err) {
      console.error('Failed to load users:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleChangeRole = async () => {
    setError('');
    try {
      await usersAPI.changeRole(changeRole.id, { role: newRole });
      setChangeRole(null);
      fetchUsers();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to change role');
    }
  };

  const handleResetPassword = async () => {
    setError('');
    try {
      await usersAPI.resetPassword(resetPwd.id, { newPassword });
      setResetPwd(null);
      setNewPassword('');
      alert('Password reset successfully');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to reset password');
    }
  };

  const handleDeactivate = async (user) => {
    if (!window.confirm(`Deactivate user "${user.username}"?`)) return;
    try {
      await usersAPI.deactivate(user.id);
      fetchUsers();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to deactivate user');
    }
  };

  const roleColors = {
    ADMIN: { color: '#a855f7', bg: 'rgba(168,85,247,0.08)', border: 'rgba(168,85,247,0.15)' },
    OPERATOR: { color: '#3b82f6', bg: 'rgba(59,130,246,0.08)', border: 'rgba(59,130,246,0.15)' },
    VIEWER: { color: '#71717a', bg: 'rgba(113,113,122,0.08)', border: 'rgba(113,113,122,0.15)' },
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner" />
        <span>Loading users...</span>
      </div>
    );
  }

  return (
    <div className="users-page">
      <Header title="Users" subtitle={`${users.length} registered user(s)`} />

      <div className="devices-table-wrapper">
        <table className="devices-table">
          <thead>
            <tr>
              <th>User</th>
              <th>Email</th>
              <th>Role</th>
              <th>Status</th>
              <th>Created</th>
              <th>Last login</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => {
              const rc = roleColors[u.role] || roleColors.VIEWER;
              return (
                <tr key={u.id}>
                  <td>
                    <div className="user-cell">
                      <div className="user-avatar" style={{ borderColor: rc.border, color: rc.color }}>
                        {u.username?.charAt(0).toUpperCase()}
                      </div>
                      <span className="device-name">{u.username}</span>
                    </div>
                  </td>
                  <td className="cell-muted">{u.email}</td>
                  <td>
                    <span
                      className="role-badge"
                      style={{ background: rc.bg, color: rc.color, borderColor: rc.border }}
                    >
                      {u.role}
                    </span>
                  </td>
                  <td>
                    <span className={`user-status ${u.active ? 'active' : 'inactive'}`}>
                      <span className="status-indicator" />
                      {u.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="cell-muted">
                    {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}
                  </td>
                  <td className="cell-muted">
                    {u.lastLoginAt ? new Date(u.lastLoginAt).toLocaleString() : 'Never'}
                  </td>
                  <td>
                    <div className="row-actions">
                      <button
                        className="edit-btn"
                        title="Change role"
                        onClick={() => { setChangeRole(u); setNewRole(u.role); setError(''); }}
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                          <circle cx="9" cy="7" r="4"/>
                          <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
                        </svg>
                      </button>
                      <button
                        className="edit-btn"
                        title="Reset password"
                        onClick={() => { setResetPwd(u); setNewPassword(''); setError(''); }}
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                          <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                          <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                        </svg>
                      </button>
                      {u.active && (
                        <button
                          className="delete-btn"
                          title="Deactivate"
                          onClick={() => handleDeactivate(u)}
                        >
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                            <circle cx="12" cy="12" r="10"/>
                            <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
                          </svg>
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {changeRole && (
        <div className="modal-overlay" onClick={() => setChangeRole(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Change role — {changeRole.username}</h3>
              <button className="modal-close" onClick={() => setChangeRole(null)}>×</button>
            </div>
            {error && <div className="modal-error">{error}</div>}
            <div className="modal-form">
              <div className="modal-form-group">
                <label>New role</label>
                <div className="role-selector">
                  {['VIEWER', 'OPERATOR', 'ADMIN'].map((role) => {
                    const rc = roleColors[role];
                    return (
                      <button
                        key={role}
                        className={`role-option ${newRole === role ? 'selected' : ''}`}
                        style={newRole === role ? { borderColor: rc.color, background: rc.bg } : {}}
                        onClick={() => setNewRole(role)}
                      >
                        <span className="role-option-name" style={newRole === role ? { color: rc.color } : {}}>{role}</span>
                        <span className="role-option-desc">
                          {role === 'VIEWER' && 'Read-only access'}
                          {role === 'OPERATOR' && 'Manage telemetry & alerts'}
                          {role === 'ADMIN' && 'Full system access'}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>
              <div className="modal-actions">
                <button className="modal-cancel" onClick={() => setChangeRole(null)}>Cancel</button>
                <button className="modal-submit" onClick={handleChangeRole}>Save role</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {resetPwd && (
        <div className="modal-overlay" onClick={() => setResetPwd(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Reset password — {resetPwd.username}</h3>
              <button className="modal-close" onClick={() => setResetPwd(null)}>×</button>
            </div>
            {error && <div className="modal-error">{error}</div>}
            <div className="modal-form">
              <div className="modal-form-group">
                <label>New password</label>
                <input
                  type="text"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Min 6 characters"
                />
              </div>
              <div className="modal-actions">
                <button className="modal-cancel" onClick={() => setResetPwd(null)}>Cancel</button>
                <button
                  className="modal-submit"
                  onClick={handleResetPassword}
                  disabled={newPassword.length < 6}
                >
                  Reset password
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}