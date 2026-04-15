import { useState, useEffect } from 'react';
import { dashboardAPI } from '../../api/dashboard';
import { simulatorAPI } from '../../api/simulator';
import { systemAPI } from '../../api/system';
import { auditAPI } from '../../api/audit';
import { useAuth } from '../../context/AuthContext';
import Header from '../../components/layout/Header';
import './Dashboard.css';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [health, setHealth] = useState(null);
  const [activityLog, setActivityLog] = useState([]);
  const [loading, setLoading] = useState(true);
  const [simulating, setSimulating] = useState(false);
  const [simResult, setSimResult] = useState(null);
  const { user } = useAuth();

  const fetchDashboard = async () => {
    try {
      const [dashRes, healthRes] = await Promise.all([
        dashboardAPI.get(),
        systemAPI.health().catch(() => null),
      ]);
      setData(dashRes.data.data);
      if (healthRes) setHealth(healthRes.data.data);
    } catch (err) {
      console.error('Dashboard error:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchActivity = async () => {
    if (user?.role !== 'ADMIN') return;
    try {
      const res = await auditAPI.getAll({ page: 0, size: 15 });
      setActivityLog(res.data.content || []);
    } catch (err) {
      console.error('Audit error:', err);
    }
  };

  useEffect(() => {
    fetchDashboard();
    fetchActivity();
  }, []);

  const handleSimulate = async () => {
    setSimulating(true);
    setSimResult(null);
    try {
      const res = await simulatorAPI.run(10);
      setSimResult(res.data.data);
      fetchDashboard();
      fetchActivity();
    } catch (err) {
      console.error('Simulation failed:', err);
    } finally {
      setSimulating(false);
    }
  };

  const actionIcons = {
    TELEMETRY_INGESTED: '📊',
    ALERT_CREATED: '🔔',
    ALERT_RESOLVED: '✅',
    DEVICE_CREATED: '📱',
    DEVICE_UPDATED: '✏️',
    DEVICE_DELETED: '🗑️',
    DEVICE_HEARTBEAT: '💚',
    USER_REGISTERED: '👤',
    USER_LOGGED_IN: '🔑',
    USER_ROLE_CHANGED: '🔄',
    RULE_CREATED: '🛡️',
    RULE_UPDATED: '🛡️',
    SIMULATION_RUN: '🎮',
    WEBHOOK_TRIGGERED: '🔗',
    COMMAND_SENT: '⚡',
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner" />
        <span>Loading dashboard...</span>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <Header title="Dashboard" subtitle="System overview and device health" />

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon stat-icon-devices">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M9 9h6M9 13h4"/></svg>
          </div>
          <div className="stat-info">
            <span className="stat-value">{data?.totalDevices || 0}</span>
            <span className="stat-label">Total devices</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon stat-icon-online">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          </div>
          <div className="stat-info">
            <span className="stat-value">{data?.onlineDevices || 0}</span>
            <span className="stat-label">Online</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon stat-icon-anomaly">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          </div>
          <div className="stat-info">
            <span className="stat-value">{data?.anomalyRate || 0}%</span>
            <span className="stat-label">Anomaly rate</span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon stat-icon-alerts">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
          </div>
          <div className="stat-info">
            <span className="stat-value">{data?.unresolvedAlerts || 0}</span>
            <span className="stat-label">Unresolved alerts</span>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        {/* Alerts by severity + simulator */}
        <div className="dashboard-card">
          <div className="card-header-row">
            <h3>Alerts by severity</h3>
            <span className="card-badge">{data?.totalAlerts || 0} total</span>
          </div>
          <div className="severity-list">
            {['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map((sev) => {
              const count = data?.alertsBySeverity?.[sev] || 0;
              const total = data?.totalAlerts || 1;
              return (
                <div key={sev} className="severity-item">
                  <div className="severity-left">
                    <div className={`severity-dot ${sev.toLowerCase()}`} />
                    <span className="severity-name">{sev}</span>
                  </div>
                  <div className="severity-right">
                    <div className="severity-bar">
                      <div className={`severity-fill ${sev.toLowerCase()}`} style={{ width: `${(count / total) * 100}%` }} />
                    </div>
                    <span className="severity-count">{count}</span>
                  </div>
                </div>
              );
            })}
          </div>
          <div className="simulator-section">
            <div className="simulator-divider" />
            <button className="simulator-btn" onClick={handleSimulate} disabled={simulating}>
              {simulating ? (<><div className="loading-spinner-sm" />Generating...</>) : (
                <><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"><polygon points="5 3 19 12 5 21 5 3"/></svg>Run simulation</>
              )}
            </button>
            {simResult && (
              <div className="sim-result">{simResult.totalReadingsGenerated} readings · {simResult.anomaliesDetected} anomalies · {simResult.executionTimeMs}ms</div>
            )}
          </div>
        </div>

        {/* System Health */}
        <div className="dashboard-card">
          <div className="card-header-row">
            <h3>System health</h3>
            <span className={`health-status ${health?.status === 'UP' ? 'up' : 'down'}`}>
              <span className="health-dot" />{health?.status || 'Unknown'}
            </span>
          </div>
          <div className="health-grid">
            <div className="health-item">
              <span className="health-item-label">Auth Service</span>
              <span className="health-item-value up">UP</span>
            </div>
            <div className="health-item">
              <span className="health-item-label">Device Service</span>
              <span className={`health-item-value ${health?.deviceService === 'UP' ? 'up' : 'down'}`}>{health?.deviceService || '—'}</span>
            </div>
            <div className="health-item">
              <span className="health-item-label">Telemetry Service</span>
              <span className="health-item-value up">UP</span>
            </div>
            <div className="health-item">
              <span className="health-item-label">Devices</span>
              <span className="health-item-value">{health?.devices ?? '—'}</span>
            </div>
            <div className="health-item">
              <span className="health-item-label">Telemetry readings</span>
              <span className="health-item-value">{health?.telemetryReadings ?? '—'}</span>
            </div>
            <div className="health-item">
              <span className="health-item-label">Unresolved alerts</span>
              <span className="health-item-value">{health?.unresolvedAlerts ?? '—'}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Activity Feed */}
      {user?.role === 'ADMIN' && activityLog.length > 0 && (
        <div className="dashboard-card" style={{ marginTop: '12px' }}>
          <div className="card-header-row">
            <h3>Activity feed</h3>
            <span className="card-badge">{activityLog.length} recent</span>
          </div>
          <div className="activity-list">
            {activityLog.map((log) => (
              <div key={log.id} className="activity-item">
                <span className="activity-icon">{actionIcons[log.action] || '📋'}</span>
                <div className="activity-info">
                  <span className="activity-action">{log.action?.replace(/_/g, ' ')}</span>
                  <span className="activity-details">
                    {log.username && <span className="activity-user">{log.username}</span>}
                    {log.details && <span> · {log.details}</span>}
                  </span>
                </div>
                <span className="activity-time">{new Date(log.timestamp).toLocaleString()}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}