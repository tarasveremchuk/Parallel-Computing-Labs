import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { devicesAPI } from '../../api/devices';
import { alertsAPI } from '../../api/alerts';
import { useAuth } from '../../context/AuthContext';
import Header from '../../components/layout/Header';
import './Devices.css';

export default function DeviceDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [device, setDevice] = useState(null);
  const [stats, setStats] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [deviceRes, statsRes, alertsRes] = await Promise.all([
          devicesAPI.getById(id),
          devicesAPI.getStats(id),
          alertsAPI.getByDevice(id, { size: 5 }),
        ]);
        setDevice(deviceRes.data.data);
        setStats(statsRes.data.data);
        setAlerts(alertsRes.data.content || []);
      } catch (err) {
        console.error('Failed to load device:', err);
        navigate('/devices');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  const handleHeartbeat = async () => {
    try {
      const res = await devicesAPI.heartbeat(id);
      setDevice(res.data.data);
    } catch (err) {
      alert(err.response?.data?.message || 'Heartbeat failed');
    }
  };

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner" />
        <span>Loading device...</span>
      </div>
    );
  }

  if (!device) return null;

  const severityColor = { CRITICAL: '#ef4444', HIGH: '#f97316', MEDIUM: '#f59e0b', LOW: '#3b82f6' };

  return (
    <div className="device-detail-page">
      <button className="back-btn" onClick={() => navigate('/devices')}>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        Back to devices
      </button>

      <Header title={device.name} subtitle={`${device.type} · ${device.location}`} />

      <div className="detail-info-row">
        <div className="detail-info-card">
          <span className="detail-info-label">Status</span>
          <span className={`device-status status-${device.status.toLowerCase()}`}>
            <span className="status-indicator" />
            {device.status}
          </span>
        </div>
        <div className="detail-info-card">
          <span className="detail-info-label">Firmware</span>
          <span className="detail-info-value mono">{device.firmwareVersion}</span>
        </div>
        <div className="detail-info-card">
          <span className="detail-info-label">Health score</span>
          <span className={`detail-info-value ${stats?.healthScore >= 80 ? 'text-green' : stats?.healthScore >= 50 ? 'text-yellow' : 'text-red'}`}>
            {stats?.healthScore ?? '—'}%
          </span>
        </div>
        <div className="detail-info-card">
          <span className="detail-info-label">Total readings</span>
          <span className="detail-info-value">{stats?.totalReadings ?? 0}</span>
        </div>
        <div className="detail-info-card">
          <span className="detail-info-label">Anomalies</span>
          <span className="detail-info-value text-red">{stats?.anomalyCount ?? 0}</span>
        </div>
      </div>

      <div className="detail-actions">
        <button className="action-btn" onClick={handleHeartbeat}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
          Send heartbeat
        </button>
      </div>

      {stats?.metrics && Object.keys(stats.metrics).length > 0 && (
        <div className="detail-section">
          <h3 className="section-title">Metric statistics</h3>
          <div className="metrics-grid">
            {Object.entries(stats.metrics).map(([metric, data]) => (
              <div key={metric} className="metric-stat-card">
                <div className="metric-stat-header">
                  <span className="metric-stat-name">{metric.replace('_', ' ')}</span>
                  <span className="metric-stat-count">{data.readingCount} readings</span>
                </div>
                <div className="metric-stat-values">
                  <div className="metric-stat-item">
                    <span className="metric-stat-label">Avg</span>
                    <span className="metric-stat-val">{data.avg ?? '—'}</span>
                  </div>
                  <div className="metric-stat-item">
                    <span className="metric-stat-label">Min</span>
                    <span className="metric-stat-val">{data.min ?? '—'}</span>
                  </div>
                  <div className="metric-stat-item">
                    <span className="metric-stat-label">Max</span>
                    <span className="metric-stat-val">{data.max ?? '—'}</span>
                  </div>
                  <div className="metric-stat-item">
                    <span className="metric-stat-label">Last</span>
                    <span className="metric-stat-val highlight">{data.lastValue ?? '—'}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {alerts.length > 0 && (
        <div className="detail-section">
          <h3 className="section-title">Recent alerts</h3>
          <div className="detail-alerts-list">
            {alerts.map((alert) => (
              <div key={alert.id} className="detail-alert-item">
                <div className="detail-alert-severity" style={{ background: severityColor[alert.severity] }} />
                <div className="detail-alert-info">
                  <span className="detail-alert-message">{alert.message}</span>
                  <span className="detail-alert-time">{new Date(alert.createdAt).toLocaleString()}</span>
                </div>
                <span className={`detail-alert-badge ${alert.resolved ? 'resolved' : 'unresolved'}`}>
                  {alert.resolved ? 'Resolved' : 'Open'}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}