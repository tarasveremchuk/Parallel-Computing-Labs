import { useState, useEffect } from 'react';
import { dashboardAPI } from '../../api/dashboard';
import { simulatorAPI } from '../../api/simulator';
import Header from '../../components/layout/Header';
import './Dashboard.css';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [simulating, setSimulating] = useState(false);
  const [simResult, setSimResult] = useState(null);

  const fetchDashboard = async () => {
    try {
      const res = await dashboardAPI.get();
      setData(res.data.data);
    } catch (err) {
      console.error('Failed to load dashboard:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const handleSimulate = async () => {
    setSimulating(true);
    setSimResult(null);
    try {
      const res = await simulatorAPI.run(10);
      setSimResult(res.data.data);
      fetchDashboard();
    } catch (err) {
      console.error('Simulation failed:', err);
    } finally {
      setSimulating(false);
    }
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
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <rect x="4" y="4" width="16" height="16" rx="2"/>
              <path d="M9 9h6M9 13h4"/>
            </svg>
          </div>
          <div className="stat-info">
            <span className="stat-value">{data?.totalDevices || 0}</span>
            <span className="stat-label">Total devices</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon stat-icon-online">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div className="stat-info">
            <span className="stat-value">{data?.onlineDevices || 0}</span>
            <span className="stat-label">Online</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon stat-icon-anomaly">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
            </svg>
          </div>
          <div className="stat-info">
            <span className="stat-value">{data?.anomalyRate || 0}%</span>
            <span className="stat-label">Anomaly rate</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon stat-icon-alerts">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
          </div>
          <div className="stat-info">
            <span className="stat-value">{data?.unresolvedAlerts || 0}</span>
            <span className="stat-label">Unresolved alerts</span>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="dashboard-card">
          <div className="card-header-row">
            <h3>Device health</h3>
            <span className="card-badge">{Object.keys(data?.deviceHealthMap || {}).length} devices</span>
          </div>
          <div className="device-health-list">
            {data?.deviceHealthMap && Object.entries(data.deviceHealthMap).map(([id, device]) => (
              <div key={id} className="device-health-item">
                <div className="device-health-left">
                  <div className={`device-health-dot ${device.healthScore >= 80 ? 'healthy' : device.healthScore >= 50 ? 'warning' : 'critical'}`} />
                  <div className="device-health-info">
                    <span className="device-health-name">{device.deviceName}</span>
                    <span className="device-health-meta">{device.totalReadings} readings · {device.anomalyCount} anomalies</span>
                  </div>
                </div>
                <div className="device-health-right">
                  <div className="health-score-bar">
                    <div
                      className={`health-score-fill ${device.healthScore >= 80 ? 'healthy' : device.healthScore >= 50 ? 'warning' : 'critical'}`}
                      style={{ width: `${device.healthScore}%` }}
                    />
                  </div>
                  <span className={`health-score-value ${device.healthScore >= 80 ? 'healthy' : device.healthScore >= 50 ? 'warning' : 'critical'}`}>
                    {device.healthScore}%
                  </span>
                </div>
              </div>
            ))}
            {(!data?.deviceHealthMap || Object.keys(data.deviceHealthMap).length === 0) && (
              <div className="empty-state">No device data yet. Run a simulation to generate telemetry.</div>
            )}
          </div>
        </div>

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
                      <div
                        className={`severity-fill ${sev.toLowerCase()}`}
                        style={{ width: `${(count / total) * 100}%` }}
                      />
                    </div>
                    <span className="severity-count">{count}</span>
                  </div>
                </div>
              );
            })}
          </div>

          <div className="simulator-section">
            <div className="simulator-divider" />
            <button
              className="simulator-btn"
              onClick={handleSimulate}
              disabled={simulating}
            >
              {simulating ? (
                <>
                  <div className="loading-spinner-sm" />
                  Generating...
                </>
              ) : (
                <>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <polygon points="5 3 19 12 5 21 5 3"/>
                  </svg>
                  Run simulation
                </>
              )}
            </button>
            {simResult && (
              <div className="sim-result">
                {simResult.totalReadingsGenerated} readings · {simResult.anomaliesDetected} anomalies · {simResult.executionTimeMs}ms
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}