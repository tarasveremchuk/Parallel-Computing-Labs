import { useState, useEffect } from 'react';
import { alertsAPI } from '../../api/alerts';
import Header from '../../components/layout/Header';
import './Alerts.css';

export default function AlertsPage() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState({ severity: '', unresolvedOnly: false });
  const [resolving, setResolving] = useState(null);
  const [resolveNote, setResolveNote] = useState('');
  const [showResolve, setShowResolve] = useState(null);

  const fetchAlerts = async () => {
    try {
      const params = { size: 50, sortBy: 'createdAt', direction: 'desc' };
      if (filter.severity) params.severity = filter.severity;
      if (filter.unresolvedOnly) params.unresolvedOnly = true;
      const res = await alertsAPI.getAll(params);
      setAlerts(res.data.content || []);
    } catch (err) {
      console.error('Failed to load alerts:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts();
  }, [filter]);

  const handleResolve = async (id) => {
    setResolving(id);
    try {
      await alertsAPI.resolve(id, { note: resolveNote || null });
      setShowResolve(null);
      setResolveNote('');
      fetchAlerts();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to resolve alert');
    } finally {
      setResolving(null);
    }
  };

  const severityConfig = {
    CRITICAL: { color: '#ef4444', bg: 'rgba(239,68,68,0.08)', border: 'rgba(239,68,68,0.15)' },
    HIGH: { color: '#f97316', bg: 'rgba(249,115,22,0.08)', border: 'rgba(249,115,22,0.15)' },
    MEDIUM: { color: '#f59e0b', bg: 'rgba(245,158,11,0.08)', border: 'rgba(245,158,11,0.15)' },
    LOW: { color: '#3b82f6', bg: 'rgba(59,130,246,0.08)', border: 'rgba(59,130,246,0.15)' },
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner" />
        <span>Loading alerts...</span>
      </div>
    );
  }

  return (
    <div className="alerts-page">
      <Header title="Alerts" subtitle={`${alerts.length} alert(s)`} />

      <div className="devices-toolbar">
        <div className="devices-filters">
          <select
            value={filter.severity}
            onChange={(e) => setFilter({ ...filter, severity: e.target.value })}
            className="filter-select"
          >
            <option value="">All severities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>

          <button
            className={`filter-toggle ${filter.unresolvedOnly ? 'active' : ''}`}
            onClick={() => setFilter({ ...filter, unresolvedOnly: !filter.unresolvedOnly })}
          >
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            Unresolved only
          </button>
        </div>
      </div>

      {showResolve && (
        <div className="modal-overlay" onClick={() => setShowResolve(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Resolve alert</h3>
              <button className="modal-close" onClick={() => setShowResolve(null)}>×</button>
            </div>
            <div className="modal-form">
              <div className="modal-form-group">
                <label>Resolution note (optional)</label>
                <textarea
                  className="resolve-textarea"
                  value={resolveNote}
                  onChange={(e) => setResolveNote(e.target.value)}
                  placeholder="Describe what was done to resolve this alert..."
                  rows={3}
                />
              </div>
              <div className="modal-actions">
                <button className="modal-cancel" onClick={() => setShowResolve(null)}>Cancel</button>
                <button
                  className="modal-submit resolve-submit"
                  onClick={() => handleResolve(showResolve)}
                  disabled={resolving === showResolve}
                >
                  {resolving === showResolve ? 'Resolving...' : 'Resolve alert'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="alerts-list">
        {alerts.map((alert) => {
          const config = severityConfig[alert.severity] || severityConfig.LOW;
          return (
            <div key={alert.id} className={`alert-card ${alert.resolved ? 'resolved' : ''}`}>
              <div className="alert-severity-bar" style={{ background: config.color }} />

              <div className="alert-content">
                <div className="alert-top-row">
                  <div className="alert-badges">
                    <span
                      className="alert-severity-badge"
                      style={{ background: config.bg, color: config.color, borderColor: config.border }}
                    >
                      {alert.severity}
                    </span>
                    <span className="alert-metric-badge">{alert.metricType?.replace('_', ' ')}</span>
                  </div>
                  <span className="alert-time">{new Date(alert.createdAt).toLocaleString()}</span>
                </div>

                <p className="alert-message">{alert.message}</p>

                <div className="alert-details">
                  <span className="alert-detail">
                    Value: <strong>{alert.actualValue}</strong>
                  </span>
                  {alert.thresholdMin != null && (
                    <span className="alert-detail">Min: {alert.thresholdMin}</span>
                  )}
                  {alert.thresholdMax != null && (
                    <span className="alert-detail">Max: {alert.thresholdMax}</span>
                  )}
                </div>

                {alert.resolutionNote && (
                  <div className="alert-resolution">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                    {alert.resolutionNote}
                  </div>
                )}

                <div className="alert-bottom-row">
                  <span className={`alert-status-badge ${alert.resolved ? 'resolved' : 'open'}`}>
                    {alert.resolved ? 'Resolved' : 'Open'}
                  </span>
                  {!alert.resolved && (
                    <button
                      className="alert-resolve-btn"
                      onClick={() => { setShowResolve(alert.id); setResolveNote(''); }}
                    >
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                      Resolve
                    </button>
                  )}
                  {alert.resolved && alert.resolvedAt && (
                    <span className="alert-resolved-time">
                      Resolved {new Date(alert.resolvedAt).toLocaleString()}
                    </span>
                  )}
                </div>
              </div>
            </div>
          );
        })}

        {alerts.length === 0 && (
          <div className="empty-state">No alerts found. Run a simulation to generate some.</div>
        )}
      </div>
    </div>
  );
}