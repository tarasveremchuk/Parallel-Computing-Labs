import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Auth.css';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [remember, setRemember] = useState(true);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-sidebar">
        <div className="sidebar-top">
          <div className="sidebar-brand">
            <div className="brand-icon">
              <div className="brand-icon-inner" />
            </div>
            <span className="brand-name">IoT Platform</span>
          </div>

          <div className="sidebar-features">
            <div className="sidebar-feature">
              <span className="feature-dot green" />
              <div className="feature-info">
                <span className="feature-title">Device monitoring</span>
                <span className="feature-desc">Track all your IoT devices</span>
              </div>
            </div>
            <div className="sidebar-feature">
              <span className="feature-dot yellow" />
              <div className="feature-info">
                <span className="feature-title">Anomaly detection</span>
                <span className="feature-desc">Rule-based threshold engine</span>
              </div>
            </div>
            <div className="sidebar-feature">
              <span className="feature-dot red" />
              <div className="feature-info">
                <span className="feature-title">Smart alerts</span>
                <span className="feature-desc">Severity-based notifications</span>
              </div>
            </div>
            <div className="sidebar-feature">
              <span className="feature-dot blue" />
              <div className="feature-info">
                <span className="feature-title">Analytics</span>
                <span className="feature-desc">Health scores and trends</span>
              </div>
            </div>
            <div className="sidebar-feature">
              <span className="feature-dot purple" />
              <div className="feature-info">
                <span className="feature-title">Team access</span>
                <span className="feature-desc">Role-based permissions</span>
              </div>
            </div>
          </div>

          <div className="sidebar-stats">
            <div className="sidebar-stat">
              <div className="stat-number">7</div>
              <div className="stat-label">Metric types</div>
            </div>
            <div className="sidebar-stat">
              <div className="stat-number">&lt;12ms</div>
              <div className="stat-label">Latency</div>
            </div>
            <div className="sidebar-stat">
              <div className="stat-number">3</div>
              <div className="stat-label">Access roles</div>
            </div>
            <div className="sidebar-stat">
              <div className="stat-number">99.9%</div>
              <div className="stat-label">Uptime</div>
            </div>
          </div>
        </div>

        <div className="sidebar-bottom">
          <div className="sidebar-status">
            <span className="status-pulse" />
            <span className="status-label">All systems operational</span>
          </div>
          <span className="sidebar-version">v1.0.0 · IoT Anomaly Detection Platform</span>
        </div>
      </div>

      <div className="auth-main">
        <div className="auth-card">
          <div className="auth-header">
            <h1>Welcome back</h1>
            <p>Sign in to your monitoring dashboard</p>
          </div>

          {error && (
            <div className="auth-error">
              <span className="error-icon">!</span>
              {error}
            </div>
          )}

          <div className="oauth-buttons">
            <button className="oauth-btn" type="button">
              <svg width="16" height="16" viewBox="0 0 24 24">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
              Google
            </button>
            <button className="oauth-btn" type="button">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="#334155">
                <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/>
              </svg>
              GitHub
            </button>
          </div>

          <div className="auth-divider">
            <div className="divider-line" />
            <span className="divider-text">or continue with email</span>
            <div className="divider-line" />
          </div>

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label htmlFor="username">Username</label>
              <div className="form-input-wrapper">
                <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter your username"
                  required
                />
                <div className="form-input-icon">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
              </div>
            </div>

            <div className="form-group">
              <div className="form-label-row">
                <label htmlFor="password">Password</label>
                <button type="button" className="form-forgot">Forgot?</button>
              </div>
              <div className="form-input-wrapper">
                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  required
                />
                <div className="form-input-icon">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                  </svg>
                </div>
              </div>
            </div>

            <div className="form-remember" onClick={() => setRemember(!remember)}>
              <div className={`remember-box ${remember ? 'checked' : ''}`}>
                {remember && (
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#fff" strokeWidth="3">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                )}
              </div>
              <span className="remember-text">Remember me for 30 days</span>
            </div>

            <button type="submit" className="auth-button" disabled={loading}>
              {loading ? 'Signing in...' : 'Sign in'}
              {!loading && (
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M5 12h14M12 5l7 7-7 7"/>
                </svg>
              )}
            </button>
          </form>

          <div className="auth-footer">
            <p>Don't have an account? <Link to="/register">Create account</Link></p>
          </div>

          <div className="auth-security">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" strokeWidth="1.5">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            <span>Secured with 256-bit encryption</span>
          </div>
        </div>
      </div>
    </div>
  );
}